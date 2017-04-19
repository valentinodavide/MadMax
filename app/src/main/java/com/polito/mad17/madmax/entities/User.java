package com.polito.mad17.madmax.entities;

import com.polito.mad17.madmax.activities.GroupsActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class User {
    private String ID;

    private String username;
    private String name;
    private String surname;
    private String email;
    private String password;
    private String profileImage; // optional, URL dell'immagine da Firebase
    private HashMap<String, Double> balanceWithUsers;    //String = userID,  Double = debito(-) o credito (+) verso gli altri utenti
    private HashMap<String, Double> balanceWithGroups;   //String = groupID, Double = debito(-) o credito (+) verso gli altri gruppi
    private SortedMap<String, Expense> addedExpenses;   //String = timestamp dell'aggiunta, Expense = oggetto spesa
    private HashMap<String, Group> userGroups;          //String = groupID, Group = oggetto Group di cui user fa parte


    public User(String ID, String username, String name, String surname, String email, String password, String profileImage) {
        this.ID = ID;
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.email = email;
        // todo criptare password
        this.password = password;
        this.profileImage = profileImage;
        this.addedExpenses = new TreeMap<>();
        this.userGroups = new HashMap<>();
        this.balanceWithUsers = new HashMap<>();
        this.balanceWithGroups = new HashMap<>();
    }

    public String getID() { return ID; }

    public void setID(String ID) { this.ID = ID; }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public HashMap<String, Double> getBalanceWithUsers() {
        return balanceWithUsers;
    }

    public void setBalanceWithUsers(HashMap<String, Double> balanceWithUsers) {
        this.balanceWithUsers = balanceWithUsers;
    }

    public HashMap<String, Double> getBalanceWithGroups() {
        return balanceWithGroups;
    }

    public void setBalanceWithGroups(HashMap<String, Double> balanceWithGroups) {
        this.balanceWithGroups = balanceWithGroups;
    }

    public SortedMap<String, Expense> getAddedExpenses() {
        return addedExpenses;
    }

    public void setAddedExpenses(SortedMap<String, Expense> addedExpenses) {
        this.addedExpenses = addedExpenses;
    }

    public HashMap<String, Group> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(HashMap<String, Group> userGroups) {
        this.userGroups = userGroups;
    }

    // todo
    public Double getTotalDebts() {
        Double total = 0d;
        for (HashMap.Entry<String, Double> debt : balanceWithUsers.entrySet()) {
            total += debt.getValue();
        }
        return total;

    }

    public void addExpense(Expense expense) {
        addedExpenses.put(expense.getID(), expense);  //spesa aggiunta alla lista spese utente
        expense.getGroup().getExpenses().put(expense.getID(), expense);   //spesa aggiunta alla lista spese del gruppo
        updateBalance(expense);
    }

    // update balance among other users and among the group this user is part of
    private void updateBalance(Expense expense) {
        // todo per ora fa il calcolo come se le spese fossero sempre equamente divise fra tutti i
        // todo     membri del gruppo (cioè come se expense.equallyDivided fosse sempre = true

        Group g = expense.getGroup();   //gruppo in cui è stato inserita la spesa

        Double total = g.getTotalExpense(); //spesa totale del gruppo aggiornata
        Double singlecredit = expense.getAmount() / g.getMembers().size();   //credito che io ho verso ogni singolo utente in virtù della spesa che ho fatto
        Double totalcredit = singlecredit * (g.getMembers().size() -1); //credito totale che io ho verso tutti gli altri membri del gruppo
        //es. se in un gruppo di 5 persone io ho pagato 10, ognuno mi deve 2
        //quindi totalcredit = 2*4 dove 4 è il n. di membri del gruppo diversi da me. In tutto devo ricevere 8.

        Double actualdebts = balanceWithGroups.get(g.getID());
        if (actualdebts != null) {
            //aggiorno il mio debito verso il gruppo
            balanceWithGroups.put(g.getID(), actualdebts + totalcredit);
        }
        else {
            System.out.println("Group not found");
        }

        //per ogni amico del gruppo in cui è stata aggiunta la spesa
        for (HashMap.Entry<String, User> friend : g.getMembers().entrySet()) {
            //se non sono io stesso
            if (!friend.getKey().equals(this.getID())) {
                //aggiorno mio credito verso di lui
                Double balance = balanceWithUsers.get(friend.getKey());
                if (balance != null) {
                    balanceWithUsers.put(friend.getKey(), balance+singlecredit);
                }
                else {
                    System.out.println("Friend not found");
                }

                //aggiorno debito dell'amico verso di me
                HashMap<String, Double> friendBalanceWithUsers = friend.getValue().getBalanceWithUsers();
                balance = friendBalanceWithUsers.get(this.getID());;

                if (balance != null) {
                    friend.getValue().getBalanceWithUsers().put(this.getID(), balance-singlecredit);
                }
                else {
                    System.out.println("Io non risulto tra i suoi debiti");
                    // => allora devo aggiungermi
                    friendBalanceWithUsers.put(this.getID(), -singlecredit);
                }

                //aggiorno debito dell'amico verso il gruppo
                HashMap<String, Double> friendBalanceWithGroups = friend.getValue().getBalanceWithGroups();
                balance = friendBalanceWithGroups.get(g.getID());
                if (balance != null) {
                    friend.getValue().getBalanceWithGroups().put(g.getID(), balance-singlecredit);
                }
                else {
                    System.out.println("Gruppo non risulta tra i suoi debiti");
                    // => allora lo devo aggiungere
                    friendBalanceWithGroups.put(g.getID(), -singlecredit);
                }
            }
        }
    }

    public void joinGroup (Group group) {
        group.getMembers().put(this.getID(), this); //aggiunto user alla lista di membri del gruppo
        //this.userGroups.add(g); //gruppo aggiunto alla lista di gruppi di cui user fa parte
        this.userGroups.put(group.getID(), group);

        //creo un debito verso il gruppo
        balanceWithGroups.put(group.getID(), 0d);

        //per ogni membro del gruppo
        for (HashMap.Entry<String, User> friend : group.getMembers().entrySet()) {
            //se non sono io stesso
            if (!friend.getKey().equals(this.getID())) {
                balanceWithUsers.put(friend.getKey(), 0d); //creo un debito verso il membro
                friend.getValue().getBalanceWithUsers().put(this.getID(), 0d); //creo un debito dal membro verso di me
            }
        }
    }

    public HashMap<String, Group> getSharedGroupsMap(User friend)
    {
        HashMap<String, Group> mygroups = new HashMap<>();
        mygroups = this.getUserGroups();

        mygroups.keySet().retainAll(friend.getUserGroups().keySet());

        return  mygroups;

    }


    public ArrayList<Group> getSharedGroupsList (User friend)
    {
        ArrayList<Group> mygroups = new ArrayList<>();
        HashMap<String, Group> mygroupsmap = new HashMap<>(this.getUserGroups());
        HashMap<String, Group> friendgroupsmap = new HashMap<>(friend.getUserGroups());
        //mygroupsmap = this.getUserGroups();
        //friendgroupsmap = friend.getUserGroups();

        mygroupsmap.keySet().retainAll(friendgroupsmap.keySet());

        for(Map.Entry<String, Group> entry : mygroupsmap.entrySet()) {
            //String key = entry.getKey();
            //HashMap value = entry.getValue();
            mygroups.add(entry.getValue());
        }

        return mygroups;
    }



    public String toString() {
        return name + " " + surname + " ";
    }
}

package com.polito.mad17.madmax.entities;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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

    //private SortedMap<String, Expense> addedExpenses;   //String = timestamp dell'aggiunta, Expense = oggetto spesa
    private HashMap<String, Double> balanceWithUser;    //String = userID,  Double = debito(-) o credito (+) verso gli altri utenti
    private HashMap<String, Double> balanceWithGroup;   //String = groupID, Double = debito(-) o credito (+) verso gli altri gruppi
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

        //addedExpenses = new TreeMap<>();
        balanceWithUser = new HashMap<>();
        balanceWithGroup = new HashMap<>();
        userGroups = new HashMap<>();
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

    /*public SortedMap<String, Expense> getAddedExpenses() {
        return addedExpenses;
    }

    public void setAddedExpenses(SortedMap<String, Expense> addedExpenses) {
        this.addedExpenses = addedExpenses;
    }*/

    public HashMap<String, Double> getBalanceWithUser() {
        return balanceWithUser;
    }

    public void setBalanceWithUser(HashMap<String, Double> balanceWithUser) {
        this.balanceWithUser = balanceWithUser;
    }

    public HashMap<String, Double> getBalanceWithGroup() {
        return balanceWithGroup;
    }

    public void setBalanceWithGroup(HashMap<String, Double> balanceWithGroup) {
        this.balanceWithGroup = balanceWithGroup;
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
        for (HashMap.Entry<String, Double> debt : balanceWithUser.entrySet())
        {
            total += debt.getValue();
        }
        return total;

    }

    public void addExpense(Expense expense) {
        //addedExpenses.put(expense.getID(), expense);  //spesa aggiunta alla lista spese utente
        expense.getGroup().getExpenses().put(expense.getID(), expense);   //spesa aggiunta alla lista spese del gruppo
        updateDebts(expense);
    }

    public void joinGroup (Group g) {
        g.getMembers().put(this.getID(), this); //aggiunto user alla lista di membri del gruppo
        this.userGroups.add(g); //gruppo aggiunto alla lista di gruppi di cui user fa parte

        //creo un debito verso il gruppo
        debtsWithGroup.put(g.getID(), 0d);

        //per ogni membro del gruppo
        for (HashMap.Entry<String, User> friend : g.getMembers().entrySet())
        {
            //se non sono io stesso
            if (friend.getKey() != this.getID())
            {
                debts.put(friend.getKey(), 0d); //creo un debito verso il membro
                friend.getValue().getDebts().put(this.getID(), 0d); //creo un debito dal membro verso di me
            }

        }


    }

    // todo se vuoi controlla i nomi (non si chiamano più debts ma balance)
    public void updateDebts(Expense expense) {
        // todo aggiusta
        Group g = expense.getGroup();   //gruppo in cui è stato inserita la spesa
        Double total = g.getTotalExpense(); //spesa totale del gruppo aggiornata
        Double singlecredit = expense.getAmount() / g.getMembers().size();   //credito che io ho verso ogni singolo utente in virtù della spesa che ho fatto
        Double totalcredit = singlecredit* (g.getMembers().size() -1); //credito totale che io ho verso tutti gli altri membri del gruppo
        //es. se in un gruppo di 5 persone io ho pagato 10, ognuno mi deve 2
        //quindi totalcredit = 2*4 dove 4 è il n. di membri del gruppo diversi da me. In tutto devo ricevere 8.

        Double actualdebts = balanceWithGroup.get(g.getID());

        if (actualdebts != null)
        {
            //aggiorno il mio debito verso il gruppo
            balanceWithGroup.put(g.getID(), actualdebts + totalcredit);
        }
        else
        {
            System.out.println("Group not found");
        }



        //per ogni amico del gruppo in cui è stata aggiunta la spesa
        for (HashMap.Entry<String, User> friend : g.getMembers().entrySet())
        {
            //se non sono io stesso
            if (friend.getKey() != this.getID())
            {
                //aggiorno mio debito verso di lui
                Double debt = balanceWithUser.get(friend.getKey());
                if (debt != null)
                {
                    balanceWithUser.put(friend.getKey(), debt+singlecredit);
                }
                else
                {
                    System.out.println("Friend not found");

                }

                //aggiorno debito dell'amico verso di me
                Double hisdebt = friend.getValue().getBalanceWithUser().get(this.getID());

                if (hisdebt != null)
                {
                    friend.getValue().getBalanceWithUser().put(this.getID(), hisdebt - singlecredit);
                }
                else
                {
                    System.out.println("Io non risulto tra i suoi debiti");

                }

                //aggiorno debito dell'amico verso il gruppo
                Double hisgroupdebt = friend.getValue().getBalanceWithGroup().get(g.getID());
                if (hisgroupdebt != null)
                {
                    friend.getValue().getBalanceWithGroup().put(g.getID(), hisgroupdebt - singlecredit);
                }
                else
                {
                    System.out.println("Gruppo non risulta tra i suoi debiti");

                }
            }
        }
    }

    public Group extractGroup (User u)
    {
        for (Map.Entry<String, Group> g : u.getUserGroups().entrySet())
        {

        }
    }


    public String toString() {
        return name + " " + surname + " ";
    }
}

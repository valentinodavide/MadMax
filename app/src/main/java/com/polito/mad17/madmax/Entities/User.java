package com.polito.mad17.madmax.Entities;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class User {

    private String ID;
    private String name;
    private String surname;
    private String profileImage;
    private SortedMap<String, Expense> addedExpenses;   //String = nome spesa, Expense = oggetto spesa
    private HashMap<String, Double> debts;  //String = userID, Double = debito che io ho con userID
    private HashMap<String, Double> debtsWithGroup;     //String = groupID, Double = debito che ho con groupID
    private List<Group> userGroups;

    public User(String ID, String name, String surname, String profileImage) {
        this.ID = ID;
        this.name = name;
        this.surname = surname;
        this.profileImage = profileImage;
        addedExpenses = new TreeMap<>();
        debts = new HashMap<>();
        debtsWithGroup = new HashMap<>();
        userGroups = new LinkedList<>();
    }

    public String getID() { return ID; }

    public void setID(String ID) { this.ID = ID; }

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

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public HashMap<String, Double> getDebts() {
        return debts;
    }

    public void setDebts(HashMap<String, Double> debts) {
        this.debts = debts;
    }

    public HashMap<String, Double> getDebtsWithGroup() {return debtsWithGroup;}

    public void setDebtsWithGroup(HashMap<String, Double> debtsWithGroup) {this.debtsWithGroup = debtsWithGroup;}
    
    public Double getTotalDebts()
    {
        Double total = 0d;
        for (HashMap.Entry<String, Double> debt : debts.entrySet())
        {
            total += debt.getValue();
        }
        return total;

    }

    public void addExpense(Expense expense)
    {
        addedExpenses.put(expense.getID(), expense);  //spesa aggiunta alla lista spese utente
        expense.getGroup().getExpenses().put(expense.getID(), expense);   //spesa aggiunta alla lista spese del gruppo
        updateDebts(expense);
    }


    public void joinGroup (Group g)
    {
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

    public void updateDebts(Expense expense)
    {
        Group g = expense.getGroup();   //gruppo in cui è stato inserita la spesa
        Double total = g.getTotalExpense(); //spesa totale del gruppo aggiornata
        Double singlecredit = expense.getAmount() / g.getMembers().size();   //credito che io ho verso ogni singolo utente in virtù della spesa che ho fatto
        Double totalcredit = singlecredit* (g.getMembers().size() -1); //credito totale che io ho verso tutti gli altri membri del gruppo
        //es. se in un gruppo di 5 persone io ho pagato 10, ognuno mi deve 2
        //quindi totalcredit = 2*4 dove 4 è il n. di membri del gruppo diversi da me. In tutto devo ricevere 8.

        Double actualdebts = debtsWithGroup.get(g.getID());

        if (actualdebts != null)
        {
            //aggiorno il mio debito verso il gruppo
            debtsWithGroup.put(g.getID(), actualdebts + totalcredit);
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
                Double debt = debts.get(friend.getKey());
                if (debt != null)
                {
                    debts.put(friend.getKey(), debt+singlecredit);
                }
                else
                {
                    System.out.println("Friend not found");

                }

                //aggiorno debito dell'amico verso di me
                Double hisdebt = friend.getValue().getDebts().get(this.getID());

                if (hisdebt != null)
                {
                    friend.getValue().getDebts().put(this.getID(), hisdebt - singlecredit);
                }
                else
                {
                    System.out.println("Io non risulto tra i suoi debiti");

                }

                //aggiorno debito dell'amico verso il gruppo
                Double hisgroupdebt = friend.getValue().getDebtsWithGroup().get(g.getID());
                if (hisgroupdebt != null)
                {
                    friend.getValue().getDebtsWithGroup().put(g.getID(), hisgroupdebt - singlecredit);
                }
                else
                {
                    System.out.println("Gruppo non risulta tra i suoi debiti");

                }
            }
        }
    }




    public String toString() {
        return name + " " + surname + " ";
    }
}

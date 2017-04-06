package com.polito.mad17.madmax.Entities;

import java.util.HashMap;

public class Group {

    private String ID;
    private String name;
    private String image;
    private Integer numberNotifications;
    static private HashMap<String, User> members;
    static private HashMap<String, Expense> expenses;

    public Group(String ID, String name, String image) {
        this.ID = ID;
        this.name = name;
        this.image = image;
        this.numberNotifications = 0;
        members = new HashMap<>();
        expenses = new HashMap<>();
    }

    public String getID() { return ID; }

    public void setID(String ID) { this.ID = ID; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getNumberNotifications() {
        return numberNotifications;
    }

    public void setNumberNotifications(Integer numberNotifications) { this.numberNotifications = numberNotifications; }

    public HashMap<String, User> getMembers() { return members; }

    public void setMembers(HashMap<String, User> members) { this.members = members; }

    public HashMap<String, Expense> getExpenses() { return expenses; }

    public void setExpenses(HashMap<String, Expense> expenses) { this.expenses = expenses; }

    public Double getTotalExpense ()  //ritorna i soldi totali spesi dal gruppo
    {
        Double total = 0d;

        for (HashMap.Entry<String, Expense> expense : expenses.entrySet())
        {
            total += expense.getValue().getAmount();
        }
        return total;
    }


    public String toString() {
        return ID + " " + name + " " + image;
    }
}

package com.polito.mad17.madmax.entities;

import java.util.HashMap;
import java.util.Map;

public class Group {
    private String ID;
    private String name;
    private String image;                       // optional, URL dell'immagine su Firebase
    private String description;                 // optional
    private Integer counterAddedExpenses;       // numero di spese aggiunte dall'ultima apertura del gruppo
    private HashMap<String, User> members;      // String: userID,      User: oggetto
    private HashMap<String, Expense> expenses;  // String: expenseID,   Expense: oggetto

    public Group(String ID, String name, String image, String description) {
        this.ID = ID;
        this.name = name;
        this.image = image;
        this.description = description;
        this.counterAddedExpenses = 0;
        this.members = new HashMap<>();
        this.expenses = new HashMap<>();
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

    public String getDescription() {
        return description;
    }

    public String setDescription(String description) {
        return this.description = description;
    }

    public Integer getNumberNotifications() {
        return counterAddedExpenses;
    }

    public void setNumberNotifications(Integer numberNotifications) { this.counterAddedExpenses = numberNotifications; }

    public HashMap<String, User> getMembers() { return members; }

    public void setMembers(HashMap<String, User> members) { this.members = members; }

    public HashMap<String, Expense> getExpenses() { return expenses; }

    public void setExpenses(HashMap<String, Expense> expenses) { this.expenses = expenses; }

    //ritorna i soldi totali spesi dal gruppo (packake-private: visibilità di default)
    Double getTotalExpense () {
        Double total = 0d;

        for (HashMap.Entry<String, Expense> expense : expenses.entrySet()) {
            total += expense.getValue().getAmount();
        }

        return total;
    }

    // todo updateCounterAddedExpenses: ogni volta che l'utente apre un gruppo il numerino che segna
    // todo     il numero di spese aggiunte dall'ultima apertura del gruppo deve essere azzerato

    public String toString() {
        return ID + " " + name + " " + image;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("groupID", ID);
        result.put("name", name);
        result.put("image", image);
        result.put("description", description);

        return result;
    }
}

package com.polito.mad17.madmax.entities;

public class Expense {

    private String ID;
    private String description;
    private Double amount;

    //private String name;
    //private String category;
    //private Boolean equallyDivided;
    //private HashMap<String, Double> partitions;

    private Group group;

    //public Expense (String ID, String description, Double amount/*,String category, Boolean equallyDivided*/) {

    public Expense (String ID, String description, String category, Double amount, Boolean equallyDivided, Group group) {
        this.ID = ID;
        this.description = description;
        this.amount = amount;
        this.group = group;

        /*
        this.category = category;
        this.equallyDivided = equallyDivided;
        this.partitions = new HashMap<>();
        partitions = new HashMap<>();
        */
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String name) {
        this.description = name;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    /*
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getEquallyDivided() {
        return equallyDivided;
    }

    public void setEquallyDivided(Boolean equallyDivided) {
        this.equallyDivided = equallyDivided;
    }
    */

    public Group getGroup() {return group;}

    public void setGroup(Group group) {this.group = group;}
}

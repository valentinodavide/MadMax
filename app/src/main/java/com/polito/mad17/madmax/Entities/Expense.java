package com.polito.mad17.madmax.Entities;

import java.util.HashMap;

public class Expense {

    private String ID;
    private String name;
    private String category;
    private Double amount;
    private Boolean equallyDivided;
    private HashMap<String, Double> partitions;



    private Group group;



    public Expense (String ID, String name, String category, Double amount, Boolean equallyDivided) {
        this.ID = ID;
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.equallyDivided = equallyDivided;
        partitions = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Boolean getEquallyDivided() {
        return equallyDivided;
    }

    public void setEquallyDivided(Boolean equallyDivided) {
        this.equallyDivided = equallyDivided;
    }

    public Group getGroup() {return group;}

    public void setGroup(Group group) {this.group = group;}

    public String getID() {return ID;}

    public void setID(String ID) {this.ID = ID;}
}

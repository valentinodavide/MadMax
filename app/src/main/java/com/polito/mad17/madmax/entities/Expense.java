package com.polito.mad17.madmax.entities;

import java.util.HashMap;

public class Expense {

    private String ID;
    private String description;
    private String category;
    private Double amount;
    private Boolean equallyDivided;
    private HashMap<String, Double> partitions;
    private String currency;
    private String image;

    private Group group;

    public Expense (String ID, String description, String category, Double amount, Boolean equallyDivided, Group group, String currency, String image) {
        this.ID = ID;
        this.description = description;
        this.category = category;
        this.amount = amount;
        this.equallyDivided = equallyDivided;
        this.partitions = new HashMap<>();
        partitions = new HashMap<>();
        this.currency = currency;
        this.image = image;

        this.group = group;

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

    public HashMap<String, Double> getPartitions() {
        return partitions;
    }

    public void setPartitions(HashMap<String, Double> partitions) {
        this.partitions = partitions;
    }

    public Group getGroup() {return group;}

    public void setGroup(Group group) {this.group = group;}

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}

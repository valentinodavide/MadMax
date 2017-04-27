package com.polito.mad17.madmax.entities;

import java.util.HashMap;
import java.util.Map;

public class Expense {
    private String ID;

    private String description;
    private String category;        // optional, corrisponde al categoryID della categoria a cui corrisponde la spesa
    private Double amount;
    private String currency;        // €, $ ...
    private String image;           // optional, URL dell'immagine su Firebase (può essere lo scontrino o una foto del prodotto)
    private String groupID;
    private Boolean equallyDivided; // se vero la spesa viene divisa equamente fra tutti gli utenti del gruppo
                                    // altrimenti viene suddivisa come specificato in partitions ->
    private HashMap<String, Double> partitions;     // String: userID, Double: frazione corrispondente a quello User


    public Expense (String ID, String description, String category, Double amount, String currency, String image, Boolean equallyDivided, String groupID) {
        this.ID = ID;
        this.description = description;
        this.category = category;
        this.amount = amount;
        this.currency = currency;
        this.image = image;
        this.equallyDivided = equallyDivided;
        this.partitions = new HashMap<>();
        this.partitions = new HashMap<>();
        this.groupID = groupID;
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

    public String getGroupID() {return groupID;}

    public void setGroupID(String groupID) {this.groupID = groupID;}

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("ID", ID);
        result.put("description", description);
        result.put("category", category);
        result.put("amount", amount);
        result.put ("currency", currency);
        result.put("image", image);
        result.put("equallyDivided", equallyDivided);
        result.put("groupID", groupID);

        return result;
    }
}

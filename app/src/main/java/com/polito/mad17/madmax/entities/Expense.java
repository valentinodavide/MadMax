package com.polito.mad17.madmax.entities;

import java.util.HashMap;
import java.util.Map;

public class Expense {
    private String ID;

    private String description;
    private String category;        // optional, corrisponde al categoryID della categoria a cui corrisponde la spesa
    private Double amount;
    private String currency;        // €, $ ...
    private String expensePhoto;           // optional, URL dell'immagine su Firebase (può essere lo scontrino o una foto del prodotto)
    private String billPhoto;
    private String groupID;
    private String creatorID;       //ID of the user who added the expense
    private Boolean equallyDivided; // se vero la spesa viene divisa equamente fra tutti gli utenti del gruppo
                                    // altrimenti viene suddivisa come specificato in participants ->
    private HashMap<String, Double> participants;     // String: userID, Double: frazione corrispondente a quello user

    private String timestamp;
    private Boolean deleted;
    //Attributi usati nel caso di pending expense
    private Integer participantsCount;
    private String groupName;
    private Integer yes;
    private Integer no;


    public Expense() { this.participants = new HashMap<>(); }

    public Expense (String ID, String description, String category, Double amount, String currency, String billPhoto, String expensePhoto, Boolean equallyDivided, String groupID, String creatorID, Boolean deleted) {
        this.ID = ID;
        this.description = description;
        this.category = category;
        this.amount = amount;
        this.currency = currency;
        this.billPhoto = billPhoto;
        this.expensePhoto = expensePhoto;
        this.equallyDivided = equallyDivided;
        this.groupID = groupID;
        this.creatorID = creatorID;
        this.participants = new HashMap<>();
        this.deleted = deleted;
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

    public String getExpensePhoto() {
        return expensePhoto;
    }

    public void setExpensePhoto(String expensePhoto) {
        this.expensePhoto = expensePhoto;
    }

    public String getBillPhoto() {return billPhoto;}

    public void setBillPhoto(String billPhoto) {this.billPhoto = billPhoto;}

    public Boolean getEquallyDivided() {
        return equallyDivided;
    }

    public void setEquallyDivided(Boolean equallyDivided) {
        this.equallyDivided = equallyDivided;
    }

    public HashMap<String, Double> getParticipants() {
        return participants;
    }

    public void setParticipants(HashMap<String, Double> participants) {
        this.participants = participants;
    }

    public String getGroupID() {return groupID;}

    public void setGroupID(String groupID) {this.groupID = groupID;}

    public String getCreatorID() {return creatorID;}

    public void setCreatorID(String creatorID) {this.creatorID = creatorID;}

    public Boolean getDeleted() {return deleted;}

    public void setDeleted(Boolean deleted) {this.deleted = deleted;}

    public String getTimestamp() {return timestamp;}

    public void setTimestamp(String timestamp) {this.timestamp = timestamp;}

    public Integer getParticipantsCount() {return participantsCount;}

    public void setParticipantsCount(Integer participantsCount) {this.participantsCount = participantsCount;}

    public String getGroupName() {return groupName;}

    public void setGroupName(String groupName) {this.groupName = groupName;}

    public Integer getYes() {return yes;}

    public void setYes(Integer yes) {this.yes = yes;}

    public Integer getNo() {return no;}

    public void setNo(Integer no) {this.no = no;}



    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("ID", ID);
        result.put("description", description);
        result.put("category", category);
        result.put("amount", amount);
        result.put ("currency", currency);
        result.put("expensePhoto", expensePhoto);
        result.put("equallyDivided", equallyDivided);
        result.put("groupID", groupID);

        return result;
    }
}

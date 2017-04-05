package com.polito.mad17.madmax.Entities;

import java.util.HashMap;

public class Group {

    private String ID;
    private String name;
    private String image;
    private Integer numberNotifications;
    private HashMap<String, User> members;
    private HashMap<String, Expense> expenses;

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

    public void setNumberNotifications(Integer numberNotifications) {
        this.numberNotifications = numberNotifications;
    }

    public String toString() {
        return name + " " + image;
    }
}

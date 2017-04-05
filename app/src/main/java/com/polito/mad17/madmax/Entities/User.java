package com.polito.mad17.madmax.Entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedMap;

import static android.os.Build.VERSION_CODES.M;

public class User {

    private String name;
    private String surname;
    private String profileImage;
    private SortedMap<String, Expense> addedExpenses;
    private HashMap<String, Double> debts;

    public User(String name, String surname, String profileImage) {
        this.name = name;
        this.surname = surname;
        this.profileImage = profileImage;
        debts = new HashMap<>();
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

    public String toString() {
        return name + " " + surname + " ";
    }
}

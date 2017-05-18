package com.polito.mad17.madmax.entities;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class Group implements Parcelable {
    private String ID;
    private String name;
    private String image;                       // optional, URL dell'immagine su Firebase
    private String description;                 // optional
    private Integer numberMembers;
    private Boolean deleted;



    private  Double balance;

    // todo da eliminare?
    private Integer counterAddedExpenses;       // numero di spese aggiunte dall'ultima apertura del gruppo

    private HashMap<String, User> members;      // String: userID,      User: oggetto
    private HashMap<String, Expense> expenses;  // String: expenseID,   Expense: oggetto

    public  Group (){};

    public Group(String ID, String name, String image, String description, Integer numberMembers) {
        this.ID = ID;
        this.name = name;
        this.image = image;
        this.description = description;
        this.numberMembers = numberMembers;
        this.counterAddedExpenses = 0;
        this.deleted = false;
        this.members = new HashMap<>();
        this.expenses = new HashMap<>();
        this.deleted = deleted;
    }

    protected Group(Parcel in) {
        ID = in.readString();
        name = in.readString();
        image = in.readString();
        description = in.readString();
        numberMembers = in.readInt();
        counterAddedExpenses = in.readInt();

        members = in.readHashMap(new ClassLoader() {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                return super.loadClass(name, resolve);
            }
        });

        expenses = in.readHashMap(new ClassLoader() {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                return super.loadClass(name, resolve);
            }
        });
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ID);
        dest.writeString(name);
        dest.writeString(image);
        dest.writeString(description);
        dest.writeInt(numberMembers);
        dest.writeInt(counterAddedExpenses);
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

    public Integer getNumberMembers() {
        return numberMembers;
    }

    public void setNumberMembers(Integer numberMembers) {
        this.numberMembers = numberMembers;
    }

    public Integer getCounterAddedExpenses() {
        return counterAddedExpenses;
    }

    public void setCounterAddedExpenses(Integer numberNotifications) { this.counterAddedExpenses = numberNotifications; }

    public HashMap<String, User> getMembers() { return members; }

    public void setMembers(HashMap<String, User> members) { this.members = members; }

    public HashMap<String, Expense> getExpenses() { return expenses; }

    public void setExpenses(HashMap<String, Expense> expenses) { this.expenses = expenses; }

    public Double getBalance() {return balance;}

    public void setBalance(Double balance) {this.balance = balance;}

    public Boolean getDeleted() {return deleted;}

    public void setDeleted(Boolean deleted) {this.deleted = deleted;}


    // todo updateCounterAddedExpenses: ogni volta che l'utente apre un gruppo il numerino che segna
    // todo     il numero di spese aggiunte dall'ultima apertura del gruppo deve essere azzerato

    public String toString() {
        return ID + " " + name;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("groupID", ID);
        result.put("name", name);
        result.put("image", image);
        result.put("description", description);
        result.put("numberMembers", numberMembers);

        return result;
    }
}

package com.polito.mad17.madmax.entities;

public class Category {
    private String ID;
    private String name;
    private String image;

    public Category(String ID, String name, String image) {
        this.ID = ID;
        this.name = name;
        this.image = image;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

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
}
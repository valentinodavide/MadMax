package com.polito.mad17.madmax.entities;

public class Comment {
    private String ID;
    private String expenseID;
    private String author;
    private String authorPhoto;
    private String message;
    private String date;
    private String time;

    public Comment(String expenseID, String author, String authorPhoto, String message) {
        this.expenseID = expenseID;
        this.author = author;
        this.authorPhoto = authorPhoto;
        this.message = message;
    }

    public Comment(String ID, String expenseID, String author, String authorPhoto, String message, String date, String time) {
        this.ID = ID;
        this.expenseID = expenseID;
        this.author = author;
        this.authorPhoto = authorPhoto;
        this.message = message;
        this.date = date;
        this.time = time;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getExpenseID() {
        return expenseID;
    }

    public void setExpenseID(String expenseID) {
        this.expenseID = expenseID;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorPhoto() {
        return authorPhoto;
    }

    public void setAuthorPhoto(String authorPhoto) {
        this.authorPhoto = authorPhoto;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}

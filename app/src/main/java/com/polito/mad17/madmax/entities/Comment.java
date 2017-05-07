package com.polito.mad17.madmax.entities;

/**
 * Created by alessandro on 06/05/17.
 */

public class Comment {

    String author;
    String message;
    String timestamp;

    public Comment(String author, String message, String timestamp) {
        this.author = author;
        this.message = message;
        this.timestamp = timestamp;
    }

    public Comment(String author, String message) {
        this.author = author;
        this.message = message;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

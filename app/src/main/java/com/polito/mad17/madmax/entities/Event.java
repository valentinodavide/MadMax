package com.polito.mad17.madmax.entities;

public class Event {
    public enum EventType {
        GROUP_ADD, /*GROUP_REMOVE,*/ GROUP_EDIT,

        GROUP_MEMBER_ADD, GROUP_MEMBER_REMOVE /*TODO to implementf*/,

        EXPENSE_ADD, EXPENSE_REMOVE, EXPENSE_EDIT,

        PENDING_EXPENSE_ADD, PENDING_EXPENSE_REMOVE, PENDING_EXPENSE_EDIT,
        PENDING_EXPENSE_VOTE_UP, PENDING_EXPENSE_VOTE_DOWN,
        PENDING_EXPENSE_APPROVED /*TODO to implement*/, PENDING_EXPENSE_NEGLECTED /*TODO to implement*/,

        /*FRIEND_INVITE, todo se abbiamo tempo di fare anche lo storico per utente*/ FRIEND_GROUP_INVITE,

        USER_PAY, USER_COMMENT_ADD
    }

    private String ID;
    private String groupID;
    private EventType eventType;
    private String subject;
    private String object;
    private String date;
    private String time;
    private Double amount;
    private String description;

    public Event(String groupID, EventType eventType, String subject, String object) {
        this.groupID = groupID;
        this.eventType = eventType;
        this.subject = subject;
        this.object = object;
    }

    public Event(String groupID, EventType eventType, String object) {
        this.groupID = groupID;
        this.eventType = eventType;
        this.object = object;
    }

    public Event(String groupID, EventType eventType, String subject, String object, Double amount) {
        this.groupID = groupID;
        this.eventType = eventType;
        this.subject = subject;
        this.object = object;
        this.amount = amount;
    }

    public Event(String ID, String groupID, EventType eventType, String subject, String object, String date, String time, Double amount, String description) {
        this.ID = ID;
        this.groupID = groupID;
        this.eventType = eventType;
        this.subject = subject;
        this.object = object;
        this.date = date;
        this.time = time;
        this.amount = amount;
        this.description = description;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
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

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

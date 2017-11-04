package com.spit.team_25.cswallet.activities;


import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {
    private Date date;
    private boolean fromMe;
    private String fromName;
    private String message;

    public Message(String fromName, String message, boolean fromMe, Date date) {
        this.fromName = fromName;
        this.message = message;
        this.fromMe = fromMe;
        this.date = date;
    }

    public String getFromName() {
        return this.fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean fromMe() {
        return this.fromMe;
    }

    public void setSelf(boolean fromMe) {
        this.fromMe = fromMe;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDate() {
        return new SimpleDateFormat("dd MMM yyyy").format(this.date);
    }

    public String getTime() {
        return new SimpleDateFormat("h:mm a").format(this.date);
    }
}
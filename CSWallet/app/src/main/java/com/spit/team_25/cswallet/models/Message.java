package com.spit.team_25.cswallet.models;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {
    private Date date;
    private boolean fromBot;
    private String fromName;
    private String message;

    public Message(String fromName, String message, boolean fromBot, Date date) {
        this.fromName = fromName;
        this.message = message;
        this.fromBot = fromBot;
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

    public boolean fromBot() {
        return this.fromBot;
    }

    public void setSelf(boolean fromBot) {
        this.fromBot = fromBot;
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

package com.spit.team_25.cswallet.models;

import java.io.Serializable;

public class Transactions implements Serializable{
    private String TID, Status, Transaction_with, Amount, Timestamp;

    public String getTID(){return TID;}

    public void setTID(String TID){this.TID = TID;}

    public String getStatus(){return Status;}

    public void setStatus(String Status){this.Status = Status;}

    public String getTransaction_with(){return Transaction_with;}

    public void setTransaction_with(String Transaction_with){this.Transaction_with = Transaction_with;}

    public String getAmount(){return Amount;}

    public void setAmount(String Amount){this.Amount = Amount;}

    public String getTimestamp(){return Timestamp;}

    public void setTimestamp(String Timestamp){this.Timestamp = Timestamp;}
}
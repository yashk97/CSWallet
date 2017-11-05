package com.spit.team_25.cswallet.models;

import java.io.Serializable;

public class User implements Serializable{

    private String name, email, balance, phone;
    private Transactions transactions= new Transactions();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Transactions getTransactions(){return transactions;}

    public void setTransactions(Transactions transactions){this.transactions = transactions;}
}

package com.spit.team_25.cswallet.models;

public class UserRequest {

    private String request;
    private User user;

    public UserRequest(User user, String request) {
        this.user = user;
        this.request = request;
    }

    private Object getRequestResult() {
        switch(request){
            case "getBalance()":
                return getBalance();
            case "getTransaction()":
                return getTransaction();
        }

        return null;
    }

    private String getBalance() {
        return user.getBalance();
    }

    private Object getTransaction() {
        return user.getTransactions();
    }
}

package com.example.yanivproject.models;

import java.io.Serializable;

public class UserPay implements Serializable {
    private static final long serialVersionUID = 1L;  // Optional

    protected User user;
    protected double owns;
    protected String status;

    public UserPay(User user, Double owns, Boolean status) {
        this.user = user;
        this.owns = owns;
        this.status = status ? "Paid" : "Unpaid";
    }

    public UserPay() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getOwns() {
        return owns;
    }

    public void setOwns(double owns) {
        this.owns = owns;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "UserPay{" +
                "user=" + user +
                ", owns=" + owns +
                ", status='" + status + '\'' +
                '}';
    }
}

package com.example.yanivproject.models;

import java.io.Serializable;

public class UserPay implements Serializable {
    private static final long serialVersionUID = 1L;  // Optional

    protected User user;
    protected double owns;
    protected String status;
    protected double percentage;   // New field for percentage-based split
    protected double customAmount; // New field for custom amount split

    public UserPay(User user, double owns, boolean status, double percentage, double customAmount) {
        this.user = user;
        this.owns = owns;
        this.status = status ? "Paid" : "Unpaid";
        this.percentage = percentage;
        this.customAmount = customAmount;
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

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public double getCustomAmount() {
        return customAmount;
    }

    public void setCustomAmount(double customAmount) {
        this.customAmount = customAmount;
    }

    @Override
    public String toString() {
        return "UserPay{" +
                "user=" + user +
                ", owns=" + owns +
                ", status='" + status + '\'' +
                ", percentage=" + percentage +
                ", customAmount=" + customAmount +
                '}';
    }
}

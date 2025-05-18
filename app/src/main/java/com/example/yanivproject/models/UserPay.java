package com.example.yanivproject.models;

import java.io.Serializable;

public class UserPay implements Serializable {
    private static final long serialVersionUID = 1L;  // Optional

    protected User user;
    protected double amount;
    protected boolean isPaid;
    protected double totalPaid;
    protected double remaining;

    public UserPay() {
    }

    public UserPay(User user, double amount) {
        this.user = user;
        this.amount = amount;
        this.isPaid = false;
        this.totalPaid = 0;
        this.remaining = amount;
    }

    public UserPay(User user, double amount, boolean isPaid, double totalPaid, double remaining) {
        this.user = user;
        this.amount = amount;
        this.isPaid = isPaid;
        this.totalPaid = totalPaid;
        this.remaining = remaining;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public double getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(double totalPaid) {
        this.totalPaid = totalPaid;
    }

    public double getRemaining() {
        return remaining;
    }

    public void setRemaining(double remaining) {
        this.remaining = remaining;
    }

    @Override
    public String toString() {
        return "UserPay{" +
                "user=" + user +
                ", amount=" + amount +
                ", isPaid=" + isPaid +
                ", totalPaid=" + totalPaid +
                ", remaining=" + remaining +
                '}';
    }
}

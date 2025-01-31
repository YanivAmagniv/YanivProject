package com.example.yanivproject.models;

public class UserPay {
    protected User user;
    protected double owns;
    protected String status;

    public UserPay(User user, Double owns, Boolean status) {
        this.user = user;
        this.owns = owns;
        this.status = status ? "Paid" : "Unpaid"; //  Convert Boolean to meaningful status
    }

    public UserPay() {
    }

    @Override
    public String toString() {
        return "UserPay{" +
                "user=" + user +
                ", owns=" + owns +
                ", status='" + status + '\'' +
                '}';
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
}

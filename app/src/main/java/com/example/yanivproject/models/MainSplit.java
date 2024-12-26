package com.example.yanivproject.models;

import java.util.ArrayList;

public class MainSplit {
    String id,status,eventDate,detail;



    String  type;  //taxi,gas,resturant,market,rent,other;
    User admin;
    ArrayList <UserPay> users;
    int dividedBy;
    double totalAmount;


    public MainSplit(String id, String status, String eventDate, String detail, String type, User admin, ArrayList<UserPay> users, int dividedBy, double totalAmount) {
        this.id = id;
        this.status = status;
        this.eventDate = eventDate;
        this.detail = detail;
        this.type = type;
        this.admin = admin;
        this.users = users;
        this.dividedBy = dividedBy;
        this.totalAmount = totalAmount;
    }

    public MainSplit() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public User getAdmin() {
        return admin;
    }

    public void setAdmin(User admin) {
        this.admin = admin;
    }

    public ArrayList<UserPay> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<UserPay> users) {
        this.users = users;
    }

    public int getDividedBy() {
        return dividedBy;
    }

    public void setDividedBy(int dividedBy) {
        this.dividedBy = dividedBy;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    @Override
    public String toString() {
        return "MainSplit{" +
                "id='" + id + '\'' +
                ", status='" + status + '\'' +
                ", eventDate='" + eventDate + '\'' +
                ", detail='" + detail + '\'' +
                ", type='" + type + '\'' +
                ", admin=" + admin +
                ", users=" + users +
                ", dividedBy=" + dividedBy +
                ", totalAmount=" + totalAmount +
                '}';
    }
}

package com.example.yanivproject.models;

import java.io.Serializable;
import java.util.ArrayList;

public class Group implements Serializable {
    private String groupId;
    private String groupName;
    private String status;
    private String eventDate;
    private String groupDescription;
    private String type;
    private User admin;
    private ArrayList<UserPay> users;
    private int dividedBy;
    private double totalAmount;

    // No-argument constructor (required by Firebase for deserialization)
    public Group() {
        this.users = new ArrayList<>(); // Initialize the list to avoid null
    }

    public Group(String groupId, String groupName, String status, String eventDate, String groupDescription, String type, User admin, ArrayList<UserPay> users, int dividedBy, double totalAmount) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.status = status;
        this.eventDate = eventDate;
        this.groupDescription = groupDescription;
        this.type = type;
        this.admin = admin;
        this.users = (users != null) ? users : new ArrayList<>(); // Ensure it's never null
        this.dividedBy = dividedBy;
        this.totalAmount = totalAmount;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
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

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
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

    @Override
    public String toString() {
        return "Group{" +
                "groupId='" + groupId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", status='" + status + '\'' +
                ", eventDate='" + eventDate + '\'' +
                ", groupDescription='" + groupDescription + '\'' +
                ", type='" + type + '\'' +
                ", admin=" + admin +
                ", users=" + users +
                ", dividedBy=" + dividedBy +
                ", totalAmount=" + totalAmount +
                '}';
    }
}
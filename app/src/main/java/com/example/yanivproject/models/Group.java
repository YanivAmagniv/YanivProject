package com.example.yanivproject.models;

import java.io.Serializable;
import java.util.ArrayList;

public class Group implements Serializable {
    private static final long serialVersionUID = 1L;

    private String groupId;
    private String groupName;
    private String status;
    private String eventDate;
    private String groupDescription;
    private String type;
    private User admin;
    private ArrayList<UserPay> users;
    private String divisionMethod;  // Store division method like "manual", "percentage"
    private int dividedBy;  // Store the numerical value of division (e.g., number of participants)
    private double totalAmount;

    // No-argument constructor (required by Firebase for deserialization)
    public Group() {
        this.users = new ArrayList<>();  // Initialize the list to avoid null
    }

    // Constructor to initialize a group with all its attributes
    public Group(String groupId, String groupName, String status, String eventDate, String groupDescription, String type, User admin, ArrayList<UserPay> users, String divisionMethod, int dividedBy, double totalAmount) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.status = status;
        this.eventDate = eventDate;
        this.groupDescription = groupDescription;
        this.type = type;
        this.admin = admin;
        this.users = (users != null) ? users : new ArrayList<>();  // Ensure it's never null
        this.divisionMethod = divisionMethod;  // Set division method (like manual, percentage)
        this.dividedBy = dividedBy;  // Number of participants to divide by
        this.totalAmount = totalAmount;
    }

    // Getters and Setters for all attributes
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

    public String getDivisionMethod() {
        return divisionMethod;
    }

    // Method to set the division method (e.g., "manual", "percentage")
    public void setDivisionMethod(String divisionMethod) {
        this.divisionMethod = divisionMethod;
    }

    public int getDividedBy() {
        return dividedBy;
    }

    // Method to set the number of people to divide by (e.g., how many members)
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
                ", divisionMethod='" + divisionMethod + '\'' +
                ", dividedBy=" + dividedBy +
                ", totalAmount=" + totalAmount +
                '}';
    }
}

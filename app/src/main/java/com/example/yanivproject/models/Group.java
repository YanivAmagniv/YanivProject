package com.example.yanivproject.models;

import java.io.Serializable;
import java.util.List;

public class Group implements Serializable {
    private static final long serialVersionUID = 1L;  // Optional

    private String groupId;
    private String groupName;
    private String status;
    private String eventDate;
    private String groupDescription;
    private String type;
    private User creator;
    private List<UserPay> userPayList;
    private String splitMethod;
    private double totalAmount;

    // No-argument constructor (required by Firebase for deserialization)
    public Group() {
        // Required empty constructor for Firebase
    }

    public Group(String groupId, String groupName, String status, String eventDate, String groupDescription, String type, User creator, List<UserPay> userPayList, String splitMethod, double totalAmount) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.status = status;
        this.eventDate = eventDate;
        this.groupDescription = groupDescription;
        this.type = type;
        this.creator = creator;
        this.userPayList = userPayList;
        this.splitMethod = splitMethod;
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

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public List<UserPay> getUserPayList() {
        return userPayList;
    }

    public void setUserPayList(List<UserPay> userPayList) {
        this.userPayList = userPayList;
    }

    public String getSplitMethod() {
        return splitMethod;
    }

    public void setSplitMethod(String splitMethod) {
        this.splitMethod = splitMethod;
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
                ", creator=" + creator +
                ", userPayList=" + userPayList +
                ", splitMethod='" + splitMethod + '\'' +
                ", totalAmount=" + totalAmount +
                '}';
    }
}

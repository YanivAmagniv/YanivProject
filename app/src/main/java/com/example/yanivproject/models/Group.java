package com.example.yanivproject.models;

import java.io.Serializable;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import android.util.Log;
import android.graphics.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

public class Group implements Serializable {
    private static final long serialVersionUID = 1L;  // Optional

    private String groupId;
    private String groupName;
    private String status;
    private String groupDescription;
    private String type;
    private User creator;
    @PropertyName("userPayList")
    private Map<String, UserPay> userPayMap;
    private String splitMethod;
    private double totalAmount;
    private String paymentDeadline;  // New field for payment deadline
    private int reminderInterval;    // New field for reminder interval in days
    private String lastReminderDate; // New field to track last reminder date

    // No-argument constructor (required by Firebase for deserialization)
    public Group() {
        this.userPayMap = new HashMap<>();  // Initialize empty map
        this.reminderInterval = 7; // Default reminder interval: 7 days
    }

    public Group(String groupId, String groupName, String status, String groupDescription, String type, User creator, List<UserPay> userPayList, String splitMethod, double totalAmount) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.status = status;
        this.groupDescription = groupDescription;
        this.type = type;
        this.creator = creator;
        this.userPayMap = new HashMap<>();
        if (userPayList != null) {
            for (int i = 0; i < userPayList.size(); i++) {
                this.userPayMap.put(String.valueOf(i), userPayList.get(i));
            }
        }
        this.splitMethod = splitMethod;
        this.totalAmount = totalAmount;
        this.reminderInterval = 7; // Default reminder interval: 7 days
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

    @PropertyName("userPayList")
    public Map<String, UserPay> getUserPayMap() {
        return userPayMap != null ? userPayMap : new HashMap<>();
    }

    @PropertyName("userPayList")
    public void setUserPayMap(Map<String, UserPay> userPayMap) {
        this.userPayMap = userPayMap != null ? userPayMap : new HashMap<>();
    }

    @Exclude
    public List<UserPay> getUserPayList() {
        if (userPayMap == null) return new ArrayList<>();
        return new ArrayList<>(userPayMap.values());
    }

    @Exclude
    public void setUserPayList(List<UserPay> userPayList) {
        this.userPayMap = new HashMap<>();
        if (userPayList != null) {
            for (int i = 0; i < userPayList.size(); i++) {
                this.userPayMap.put(String.valueOf(i), userPayList.get(i));
            }
        }
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

    public String getPaymentDeadline() {
        return paymentDeadline;
    }

    public void setPaymentDeadline(String paymentDeadline) {
        this.paymentDeadline = paymentDeadline;
    }

    public long getDaysUntilDeadline() {
        if (paymentDeadline == null || paymentDeadline.isEmpty()) return -1;
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date deadline = sdf.parse(paymentDeadline);
            Date today = new Date();
            
            if (deadline != null) {
                long diffInMillis = deadline.getTime() - today.getTime();
                return diffInMillis / (24 * 60 * 60 * 1000);
            }
        } catch (Exception e) {
            Log.e("Group", "Error calculating days until deadline", e);
        }
        return -1;
    }

    public String getDeadlineStatus() {
        long daysUntilDeadline = getDaysUntilDeadline();
        
        if (daysUntilDeadline < 0) {
            return "NO_DEADLINE";
        } else if (daysUntilDeadline == 0) {
            return "DUE_TODAY";
        } else if (daysUntilDeadline <= 3) {
            return "URGENT";
        } else if (daysUntilDeadline <= 7) {
            return "WARNING";
        } else {
            return "OK";
        }
    }

    public int getDeadlineColor() {
        String status = getDeadlineStatus();
        switch (status) {
            case "DUE_TODAY":
            case "URGENT":
                return Color.RED;
            case "WARNING":
                return Color.rgb(255, 165, 0); // Orange
            case "OK":
                return Color.GREEN;
            default:
                return Color.GRAY;
        }
    }

    public int getReminderInterval() {
        return reminderInterval;
    }

    public void setReminderInterval(int reminderInterval) {
        this.reminderInterval = reminderInterval;
    }

    public String getLastReminderDate() {
        return lastReminderDate;
    }

    public void setLastReminderDate(String lastReminderDate) {
        this.lastReminderDate = lastReminderDate;
    }

    @Override
    public String toString() {
        return "Group{" +
                "groupId='" + groupId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", status='" + status + '\'' +
                ", groupDescription='" + groupDescription + '\'' +
                ", type='" + type + '\'' +
                ", creator=" + creator +
                ", userPayMap=" + userPayMap +
                ", splitMethod='" + splitMethod + '\'' +
                ", totalAmount=" + totalAmount +
                ", paymentDeadline='" + paymentDeadline + '\'' +
                ", reminderInterval=" + reminderInterval +
                ", lastReminderDate='" + lastReminderDate + '\'' +
                '}';
    }
}

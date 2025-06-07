// Group.java
// This class represents a group in the application
// Manages group information, payment details, and member payments
// Implements Serializable for easy data transfer between activities

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

/**
 * Model class representing a group in the application
 * Contains group information, payment details, and member payments
 * Implements Serializable for easy data transfer between activities
 */
public class Group implements Serializable {
    // Serialization version ID for compatibility
    private static final long serialVersionUID = 1L;

    // Group identification and basic information
    private String groupId;           // Unique identifier for the group
    private String groupName;         // Name of the group
    private String status;            // Current status of the group
    private String groupDescription;  // Description of the group
    private String type;              // Type of the group
    private User creator;             // User who created the group

    // Payment and member information
    @PropertyName("userPayList")
    private Object userPayList;       // List of user payments (can be List or Map)
    private String splitMethod;       // Method used to split payments
    private double totalAmount;       // Total amount for the group
    private String paymentDeadline;   // Deadline for payments
    private int reminderInterval;     // Days between payment reminders
    private String lastReminderDate;  // Date of last reminder sent

    /**
     * Default constructor required for Firebase serialization
     * Initializes empty userPayList and default reminder interval
     */
    public Group() {
        this.userPayList = new ArrayList<>();
        this.reminderInterval = 7;
    }

    /**
     * Constructor for creating a new group
     * @param groupId Unique identifier for the group
     * @param groupName Name of the group
     * @param status Current status of the group
     * @param groupDescription Description of the group
     * @param type Type of the group
     * @param creator User who created the group
     * @param userPayList List of user payments
     * @param splitMethod Method used to split payments
     * @param totalAmount Total amount for the group
     */
    public Group(String groupId, String groupName, String status, String groupDescription, 
                String type, User creator, List<UserPay> userPayList, String splitMethod, 
                double totalAmount) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.status = status;
        this.groupDescription = groupDescription;
        this.type = type;
        this.creator = creator;
        this.userPayList = userPayList != null ? userPayList : new ArrayList<>();
        this.splitMethod = splitMethod;
        this.totalAmount = totalAmount;
        this.reminderInterval = 7;
    }

    // Getters and setters for basic group information

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

    /**
     * Gets the user payment list as stored in Firebase
     * @return Object containing the user payment list (List or Map)
     */
    @PropertyName("userPayList")
    public Object getUserPayList() {
        return userPayList;
    }

    /**
     * Sets the user payment list
     * Handles both List and Map formats for Firebase compatibility
     * @param userPayList The user payment list to set
     */
    @PropertyName("userPayList")
    public void setUserPayList(Object userPayList) {
        if (userPayList instanceof List) {
            this.userPayList = userPayList;
        } else if (userPayList instanceof Map) {
            this.userPayList = userPayList;
        } else {
            this.userPayList = new ArrayList<>();
        }
    }

    /**
     * Gets the user payment list as a List<UserPay>
     * Converts from Firebase format if necessary
     * @return List of UserPay objects
     */
    @Exclude
    public List<UserPay> getUserPayListAsList() {
        if (userPayList == null) {
            return new ArrayList<>();
        }
        
        try {
            if (userPayList instanceof List) {
                return convertListToUserPayList((List<?>) userPayList);
            } else if (userPayList instanceof Map) {
                return convertMapToUserPayList((Map<String, Object>) userPayList);
            }
        } catch (Exception e) {
            Log.e("Group", "Error converting userPayList", e);
        }
        return new ArrayList<>();
    }

    /**
     * Converts a List of objects to a List of UserPay objects
     * @param list The list to convert
     * @return List of UserPay objects
     */
    private List<UserPay> convertListToUserPayList(List<?> list) {
        List<UserPay> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof UserPay) {
                result.add((UserPay) item);
            } else if (item instanceof Map) {
                result.add(convertMapToUserPay((Map<String, Object>) item));
            }
        }
        return result;
    }

    /**
     * Converts a Map of objects to a List of UserPay objects
     * @param map The map to convert
     * @return List of UserPay objects
     */
    private List<UserPay> convertMapToUserPayList(Map<String, Object> map) {
        List<UserPay> result = new ArrayList<>();
        for (Object value : map.values()) {
            if (value instanceof Map) {
                result.add(convertMapToUserPay((Map<String, Object>) value));
            }
        }
        return result;
    }

    /**
     * Converts a Map to a UserPay object
     * @param userPayMap The map to convert
     * @return UserPay object
     */
    private UserPay convertMapToUserPay(Map<String, Object> userPayMap) {
        UserPay userPay = new UserPay();
        
        // Convert user information
        if (userPayMap.containsKey("user")) {
            Map<String, Object> userMap = (Map<String, Object>) userPayMap.get("user");
            User user = new User();
            user.setId((String) userMap.get("id"));
            user.setFname((String) userMap.get("fname"));
            user.setLname((String) userMap.get("lname"));
            user.setEmail((String) userMap.get("email"));
            user.setPhone((String) userMap.get("phone"));
            userPay.setUser(user);
        }
        
        // Convert payment information
        convertPaymentInfo(userPayMap, userPay);
        
        return userPay;
    }

    /**
     * Converts payment information from a Map to a UserPay object
     * @param userPayMap The map containing payment information
     * @param userPay The UserPay object to update
     */
    private void convertPaymentInfo(Map<String, Object> userPayMap, UserPay userPay) {
        // Convert amount
        if (userPayMap.containsKey("amount")) {
            Object amount = userPayMap.get("amount");
            if (amount instanceof Number) {
                userPay.setAmount(((Number) amount).doubleValue());
            } else if (amount instanceof String) {
                userPay.setAmount(Double.parseDouble((String) amount));
            }
        }
        
        // Convert paid status
        if (userPayMap.containsKey("paid")) {
            Object paid = userPayMap.get("paid");
            if (paid instanceof Boolean) {
                userPay.setPaid((Boolean) paid);
            } else if (paid instanceof String) {
                userPay.setPaid(Boolean.parseBoolean((String) paid));
            }
        }
        
        // Convert payment status
        if (userPayMap.containsKey("paymentStatus")) {
            String status = (String) userPayMap.get("paymentStatus");
            if (status != null) {
                try {
                    userPay.setPaymentStatus(UserPay.PaymentStatus.valueOf(status));
                } catch (IllegalArgumentException e) {
                    userPay.setPaymentStatus(UserPay.PaymentStatus.NOT_PAID);
                }
            }
        }
        
        // Convert payment date
        if (userPayMap.containsKey("paymentDate")) {
            userPay.setPaymentDate((String) userPayMap.get("paymentDate"));
        }
        
        // Convert remaining amount
        if (userPayMap.containsKey("remaining")) {
            Object remaining = userPayMap.get("remaining");
            if (remaining instanceof Number) {
                userPay.setRemaining(((Number) remaining).doubleValue());
            } else if (remaining instanceof String) {
                userPay.setRemaining(Double.parseDouble((String) remaining));
            }
        }
        
        // Convert total paid amount
        if (userPayMap.containsKey("totalPaid")) {
            Object totalPaid = userPayMap.get("totalPaid");
            if (totalPaid instanceof Number) {
                userPay.setTotalPaid(((Number) totalPaid).doubleValue());
            } else if (totalPaid instanceof String) {
                userPay.setTotalPaid(Double.parseDouble((String) totalPaid));
            }
        }
    }

    // Getters and setters for payment information

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

    /**
     * Calculates the number of days until the payment deadline
     * @return Number of days until deadline, or -1 if no deadline set
     */
    public long getDaysUntilDeadline() {
        if (paymentDeadline == null) {
            return -1;
        }

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

    /**
     * Gets the status of the payment deadline
     * @return String describing the deadline status
     */
    public String getDeadlineStatus() {
        long daysUntilDeadline = getDaysUntilDeadline();
        
        if (daysUntilDeadline < 0) {
            return "No deadline set";
        } else if (daysUntilDeadline == 0) {
            return "Due today";
        } else if (daysUntilDeadline == 1) {
            return "Due tomorrow";
        } else if (daysUntilDeadline < 7) {
            return daysUntilDeadline + " days remaining";
        } else {
            return "Due in " + (daysUntilDeadline / 7) + " weeks";
        }
    }

    /**
     * Gets the color to display for the deadline status
     * @return Color value based on deadline proximity
     */
    public int getDeadlineColor() {
        long daysUntilDeadline = getDaysUntilDeadline();
        
        if (daysUntilDeadline < 0) {
            return Color.GRAY;
        } else if (daysUntilDeadline <= 3) {
            return Color.RED;
        } else if (daysUntilDeadline <= 7) {
            return Color.rgb(255, 165, 0); // Orange
        } else {
            return Color.GREEN;
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

    /**
     * Sets the deadline date for the group
     * @param deadlineDate The deadline date to set
     */
    public void setDeadlineDate(String deadlineDate) {
        this.paymentDeadline = deadlineDate;
    }

    /**
     * Returns a string representation of the group
     * @return String containing all group fields
     */
    @Override
    public String toString() {
        return "Group{" +
                "groupId='" + groupId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", status='" + status + '\'' +
                ", groupDescription='" + groupDescription + '\'' +
                ", type='" + type + '\'' +
                ", creator=" + creator +
                ", userPayList=" + userPayList +
                ", splitMethod='" + splitMethod + '\'' +
                ", totalAmount=" + totalAmount +
                ", paymentDeadline='" + paymentDeadline + '\'' +
                ", reminderInterval=" + reminderInterval +
                ", lastReminderDate='" + lastReminderDate + '\'' +
                '}';
    }
}

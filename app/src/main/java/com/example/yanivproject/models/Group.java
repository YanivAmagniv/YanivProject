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
    private static final long serialVersionUID = 1L;

    private String groupId;
    private String groupName;
    private String status;
    private String groupDescription;
    private String type;
    private User creator;
    @PropertyName("userPayList")
    private Object userPayList;  // Changed to Object to handle both formats
    private String splitMethod;
    private double totalAmount;
    private String paymentDeadline;
    private int reminderInterval;
    private String lastReminderDate;

    // No-argument constructor (required by Firebase for deserialization)
    public Group() {
        this.userPayList = new ArrayList<>();
        this.reminderInterval = 7;
    }

    public Group(String groupId, String groupName, String status, String groupDescription, String type, User creator, List<UserPay> userPayList, String splitMethod, double totalAmount) {
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
    public Object getUserPayList() {
        return userPayList;
    }

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

    @Exclude
    public List<UserPay> getUserPayListAsList() {
        if (userPayList == null) {
            return new ArrayList<>();
        }
        
        try {
            if (userPayList instanceof List) {
                List<?> list = (List<?>) userPayList;
                List<UserPay> result = new ArrayList<>();
                for (Object item : list) {
                    if (item instanceof UserPay) {
                        result.add((UserPay) item);
                    } else if (item instanceof Map) {
                        Map<String, Object> userPayMap = (Map<String, Object>) item;
                        UserPay userPay = new UserPay();
                        
                        // Convert the map to UserPay object
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
                        
                        if (userPayMap.containsKey("amount")) {
                            Object amount = userPayMap.get("amount");
                            if (amount instanceof Number) {
                                userPay.setAmount(((Number) amount).doubleValue());
                            } else if (amount instanceof String) {
                                userPay.setAmount(Double.parseDouble((String) amount));
                            }
                        }
                        
                        if (userPayMap.containsKey("paid")) {
                            Object paid = userPayMap.get("paid");
                            if (paid instanceof Boolean) {
                                userPay.setPaid((Boolean) paid);
                            } else if (paid instanceof String) {
                                userPay.setPaid(Boolean.parseBoolean((String) paid));
                            }
                        }
                        
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
                        
                        if (userPayMap.containsKey("paymentDate")) {
                            userPay.setPaymentDate((String) userPayMap.get("paymentDate"));
                        }
                        
                        if (userPayMap.containsKey("remaining")) {
                            Object remaining = userPayMap.get("remaining");
                            if (remaining instanceof Number) {
                                userPay.setRemaining(((Number) remaining).doubleValue());
                            } else if (remaining instanceof String) {
                                userPay.setRemaining(Double.parseDouble((String) remaining));
                            }
                        }
                        
                        if (userPayMap.containsKey("totalPaid")) {
                            Object totalPaid = userPayMap.get("totalPaid");
                            if (totalPaid instanceof Number) {
                                userPay.setTotalPaid(((Number) totalPaid).doubleValue());
                            } else if (totalPaid instanceof String) {
                                userPay.setTotalPaid(Double.parseDouble((String) totalPaid));
                            }
                        }
                        
                        result.add(userPay);
                    }
                }
                return result;
            } else if (userPayList instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) userPayList;
                List<UserPay> result = new ArrayList<>();
                for (Object value : map.values()) {
                    if (value instanceof Map) {
                        Map<String, Object> userPayMap = (Map<String, Object>) value;
                        UserPay userPay = new UserPay();
                        
                        // Convert the map to UserPay object
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
                        
                        if (userPayMap.containsKey("amount")) {
                            Object amount = userPayMap.get("amount");
                            if (amount instanceof Number) {
                                userPay.setAmount(((Number) amount).doubleValue());
                            } else if (amount instanceof String) {
                                userPay.setAmount(Double.parseDouble((String) amount));
                            }
                        }
                        
                        if (userPayMap.containsKey("paid")) {
                            Object paid = userPayMap.get("paid");
                            if (paid instanceof Boolean) {
                                userPay.setPaid((Boolean) paid);
                            } else if (paid instanceof String) {
                                userPay.setPaid(Boolean.parseBoolean((String) paid));
                            }
                        }
                        
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
                        
                        if (userPayMap.containsKey("paymentDate")) {
                            userPay.setPaymentDate((String) userPayMap.get("paymentDate"));
                        }
                        
                        if (userPayMap.containsKey("remaining")) {
                            Object remaining = userPayMap.get("remaining");
                            if (remaining instanceof Number) {
                                userPay.setRemaining(((Number) remaining).doubleValue());
                            } else if (remaining instanceof String) {
                                userPay.setRemaining(Double.parseDouble((String) remaining));
                            }
                        }
                        
                        if (userPayMap.containsKey("totalPaid")) {
                            Object totalPaid = userPayMap.get("totalPaid");
                            if (totalPaid instanceof Number) {
                                userPay.setTotalPaid(((Number) totalPaid).doubleValue());
                            } else if (totalPaid instanceof String) {
                                userPay.setTotalPaid(Double.parseDouble((String) totalPaid));
                            }
                        }
                        
                        result.add(userPay);
                    }
                }
                return result;
            }
        } catch (Exception e) {
            Log.e("Group", "Error converting userPayList", e);
        }
        
        return new ArrayList<>();
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
                ", userPayList=" + userPayList +
                ", splitMethod='" + splitMethod + '\'' +
                ", totalAmount=" + totalAmount +
                ", paymentDeadline='" + paymentDeadline + '\'' +
                ", reminderInterval=" + reminderInterval +
                ", lastReminderDate='" + lastReminderDate + '\'' +
                '}';
    }
}

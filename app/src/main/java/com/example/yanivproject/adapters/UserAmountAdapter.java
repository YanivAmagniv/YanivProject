// UserAmountAdapter.java
// This adapter handles the display and editing of user payment amounts in a RecyclerView
// Supports different split methods: equal split, percentage-based split, and custom amounts
// Manages real-time validation and updates of payment amounts
// Provides feedback on total amount distribution and remaining balance

package com.example.yanivproject.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yanivproject.R;
import com.example.yanivproject.models.User;
import com.example.yanivproject.models.UserPay;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter class for managing user payment amounts in a RecyclerView
 * Features:
 * - Multiple payment split methods
 * - Real-time amount validation
 * - Dynamic UI updates
 * - Error handling and feedback
 * - Total amount tracking
 * 
 * Split Methods:
 * 1. Equal split - divides total equally among users
 * 2. Percentage-based split - allows custom percentage distribution
 * 3. Custom amounts - allows manual amount entry for each user
 */
public class UserAmountAdapter extends RecyclerView.Adapter<UserAmountAdapter.ViewHolder> {
    // List of users to display
    private List<User> users = new ArrayList<>();
    // Map of user IDs to their payment amounts
    private Map<String, Double> userAmounts = new HashMap<>();
    // Total amount to be split among users
    private double totalAmount;
    // Method used to split the amount (equal, percentage, or custom)
    private String splitMethod;
    // Listener for amount change events
    private OnAmountChangedListener listener;

    /**
     * Interface for receiving amount change notifications
     * Provides real-time feedback on payment distribution
     * Used to update UI with current totals and remaining amounts
     */
    public interface OnAmountChangedListener {
        /**
         * Called when any user's amount changes
         * @param currentTotal The current sum of all amounts
         * @param remaining The difference between total and current sum
         */
        void onAmountChanged(double currentTotal, double remaining);
    }

    /**
     * Sets the listener for amount change events
     * @param listener The listener to receive amount change notifications
     */
    public void setOnAmountChangedListener(OnAmountChangedListener listener) {
        this.listener = listener;
    }

    /**
     * Updates the adapter with new user list and payment information
     * Initializes amounts based on the selected split method
     * @param users List of users to display
     * @param totalAmount Total amount to be split
     * @param splitMethod Method to use for splitting the amount
     */
    public void updateUsers(List<User> users, double totalAmount, String splitMethod) {
        this.users = users;
        this.totalAmount = totalAmount;
        this.splitMethod = splitMethod;
        
        // Initialize or update amounts based on split method
        if (splitMethod.equals("חלוקה שווה")) {
            // Equal split - divide total by number of users
            double equalAmount = totalAmount / users.size();
            for (User user : users) {
                userAmounts.put(user.getId(), equalAmount);
            }
        } else if (splitMethod.equals("חלוקה לפי אחוזים")) {
            // Percentage split - initialize with equal percentages
            double defaultPercentage = 100.0 / users.size();
            for (User user : users) {
                if (!userAmounts.containsKey(user.getId())) {
                    userAmounts.put(user.getId(), defaultPercentage);
                }
            }
        } else {
            // Custom split - initialize with 0 if not set
            for (User user : users) {
                if (!userAmounts.containsKey(user.getId())) {
                    userAmounts.put(user.getId(), 0.0);
                }
            }
        }
        
        notifyDataSetChanged();
        updateTotalAndNotifyListener();
    }

    /**
     * Calculates the current total of all user amounts
     * Handles both regular amounts and percentage-based calculations
     * @return The current total amount
     */
    public double getCurrentTotal() {
        double total = 0;
        for (Double amount : userAmounts.values()) {
            if (splitMethod.equals("חלוקה לפי אחוזים")) {
                total += (amount / 100.0) * totalAmount;
            } else {
                total += amount;
            }
        }
        return total;
    }

    /**
     * Updates the total amount and notifies the listener of changes
     * Calculates remaining amount and triggers callback
     * Used for real-time UI updates
     */
    private void updateTotalAndNotifyListener() {
        if (listener != null) {
            double currentTotal = getCurrentTotal();
            double remaining = totalAmount - currentTotal;
            listener.onAmountChanged(currentTotal, remaining);
        }
    }

    /**
     * Validates that the total amount matches the required total
     * For percentage split, ensures total is 100%
     * For other methods, ensures sum equals total amount
     * @return true if the total is valid, false otherwise
     */
    public boolean validateTotalAmount() {
        if (splitMethod.equals("חלוקה לפי אחוזים")) {
            double totalPercentage = 0;
            for (Double percentage : userAmounts.values()) {
                totalPercentage += percentage;
            }
            return Math.abs(totalPercentage - 100.0) < 0.01;
        } else {
            return Math.abs(getCurrentTotal() - totalAmount) < 0.01;
        }
    }

    /**
     * Creates a list of UserPay objects from the current amounts
     * Converts percentages to actual amounts if using percentage split
     * @return List of UserPay objects with calculated amounts
     */
    public ArrayList<UserPay> getUserPayList() {
        ArrayList<UserPay> userPays = new ArrayList<>();
        for (User user : users) {
            double amount = userAmounts.getOrDefault(user.getId(), 0.0);
            if (splitMethod.equals("חלוקה לפי אחוזים")) {
                amount = (amount / 100.0) * totalAmount;
            }
            userPays.add(new UserPay(user, amount));
        }
        return userPays;
    }

    /**
     * Creates new ViewHolder instances for user amount items
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View
     * @return A new ViewHolder that holds a View of the given view type
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_amount, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds user data and amount information to the ViewHolder
     * Sets up text change listeners and validation
     * Handles different split methods and their specific behaviors
     * @param holder The ViewHolder to bind data to
     * @param position The position of the item in the data set
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.tvUserName.setText(user.getName());
        
        double value = userAmounts.getOrDefault(user.getId(), 0.0);
        holder.etAmount.setText(String.format("%.2f", value));
        
        if (splitMethod.equals("חלוקה שווה")) {
            // Disable editing for equal split
            holder.etAmount.setEnabled(false);
            holder.inputLayout.setError(null);
        } else {
            // Enable editing for other split methods
            holder.etAmount.setEnabled(true);
            holder.etAmount.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        // Parse and validate the entered amount
                        double amount = s.length() > 0 ? Double.parseDouble(s.toString()) : 0.0;
                        userAmounts.put(user.getId(), amount);
                        
                        // Calculate and display remaining amount
                        double currentTotal = getCurrentTotal();
                        double remaining = totalAmount - currentTotal;
                        
                        if (Math.abs(remaining) > 0.01) {
                            if (remaining > 0) {
                                holder.inputLayout.setError("חסר " + String.format("₪%.2f", remaining));
                            } else {
                                holder.inputLayout.setError("עודף " + String.format("₪%.2f", -remaining));
                            }
                        } else {
                            holder.inputLayout.setError(null);
                        }
                        
                        updateTotalAndNotifyListener();
                    } catch (NumberFormatException e) {
                        holder.inputLayout.setError("נא להזין מספר תקין");
                        userAmounts.put(user.getId(), 0.0);
                    }
                }
            });
        }
        
        // Set appropriate label based on split method
        if (splitMethod.equals("חלוקה לפי אחוזים")) {
            holder.tvAmountLabel.setText("%");
        } else {
            holder.tvAmountLabel.setText("₪");
        }
    }

    /**
     * Returns the total number of items in the data set
     * @return The number of users in the list
     */
    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * ViewHolder class for user amount items
     * Holds references to views in the item layout
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName;      // Displays the user's name
        EditText etAmount;       // Input field for the amount
        TextView tvAmountLabel;  // Label showing the unit (₪ or %)
        TextInputLayout inputLayout; // Container for input field with error handling

        /**
         * Constructor for ViewHolder
         * Initializes view references
         * @param itemView The view for the item
         */
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            etAmount = itemView.findViewById(R.id.etAmount);
            tvAmountLabel = itemView.findViewById(R.id.tvAmountLabel);
            inputLayout = itemView.findViewById(R.id.inputLayout);
        }
    }

    /**
     * Sets the list of users for the adapter
     * @param users List of users to display
     */
    public void setUsers(ArrayList<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    /**
     * Sets the total amount to be split
     * @param totalAmount The total amount
     */
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
        notifyDataSetChanged();
    }

    /**
     * Sets the method used for splitting the amount
     * @param method The splitting method
     */
    public void setSplitMethod(String method) {
        this.splitMethod = method;
        
        // If switching to equal split, automatically calculate equal amounts
        if (method.equals("חלוקה שווה") && !users.isEmpty()) {
            double equalAmount = totalAmount / users.size();
            for (User user : users) {
                userAmounts.put(user.getId(), equalAmount);
            }
            notifyDataSetChanged();
            updateTotalAndNotifyListener();
        }
    }

    /**
     * Gets the amount for a specific user
     * @param user The user to get the amount for
     * @return The amount for the user
     */
    public double getAmountForUser(User user) {
        return userAmounts.getOrDefault(user.getId(), 0.0);
    }
} 
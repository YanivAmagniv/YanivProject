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

public class UserAmountAdapter extends RecyclerView.Adapter<UserAmountAdapter.ViewHolder> {
    private List<User> users = new ArrayList<>();
    private Map<String, Double> userAmounts = new HashMap<>();
    private double totalAmount;
    private String splitMethod;
    private OnAmountChangedListener listener;

    public interface OnAmountChangedListener {
        void onAmountChanged(double currentTotal, double remaining);
    }

    public void setOnAmountChangedListener(OnAmountChangedListener listener) {
        this.listener = listener;
    }

    public void updateUsers(List<User> users, double totalAmount, String splitMethod) {
        this.users = users;
        this.totalAmount = totalAmount;
        this.splitMethod = splitMethod;
        
        // Initialize or update amounts based on split method
        if (splitMethod.equals("חלוקה שווה")) {
            double equalAmount = totalAmount / users.size();
            for (User user : users) {
                userAmounts.put(user.getId(), equalAmount);
            }
        } else if (splitMethod.equals("חלוקה לפי אחוזים")) {
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

    private void updateTotalAndNotifyListener() {
        if (listener != null) {
            double currentTotal = getCurrentTotal();
            double remaining = totalAmount - currentTotal;
            listener.onAmountChanged(currentTotal, remaining);
        }
    }

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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_amount, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.tvUserName.setText(user.getName());
        
        double value = userAmounts.getOrDefault(user.getId(), 0.0);
        holder.etAmount.setText(String.format("%.2f", value));
        
        if (splitMethod.equals("חלוקה שווה")) {
            holder.etAmount.setEnabled(false);
            holder.inputLayout.setError(null);
        } else {
            holder.etAmount.setEnabled(true);
            holder.etAmount.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        double amount = s.length() > 0 ? Double.parseDouble(s.toString()) : 0.0;
                        userAmounts.put(user.getId(), amount);
                        
                        // Validate and show error if needed
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
        
        if (splitMethod.equals("חלוקה לפי אחוזים")) {
            holder.tvAmountLabel.setText("%");
        } else {
            holder.tvAmountLabel.setText("₪");
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName;
        EditText etAmount;
        TextView tvAmountLabel;
        TextInputLayout inputLayout;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            etAmount = itemView.findViewById(R.id.etAmount);
            tvAmountLabel = itemView.findViewById(R.id.tvAmountLabel);
            inputLayout = itemView.findViewById(R.id.inputLayout);
        }
    }
} 
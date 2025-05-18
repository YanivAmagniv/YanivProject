package com.example.yanivproject.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yanivproject.R;
import com.example.yanivproject.models.User;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserSelectionAdapter extends RecyclerView.Adapter<UserSelectionAdapter.ViewHolder> {
    private List<User> users = new ArrayList<>();
    private Set<String> selectedUserIds = new HashSet<>();
    private OnUserSelectionChangedListener listener;

    public interface OnUserSelectionChangedListener {
        void onUserSelectionChanged(List<User> selectedUsers);
    }

    public void setOnUserSelectionChangedListener(OnUserSelectionChangedListener listener) {
        this.listener = listener;
    }

    public void updateUsers(List<User> users) {
        this.users = new ArrayList<>(users);
        notifyDataSetChanged();
    }

    public List<User> getSelectedUsers() {
        List<User> selectedUsers = new ArrayList<>();
        for (User user : users) {
            if (selectedUserIds.contains(user.getId())) {
                selectedUsers.add(user);
            }
        }
        return selectedUsers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.tvUserName.setText(user.getName());
        
        boolean isSelected = selectedUserIds.contains(user.getId());
        updateItemAppearance(holder, isSelected);

        holder.itemView.setOnClickListener(v -> {
            boolean newSelectedState = !selectedUserIds.contains(user.getId());
            if (newSelectedState) {
                selectedUserIds.add(user.getId());
            } else {
                selectedUserIds.remove(user.getId());
            }
            updateItemAppearance(holder, newSelectedState);
            if (listener != null) {
                listener.onUserSelectionChanged(getSelectedUsers());
            }
        });
    }

    private void updateItemAppearance(ViewHolder holder, boolean isSelected) {
        if (isSelected) {
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.getContext(), R.color.colorPrimary));
            holder.tvUserName.setTextColor(
                ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
        } else {
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
            holder.tvUserName.setTextColor(
                ContextCompat.getColor(holder.itemView.getContext(), android.R.color.black));
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName;
        MaterialCardView cardView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            cardView = (MaterialCardView) itemView;
        }
    }
} 
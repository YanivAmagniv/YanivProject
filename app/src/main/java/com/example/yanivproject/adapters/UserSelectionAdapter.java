// UserSelectionAdapter.java
// This adapter handles the display and selection of users in a RecyclerView
// Manages user selection state and provides visual feedback for selected items
// Supports multiple user selection with callback notifications
// Implements Material Design card-based selection UI

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

/**
 * Adapter class for managing user selection in a RecyclerView
 * Handles user selection state and provides visual feedback
 * Supports multiple selection with callback notifications
 * Features:
 * - Multiple user selection
 * - Visual feedback using Material Design cards
 * - Efficient selection state management
 * - Real-time selection change notifications
 */
public class UserSelectionAdapter extends RecyclerView.Adapter<UserSelectionAdapter.ViewHolder> {
    // List of all available users
    private List<User> users = new ArrayList<>();
    // Set of selected user IDs for efficient lookup
    private Set<String> selectedUserIds = new HashSet<>();
    // Listener for selection change events
    private OnUserSelectionChangedListener listener;

    /**
     * Interface for receiving user selection change notifications
     * Provides the list of currently selected users
     * Used to notify parent components of selection changes
     */
    public interface OnUserSelectionChangedListener {
        /**
         * Called when the selection state of any user changes
         * @param selectedUsers List of currently selected users
         */
        void onUserSelectionChanged(List<User> selectedUsers);
    }

    /**
     * Sets the listener for selection change events
     * @param listener The listener to receive selection change notifications
     */
    public void setOnUserSelectionChangedListener(OnUserSelectionChangedListener listener) {
        this.listener = listener;
    }

    /**
     * Sets the list of users for the adapter
     * @param users List of users to display
     */
    public void setUsers(List<User> users) {
        this.users = new ArrayList<>(users);
        notifyDataSetChanged();
    }

    /**
     * Gets the list of currently selected users
     * Filters the users list based on selected IDs
     * @return List of User objects that are currently selected
     */
    public List<User> getSelectedUsers() {
        List<User> selectedUsers = new ArrayList<>();
        for (User user : users) {
            if (selectedUserIds.contains(user.getId())) {
                selectedUsers.add(user);
            }
        }
        return selectedUsers;
    }

    /**
     * Creates new ViewHolder instances for user selection items
     * Inflates the item layout and creates a new ViewHolder
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View
     * @return A new ViewHolder that holds a View of the given view type
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_selection, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds user data to the ViewHolder and sets up selection handling
     * Updates visual appearance based on selection state
     * Implements click handling for selection toggle
     * @param holder The ViewHolder to bind data to
     * @param position The position of the item in the data set
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.tvUserName.setText(user.getName());
        
        // Update visual appearance based on current selection state
        boolean isSelected = selectedUserIds.contains(user.getId());
        updateItemAppearance(holder, isSelected);

        // Set up click listener for selection toggle
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

    /**
     * Updates the visual appearance of an item based on its selection state
     * Changes background color and text color for selected/unselected states
     * @param holder The ViewHolder containing the views to update
     * @param isSelected Whether the item is currently selected
     */
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

    /**
     * Returns the total number of items in the data set
     * @return The total number of users in the list
     */
    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * ViewHolder class for user selection items
     * Holds references to views in the item layout
     * Manages the visual representation of a user item
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName;      // Displays the user's name
        MaterialCardView cardView; // Container for the user item with Material Design styling

        /**
         * Constructor for ViewHolder
         * Initializes view references
         * @param itemView The view for the item
         */
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            cardView = (MaterialCardView) itemView;
        }
    }
} 
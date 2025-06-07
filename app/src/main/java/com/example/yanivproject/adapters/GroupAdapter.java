// GroupAdapter.java
// This adapter handles the display of groups in a RecyclerView
// Manages the binding of group data to UI elements and handles group item clicks
// Extends RecyclerView.Adapter for efficient list display
// Implements navigation to group details on item click

package com.example.yanivproject.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.yanivproject.R;
import com.example.yanivproject.models.Group;
import com.example.yanivproject.screens.ExistentGroup;
import com.example.yanivproject.screens.GroupDetailsActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter class for displaying groups in a RecyclerView
 * Handles the creation and binding of group item views
 * Manages click events to navigate to group details
 * Features:
 * - Efficient group list display
 * - Click handling for group details navigation
 * - Null-safe data binding
 * - Debug logging for data verification
 */
public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    // List of groups to display
    private List<Group> groupList;
    // Application context for starting activities
    private Context context;

    /**
     * Constructor for the adapter
     * Initializes the adapter with a list of groups and context
     * Ensures groupList is never null
     * @param groupList List of groups to display
     * @param context Application context for starting activities
     */
    public GroupAdapter(List<Group> groupList, Context context) {
        this.groupList = (groupList != null) ? groupList : new ArrayList<>();
        this.context = context;
    }

    /**
     * ViewHolder class to represent individual group items
     * Holds references to the views that will be populated with group data
     * Manages the visual representation of a group item
     */
    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        // UI elements for displaying group information
        TextView groupName;        // Displays the name of the group
        TextView groupDescription; // Displays the group description
        TextView groupDate;        // Displays the group date (if applicable)

        /**
         * Constructor for the ViewHolder
         * Initializes view references from the item layout
         * @param itemView The view representing a single group item
         */
        public GroupViewHolder(View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.group_name);
            groupDescription = itemView.findViewById(R.id.group_description);
        }
    }

    /**
     * Creates new ViewHolder instances for group items
     * Inflates the layout for each group item
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View
     * @return A new ViewHolder that holds a View of the given view type
     */
    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout for each group item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_item, parent, false);
        return new GroupViewHolder(view);
    }

    /**
     * Binds group data to the ViewHolder
     * Sets up click listeners for navigation to group details
     * Implements null-safe data binding
     * @param holder The ViewHolder to bind data to
     * @param position The position of the item in the data set
     */
    @Override
    public void onBindViewHolder(GroupViewHolder holder, int position) {
        Group group = groupList.get(position);

        // Debug log to ensure data is coming through
        Log.d("GroupAdapter", "Binding group at position " + position + ": " + group.getGroupName());

        // Set group name with null check
        holder.groupName.setText(group.getGroupName() != null ? group.getGroupName() : "No Name Available");

        // Set up click listener to navigate to group details
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, GroupDetailsActivity.class);
            intent.putExtra("group", group);
            context.startActivity(intent);
        });
    }

    /**
     * Returns the total number of items in the data set
     * @return The total number of groups in the list
     */
    @Override
    public int getItemCount() {
        return groupList.size();
    }
}

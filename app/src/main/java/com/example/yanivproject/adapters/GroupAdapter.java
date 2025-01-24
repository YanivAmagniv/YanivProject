package com.example.yanivproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.yanivproject.R;
import com.example.yanivproject.models.Group;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private List<Group> groupList;

    // Constructor for the adapter
    public GroupAdapter(List<Group> groupList) {
        this.groupList = (groupList != null) ? groupList : new ArrayList<>();
    }

    // ViewHolder class to represent individual group items
    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView groupName;
        TextView groupDescription;
        TextView groupDate;

        public GroupViewHolder(View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.group_name);
            groupDescription = itemView.findViewById(R.id.group_description);
            groupDate = itemView.findViewById(R.id.group_date);
        }
    }

    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout for each group item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_item, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GroupViewHolder holder, int position) {
        // Get the group at the given position
        Group group = groupList.get(position);

        // Bind the group data to the views
        holder.groupName.setText(group.getGroupId()); // Or use group.getType() if that makes more sense.
        holder.groupDescription.setText(group.getGroupDescription());
        holder.groupDate.setText(group.getEventDate());

        holder.itemView.setOnClickListener(v -> {
            // Handle click event (e.g., open a new activity or show a dialog)
            Toast.makeText(v.getContext(), "Clicked: " + group.getGroupId(), Toast.LENGTH_SHORT).show();
            Toast.makeText(v.getContext(), "Clicked: " + group.getGroupDescription(), Toast.LENGTH_SHORT).show();
            Toast.makeText(v.getContext(), "Clicked: " + group.getEventDate(), Toast.LENGTH_SHORT).show();


        });
    }

    @Override
    public int getItemCount() {
        // Return the size of the group list
        return groupList.size();
    }
}

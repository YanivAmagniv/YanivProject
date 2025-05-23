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

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private List<Group> groupList;
    private Context context;

    // Constructor for the adapter
    public GroupAdapter(List<Group> groupList,Context context) {
        this.groupList = (groupList != null) ? groupList : new ArrayList<>();
        this.context = context;

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
        Group group = groupList.get(position);

        // Debug log to ensure data is coming through
        Log.d("GroupAdapter", "Binding group at position " + position + ": " + group.getGroupName());

        // Set group name
        holder.groupName.setText(group.getGroupName() != null ? group.getGroupName() : "No Name Available");

        // Handle item click
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, GroupDetailsActivity.class);
            intent.putExtra("group", group);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }
}

package com.example.yanivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yanivproject.R;
import com.example.yanivproject.adapters.GroupAdapter;
import com.example.yanivproject.models.Group;
import com.example.yanivproject.models.UserPay;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ExistentGroup extends NavActivity {
    private RecyclerView recyclerView;
    private GroupAdapter adapter;
    private ArrayList<Group> groupList;
    private DatabaseReference groupsRef;
    private String userId;
    private TextView noGroupsText;
    private Button addGroupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_existent_group);
        setupNavigationDrawer();

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        noGroupsText = findViewById(R.id.noGroupsText);
        addGroupButton = findViewById(R.id.addGroupButton);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        groupList = new ArrayList<>();
        adapter = new GroupAdapter(groupList, this);
        recyclerView.setAdapter(adapter);

        // Get current user ID
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize Firebase reference
        groupsRef = FirebaseDatabase.getInstance().getReference("groups");

        // Load groups
        loadGroups();

        // Set up add group button
        addGroupButton.setOnClickListener(v -> {
            Intent intent = new Intent(ExistentGroup.this, AddNewEvent.class);
            startActivity(intent);
        });
    }

    private void loadGroups() {
        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Group group = snapshot.getValue(Group.class);
                    if (group != null) {
                        boolean isCreator = group.getCreator() != null && 
                                         group.getCreator().getId().equals(userId);
                        boolean isMember = false;

                        // Check if user is a member
                        if (group.getUserPayList() != null) {
                            for (UserPay userPay : group.getUserPayList()) {
                                if (userPay.getUser().getId().equals(userId)) {
                                    isMember = true;
                                    break;
                                }
                            }
                        }

                        // Add group to list if user is creator or member
                        if (isCreator || isMember) {
                            groupList.add(group);
                        }
                    }
                }

                // Update UI
                if (groupList.isEmpty()) {
                    noGroupsText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    noGroupsText.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ExistentGroup", "Failed to load groups", error.toException());
                Toast.makeText(ExistentGroup.this, "Failed to load groups", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void goBack(View view) {
        onBackPressed();  // This will navigate back to the previous activity
    }
}

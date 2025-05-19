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

public class ExistentGroup extends NavActivity {
    private RecyclerView rvCreatedGroups;
    private RecyclerView rvJoinedGroups;
    private RecyclerView rvPaidGroups;
    private GroupAdapter createdGroupsAdapter;
    private GroupAdapter joinedGroupsAdapter;
    private GroupAdapter paidGroupsAdapter;
    private ArrayList<Group> createdGroupsList;
    private ArrayList<Group> joinedGroupsList;
    private ArrayList<Group> paidGroupsList;
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
        rvCreatedGroups = findViewById(R.id.rvCreatedGroups);
        rvJoinedGroups = findViewById(R.id.rvJoinedGroups);
        rvPaidGroups = findViewById(R.id.rvPaidGroups);
        noGroupsText = findViewById(R.id.noGroupsText);
        addGroupButton = findViewById(R.id.addGroupButton);

        // Set up RecyclerViews
        createdGroupsList = new ArrayList<>();
        joinedGroupsList = new ArrayList<>();
        paidGroupsList = new ArrayList<>();
        
        createdGroupsAdapter = new GroupAdapter(createdGroupsList, this);
        joinedGroupsAdapter = new GroupAdapter(joinedGroupsList, this);
        paidGroupsAdapter = new GroupAdapter(paidGroupsList, this);
        
        rvCreatedGroups.setLayoutManager(new LinearLayoutManager(this));
        rvJoinedGroups.setLayoutManager(new LinearLayoutManager(this));
        rvPaidGroups.setLayoutManager(new LinearLayoutManager(this));
        
        rvCreatedGroups.setAdapter(createdGroupsAdapter);
        rvJoinedGroups.setAdapter(joinedGroupsAdapter);
        rvPaidGroups.setAdapter(paidGroupsAdapter);

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
                createdGroupsList.clear();
                joinedGroupsList.clear();
                paidGroupsList.clear();
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Group group = snapshot.getValue(Group.class);
                    if (group != null) {
                        boolean isCreator = group.getCreator() != null && 
                                         group.getCreator().getId().equals(userId);
                        boolean isMember = false;

                        // Check if user is a member
                        if (group.getUserPayList() != null) {
                            for (UserPay userPay : group.getUserPayList()) {
                                if (userPay != null && userPay.getUser() != null && 
                                    userPay.getUser().getId().equals(userId)) {
                                    isMember = true;
                                    break;
                                }
                            }
                        }

                        // Add group to appropriate list
                        if ("Paid".equals(group.getStatus())) {
                            if (isCreator || isMember) {
                                paidGroupsList.add(group);
                            }
                        } else {
                            if (isCreator) {
                                createdGroupsList.add(group);
                            } else if (isMember) {
                                joinedGroupsList.add(group);
                            }
                        }
                    }
                }

                // Update UI
                if (createdGroupsList.isEmpty() && joinedGroupsList.isEmpty() && paidGroupsList.isEmpty()) {
                    noGroupsText.setVisibility(View.VISIBLE);
                    rvCreatedGroups.setVisibility(View.GONE);
                    rvJoinedGroups.setVisibility(View.GONE);
                    rvPaidGroups.setVisibility(View.GONE);
                } else {
                    noGroupsText.setVisibility(View.GONE);
                    rvCreatedGroups.setVisibility(createdGroupsList.isEmpty() ? View.GONE : View.VISIBLE);
                    rvJoinedGroups.setVisibility(joinedGroupsList.isEmpty() ? View.GONE : View.VISIBLE);
                    rvPaidGroups.setVisibility(paidGroupsList.isEmpty() ? View.GONE : View.VISIBLE);
                }

                createdGroupsAdapter.notifyDataSetChanged();
                joinedGroupsAdapter.notifyDataSetChanged();
                paidGroupsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ExistentGroup", "Failed to load groups", error.toException());
                Toast.makeText(ExistentGroup.this, "Failed to load groups", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void goBack(View view) {
        onBackPressed();
    }
}

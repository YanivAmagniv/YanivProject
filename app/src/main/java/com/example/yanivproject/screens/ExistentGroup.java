// ExistentGroup.java
// This activity displays existing groups in three categories:
// - Created groups (groups where user is creator)
// - Joined groups (groups where user is participant)
// - Paid groups (groups where payments are complete)
// Implements real-time updates using Firebase Database

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

/**
 * Activity for displaying existing groups
 * Features:
 * - Three separate lists for different group states
 * - Real-time updates from Firebase
 * - Navigation to group details
 * - Creation of new groups
 * - Empty state handling
 */
public class ExistentGroup extends NavActivity {
    // RecyclerViews for different group categories
    private RecyclerView rvCreatedGroups;  // Groups created by the user
    private RecyclerView rvJoinedGroups;   // Groups joined by the user
    private RecyclerView rvPaidGroups;     // Groups with completed payments
    
    // Adapters for each RecyclerView
    private GroupAdapter createdGroupsAdapter;
    private GroupAdapter joinedGroupsAdapter;
    private GroupAdapter paidGroupsAdapter;
    
    // Lists to store groups for each category
    private ArrayList<Group> createdGroupsList;
    private ArrayList<Group> joinedGroupsList;
    private ArrayList<Group> paidGroupsList;
    
    // Firebase references and user data
    private DatabaseReference groupsRef;    // Reference to groups in Firebase
    private String userId;                  // Current user's ID
    
    // UI components
    private TextView noGroupsText;          // Text shown when no groups exist
    private Button addGroupButton;          // Button to create new group

    /**
     * Called when the activity is first created
     * Initializes UI components and sets up Firebase listeners
     * @param savedInstanceState Bundle containing the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_existent_group);
        setupNavigationDrawer();

        // Initialize views
        initializeViews();
        
        // Set up RecyclerViews
        setupRecyclerViews();

        // Get current user ID
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize Firebase reference
        groupsRef = FirebaseDatabase.getInstance().getReference("groups");

        // Load groups
        loadGroups();

        // Set up add group button
        setupAddGroupButton();
    }

    /**
     * Initializes all UI components
     * Finds and sets up views for user interaction
     */
    private void initializeViews() {
        rvCreatedGroups = findViewById(R.id.rvCreatedGroups);
        rvJoinedGroups = findViewById(R.id.rvJoinedGroups);
        rvPaidGroups = findViewById(R.id.rvPaidGroups);
        noGroupsText = findViewById(R.id.noGroupsText);
        addGroupButton = findViewById(R.id.addGroupButton);
    }

    /**
     * Sets up RecyclerViews and their adapters
     * Initializes lists and configures layout managers
     */
    private void setupRecyclerViews() {
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
    }

    /**
     * Sets up the add group button
     * Navigates to AddNewEvent activity on click
     */
    private void setupAddGroupButton() {
        addGroupButton.setOnClickListener(v -> {
            Intent intent = new Intent(ExistentGroup.this, AddNewEvent.class);
            startActivity(intent);
        });
    }

    /**
     * Loads groups from Firebase Database
     * Categorizes groups based on user's role and payment status
     * Updates UI in real-time when data changes
     */
    private void loadGroups() {
        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear existing lists
                createdGroupsList.clear();
                joinedGroupsList.clear();
                paidGroupsList.clear();
                
                // Process each group
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Group group = snapshot.getValue(Group.class);
                        if (group != null) {
                            // Log the raw group data for debugging
                            Log.d("ExistentGroup", "Raw group data: " + snapshot.getValue());
                            
                            // Check if user is creator
                            boolean isCreator = false;
                            if (group.getCreator() != null) {
                                isCreator = group.getCreator().getId().equals(userId);
                                Log.d("ExistentGroup", "Creator check - Group: " + group.getGroupName() + 
                                    ", Creator ID: " + group.getCreator().getId() + 
                                    ", Current User ID: " + userId + 
                                    ", Is Creator: " + isCreator);
                            } else {
                                Log.w("ExistentGroup", "Group has no creator: " + group.getGroupName());
                            }

                            // Check if user is member
                            boolean isMember = false;
                            List<UserPay> userPayList = group.getUserPayListAsList();
                            if (userPayList != null) {
                                for (UserPay userPay : userPayList) {
                                    if (userPay != null && userPay.getUser() != null && 
                                        userPay.getUser().getId().equals(userId)) {
                                        isMember = true;
                                        Log.d("ExistentGroup", "Found user as member in group: " + group.getGroupName());
                                        break;
                                    }
                                }
                            }

                            // Categorize group based on user's role and payment status
                            if (isCreator) {
                                // For creator, only show in paid if all members have paid
                                boolean allPaid = group.getUserPayListAsList().stream().allMatch(UserPay::isPaid);
                                if (allPaid) {
                                    paidGroupsList.add(group);
                                    Log.d("ExistentGroup", "Added to paid groups (creator): " + group.getGroupName());
                                } else {
                                    createdGroupsList.add(group);
                                    Log.d("ExistentGroup", "Added to created groups (creator): " + group.getGroupName());
                                }
                            } else if (isMember) {
                                // For participants, show in paid if their payment is approved
                                boolean isPaid = false;
                                for (UserPay userPay : group.getUserPayListAsList()) {
                                    if (userPay.getUser().getId().equals(userId) && 
                                        userPay.getPaymentStatus() == UserPay.PaymentStatus.PAID) {
                                        isPaid = true;
                                        break;
                                    }
                                }
                                
                                if (isPaid) {
                                    paidGroupsList.add(group);
                                    Log.d("ExistentGroup", "Added to paid groups (participant): " + group.getGroupName());
                                } else {
                                    joinedGroupsList.add(group);
                                    Log.d("ExistentGroup", "Added to joined groups (participant): " + group.getGroupName());
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("ExistentGroup", "Error processing group: " + snapshot.getKey(), e);
                    }
                }

                // Update UI based on group lists
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ExistentGroup", "Failed to load groups", error.toException());
                Toast.makeText(ExistentGroup.this, "Failed to load groups", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Updates the UI based on the state of group lists
     * Shows/hides appropriate views and updates adapters
     */
    private void updateUI() {
        if (createdGroupsList.isEmpty() && joinedGroupsList.isEmpty() && paidGroupsList.isEmpty()) {
            noGroupsText.setVisibility(View.VISIBLE);
            rvCreatedGroups.setVisibility(View.GONE);
            rvJoinedGroups.setVisibility(View.GONE);
            rvPaidGroups.setVisibility(View.GONE);
            Log.d("ExistentGroup", "No groups found for user: " + userId);
        } else {
            noGroupsText.setVisibility(View.GONE);
            rvCreatedGroups.setVisibility(createdGroupsList.isEmpty() ? View.GONE : View.VISIBLE);
            rvJoinedGroups.setVisibility(joinedGroupsList.isEmpty() ? View.GONE : View.VISIBLE);
            rvPaidGroups.setVisibility(paidGroupsList.isEmpty() ? View.GONE : View.VISIBLE);
            Log.d("ExistentGroup", "Groups found - Created: " + createdGroupsList.size() + 
                ", Joined: " + joinedGroupsList.size() + 
                ", Paid: " + paidGroupsList.size());
        }

        createdGroupsAdapter.notifyDataSetChanged();
        joinedGroupsAdapter.notifyDataSetChanged();
        paidGroupsAdapter.notifyDataSetChanged();
    }

    /**
     * Handles back button press
     * Navigates back to the previous activity
     * @param view The view that triggered the back action
     */
    public void goBack(View view) {
        onBackPressed();
    }
}

package com.example.yanivproject.screens;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.yanivproject.R;
import com.example.yanivproject.models.Group;
import com.example.yanivproject.models.User;
import com.example.yanivproject.models.UserPay;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class GroupDetailsActivity extends NavActivity {
    private TextView groupNameText, groupDescriptionText, groupDateText, groupTypeText, groupTotalAmountText, groupParticipantsText, groupStatusText;
    private Button deleteButton, editStatusButton;
    private Group group;
    private DatabaseReference groupRef;
    private TextView userAmountDueText;
    private String currentUserId; // Assume you have a way to get the current logged-in user ID


    private int numberOfParticipants;

    private double userOwedAmount;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);
        setupNavigationDrawer();

        // Initialize Views
        groupNameText = findViewById(R.id.group_name);
        groupDescriptionText = findViewById(R.id.group_description);
        groupDateText = findViewById(R.id.group_date);
        groupTypeText = findViewById(R.id.group_type);
        groupTotalAmountText = findViewById(R.id.group_totalAmount);
        groupParticipantsText = findViewById(R.id.group_participants);
        groupStatusText = findViewById(R.id.group_status);
        deleteButton = findViewById(R.id.delete_button);
        editStatusButton = findViewById(R.id.btnEditStatus);
        Button markCurrentUserPaidButton = findViewById(R.id.btnMarkCurrentUserPaid);

        userAmountDueText = findViewById(R.id.user_amount_due);

        // Retrieve the group object passed from the adapter
        group = (Group) getIntent().getSerializableExtra("group");

        if (group != null) {
            groupRef = FirebaseDatabase.getInstance().getReference("groups").child(group.getGroupId());

            // Set group details to the views
            groupNameText.setText(group.getGroupName());
            groupDescriptionText.setText(group.getGroupDescription() != null ? group.getGroupDescription() : "No description available");
            groupDateText.setText(group.getEventDate());
            groupTypeText.setText(group.getType());
            groupTotalAmountText.setText(" " + getSplittingMethod(group.getTotalAmount()));
            groupStatusText.setText(group.getStatus());

            currentUserId = "someUserId"; // Fetch from FirebaseAuth or shared preferences

            if (group != null) {
                // Calculate and display the user's owed amount
                displayUserOwedAmount();
            }

            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                currentUserId = firebaseUser.getUid(); // This is the current user's unique ID
            } else {
                Log.e("GroupDetailsActivity", "User not logged in");
            }

            // Set up mark current user paid button
            markCurrentUserPaidButton.setOnClickListener(v -> {
                // Find the current user's UserPay object
                for (UserPay userPay : group.getUserPayList()) {
                    if (userPay.getUser().getId().equals(currentUserId)) {
                        if (!userPay.isPaid()) {
                            markUserAsPaid(userPay);
                        } else {
                            Toast.makeText(this, "You have already marked your payment as complete", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }
                }
            });

            // Prepare participant names
            updateParticipantsList();

            // Disable "Mark as Paid" button if already paid
            if ("Paid".equals(group.getStatus())) {
                editStatusButton.setEnabled(false);
                markCurrentUserPaidButton.setEnabled(false);
            }
        } else {
            Log.e("GroupDetailsActivity", "Group is null. Could not retrieve group details.");
            Toast.makeText(this, "Failed to load group details.", Toast.LENGTH_SHORT).show();
        }

        // Set up delete button
        deleteButton.setOnClickListener(v -> deleteGroup());

        // Set up edit status button
        editStatusButton.setOnClickListener(v -> markGroupAsPaid());
    }

    // Utility method to format currency
    private String getSplittingMethod(double amount) {
        return String.format(Locale.US, "$%.2f", amount);
    }

    private void displayUserOwedAmount() {
        double userShare = 0;

        if (group.getUserPayList() != null) {
            for (UserPay userPay : group.getUserPayList()) {
                if (userPay.getUser().getId().equals(currentUserId)) {
                    Log.d("DEBUG", "Matched current user: " + currentUserId);

                    if (group.getSplitMethod().equals("חלוקה לפי אחוזים")) {
                        userShare = (userPay.getAmount() / group.getTotalAmount()) * 100;
                        Log.d("DEBUG", "Percentage Split - User Share: " + userShare);
                    } else if (group.getSplitMethod().equals("חלוקה מותאמת אישית")) {
                        userShare = userPay.getAmount();
                        Log.d("DEBUG", "Custom Split - User Share: " + userShare);
                    } else { // Equal split
                        userShare = userPay.getAmount();
                        Log.d("DEBUG", "Equal Split - User Share: " + userShare);
                    }
                    break;
                }
            }
        } else {
            Log.e("DEBUG", "group.getUserPayList() is null");
        }

        Log.d("DEBUG", "Final User Share: " + userShare);
        userAmountDueText.setText(getCurrencyString(userShare));
    }

    private void markUserAsPaid(UserPay userPay) {
        if (group != null && group.getGroupId() != null) {
            // Find the index of the user in the userPayList
            int userIndex = -1;
            for (int i = 0; i < group.getUserPayList().size(); i++) {
                if (group.getUserPayList().get(i).getUser().getId().equals(userPay.getUser().getId())) {
                    userIndex = i;
                    break;
                }
            }

            if (userIndex != -1) {
                // Update the user's payment status in Firebase
                groupRef.child("userPayList").child(String.valueOf(userIndex)).child("isPaid").setValue(true)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(GroupDetailsActivity.this, 
                                "Payment marked as completed for " + userPay.getUser().getName(), 
                                Toast.LENGTH_SHORT).show();
                            
                            // Update local data
                            userPay.setPaid(true);
                            
                            // Check if all users have paid
                            checkAndUpdateGroupStatus();
                            
                            // Refresh the participants list
                            updateParticipantsList();
                        } else {
                            Toast.makeText(GroupDetailsActivity.this, 
                                "Failed to update payment status", 
                                Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        }
    }

    private void updateParticipantsList() {
        StringBuilder participants = new StringBuilder();
        for (UserPay userPay : group.getUserPayList()) {
            participants.append(userPay.getUser().getName())
                      .append("  ")
                      .append(userPay.isPaid() ? "שולם" : "לא שולם")
                      .append("\n");
        }
        groupParticipantsText.setText(participants.toString());
    }

    private String getCurrencyString(double totalAmount) {
        return String.format(Locale.US, "$%.2f", totalAmount);
    }

    private void deleteGroup() {
        if (group != null && group.getGroupId() != null) {
            FirebaseDatabase.getInstance().getReference("groups")
                    .child(group.getGroupId())
                    .removeValue()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Group deleted successfully", Toast.LENGTH_SHORT).show();
                            finish();  // Close activity after deletion
                        } else {
                            Toast.makeText(this, "Failed to delete group", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Invalid group, cannot delete", Toast.LENGTH_SHORT).show();
        }
    }

    private void markGroupAsPaid() {
        if (group != null && group.getGroupId() != null) {
            groupRef.child("status").setValue("Paid").addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(GroupDetailsActivity.this, "Status updated to Paid", Toast.LENGTH_SHORT).show();
                    groupStatusText.setText("Paid");
                    editStatusButton.setEnabled(false);
                } else {
                    Toast.makeText(GroupDetailsActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void checkAndUpdateGroupStatus() {
        if (group != null && group.getUserPayList() != null) {
            boolean allPaid = true;
            for (UserPay userPay : group.getUserPayList()) {
                if (!userPay.isPaid()) {
                    allPaid = false;
                    break;
                }
            }

            if (allPaid && !"Paid".equals(group.getStatus())) {
                markGroupAsPaid();
            }
        }
    }

    public void goBack(View view) {
        onBackPressed();  // This will navigate back to the previous activity
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh group data and check status when activity resumes
        if (group != null && group.getGroupId() != null) {
            groupRef.get().addOnSuccessListener(snapshot -> {
                Group updatedGroup = snapshot.getValue(Group.class);
                if (updatedGroup != null) {
                    group = updatedGroup;
                    // Update UI with new data
                    groupStatusText.setText(group.getStatus());
                    // Check if all users have paid
                    checkAndUpdateGroupStatus();
                }
            });
        }
    }
}

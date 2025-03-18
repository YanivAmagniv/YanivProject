package com.example.yanivproject.screens;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class GroupDetailsActivity extends AppCompatActivity {
    private TextView groupNameText, groupDescriptionText, groupDateText, groupTypeText, groupTotalAmountText, groupParticipantsText, groupStatusText;
    private Button deleteButton, editStatusButton;
    private Group group;
    private DatabaseReference groupRef;
    private TextView userAmountDueText;
    private String currentUserId; // Assume you have a way to get the current logged-in user ID

    private double totalAmount;
    private int numberOfParticipants;
    private String splittingMethod;
    private double userOwedAmount;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

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


            // Handle each splitting method
            if ("Even Split".equals(splittingMethod)) {
                // Split evenly among all participants
                userOwedAmount = totalAmount / numberOfParticipants;
            } else if ("Percentage Split".equals(splittingMethod)) {
                // Assume the user has a percentage input
                double userPercentage = getUserPercentage(currentUserId); // Fetch user's percentage
                userOwedAmount = (userPercentage / 100) * totalAmount;
            } else if ("Custom Split".equals(splittingMethod)) {
                // If user has input a custom amount
                userOwedAmount = getUserCustomAmount(currentUserId); // Fetch custom amount for the user
            }

            // Prepare participant names
            StringBuilder participants = new StringBuilder();
            if (group.getUsers() != null) {
                for (UserPay userPay : group.getUsers()) {
                    User user = userPay.getUser();
                    if (user != null) {
                        participants.append(user.getFullName()).append("\n");
                    } else {
                        participants.append("Unknown User\n");
                    }
                }
            } else {
                participants.append("No participants available");
            }
            groupParticipantsText.setText(participants.toString());

            // Disable "Mark as Paid" button if already paid
            if ("Paid".equals(group.getStatus())) {
                editStatusButton.setEnabled(false);
            }
        } else {
            Log.e("GroupDetailsActivity", "Group is null. Could not retrieve group details.");
            Toast.makeText(this, "Failed to load group details.", Toast.LENGTH_SHORT).show();
        }

        // Set up delete button
        deleteButton.setOnClickListener(v -> deleteGroup());

        // Set up edit status button
        editStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markGroupAsPaid();
            }
        });
    }

    // Utility method to format currency
    private String getSplittingMethod(double amount) {
        return String.format("$%.2f", amount);
    }

    // Helper method to get the user's percentage (this could come from a field in the database)
    private double getUserPercentage(String userId) {
        double userPercentage = 0.0;

        // Assuming you have a list of `UserPay` objects in your group model
        if (group.getUsers() != null) {
            for (UserPay userPay : group.getUsers()) {
                if (userPay.getUser().getId().equals(userId)) {
                    userPercentage = userPay.getPercentage(); // Assuming `percentage` is a field in UserPay
                    break;
                }
            }
        }

        return userPercentage;
    }

    // Helper method to get the user's custom amount (could be user input or from the database)
    private double getUserCustomAmount(String userId) {
        double userCustomAmount = 0.0;

        // Assuming each user in `UserPay` has a custom amount field
        if (group.getUsers() != null) {
            for (UserPay userPay : group.getUsers()) {
                if (userPay.getUser().getId().equals(userId)) {
                    userCustomAmount = userPay.getCustomAmount(); // Assuming `customAmount` is a field in UserPay
                    break;
                }
            }
        }

        return userCustomAmount;
    }

    private void displayUserOwedAmount() {
        double userShare = 0.0;

        if (group.getUsers() != null) {
            for (UserPay userPay : group.getUsers()) {
                if (userPay.getUser().getId().equals(currentUserId)) {
                    if ("Even Split".equals(splittingMethod)) {
                        // Split evenly among all participants
                        userShare = totalAmount / numberOfParticipants;
                    } else if ("Percentage Split".equals(splittingMethod)) {
                        // Use the user's percentage responsibility
                        double userPercentage = getUserPercentage(currentUserId);
                        userShare = (userPercentage / 100) * totalAmount;
                    } else if ("Custom Split".equals(splittingMethod)) {
                        // Use the custom amount the user owes
                        userShare = getUserCustomAmount(currentUserId);
                    }
                    break;
                }
            }
        }

        userAmountDueText.setText(getCurrencyString(userShare));
    }

    private String getCurrencyString(double totalAmount) {
        return String.format("$%.2f", totalAmount);
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
}

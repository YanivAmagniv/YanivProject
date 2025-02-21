package com.example.yanivproject.screens;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yanivproject.R;
import com.example.yanivproject.models.Group;
import com.example.yanivproject.models.User;
import com.example.yanivproject.models.UserPay;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class GroupDetailsActivity extends AppCompatActivity {
    private TextView groupNameText, groupDescriptionText, groupDateText, groupTypeText, groupCurrencyText, groupParticipantsText;
    private Button deleteButton;
    private Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        // Initialize Views
        groupNameText = findViewById(R.id.group_name);
        groupDescriptionText = findViewById(R.id.group_description);
        groupDateText = findViewById(R.id.group_date);
        groupTypeText = findViewById(R.id.group_type);
        groupCurrencyText = findViewById(R.id.group_currency);
        groupParticipantsText = findViewById(R.id.group_participants);
        deleteButton = findViewById(R.id.delete_button);

        // Retrieve the group object passed from the adapter
        group = (Group) getIntent().getSerializableExtra("group");

        if (group != null) {
            // Set group details to the views
            groupNameText.setText(group.getGroupName());
            groupDescriptionText.setText(group.getGroupDescription() != null ? group.getGroupDescription() : "No description available");
            groupDateText.setText(group.getEventDate());
            groupTypeText.setText(group.getType());
            groupCurrencyText.setText("Currency: " + getCurrencyString(group.getTotalAmount()));

            // Prepare participant names
            StringBuilder participants = new StringBuilder();
            if (group.getUsers() != null) {
                for (UserPay userPay : group.getUsers()) {
                    User user = userPay.getUser();
                    if (user != null) {
                        participants.append(user.getFullName()).append("\n");
                    } else {
                        participants.append("Unknown User\n"); // Fallback for null user
                    }
                }
            } else {
                participants.append("No participants available");
            }
            groupParticipantsText.setText(participants.toString());
        } else {
            Log.e("GroupDetailsActivity", "Group is null. Could not retrieve group details.");
            Toast.makeText(this, "Failed to load group details.", Toast.LENGTH_SHORT).show();
        }

        // Set up the delete button
        deleteButton.setOnClickListener(v -> deleteGroup());
    }

    private String getCurrencyString(double totalAmount) {
        // Format the currency string as needed
        return String.format("$%.2f", totalAmount);  // Example formatting, change as needed
    }

    private void deleteGroup() {
        if (group != null && group.getGroupId() != null) {
            FirebaseDatabase.getInstance().getReference("groups")
                    .child(group.getGroupId())
                    .removeValue()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Group deleted successfully", Toast.LENGTH_SHORT).show();
                            finish();  // Close the activity after deletion
                        } else {
                            Toast.makeText(this, "Failed to delete group", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Invalid group, cannot delete", Toast.LENGTH_SHORT).show();
        }
    }
}

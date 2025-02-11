package com.example.yanivproject.screens;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yanivproject.R;
import com.example.yanivproject.models.Group;
import com.google.firebase.database.FirebaseDatabase;

public class GroupDetailsActivity extends AppCompatActivity {

    private TextView groupNameText, groupDescriptionText, groupDateText, groupTypeText;
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
        deleteButton = findViewById(R.id.delete_button);

        // Retrieve the group object passed from the adapter
        group = (Group) getIntent().getSerializableExtra("group");

        if (group != null) {
            // Set group details to the views
            groupNameText.setText(group.getGroupName());
            groupDescriptionText.setText(group.getGroupDescription());
            groupDateText.setText(group.getEventDate());
            groupTypeText.setText(group.getType());
        }

        // Set up the delete button
        deleteButton.setOnClickListener(v -> deleteGroup());
    }

    private void deleteGroup() {
        // Remove the group from Firebase using its groupId
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

    }
}
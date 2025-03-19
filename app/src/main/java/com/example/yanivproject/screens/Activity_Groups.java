package com.example.yanivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.yanivproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Activity_Groups extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_groups);

        // Check if the current user is an admin
        if (isAdmin()) {
            findViewById(R.id.btnAdminPage).setVisibility(View.VISIBLE); // Show admin button
        } else {
            findViewById(R.id.btnAdminPage).setVisibility(View.GONE); // Hide admin button
        }
    }

    // Check if the current user is an admin
    private boolean isAdmin() {
        // Replace this with actual logic for determining if the user is an admin.
        // For now, we'll just assume the user is admin if they are logged in.
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return userId != null && userId.equals("adminUserId"); // Replace with actual admin check
    }

    public void go_newgroup(View view) {
        Intent go = new Intent(getApplicationContext(), AddNewEvent.class);
        startActivity(go);
    }

    public void go_ExistentGroup(View v) {
        Intent go = new Intent(getApplicationContext(), ExistentGroup.class);
        startActivity(go);
    }


    public void go_UserDetails(View view) {
        Intent go = new Intent(getApplicationContext(), UserDetails.class);
        startActivity(go);
    }

    public void goAdminPage(View view) {
        Intent intent = new Intent(getApplicationContext(), AdminActivity.class);
        startActivity(intent);
    }
    public void goBack(View view) {
        onBackPressed();  // This will navigate back to the previous activity
    }
}

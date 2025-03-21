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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Activity_Groups extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_groups);

        // Check if the current user is an admin
        checkIfAdmin();

    }

    // Check if the current user is an admin
    private void checkIfAdmin() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Boolean isAdmin = snapshot.child("isAdmin").getValue(Boolean.class);
                if (isAdmin != null && isAdmin) {
                    findViewById(R.id.btnAdminPage).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.btnAdminPage).setVisibility(View.GONE);
                }
            }
        }).addOnFailureListener(e -> {
            findViewById(R.id.btnAdminPage).setVisibility(View.GONE);
        });
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

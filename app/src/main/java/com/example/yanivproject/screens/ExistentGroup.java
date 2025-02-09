package com.example.yanivproject.screens;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yanivproject.R;
import com.example.yanivproject.adapters.GroupAdapter;
import com.example.yanivproject.services.AuthenticationService;
import com.example.yanivproject.services.DatabaseService;

import com.example.yanivproject.models.Group;  //Import the Group model
import com.example.yanivproject.models.UserPay;  // If you have this class for user payments
import com.example.yanivproject.models.User;  // If you have this class for users
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ExistentGroup extends AppCompatActivity {
    DatabaseService databaseService;
    RecyclerView rvMyGroups;

    List<Group> groupList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_existent_group);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize RecyclerView
        rvMyGroups = findViewById(R.id.rvMyGroups);
        rvMyGroups.setLayoutManager(new LinearLayoutManager(this));

        // Initialize services and group list
        databaseService = DatabaseService.getInstance();
        String userId = AuthenticationService.getInstance().getCurrentUserId(); // Get current user ID
        groupList = new ArrayList<>();

        // Set adapter
        GroupAdapter adapter = new GroupAdapter(groupList);
        rvMyGroups.setAdapter(adapter);

        // Fetch groups from Firebase and filter by admin ID or members list
        databaseService.getGroups(new DatabaseService.DatabaseCallback<List<Group>>() {
            @Override
            public void onCompleted(List<Group> groups) {
                groupList.clear();

                for (Group group : groups) {
                    // Check if user is the admin or a member
                    boolean isAdmin = group.getAdmin().getId().equals(userId);
                    boolean isMember = false;

                    if (group.getUsers() != null) {  // Ensure it's not null before looping
                        for (UserPay userPay : group.getUsers()) {  // Loop through UserPay objects
                            if (userPay.getUser() != null && userPay.getUser().getId().equals(userId)) {
                                isMember = true;
                                break; //  Stop the loop immediately after finding the user
                            }
                        }
                    }

                    if (isAdmin || isMember) {
                        groupList.add(group);
                    }
                }

                adapter.notifyDataSetChanged(); // Refresh UI
            }

            @Override
            public void onFailed(Exception e) {
                Log.e("ExistentGroup", "Error fetching groups", e);
            }
        });


    }

}
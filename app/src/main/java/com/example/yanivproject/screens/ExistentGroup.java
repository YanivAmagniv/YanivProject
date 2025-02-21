package com.example.yanivproject.screens;

import android.os.Bundle;
import android.util.Log;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yanivproject.R;
import com.example.yanivproject.adapters.GroupAdapter;
import com.example.yanivproject.models.Group;
import com.example.yanivproject.models.UserPay;
import com.example.yanivproject.services.AuthenticationService;
import com.example.yanivproject.services.DatabaseService;

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
        setContentView(R.layout.activity_existent_group);  // Ensure this matches your layout file name

        // Set up window insets for edge-to-edge support
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
        String userId = AuthenticationService.getInstance().getCurrentUserId();  // Get current user ID
        groupList = new ArrayList<>();

        // Set adapter
        GroupAdapter adapter = new GroupAdapter(groupList, this);
        rvMyGroups.setAdapter(adapter);

        // Fetch groups from Firebase and filter by admin ID or members list
        databaseService.getGroups(new DatabaseService.DatabaseCallback<List<Group>>() {
            @Override
            public void onCompleted(List<Group> groups) {
                groupList.clear();

                for (Group group : groups) {
                    boolean isAdmin = group.getAdmin().getId().equals(userId);
                    boolean isMember = false;

                    if (group.getUsers() != null) {
                        for (UserPay userPay : group.getUsers()) {
                            if (userPay.getUser() != null && userPay.getUser().getId().equals(userId)) {
                                isMember = true;
                                break;
                            }
                        }
                    }

                    if (isAdmin || isMember) {
                        groupList.add(group);
                    }
                }

                Log.d("ExistentGroup", "Groups fetched: " + groupList.size());

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed(Exception e) {
                Log.e("ExistentGroup", "Error fetching groups", e);
            }
        });
    }
}

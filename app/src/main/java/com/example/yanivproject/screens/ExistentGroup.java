package com.example.yanivproject.screens;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

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
    RecyclerView rvPaidGroups, rvUnpaidGroups;

    List<Group> paidGroups = new ArrayList<>();
    List<Group> unpaidGroups = new ArrayList<>();

    GroupAdapter paidAdapter, unpaidAdapter;

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

        // Initialize RecyclerViews for Paid and Unpaid groups
        rvPaidGroups = findViewById(R.id.rvPaidGroups);
        rvUnpaidGroups = findViewById(R.id.rvUnpaidGroups);

        rvPaidGroups.setLayoutManager(new LinearLayoutManager(this));
        rvUnpaidGroups.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapters
        paidAdapter = new GroupAdapter(paidGroups, this);
        unpaidAdapter = new GroupAdapter(unpaidGroups, this);

        rvPaidGroups.setAdapter(paidAdapter);
        rvUnpaidGroups.setAdapter(unpaidAdapter);

        // Initialize Firebase database service
        databaseService = DatabaseService.getInstance();
        String userId = AuthenticationService.getInstance().getCurrentUserId(); // Get current user ID

        fetchGroups(userId);
    }

    private void fetchGroups(String userId) {
        databaseService.getGroups(new DatabaseService.DatabaseCallback<List<Group>>() {
            @Override
            public void onCompleted(List<Group> groups) {
                paidGroups.clear();
                unpaidGroups.clear();

                for (Group group : groups) {
                    boolean isAdmin = group.getAdmin() != null && group.getAdmin().getId().equals(userId);
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
                        if ("Paid".equals(group.getStatus())) {
                            paidGroups.add(group);
                        } else {
                            unpaidGroups.add(group);
                        }
                    }
                }

                Log.d("ExistentGroup", "Paid Groups: " + paidGroups.size() + ", Unpaid Groups: " + unpaidGroups.size());

                updateUI();
            }

            @Override
            public void onFailed(Exception e) {
                Log.e("ExistentGroup", "Error fetching groups", e);
            }
        });
    }

    private void updateUI() {
        paidAdapter.notifyDataSetChanged();
        unpaidAdapter.notifyDataSetChanged();
    }
    public void goBack(View view) {
        onBackPressed();  // This will navigate back to the previous activity
    }

}

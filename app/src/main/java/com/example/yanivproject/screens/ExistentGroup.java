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
        rvMyGroups = findViewById(R.id.rvMyGroups); // Ensure the ID matches in the XML layout
        rvMyGroups.setLayoutManager(new LinearLayoutManager(this));

        // Initialize group list
        databaseService = DatabaseService.getInstance();
        groupList = new ArrayList<>();

        // Set adapter
        GroupAdapter adapter = new GroupAdapter(groupList);
        rvMyGroups.setAdapter(adapter);

        // Fetch groups from DatabaseService
        DatabaseService databaseService = DatabaseService.getInstance();
        databaseService.getGroups(new DatabaseService.DatabaseCallback<List<Group>>() {
            @Override
            public void onCompleted(List<Group> groups) {
                groupList.clear();
                groupList.addAll(groups);
                adapter.notifyDataSetChanged(); // Notify the adapter about the data change
            }

            @Override
            public void onFailed(Exception e) {
                Log.e("ExistentGroup", "Error fetching groups", e);
            }
        });


    }

}
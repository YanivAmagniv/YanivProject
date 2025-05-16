package com.example.yanivproject.screens;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yanivproject.R;
import com.example.yanivproject.adapters.UserAdapter;
import com.example.yanivproject.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends NavActivity {

    private ListView userListView;
    private List<User> userList;
    private UserAdapter adapter;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        setupNavigationDrawer();

        // Initialize views and Firebase reference
        userListView = findViewById(R.id.userListView);
        userList = new ArrayList<>();
        adapter = new UserAdapter(this, R.layout.user_item, userList);
        userListView.setAdapter(adapter);

        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        // Fetch all users
        fetchAllUsers();
    }

    private void fetchAllUsers() {
        usersRef.get().addOnSuccessListener(snapshot -> {
            userList.clear();
            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                User user = userSnapshot.getValue(User.class);
                if (user != null) {
                    userList.add(user);
                }
            }
            adapter.notifyDataSetChanged(); // Safe to call now
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to fetch users", Toast.LENGTH_SHORT).show();
        });
    }
}

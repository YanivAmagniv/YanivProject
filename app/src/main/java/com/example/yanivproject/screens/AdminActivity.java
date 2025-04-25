package com.example.yanivproject.screens;

import android.os.Bundle;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yanivproject.R;
import com.example.yanivproject.adapters.UserAdapter;
import com.example.yanivproject.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private EditText etSearch;
    private ListView userListView;
    private DatabaseReference userRef;
    private List<User> userList;
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);


        // Initialize views
        etSearch = findViewById(R.id.etSearch);
        userListView = findViewById(R.id.userListView);

        // Initialize Firebase reference
        userRef = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize user list and adapter
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this, R.layout.user_item, R.id.tvfname, userList);
        userListView.setAdapter(userAdapter);

        // Fetch all users from the Firebase database
        fetchAllUsers();

        // Implement search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                searchUsers(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish(); // Close activity if user is not logged in
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        userRef.get().addOnSuccessListener(snapshot -> {
            Boolean isAdmin = snapshot.child("isAdmin").getValue(Boolean.class);
            if (isAdmin == null || !isAdmin) {
                Toast.makeText(AdminActivity.this, "Access Denied!", Toast.LENGTH_SHORT).show();
                finish(); // Close the activity if user is not admin
            }
        }).addOnFailureListener(e -> finish());
    }

    private void fetchAllUsers() {
        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                userList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }
                userAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(AdminActivity.this, "Failed to load users.", Toast.LENGTH_SHORT).show();
        });
    }

    private void searchUsers(String query) {
        Query firebaseQuery = userRef.orderByChild("fname").startAt(query).endAt(query + "\uf8ff");

        firebaseQuery.get().addOnSuccessListener(snapshot -> {
            userList.clear();
            if (snapshot.exists()) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }
                userAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(AdminActivity.this, "Failed to search users.", Toast.LENGTH_SHORT).show();
        });
    }
}

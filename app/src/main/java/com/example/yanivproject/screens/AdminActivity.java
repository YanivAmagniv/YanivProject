// AdminActivity.java
// This activity provides administrative functionality for managing users
// Features:
// - Display list of all users in the system
// - Real-time search functionality
// - Admin-only access control
// - User management capabilities

package com.example.yanivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yanivproject.R;
import com.example.yanivproject.adapters.UserAdapter;
import com.example.yanivproject.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for administrative user management
 * Provides functionality to view and manage all users in the system
 * Implements real-time search and filtering capabilities
 */
public class AdminActivity extends NavActivity {

    // UI Components
    private ListView userListView;        // Displays the list of users
    private List<User> userList;          // Stores all users
    private List<User> filteredUserList;  // Stores filtered users for search
    private UserAdapter adapter;          // Adapter for the user list
    private DatabaseReference usersRef;   // Firebase reference to users
    private FirebaseAuth mAuth;           // Firebase authentication instance
    private EditText searchBar;           // Search input field

    /**
     * Called when the activity is first created
     * Initializes UI components and checks admin access
     * @param savedInstanceState Bundle containing the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check if user is admin before showing the activity
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "יש להתחבר תחילה", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Check admin status in database
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        userRef.get().addOnSuccessListener(snapshot -> {
            User user = snapshot.getValue(User.class);
            if (user == null || !user.getAdmin()) {
                Toast.makeText(this, "גישה למנהלים בלבד", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return;
            }
            
            // User is admin, continue with activity setup
            setContentView(R.layout.activity_admin);
            setupNavigationDrawer();

            // Initialize views and Firebase reference
            userListView = findViewById(R.id.userListView);
            searchBar = findViewById(R.id.etSearch);
            userList = new ArrayList<>();
            filteredUserList = new ArrayList<>();
            adapter = new UserAdapter(this, R.layout.user_item, filteredUserList);
            userListView.setAdapter(adapter);

            // Setup search functionality
            setupSearchBar();

            usersRef = FirebaseDatabase.getInstance().getReference("Users");

            // Fetch all users
            fetchAllUsers();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "שגיאה בטעינת פרטי משתמש", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Sets up the search bar functionality
     * Implements real-time filtering as user types
     */
    private void setupSearchBar() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Filters the user list based on search query
     * Matches against user's first name, last name, and email
     * @param query The search query string
     */
    private void filterUsers(String query) {
        filteredUserList.clear();
        if (query.isEmpty()) {
            filteredUserList.addAll(userList);
        } else {
            query = query.toLowerCase();
            for (User user : userList) {
                if (user.getFname() != null && user.getFname().toLowerCase().contains(query) ||
                    user.getLname() != null && user.getLname().toLowerCase().contains(query) ||
                    user.getEmail() != null && user.getEmail().toLowerCase().contains(query)) {
                    filteredUserList.add(user);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * Fetches all users from Firebase Database
     * Updates the UI with the fetched users
     * Handles success and failure cases
     */
    private void fetchAllUsers() {
        usersRef.get().addOnSuccessListener(snapshot -> {
            userList.clear();
            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                User user = userSnapshot.getValue(User.class);
                if (user != null) {
                    userList.add(user);
                }
            }
            // Initialize filtered list with all users
            filteredUserList.clear();
            filteredUserList.addAll(userList);
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to fetch users", Toast.LENGTH_SHORT).show();
        });
    }
}

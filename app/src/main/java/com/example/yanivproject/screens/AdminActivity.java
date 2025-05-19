package com.example.yanivproject.screens;

import android.content.Intent;
import android.os.Bundle;
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

public class AdminActivity extends NavActivity {

    private ListView userListView;
    private List<User> userList;
    private UserAdapter adapter;
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;

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
            userList = new ArrayList<>();
            adapter = new UserAdapter(this, R.layout.user_item, userList);
            userListView.setAdapter(adapter);

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

    private void fetchAllUsers() {
        usersRef.get().addOnSuccessListener(snapshot -> {
            userList.clear();
            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                User user = userSnapshot.getValue(User.class);
                if (user != null) {
                    userList.add(user);
                }
            }
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to fetch users", Toast.LENGTH_SHORT).show();
        });
    }
}

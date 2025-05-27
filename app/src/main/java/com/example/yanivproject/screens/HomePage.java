package com.example.yanivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.yanivproject.R;
import com.example.yanivproject.models.User;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomePage extends NavActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Button btnAdminPage;
    private TextView welcomeText;
    private User currentUser;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawerLayout;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);
        setupNavigationDrawer();

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize views
        btnAdminPage = findViewById(R.id.btnAdminPage);
        btnAdminPage.setVisibility(View.GONE); // Default to hidden
        welcomeText = findViewById(R.id.welcomeText);
        drawerLayout = findViewById(R.id.drawer_layout);

        // Get current user ID
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
            loadUserData();
        }

        // Check if the current user is an admin
        checkIfAdmin();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Menu menu = navigationView.getMenu();

        // Set up the navigation drawer toggle
        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        ) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Make sure these lines are here
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Optional: Hide admin menu item for non-admins
        if (currentUser != null && !currentUser.getAdmin()) {
            MenuItem adminItem = menu.findItem(R.id.nav_admin_page);
            if (adminItem != null) {
                adminItem.setVisible(false);
            }
        }
    }

    private void loadUserData() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    
                    // Handle null values
                    firstName = (firstName != null) ? firstName : "";
                    lastName = (lastName != null) ? lastName : "";
                    
                    String fullName = (firstName + " " + lastName).trim();
                    if (fullName.isEmpty()) {
                        fullName = "משתמש";
                    }
                    
                    // Update welcome message
                    welcomeText.setText("ברוך הבא, " + fullName + "!");
                } else {
                    // Handle case where user data doesn't exist
                    welcomeText.setText("ברוך הבא!");
                    Log.e("HomePage", "User data not found for ID: " + currentUserId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                welcomeText.setText("ברוך הבא!");
                Toast.makeText(HomePage.this, "שגיאה בטעינת נתוני משתמש", Toast.LENGTH_SHORT).show();
                Log.e("HomePage", "Database error: " + error.getMessage());
            }
        });
    }

    // Check if the current user is an admin
    private void checkIfAdmin() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Boolean isAdmin = snapshot.child("admin").getValue(Boolean.class);
                if (isAdmin != null && isAdmin) {
                    btnAdminPage.setVisibility(View.VISIBLE);
                } else {
                    btnAdminPage.setVisibility(View.GONE);
                }
            }
        }).addOnFailureListener(e -> btnAdminPage.setVisibility(View.GONE));
        Log.d("AdminCheck", "Current UID: " + currentUser.getUid());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Don't navigate, we're already in HomePage
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_new_group) {
            startActivity(new Intent(this, AddNewEvent.class));
        } else if (id == R.id.nav_existent_groups) {
            startActivity(new Intent(this, ExistentGroup.class));
        } else if (id == R.id.nav_user_details) {
            startActivity(new Intent(this, UserDetails.class));
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (id == R.id.nav_admin_page) {
            startActivity(new Intent(this, AdminActivity.class));
        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, Login.class));
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    // Button click handlers
    public void go_newgroup(View view) {
        startActivity(new Intent(this, AddNewEvent.class));
    }

    public void go_ExistentGroup(View v) {
        startActivity(new Intent(this, ExistentGroup.class));
    }

    public void go_UserDetails(View view) {
        startActivity(new Intent(this, UserDetails.class));
    }

    public void go_About(View view) {
        startActivity(new Intent(this, AboutActivity.class));
    }

    public void goAdminPage(View view) {
        startActivity(new Intent(this, AdminActivity.class));
    }

    public void goBack(View view) {
        onBackPressed();
    }
}

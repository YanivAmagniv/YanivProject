package com.example.yanivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.yanivproject.R;
import com.example.yanivproject.models.User;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Activity_Groups extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {

    private Button btnAdminPage;
    private User currentUser;
    ActionBarDrawerToggle toggle;

    DrawerLayout drawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_groups);

        btnAdminPage = findViewById(R.id.btnAdminPage);  // Initialize here
        btnAdminPage.setVisibility(View.GONE); // Default to hidden
        drawerLayout = findViewById(R.id.drawer_layout);



        // Check if the current user is an admin
        checkIfAdmin();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Menu menu = navigationView.getMenu();

        // Set the listener for navigation item clicks
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return onNavigationItemSelected(item);
            }
        });

        // Enable the hamburger icon
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

// Enable the ActionBar's Up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


// Optional: Hide admin menu item for non-admins

        if (currentUser != null && !currentUser.getAdmin()) {
            MenuItem adminItem = menu.findItem(R.id.nav_admin_page);
            if (adminItem != null) {
                adminItem.setVisible(false);
            }
        }
    }

    // Check if the current user is an admin
    private void checkIfAdmin() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Boolean isAdmin = snapshot.child("admin").getValue(Boolean.class);
                Toast.makeText(this, "isAdmin: " + isAdmin, Toast.LENGTH_SHORT).show();  // 👈 Add this

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
            startActivity(new Intent(this, MainActivity.class));
        } else if (id == R.id.nav_new_group) {
            startActivity(new Intent(this, AddNewEvent.class));
        } else if (id == R.id.nav_existent_groups) {
            startActivity(new Intent(this, ExistentGroup.class));
        } else if (id == R.id.nav_user_details) {
            startActivity(new Intent(this, UserDetails.class));
        } else if (id == R.id.nav_admin_page) {
            startActivity(new Intent(this, AdminActivity.class));
        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, Login.class));
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

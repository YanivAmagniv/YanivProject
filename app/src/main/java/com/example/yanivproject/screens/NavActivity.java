// NavActivity.java
// This is a base activity that provides navigation drawer functionality
// Features:
// - Common navigation drawer setup
// - Admin menu item visibility control
// - Navigation item selection handling
// - Drawer state management

package com.example.yanivproject.screens;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Base activity that implements navigation drawer functionality
 * Provides common navigation features for all activities that extend it
 * Manages drawer state and navigation item selection
 */
public abstract class NavActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // Protected variables that child activities can access
    protected DrawerLayout drawerLayout;      // The navigation drawer layout
    protected ActionBarDrawerToggle toggle;   // Toggle for opening/closing drawer
    protected NavigationView navigationView;  // The navigation view containing menu items

    /**
     * Sets up the navigation drawer with toolbar and toggle
     * Called by child activities to initialize navigation
     */
    protected void setupNavigationDrawer() {
        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Find our drawer and navigation view
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Make sure we have an ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        // Create the toggle for opening/closing the drawer
        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        // Add the toggle to the drawer
        drawerLayout.addDrawerListener(toggle);
        
        // Important: This needs to be called after setting up the DrawerLayout
        toggle.syncState();

        // Set up the listener for navigation item clicks
        navigationView.setNavigationItemSelectedListener(this);

        // Check if user is admin and hide admin menu item if not
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
            userRef.get().addOnSuccessListener(snapshot -> {
                User user = snapshot.getValue(User.class);
                if (user != null && !user.getAdmin()) {
                    // Hide admin menu item for non-admin users
                    navigationView.getMenu().findItem(R.id.nav_admin_page).setVisible(false);
                }
            });
        }
    }

    /**
     * Handles configuration changes to maintain drawer state
     * @param newConfig The new configuration
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    /**
     * Handles back button press
     * Closes drawer if open, otherwise performs normal back action
     */
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Handles navigation item selection
     * @param item The selected menu item
     * @return true if the item was handled
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            startActivity(new Intent(this, HomePage.class));
        } else if (id == R.id.nav_existent_groups) {
            startActivity(new Intent(this, ExistentGroup.class));
        } else if (id == R.id.nav_user_details) {
            startActivity(new Intent(this, UserDetails.class));
        } else if (id == R.id.nav_admin_page) {
            startActivity(new Intent(this, AdminActivity.class));
        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
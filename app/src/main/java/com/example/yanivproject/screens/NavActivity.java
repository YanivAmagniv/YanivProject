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
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public abstract class NavActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // Protected variables that child activities can access
    protected DrawerLayout drawerLayout;
    protected ActionBarDrawerToggle toggle;
    protected NavigationView navigationView;

    // Method to set up the navigation drawer - called by child activities
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
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (toggle != null) {
            toggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (toggle != null) {
            toggle.onConfigurationChanged(newConfig);
        }
    }

    // Handle navigation item clicks
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // Don't navigate if we're already on the selected screen
        if (this.getClass().getSimpleName().equals(getDestinationActivity(id).getSimpleName())) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        // Start the appropriate activity based on which menu item was clicked
        if (id == R.id.nav_home) {
            Intent intent = new Intent(this, HomePage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
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
            Intent intent = new Intent(this, Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        // Close the drawer after handling the click
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // Helper method to get the destination activity class
    private Class<?> getDestinationActivity(int id) {
        if (id == R.id.nav_home) return HomePage.class;
        else if (id == R.id.nav_new_group) return AddNewEvent.class;
        else if (id == R.id.nav_existent_groups) return ExistentGroup.class;
        else if (id == R.id.nav_user_details) return UserDetails.class;
        else if (id == R.id.nav_admin_page) return AdminActivity.class;
        return null;
    }

    // Handle the hamburger menu click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Handle back button press
    @Override
    public void onBackPressed() {
        // If drawer is open, close it
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // Otherwise, do normal back button behavior
            super.onBackPressed();
        }
    }
}
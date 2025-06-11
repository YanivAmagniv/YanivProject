// AboutActivity.java
// This activity displays information about the application
// Features:
// - App information and description
// - Version details
// - Developer information
// - Navigation back to previous screen

package com.example.yanivproject.screens;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.yanivproject.R;

/**
 * Activity for displaying application information
 * Provides details about the app, its features, and developers
 * Implements a toolbar with back navigation
 */
public class AboutActivity extends AppCompatActivity {

    /**
     * Called when the activity is first created
     * Sets up the UI and toolbar with back navigation
     * @param savedInstanceState Bundle containing the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Set up the toolbar with back navigation
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    /**
     * Handles the back navigation when the up button is pressed
     * @return true to indicate the event was handled
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 
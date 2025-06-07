// MainActivity.java
// This is the main entry point of the application after the splash screen
// It handles the initial user interface and navigation to other main screens

package com.example.yanivproject.screens;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import com.google.android.material.button.MaterialButton;
import com.example.yanivproject.R;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    // UI Components
    private MaterialButton btnRegister, btnLogin, btnAbout;
    
    // Constants for logging and permission handling
    private static final String TAG = "MainActivity";
    private static final int NOTIFICATION_PERMISSION_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        try {
            // Initialize all UI components
            initViews();
        } catch (Exception e) {
            // Log any errors during initialization
            Log.e(TAG, "Error initializing views: " + e.getMessage());
        }

        // Request notification permission for Android 13 (API 33) and above
        // This is required for sending notifications to users
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    // Initialize all UI components and set up click listeners
    private void initViews() {
        // Find and initialize buttons
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);
        btnAbout = findViewById(R.id.btnAbout);

        // Set click listeners
        btnRegister.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        // About button uses lambda for click handling
        btnAbout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        });
    }

    // Handle all button clicks in the activity
    @Override
    public void onClick(View v) {
        try {
            if (v.getId() == R.id.btnRegister) {
                // Navigate to Registration screen
                Intent goReg = new Intent(this, Register.class);
                startActivity(goReg);
            } else if (v.getId() == R.id.btnLogin) {
                // Navigate to Login screen
                Intent goLog = new Intent(this, Login.class);
                startActivity(goLog);
            } else if (v.getId() == R.id.btnAbout) {
                // TODO: Implement about functionality
                Log.d(TAG, "About button clicked");
            }
        } catch (Exception e) {
            // Log any errors during navigation
            Log.e(TAG, "Error handling click: " + e.getMessage());
        }
    }

    // Handle permission request results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, notifications can be posted
            } else {
                // Permission denied, handle accordingly
            }
        }
    }
}
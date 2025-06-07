// Login.java
// This activity handles user authentication and login functionality
// It integrates with Firebase Authentication and stores user credentials
// Manages user session and FCM token for notifications

package com.example.yanivproject.screens;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yanivproject.R;
import com.example.yanivproject.services.AuthenticationService;
import com.example.yanivproject.services.DatabaseService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Activity for handling user login and authentication
 * Manages user credentials, Firebase authentication, and FCM token storage
 * Provides edge-to-edge display support and persistent login
 */
public class Login extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    // UI Components for user input
    private EditText etEmail;     // Email input field
    private EditText etPassword;  // Password input field
    private Button btnLog;        // Login button
    private String email, pass;   // User credentials

    // Service instances for authentication and database operations
    private AuthenticationService authenticationService;  // Handles user authentication
    private DatabaseService databaseService;             // Handles database operations

    // SharedPreferences key for storing user credentials
    public static final String MyPREFERENCES = "MyPrefs";

    // SharedPreferences instance for persistent storage
    private SharedPreferences sharedpreferences;

    /**
     * Called when the activity is first created
     * Sets up the UI, initializes services, and loads saved credentials
     * @param savedInstanceState Bundle containing the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge display for modern Android UI
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        
        // Handle system insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize service instances
        authenticationService = AuthenticationService.getInstance();
        databaseService = DatabaseService.getInstance();

        // Initialize UI components
        initViews();

        // Load saved credentials if they exist
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        email = sharedpreferences.getString("email", "");
        pass = sharedpreferences.getString("password", "");
        etEmail.setText(email);
        etPassword.setText(pass);
        btnLog.setOnClickListener(this);
    }

    /**
     * Initializes all UI components
     * Finds and sets up views for user interaction
     */
    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLog = findViewById(R.id.btnLog);
    }

    /**
     * Handles login button click
     * Validates and processes user credentials
     * @param view The view that was clicked
     */
    @Override
    public void onClick(View view) {
        email = etEmail.getText().toString();
        pass = etPassword.getText().toString();

        // Attempt to sign in using the authentication service
        authenticationService.signIn(email, pass, new AuthenticationService.AuthCallback<String>() {
            @Override
            public void onCompleted(String id) {
                // Sign in success, update UI with the signed-in user's information
                Log.d("TAG", "signInWithEmail:success");
                handleSuccessfulLogin(id);
            }

            @Override
            public void onFailed(Exception e) {
                // If sign in fails, display a message to the user
                Log.w("TAG", "signInWithEmail:failure", e);
                Toast.makeText(getApplicationContext(), "Authentication failed.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Handles successful login process
     * Stores FCM token and navigates to HomePage
     * @param userId The ID of the successfully logged-in user
     */
    private void handleSuccessfulLogin(String userId) {
        Log.d("Login", "Handling successful login for user: " + userId);
        
        // Get and store Firebase Cloud Messaging (FCM) token for notifications
        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String token = task.getResult();
                    Log.d("Login", "Got FCM token: " + token);
                    // Store the token in the database for push notifications
                    FirebaseDatabase.getInstance().getReference("users")
                        .child(userId)
                        .child("fcmToken")
                        .setValue(token)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("Login", "FCM token stored successfully");
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Login", "Error storing FCM token", e);
                        });
                } else {
                    Log.e("Login", "Failed to get FCM token", task.getException());
                }
            });

        // Navigate to HomePage after successful login
        Intent go = new Intent(getApplicationContext(), HomePage.class);
        startActivity(go);
    }

    /**
     * Required interface method for AdapterView.OnItemSelectedListener
     * Not used in this activity
     */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
    }

    /**
     * Required interface method for AdapterView.OnItemSelectedListener
     * Not used in this activity
     */
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    /**
     * Handles back button press
     * Navigates back to the previous activity
     * @param view The view that triggered the back action
     */
    public void goBack(View view) {
        onBackPressed();  // Navigate back to the previous activity
    }
}

// Register.java
// This activity handles new user registration
// It validates user input and creates new user accounts in Firebase
// Implements form validation and error handling
// Manages user profile creation and credential storage

package com.example.yanivproject.screens;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.yanivproject.R;
import com.example.yanivproject.models.User;
import com.example.yanivproject.services.AuthenticationService;
import com.example.yanivproject.services.DatabaseService;
import com.example.yanivproject.utils.SharedPreferencesUtil;

/**
 * Activity for handling new user registration
 * Manages user input validation, account creation, and profile setup
 * Integrates with Firebase Authentication and Database
 */
public class Register extends AppCompatActivity {
    // UI Components for user input
    private EditText etFName;    // First name input field
    private EditText etLName;    // Last name input field
    private EditText etPhone;    // Phone number input field
    private EditText etEmail;    // Email input field
    private EditText etPass;     // Password input field
    private Button btnReg;       // Registration button

    // Variables to store user input
    private String fName;        // First name
    private String lName;        // Last name
    private String phone;        // Phone number
    private String email;        // Email address
    private String pass;         // Password

    // Service instances for authentication and database operations
    private AuthenticationService authenticationService;  // Handles user authentication
    private DatabaseService databaseService;             // Handles database operations
    
    // SharedPreferences key for storing user credentials
    public static final String MyPREFERENCES = "MyPrefs";
    private SharedPreferences sharedpreferences;

    /**
     * Called when the activity is first created
     * Sets up the UI and initializes services
     * @param savedInstanceState Bundle containing the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge display for modern Android UI
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        
        // Handle system insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Initialize SharedPreferences for storing user credentials
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        init_views();

        // Initialize service instances
        authenticationService = AuthenticationService.getInstance();
        databaseService = DatabaseService.getInstance();
    }

    /**
     * Initializes all UI components
     * Finds and sets up views for user interaction
     */
    private void init_views() {
        btnReg = findViewById(R.id.btnReg);
        etFName = findViewById(R.id.etFname);
        etLName = findViewById(R.id.etLname);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPassword);
    }

    /**
     * Handles registration button click
     * Validates user input and creates new account
     * @param v The view that was clicked
     */
    public void onClick(View v) {
        // Get user input from all fields
        fName = etFName.getText().toString();
        lName = etLName.getText().toString();
        phone = etPhone.getText().toString();
        email = etEmail.getText().toString();
        pass = etPass.getText().toString();

        // Validate all input fields
        Boolean isValid = true;
        
        // Validate first name (minimum 2 characters)
        if (fName.length() < 2) {
            etFName.setError("שם פרטי קצר מדי");
            isValid = false;
        }
        
        // Validate last name (minimum 2 characters)
        if (lName.length() < 2) {
            Toast.makeText(Register.this, "שם משפחה קצר מדי", Toast.LENGTH_LONG).show();
            isValid = false;
        }
        
        // Validate phone number (9-10 digits)
        if (phone.length() < 9 || phone.length() > 10) {
            Toast.makeText(Register.this, "מספר הטלפון לא תקין", Toast.LENGTH_LONG).show();
            isValid = false;
        }

        // Validate email format (must contain @)
        if (!email.contains("@")) {
            Toast.makeText(Register.this, "כתובת האימייל לא תקינה", Toast.LENGTH_LONG).show();
            isValid = false;
        }
        
        // Validate password length (6-20 characters)
        if (pass.length() < 6) {
            Toast.makeText(Register.this, "הסיסמה קצרה מדי", Toast.LENGTH_LONG).show();
            isValid = false;
        }
        if (pass.length() > 20) {
            Toast.makeText(Register.this, "הסיסמה ארוכה מדי", Toast.LENGTH_LONG).show();
            isValid = false;
        }

        // If all validations pass, proceed with registration
        if (isValid) {
            // Create new user account in Firebase Authentication
            authenticationService.signUp(email, pass, new AuthenticationService.AuthCallback<String>() {
                @Override
                public void onCompleted(String uid) {
                    // Authentication successful, create user profile in database
                    Log.d("TAG", "createUserWithEmail:success");
                    User newUser = new User(uid, fName, lName, phone, email, pass, false);
                    
                    // Save user data to Firebase Database
                    databaseService.createNewUser(newUser, new DatabaseService.DatabaseCallback<Void>() {
                        @Override
                        public void onCompleted(Void object) {
                            // Save credentials to SharedPreferences for auto-login
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString("email", email);
                            editor.putString("password", pass);
                            editor.commit();
                            SharedPreferencesUtil.saveUser(Register.this,newUser);
                            // Navigate to main activity
                            Intent goLog = new Intent(Register.this, MainActivity.class);
                            startActivity(goLog);
                        }

                        @Override
                        public void onFailed(Exception e) {
                            // Handle database errors
                            Log.w("TAG", "createUserWithEmail:failure", e);
                            if (e instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                                Toast.makeText(Register.this, "כתובת האימייל כבר קיימת במערכת. אנא השתמש בכתובת אחרת או התחבר.",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(Register.this, "ההרשמה נכשלה, אנא נסה שוב.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                @Override
                public void onFailed(Exception e) {
                    // Handle authentication errors
                    Log.w("TAG", "createUserWithEmail:failure", e);
                    if (e instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                        Toast.makeText(Register.this, "כתובת האימייל כבר קיימת במערכת. אנא השתמש בכתובת אחרת או התחבר.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(Register.this, "ההרשמה נכשלה, אנא נסה שוב.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
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
package com.example.yanivproject.screens;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yanivproject.R;
import com.example.yanivproject.models.User;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserDetails extends AppCompatActivity {
    private EditText etFirstName, etLastName, etPhone, etEmail, etCity, etCurrentPassword, etNewPassword ,etIsAdmin;
    private Button btnEdit, btnUpdate, btnChangePassword;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        userId = currentUser.getUid(); // Get the logged-in user's ID
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // Initialize UI elements
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etCity = findViewById(R.id.etCity);
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnEdit = findViewById(R.id.btnEdit);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        etIsAdmin = findViewById(R.id.etIsAdmin);


        // Disable editing by default
        setEditMode(false);

        // Load user details
        loadUserDetails();

        // Edit button - Enables editing
        btnEdit.setOnClickListener(v -> setEditMode(true));

        // Save button - Updates user details
        btnUpdate.setOnClickListener(v -> updateUserData());

        // Change Password button
        btnChangePassword.setOnClickListener(v -> changeUserPassword());
    }

    // Load user details into the EditText fields
    private void loadUserDetails() {
        if (currentUser != null) {
            etFirstName.setText(currentUser.getDisplayName()); // Firebase Auth display name
            etEmail.setText(currentUser.getEmail()); // Firebase Auth email
            // Load other details from Firebase Database if stored
            userRef.get().addOnSuccessListener(snapshot -> {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    etFirstName.setText(user.getFname());
                    etLastName.setText(user.getLname());
                    etPhone.setText(user.getPhone());
                    etCity.setText(user.getCity());
                    etIsAdmin.setText(user.getAdmin() ? "Is Admin" : "Not Admin");
                }
            }).addOnFailureListener(e -> Toast.makeText(this, "Failed to load user data.", Toast.LENGTH_SHORT).show());
        }
    }

    // Enable or disable editing
    private void setEditMode(boolean enabled) {
        etFirstName.setEnabled(enabled);
        etLastName.setEnabled(enabled);
        etPhone.setEnabled(enabled);
        etCity.setEnabled(enabled);
        etEmail.setEnabled(false); // Email should only be changed via Firebase Auth

        btnEdit.setVisibility(enabled ? View.GONE : View.VISIBLE);
        btnUpdate.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    // Update user details in Firebase
    private void updateUserData() {
        String newFirstName = etFirstName.getText().toString().trim();
        String newLastName = etLastName.getText().toString().trim();
        String newPhone = etPhone.getText().toString().trim();
        String newCity = etCity.getText().toString().trim();

        if (newFirstName.isEmpty() || newLastName.isEmpty() || newPhone.isEmpty() || newCity.isEmpty()) {
            Toast.makeText(this, "All fields must be filled!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update Firebase Database
        userRef.child("fname").setValue(newFirstName);
        userRef.child("lname").setValue(newLastName);
        userRef.child("phone").setValue(newPhone);
        userRef.child("city").setValue(newCity)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                    setEditMode(false);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update!", Toast.LENGTH_SHORT).show());
    }

    // Change user password securely
    private void changeUserPassword() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();

        if (currentPassword.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in both fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Password validation (you can add more validation as needed)
        if (newPassword.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reauthenticate the user before updating password
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);

        currentUser.reauthenticate(credential).addOnSuccessListener(aVoid -> {
            // Reauthentication successful, update password in Firebase Authentication
            currentUser.updatePassword(newPassword).addOnSuccessListener(aVoid1 -> {
                // Password updated in Firebase Authentication, now update it in Realtime Database
                userRef.child("password").setValue(newPassword).addOnSuccessListener(aVoid2 -> {
                    Toast.makeText(UserDetails.this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(UserDetails.this, "Failed to update password in Database!", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(UserDetails.this, "Error updating password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(UserDetails.this, "Reauthentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
    public void goBack(View view) {
        onBackPressed();  // This will navigate back to the previous activity
    }
}
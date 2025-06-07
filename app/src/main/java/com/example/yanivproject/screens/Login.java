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

public class Login extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    EditText etEmail, etPassword;
    Button btnLog;
    String email, pass;

    AuthenticationService authenticationService;
    DatabaseService databaseService;


    public static final String MyPREFERENCES="MyPrefs";

    SharedPreferences sharedpreferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /// get the instance of the authentication service
        authenticationService = AuthenticationService.getInstance();
        /// get the instance of the database service
        databaseService = DatabaseService.getInstance();

        initViews();




        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        email = sharedpreferences.getString("email", "");
        pass = sharedpreferences.getString("password", "");
        etEmail.setText(email);
        etPassword.setText(pass);
        btnLog.setOnClickListener(this);



    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLog = findViewById(R.id.btnLog);



    }

    @Override
    public void onClick(View view) {
        email = etEmail.getText().toString();
        pass = etPassword.getText().toString();

        authenticationService.signIn(email, pass, new AuthenticationService.AuthCallback<String>() {
            @Override
            public void onCompleted(String id) {
                // Sign in success, update UI with the signed-in user's information
                Log.d("TAG", "signInWithEmail:success");
                handleSuccessfulLogin(id);
            }

            @Override
            public void onFailed(Exception e) {
                // If sign in fails, display a message to the user.
                Log.w("TAG", "signInWithEmail:failure", e);
                Toast.makeText(getApplicationContext(), "Authentication failed.",
                        Toast.LENGTH_SHORT).show();
//                                updateUI(null);
            }
        });

    }

    private void handleSuccessfulLogin(String userId) {
        Log.d("Login", "Handling successful login for user: " + userId);
        // Get and store FCM token
        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String token = task.getResult();
                    Log.d("Login", "Got FCM token: " + token);
                    // Store the token in the database
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

        // Continue with your existing login success code
        Intent go = new Intent(getApplicationContext(), HomePage.class);
        startActivity(go);
    }




    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
    public void goBack(View view) {
        onBackPressed();  // This will navigate back to the previous activity
    }
}

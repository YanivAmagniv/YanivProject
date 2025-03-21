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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


import androidx.annotation.NonNull;

import com.example.yanivproject.R;
import com.example.yanivproject.models.User;
import com.example.yanivproject.services.AuthenticationService;
import com.example.yanivproject.services.DatabaseService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class Register extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    EditText etFName, etLName, etPhone, etEmail, etPass;
    Button btnReg;


    String fName,lName, phone, email, pass, city;
    Spinner spCity;


    private AuthenticationService authenticationService;
    private DatabaseService databaseService;
    public static final String MyPREFERENCES = "MyPrefs" ;
    SharedPreferences sharedpreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        init_views();


        /// get the instance of the authentication service
        authenticationService = AuthenticationService.getInstance();
        /// get the instance of the database service
        databaseService = DatabaseService.getInstance();


    }

    private void init_views() {
        btnReg=findViewById(R.id.btnReg);
        etFName=findViewById(R.id.etFname);
        etLName=findViewById(R.id.etLname);
        etPhone=findViewById(R.id.etPhone);
        etEmail=findViewById(R.id.etEmail);
        etPass=findViewById(R.id.etPassword);

        spCity=findViewById(R.id.spCity);
        spCity.setOnItemSelectedListener(this);
    }
    public void onClick(View v) {
        fName=etFName.getText().toString();
        lName=etLName.getText().toString();
        phone=etPhone.getText().toString();
        email=etEmail.getText().toString();
        pass=etPass.getText().toString();


        //check if registration is valid
        Boolean isValid=true;
        if (fName.length()<2){

            etFName.setError("שם פרטי קצר מדי");
            isValid = false;
        }
        if (lName.length()<2){
            Toast.makeText(Register.this,"שם משפחה קצר מדי", Toast.LENGTH_LONG).show();
            isValid = false;
        }
        if (phone.length()<9||phone.length()>10){
            Toast.makeText(Register.this,"מספר הטלפון לא תקין", Toast.LENGTH_LONG).show();
            isValid = false;
        }

        if (!email.contains("@")){
            Toast.makeText(Register.this,"כתובת האימייל לא תקינה", Toast.LENGTH_LONG).show();
            isValid = false;
        }
        if(pass.length()<6){
            Toast.makeText(Register.this,"הסיסמה קצרה מדי", Toast.LENGTH_LONG).show();
            isValid = false;
        }
        if(pass.length()>20){
            Toast.makeText(Register.this,"הסיסמה ארוכה מדי", Toast.LENGTH_LONG).show();
            isValid = false;
        }

        if (isValid==true) {
            FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            boolean emailExists = task.getResult().getSignInMethods().size() > 0;
                            if (emailExists) {
                                Toast.makeText(Register.this, "Email already in use. Try logging in.", Toast.LENGTH_LONG).show();
                            } else {
                                registerUser(email, pass);
                            }
                        } else {
                            Toast.makeText(Register.this, "Error checking email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

            authenticationService.signUp(email, pass, new AuthenticationService.AuthCallback<String>() {
                @Override
                public void onCompleted(String uid) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAG", "createUserWithEmail:success");
                    User newUser=new User(uid, fName, lName,phone, email,pass,city,false);
                    databaseService.createNewUser(newUser, new DatabaseService.DatabaseCallback<Void>() {
                        @Override
                        public void onCompleted(Void object) {
                            SharedPreferences.Editor editor = sharedpreferences.edit();

                            editor.putString("email", email);
                            editor.putString("password", pass);

                            editor.commit();
                            Intent goLog=new Intent(Register.this, MainActivity.class);
                            startActivity(goLog);
                        }

                        @Override
                        public void onFailed(Exception e) {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "createUserWithEmail:failure", e);
                            Toast.makeText(Register.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                }

                @Override
                public void onFailed(Exception e) {
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "createUserWithEmail:failure", e);
                    Toast.makeText(Register.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                }
            });

        }

    }
    private void registerUser(String email, String pass) {
        authenticationService.signUp(email, pass, new AuthenticationService.AuthCallback<String>() {
            @Override
            public void onCompleted(String uid) {
                Log.d("TAG", "createUserWithEmail:success");

                User newUser = new User(uid, fName, lName, phone, email, pass, city, false);
                databaseService.createNewUser(newUser, new DatabaseService.DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(Void object) {
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString("email", email);
                        editor.putString("password", pass);
                        editor.apply();
                        startActivity(new Intent(Register.this, MainActivity.class));
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Log.w("TAG", "createUserWithEmail:failure", e);
                        Toast.makeText(Register.this, "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailed(Exception e) {
                Log.w("TAG", "Authentication failed", e);
                Toast.makeText(Register.this, "Authentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        city= (String) adapterView.getItemAtPosition(i);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
    public void goBack(View view) {
        onBackPressed();  // This will navigate back to the previous activity
    }
}
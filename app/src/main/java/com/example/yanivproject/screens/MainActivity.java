package com.example.yanivproject.screens;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import com.google.android.material.button.MaterialButton;
import com.example.yanivproject.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private MaterialButton btnRegister, btnLogin, btnAbout;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        try {
            initViews();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
        }
    }

    private void initViews() {
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);
        btnAbout = findViewById(R.id.btnAbout);

        btnRegister.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        btnAbout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        try {
            if (v.getId() == R.id.btnRegister) {
                Intent goReg = new Intent(this, Register.class);
                startActivity(goReg);
            } else if (v.getId() == R.id.btnLogin) {
                Intent goLog = new Intent(this, Login.class);
                startActivity(goLog);
            } else if (v.getId() == R.id.btnAbout) {
                // TODO: Implement about functionality
                Log.d(TAG, "About button clicked");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling click: " + e.getMessage());
        }
    }
}
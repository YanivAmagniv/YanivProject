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
    private MaterialButton btnRegister, btnLogin, btnAbout;
    private static final String TAG = "MainActivity";
    private static final int NOTIFICATION_PERMISSION_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        try {
            initViews();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
        }

        // Request notification permission for Android 13 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
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
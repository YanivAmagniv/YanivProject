package com.example.yanivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yanivproject.R;

public class Activity_Groups extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_groups);

    }

    public void go_newgroup(View view) {
        Intent go = new Intent(getApplicationContext(), AddNewEvent.class);
        startActivity(go);
    }
    public void go_ExistentGroup(View v) {
        Intent go = new Intent(getApplicationContext(), ExistentGroup.class);
        startActivity(go);
    }
}
package com.example.yanivproject.screens;

import android.os.Bundle;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yanivproject.R;
import com.example.yanivproject.services.DatabaseService;

import com.example.yanivproject.models.Group;  //Import the Group model
import com.example.yanivproject.models.UserPay;  // If you have this class for user payments
import com.example.yanivproject.models.User;  // If you have this class for users

import java.util.ArrayList;
import java.util.List;


public class ExistentGroup extends AppCompatActivity {
    DatabaseService databaseService;
    ListView lvMyGroups;
    List<Group> groupList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_existent_group);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        lvMyGroups=findViewById(R.id.lstMyGroups);
        databaseService=DatabaseService.getInstance();
        // Set up the adapter with the list of groups
        GroupAdapter adapter = new GroupAdapter(this, groupList);
        lstMyGroups.setAdapter(adapter);


    }
}
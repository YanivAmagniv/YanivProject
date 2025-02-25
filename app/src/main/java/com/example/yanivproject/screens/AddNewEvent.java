package com.example.yanivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.yanivproject.R;
import com.example.yanivproject.adapters.UserNamAdapter;
import com.example.yanivproject.models.Group;  // Updated import
import com.example.yanivproject.models.User;
import com.example.yanivproject.models.UserPay;
import com.example.yanivproject.services.AuthenticationService;
import com.example.yanivproject.services.DatabaseService;


import java.util.ArrayList;
import java.util.List;

public class AddNewEvent extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    Spinner spCurrency, spEventType;
    CalendarView cvEventDate;
    TextView dateTextView;
    String stDate;

    EditText etGroupName, etDescription;
    String stGroupName, stDescription, stSPcurrency, stSPeventType;

    Button btnCreateGroup;
    DatabaseService databaseService;
    private AuthenticationService authenticationService;
    private String uid;

    User user;
    ListView lvMembers, lvSelectedMembers;
    ArrayList<User> users = new ArrayList<>();
    UserNamAdapter<User> adapter;
    private UserNamAdapter<User> selectedAdapter;
    ArrayList<User> usersSelected = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_newgroup);

        databaseService = DatabaseService.getInstance();
        authenticationService = AuthenticationService.getInstance();
        uid = authenticationService.getCurrentUserId();

        initViews();

        users = new ArrayList<>();
        adapter = new UserNamAdapter<>(AddNewEvent.this, 0, 0, users);
        lvMembers.setAdapter(adapter);
        lvMembers.setOnItemClickListener(this);

        selectedAdapter = new UserNamAdapter<>(AddNewEvent.this, 0, 0, usersSelected);
        lvSelectedMembers.setAdapter(selectedAdapter);

        lvSelectedMembers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User selectedUser = usersSelected.get(position);
                usersSelected.remove(selectedUser);
                selectedAdapter.notifyDataSetChanged();
            }
        });

        databaseService.getUsers(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> object) {
                users.clear();
                users.addAll(object);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed(Exception e) {
                Log.e("TAG", "onFailed: ", e);
            }
        });

        databaseService.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User u) {
                user = u;
            }

            @Override
            public void onFailed(Exception e) {
            }
        });
    }

    private void initViews() {
        btnCreateGroup = findViewById(R.id.btnCreateGroup);
        lvSelectedMembers = findViewById(R.id.lvSelectedMembers);
        spCurrency = findViewById(R.id.spCurrency);
        spEventType = findViewById(R.id.spEventType);
        etDescription = findViewById(R.id.etDescription);
        etGroupName = findViewById(R.id.etGroupName);
        lvMembers = findViewById(R.id.lvallMembers);
        btnCreateGroup.setOnClickListener(this);

        cvEventDate = findViewById(R.id.cvEventDate);
        dateTextView = findViewById(R.id.dateTextView);
        cvEventDate.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            stDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            dateTextView.setText(stDate);
        });
    }

    @Override
    public void onClick(View v) {
        if (v == btnCreateGroup) {
            addGroupToDatabase();  // Updated method call
        }
    }

    private void addGroupToDatabase() {
        stSPcurrency = spCurrency.getSelectedItem().toString();
        stSPeventType = spEventType.getSelectedItem().toString();
        stDescription = etDescription.getText().toString().trim();
        stGroupName = etGroupName.getText().toString().trim();

        if (!isValidInput()) {
            return;
        }
        ArrayList<UserPay> userPayList = new ArrayList<>();  // Create a new list to hold UserPay objects

        // Iterate over each selected user and create a UserPay object
        for (User selectedUser : usersSelected) {
            userPayList.add(new UserPay(selectedUser, 0.0, false)); // Assuming they owe 0.0 and haven't paid
        }
        String groupId = databaseService.generateGroupId();  // Generate a new ID for the group
        // Create a new Group instance
        Group group = new Group(groupId, stGroupName, "not paid", stDate, stDescription, stSPeventType, user, userPayList, 5, 1000.0);

        databaseService.createNewGroup(group, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                Toast.makeText(AddNewEvent.this, "Event created successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(AddNewEvent.this, "Failed to create event. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidInput() {
        if (stGroupName.isEmpty()) {
            etGroupName.setError("Group name is required");
            etGroupName.requestFocus();
            return false;
        }
        if (stDescription.isEmpty()) {
            etDescription.setError("Description is required");
            etDescription.requestFocus();
            return false;
        }
        if (stDate == null || stDate.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (usersSelected.isEmpty()) {
            Toast.makeText(this, "Please select at least one member", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        User selectedUser = (User) parent.getItemAtPosition(position);
        if (!usersSelected.contains(selectedUser)) {
            usersSelected.add(selectedUser);
            selectedAdapter.notifyDataSetChanged();
        }
    }
}

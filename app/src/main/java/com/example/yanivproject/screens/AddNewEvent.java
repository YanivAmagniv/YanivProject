package com.example.yanivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yanivproject.R;
import com.example.yanivproject.adapters.UserAdapter;
import com.example.yanivproject.adapters.UserNamAdapter;
import com.example.yanivproject.models.MainSplit;
import com.example.yanivproject.models.User;
import com.example.yanivproject.models.UserPay;
import com.example.yanivproject.services.AuthenticationService;
import com.example.yanivproject.services.DatabaseService;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
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

    ListView lvMembers,lvSelectedMembers;


    ArrayList<User> users=new ArrayList<>();
    UserNamAdapter<User> adapter;
    private UserNamAdapter<User> selectedAdapter;


    ArrayList<User> usersSelected=new ArrayList<>();

    String members="";


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

        // Click listener for removing users from lvSelectedMembers
        lvSelectedMembers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User selectedUser = usersSelected.get(position);  // Get the clicked user

                usersSelected.remove(selectedUser);  // Remove from selected list

                selectedAdapter.notifyDataSetChanged();  // Refresh the selected list
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

        //spDividedBy= findViewById(R.id.spDividedBy);


        lvSelectedMembers = findViewById(R.id.lvSelectedMembers);


        spCurrency = findViewById(R.id.spCurrency);
        spEventType = findViewById(R.id.spEventType);
        etDescription = findViewById(R.id.etDescription);
        etGroupName = findViewById(R.id.etGroupName);

        lvMembers=findViewById(R.id.lvallMembers);




        btnCreateGroup.setOnClickListener(this);

        cvEventDate = findViewById(R.id.cvEventDate);
        dateTextView = findViewById(R.id.dateTextView);  // A TextView to show the date

        // Set a listener to get the date when it's selected
        cvEventDate.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Convert the selected date to a string
            stDate = year + "-" + (month + 1) + "-" + dayOfMonth;  // Format: yyyy-MM-dd

            // Display the selected date as a string
            dateTextView.setText(stDate);
        });

    }


    @Override
    public void onClick(View v) {
        if (v == btnCreateGroup) {
            addMainSplitToDatabase();
        }

    }


    private void addMainSplitToDatabase() {
        // Get the values from the input fields
        stSPcurrency = spCurrency.getSelectedItem().toString();
        stSPeventType = spEventType.getSelectedItem().toString();
        stDescription = etDescription.getText().toString().trim();
        stGroupName = etGroupName.getText().toString().trim();

        // Validate input
        if (!isValidInput()) {
            return;  // Stop if validation fails
        }

        // Generate a new ID for the event
        String id = databaseService.generateMainSplitId();

        // public MainSplit(String id, String status, String eventDate, String detail, String type, User admin, ArrayList<UserPay> users, int dividedBy, double totalAmount) {
        MainSplit mainSplit = new MainSplit(id, "not paid", stDate, stDescription, stSPeventType, user, usersSelected, 5, 1000.0);

        databaseService.createNewMainSplit(mainSplit, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                Toast.makeText(AddNewEvent.this, "Event created successfully!", Toast.LENGTH_SHORT).show();
                finish();  // Close the activity
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(AddNewEvent.this, "Failed to create event. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Validation method
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

        if (!usersSelected.contains(selectedUser)) {  // Prevent duplicate selections
            usersSelected.add(selectedUser);
            selectedAdapter.notifyDataSetChanged(); // Refresh selected members list
        }
    }


}
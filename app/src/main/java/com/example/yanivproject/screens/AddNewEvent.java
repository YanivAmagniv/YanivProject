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
    EditText etTotalAmount;
    Spinner spSplittingMethod;
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
    private EditText etUserPercentage;
    private EditText etUserCustomAmount;

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

        String[] splittingMethods = {"Equal Split", "Percentage-based", "Custom Split"};
        ArrayAdapter<String> splitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, splittingMethods);
        splitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSplittingMethod.setAdapter(splitAdapter);

        selectedAdapter = new UserNamAdapter<>(AddNewEvent.this, 0, 0, usersSelected);
        lvSelectedMembers.setAdapter(selectedAdapter);

        spSplittingMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMethod = spSplittingMethod.getSelectedItem().toString();

                if (selectedMethod.equals("Percentage-based")) {
                    etUserPercentage.setVisibility(View.VISIBLE);
                    etUserCustomAmount.setVisibility(View.GONE);
                } else if (selectedMethod.equals("Custom Split")) {
                    etUserCustomAmount.setVisibility(View.VISIBLE);
                    etUserPercentage.setVisibility(View.GONE);
                } else {
                    etUserPercentage.setVisibility(View.GONE);
                    etUserCustomAmount.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

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

        etTotalAmount = findViewById(R.id.etTotalAmount);
        spSplittingMethod = findViewById(R.id.spSplittingMethod);
        etUserPercentage = findViewById(R.id.etUserPercentage);
        etUserCustomAmount = findViewById(R.id.etUserCustomAmount);

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


        String totalAmountStr = etTotalAmount.getText().toString().trim();
        String selectedSplittingMethod = spSplittingMethod.getSelectedItem().toString();


        if (!isValidInput()) {
            return;
        }


        ArrayList<UserPay> userPayList = new ArrayList<>();
        double totalAmount = Double.parseDouble(totalAmountStr);

        if (selectedSplittingMethod.equals("Equal Split")) {
            double equalShare = totalAmount / usersSelected.size();
            for (User selectedUser : usersSelected) {
                userPayList.add(new UserPay(selectedUser, equalShare, false));
            }
        } else if (selectedSplittingMethod.equals("Percentage-based")) {
            double totalPercentage = 0.0;

            for (User selectedUser : usersSelected) {
                double userPercentage = getUserPercentage();
                double userAmount = totalAmount * (userPercentage / 100);
                totalPercentage += userPercentage;
                userPayList.add(new UserPay(selectedUser, userAmount, false));
            }
            if (totalPercentage != 100.0) {
                Toast.makeText(this, "Total percentage must equal 100%", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (selectedSplittingMethod.equals("Custom Split")) {
            for (User selectedUser : usersSelected) {
                double userAmount = getUserCustomAmount();
                userPayList.add(new UserPay(selectedUser, userAmount, false));
            }
        }



        if (totalAmountStr.isEmpty()) {
            etTotalAmount.setError("Total amount is required");
            etTotalAmount.requestFocus();
            return;
        }




        String groupId = databaseService.generateGroupId();  // Generate a new ID for the group
        // Create a new Group instance
        Group group = new Group(groupId, stGroupName, "not paid", stDate, stDescription, stSPeventType, user, userPayList, selectedSplittingMethod, totalAmount);




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

    // Get user-entered percentage (default 100 if empty)
    private double getUserPercentage() {
        String percentageStr = etUserPercentage.getText().toString().trim();
        return percentageStr.isEmpty() ? 100.0 : Double.parseDouble(percentageStr);
    }

    // Get user-entered custom amount (default 0 if empty)
    private double getUserCustomAmount() {
        String amountStr = etUserCustomAmount.getText().toString().trim();
        return amountStr.isEmpty() ? 0.0 : Double.parseDouble(amountStr);
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

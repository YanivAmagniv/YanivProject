package com.example.yanivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yanivproject.R;
import com.example.yanivproject.adapters.UserAmountAdapter;
import com.example.yanivproject.adapters.UserSelectionAdapter;
import com.example.yanivproject.models.Group;
import com.example.yanivproject.models.User;
import com.example.yanivproject.models.UserPay;
import com.example.yanivproject.services.AuthenticationService;
import com.example.yanivproject.services.DatabaseService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AddNewEvent extends NavActivity implements View.OnClickListener {
    private AutoCompleteTextView spCurrency, spEventType, spSplittingMethod;
    private CalendarView cvEventDate;
    private TextView dateTextView;
    private TextInputEditText etTotalAmount;
    private String stDate;

    private TextInputEditText etGroupName, etDescription;
    private String stGroupName, stDescription, stSPcurrency, stSPeventType;

    private MaterialButton btnCreateGroup, btnBack;
    private DatabaseService databaseService;
    private AuthenticationService authenticationService;
    private String uid;

    private User user;
    private ArrayList<User> usersSelected = new ArrayList<>();
    
    private RecyclerView rvUserAmounts, rvParticipants;
    private UserAmountAdapter userAmountAdapter;
    private UserSelectionAdapter userSelectionAdapter;
    private TextView tvSplitExplanation, tvTotalSplit;
    private double totalAmount = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newgroup);
        setupNavigationDrawer();

        databaseService = DatabaseService.getInstance();
        authenticationService = AuthenticationService.getInstance();
        uid = authenticationService.getCurrentUserId();

        initViews();
        setupAdapters();
        setupListeners();
        loadUsers();
    }

    private void initViews() {
        btnCreateGroup = findViewById(R.id.btnCreateGroup);
        btnBack = findViewById(R.id.btnBack);
        spCurrency = findViewById(R.id.spCurrency);
        spEventType = findViewById(R.id.spEventType);
        etDescription = findViewById(R.id.etDescription);
        etGroupName = findViewById(R.id.etGroupName);
        etTotalAmount = findViewById(R.id.etTotalAmount);
        spSplittingMethod = findViewById(R.id.spSplittingMethod);
        rvUserAmounts = findViewById(R.id.rvUserAmounts);
        rvParticipants = findViewById(R.id.rvParticipants);
        cvEventDate = findViewById(R.id.cvEventDate);
        dateTextView = findViewById(R.id.dateTextView);
        tvSplitExplanation = findViewById(R.id.tvSplitExplanation);
        tvTotalSplit = findViewById(R.id.tvTotalSplit);

        btnCreateGroup.setOnClickListener(this);
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupAdapters() {
        // Setup spinners
        setupSpinners();

        // Setup RecyclerViews
        rvParticipants.setLayoutManager(new LinearLayoutManager(this));
        userSelectionAdapter = new UserSelectionAdapter();
        userSelectionAdapter.setOnUserSelectionChangedListener(selectedUsers -> {
            usersSelected.clear();
            usersSelected.addAll(selectedUsers);
            updateUserAmountsList();
        });
        rvParticipants.setAdapter(userSelectionAdapter);

        rvUserAmounts.setLayoutManager(new LinearLayoutManager(this));
        userAmountAdapter = new UserAmountAdapter();
        userAmountAdapter.setOnAmountChangedListener((currentTotal, remaining) -> {
            tvTotalSplit.setVisibility(View.VISIBLE);
            if (Math.abs(remaining) < 0.01) {
                tvTotalSplit.setText(String.format("סה״כ: ₪%.2f", currentTotal));
                tvTotalSplit.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvTotalSplit.setText(String.format("סה״כ: ₪%.2f (נותר: ₪%.2f)", currentTotal, remaining));
                tvTotalSplit.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        });
        rvUserAmounts.setAdapter(userAmountAdapter);
    }

    private void setupSpinners() {
        // Currency spinner
        ArrayAdapter<CharSequence> currencyAdapter = ArrayAdapter.createFromResource(
                this, R.array.currencyArr, android.R.layout.simple_dropdown_item_1line);
        spCurrency.setAdapter(currencyAdapter);

        // Event type spinner
        ArrayAdapter<CharSequence> eventTypeAdapter = ArrayAdapter.createFromResource(
                this, R.array.typeOfEventArr, android.R.layout.simple_dropdown_item_1line);
        spEventType.setAdapter(eventTypeAdapter);

        // Splitting method spinner
        String[] splittingMethods = {"חלוקה שווה", "חלוקה לפי אחוזים", "חלוקה מותאמת אישית"};
        ArrayAdapter<String> splitAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, splittingMethods);
        spSplittingMethod.setAdapter(splitAdapter);
    }

    private void setupListeners() {
        cvEventDate.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            stDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            dateTextView.setText(stDate);
        });

        spSplittingMethod.setOnItemClickListener((parent, view, position, id) -> {
            String method = spSplittingMethod.getText().toString();
            updateSplitExplanation(method);
            updateUserAmountsList();
        });

        etTotalAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    totalAmount = s.length() > 0 ? Double.parseDouble(s.toString()) : 0.0;
                    updateUserAmountsList();
                } catch (NumberFormatException e) {
                    etTotalAmount.setError("נא להזין מספר תקין");
                }
            }
        });
    }

    private void updateSplitExplanation(String method) {
        tvSplitExplanation.setVisibility(View.VISIBLE);
        switch (method) {
            case "חלוקה שווה":
                tvSplitExplanation.setText("הסכום יתחלק שווה בשווה בין כל המשתתפים");
                break;
            case "חלוקה לפי אחוזים":
                tvSplitExplanation.setText("הזן אחוז עבור כל משתתף (סה״כ חייב להיות 100%)");
                break;
            case "חלוקה מותאמת אישית":
                tvSplitExplanation.setText("הזן סכום ספציפי עבור כל משתתף");
                break;
        }
    }

    private void updateUserAmountsList() {
        if (usersSelected.isEmpty() || totalAmount <= 0) {
            rvUserAmounts.setVisibility(View.GONE);
            tvTotalSplit.setVisibility(View.GONE);
            return;
        }

        String splitMethod = spSplittingMethod.getText().toString();
        userAmountAdapter.updateUsers(usersSelected, totalAmount, splitMethod);
        rvUserAmounts.setVisibility(View.VISIBLE);
        
        updateTotalDisplay();
    }

    private void updateTotalDisplay() {
        double currentTotal = userAmountAdapter.getCurrentTotal();
        double remaining = totalAmount - currentTotal;
        tvTotalSplit.setVisibility(View.VISIBLE);
        tvTotalSplit.setText(String.format("סה״כ: ₪%.2f (נותר: ₪%.2f)", currentTotal, remaining));
        tvTotalSplit.setTextColor(Math.abs(remaining) < 0.01 ? 
            getResources().getColor(android.R.color.holo_green_dark) : 
            getResources().getColor(android.R.color.holo_red_dark));
    }

    private void loadUsers() {
        databaseService.getUsers(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                userSelectionAdapter.updateUsers(users);
            }

            @Override
            public void onFailed(Exception e) {
                Log.e("TAG", "Failed to load users: ", e);
                Toast.makeText(AddNewEvent.this, "שגיאה בטעינת משתמשים", Toast.LENGTH_SHORT).show();
            }
        });

        databaseService.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User u) {
                user = u;
            }

            @Override
            public void onFailed(Exception e) {
                Log.e("TAG", "Failed to load current user: ", e);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == btnCreateGroup) {
            if (validateInput()) {
                createNewGroup();
            }
        }
    }

    private boolean validateInput() {
        stGroupName = etGroupName.getText().toString().trim();
        stDescription = etDescription.getText().toString().trim();
        stSPcurrency = spCurrency.getText().toString();
        stSPeventType = spEventType.getText().toString();
        String totalAmountStr = etTotalAmount.getText().toString().trim();

        if (stGroupName.isEmpty()) {
            etGroupName.setError("נא להזין שם קבוצה");
            etGroupName.requestFocus();
            return false;
        }
        if (stDescription.isEmpty()) {
            etDescription.setError("נא להזין תיאור");
            etDescription.requestFocus();
            return false;
        }
        if (stDate == null || stDate.isEmpty()) {
            Toast.makeText(this, "נא לבחור תאריך", Toast.LENGTH_SHORT).show();
            cvEventDate.requestFocus();
            return false;
        }
        if (usersSelected.isEmpty()) {
            Toast.makeText(this, "נא לבחור לפחות משתתף אחד", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (totalAmountStr.isEmpty()) {
            etTotalAmount.setError("נא להזין סכום");
            etTotalAmount.requestFocus();
            return false;
        }
        if (spSplittingMethod.getText().toString().isEmpty()) {
            spSplittingMethod.setError("נא לבחור שיטת חלוקה");
            spSplittingMethod.requestFocus();
            return false;
        }
        if (!userAmountAdapter.validateTotalAmount()) {
            double currentTotal = userAmountAdapter.getCurrentTotal();
            double diff = Math.abs(totalAmount - currentTotal);
            Toast.makeText(this, 
                String.format("סכום החלוקה אינו תואם לסכום הכולל (הפרש: ₪%.2f)", diff), 
                Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void createNewGroup() {
        double totalAmount = Double.parseDouble(etTotalAmount.getText().toString().trim());
        ArrayList<UserPay> userPayList = userAmountAdapter.getUserPayList();
        
        String groupId = databaseService.generateGroupId();
        Group group = new Group(groupId, stGroupName, "not paid", stDate, stDescription, 
                stSPeventType, user, userPayList, spSplittingMethod.getText().toString(), totalAmount);

        databaseService.createNewGroup(group, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                Toast.makeText(AddNewEvent.this, "הקבוצה נוצרה בהצלחה!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(AddNewEvent.this, "שגיאה ביצירת הקבוצה. נסה שוב.", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Failed to create group: ", e);
            }
        });
    }
}

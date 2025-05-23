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
import android.widget.CheckBox;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class AddNewEvent extends NavActivity implements View.OnClickListener {
    private AutoCompleteTextView spEventType, spSplittingMethod;
    private CalendarView cvPaymentDeadline;
    private TextView deadlineTextView;
    private String stDeadlineDate;
    private TextInputEditText etTotalAmount;

    private TextInputEditText etGroupName, etDescription;
    private String stGroupName, stDescription, stSPeventType;

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

    private CheckBox cbHasDeadline;
    private static final int MIN_DAYS_UNTIL_DEADLINE = 1;
    private static final int MAX_DAYS_UNTIL_DEADLINE = 90; // 3 months

    private View deadlineContainer;

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
        spEventType = findViewById(R.id.spEventType);
        etDescription = findViewById(R.id.etDescription);
        etGroupName = findViewById(R.id.etGroupName);
        etTotalAmount = findViewById(R.id.etTotalAmount);
        spSplittingMethod = findViewById(R.id.spSplittingMethod);
        rvUserAmounts = findViewById(R.id.rvUserAmounts);
        rvParticipants = findViewById(R.id.rvParticipants);
        cvPaymentDeadline = findViewById(R.id.cvPaymentDeadline);
        deadlineTextView = findViewById(R.id.deadlineTextView);
        tvSplitExplanation = findViewById(R.id.tvSplitExplanation);
        tvTotalSplit = findViewById(R.id.tvTotalSplit);
        cbHasDeadline = findViewById(R.id.cbHasDeadline);
        deadlineContainer = findViewById(R.id.deadlineContainer);

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
        cbHasDeadline.setOnCheckedChangeListener((buttonView, isChecked) -> {
            deadlineContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) {
                stDeadlineDate = null;
                deadlineTextView.setText("תאריך היעד שנבחר");
            }
        });

        cvPaymentDeadline.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            stDeadlineDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            deadlineTextView.setText(stDeadlineDate);
            
            // Enhanced deadline validation
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date deadline = sdf.parse(stDeadlineDate);
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                Date today = calendar.getTime();
                
                if (deadline != null) {
                    // Calculate days difference
                    long diffInMillis = deadline.getTime() - today.getTime();
                    long daysUntilDeadline = diffInMillis / (24 * 60 * 60 * 1000);
                    
                    if (daysUntilDeadline < MIN_DAYS_UNTIL_DEADLINE) {
                        Toast.makeText(this, "תאריך היעד חייב להיות לפחות יום אחד בעתיד", Toast.LENGTH_SHORT).show();
                        stDeadlineDate = null;
                        deadlineTextView.setText("תאריך היעד שנבחר");
                    } else if (daysUntilDeadline > MAX_DAYS_UNTIL_DEADLINE) {
                        Toast.makeText(this, "תאריך היעד לא יכול להיות יותר מ-3 חודשים בעתיד", Toast.LENGTH_SHORT).show();
                        stDeadlineDate = null;
                        deadlineTextView.setText("תאריך היעד שנבחר");
                    }
                }
            } catch (Exception e) {
                Log.e("AddNewEvent", "Error validating deadline date", e);
            }
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
        if (cbHasDeadline.isChecked() && (stDeadlineDate == null || stDeadlineDate.isEmpty())) {
            Toast.makeText(this, "נא לבחור תאריך יעד לתשלום", Toast.LENGTH_SHORT).show();
            cvPaymentDeadline.requestFocus();
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
        Group group = new Group(groupId, stGroupName, "not paid", stDescription, 
                stSPeventType, user, userPayList, spSplittingMethod.getText().toString(), totalAmount);
        
        // Set the payment deadline only if the checkbox is checked and a date was provided
        if (cbHasDeadline.isChecked() && stDeadlineDate != null && !stDeadlineDate.isEmpty()) {
            group.setPaymentDeadline(stDeadlineDate);
        }

        databaseService.createNewGroup(group, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                Toast.makeText(AddNewEvent.this, "הקבוצה נוצרה בהצלחה!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), ExistentGroup.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
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

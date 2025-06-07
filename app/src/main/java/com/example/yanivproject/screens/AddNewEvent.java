// AddNewEvent.java
// This activity handles the creation of new group events
// It manages user selection, payment splitting, and deadline setting
// Implements various payment splitting methods and validation
// Provides real-time feedback on payment distribution

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

/**
 * Activity for creating new group events
 * Features:
 * - User selection and management
 * - Multiple payment splitting methods
 * - Deadline setting and validation
 * - Real-time payment distribution feedback
 * - Input validation and error handling
 * - Group creation with Firebase integration
 */
public class AddNewEvent extends NavActivity implements View.OnClickListener {
    // UI Components for event details
    private AutoCompleteTextView spEventType;      // Dropdown for event type selection
    private AutoCompleteTextView spSplittingMethod; // Dropdown for payment splitting method
    private CalendarView cvPaymentDeadline;        // Calendar for deadline selection
    private TextView deadlineTextView;             // Displays selected deadline
    private String stDeadlineDate;                 // Stores selected deadline date
    private TextInputEditText etTotalAmount;       // Input for total group amount

    // UI Components for group information
    private TextInputEditText etGroupName;         // Input for group name
    private TextInputEditText etDescription;       // Input for group description
    private String stGroupName;                    // Stores group name
    private String stDescription;                  // Stores group description
    private String stSPeventType;                  // Stores selected event type

    // Action buttons
    private MaterialButton btnCreateGroup;         // Button to create new group
    private MaterialButton btnBack;                // Button to go back
    
    // Service instances
    private DatabaseService databaseService;       // Handles database operations
    private AuthenticationService authenticationService; // Handles user authentication
    private String uid;                           // Current user ID

    // User management
    private User user;                            // Current user object
    private ArrayList<User> usersSelected = new ArrayList<>(); // Selected participants
    
    // RecyclerViews and adapters for user selection and amount input
    private RecyclerView rvUserAmounts;           // RecyclerView for amount inputs
    private RecyclerView rvParticipants;          // RecyclerView for participant selection
    private UserAmountAdapter userAmountAdapter;  // Adapter for amount inputs
    private UserSelectionAdapter userSelectionAdapter; // Adapter for participant selection
    private TextView tvSplitExplanation;          // Explains current splitting method
    private TextView tvTotalSplit;                // Shows total split amount
    private double totalAmount = 0.0;             // Total group amount

    // Deadline management
    private CheckBox cbHasDeadline;               // Toggle for deadline setting
    private static final int MIN_DAYS_UNTIL_DEADLINE = 1;    // Minimum days until deadline
    private static final int MAX_DAYS_UNTIL_DEADLINE = 90;   // Maximum days until deadline (3 months)
    private View deadlineContainer;               // Container for deadline UI elements

    /**
     * Called when the activity is first created
     * Initializes UI components and sets up event listeners
     * @param savedInstanceState Bundle containing the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newgroup);
        setupNavigationDrawer();

        // Initialize services
        databaseService = DatabaseService.getInstance();
        authenticationService = AuthenticationService.getInstance();
        uid = authenticationService.getCurrentUserId();

        // Set up UI components and listeners
        initViews();
        setupAdapters();
        setupListeners();
        loadUsers();
    }

    /**
     * Initializes all UI components
     * Sets up click listeners and finds views
     */
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

        // Set up click listeners
        btnCreateGroup.setOnClickListener(this);
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    /**
     * Sets up adapters for user selection and amount input
     * Configures RecyclerViews and their adapters
     * Sets up spinners for event type and splitting method
     */
    private void setupAdapters() {
        // Setup spinners for event type and splitting method
        setupSpinners();

        // Setup RecyclerView for participant selection
        rvParticipants.setLayoutManager(new LinearLayoutManager(this));
        userSelectionAdapter = new UserSelectionAdapter();
        userSelectionAdapter.setOnUserSelectionChangedListener(selectedUsers -> {
            usersSelected.clear();
            usersSelected.addAll(selectedUsers);
            updateUserAmountsList();
        });
        rvParticipants.setAdapter(userSelectionAdapter);

        // Setup RecyclerView for amount input
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

    /**
     * Sets up spinners with predefined options
     * Configures event type and splitting method dropdowns
     */
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

    /**
     * Sets up all event listeners
     * Configures listeners for:
     * - Deadline checkbox
     * - Calendar view
     * - Splitting method selection
     * - Total amount input
     */
    private void setupListeners() {
        // Deadline checkbox listener
        cbHasDeadline.setOnCheckedChangeListener((buttonView, isChecked) -> {
            deadlineContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) {
                stDeadlineDate = null;
                deadlineTextView.setText("תאריך היעד שנבחר");
            }
        });

        // Calendar view listener for deadline selection
        cvPaymentDeadline.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            stDeadlineDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            deadlineTextView.setText(stDeadlineDate);
            
            // Validate selected deadline date
            validateDeadlineDate();
        });

        // Splitting method selection listener
        spSplittingMethod.setOnItemClickListener((parent, view, position, id) -> {
            String method = spSplittingMethod.getText().toString();
            updateSplitExplanation(method);
            updateUserAmountsList();
        });

        // Total amount input listener
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

    /**
     * Validates the selected deadline date
     * Ensures deadline is within allowed range (1-90 days)
     * Shows error message if validation fails
     */
    private void validateDeadlineDate() {
        if (stDeadlineDate != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date deadlineDate = sdf.parse(stDeadlineDate);
                Date currentDate = new Date();
                
                // Calculate days difference
                long diffInMillis = deadlineDate.getTime() - currentDate.getTime();
                long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);
                
                if (diffInDays < MIN_DAYS_UNTIL_DEADLINE) {
                    deadlineTextView.setError("תאריך היעד חייב להיות לפחות יום אחד מהיום");
                    stDeadlineDate = null;
                } else if (diffInDays > MAX_DAYS_UNTIL_DEADLINE) {
                    deadlineTextView.setError("תאריך היעד לא יכול להיות יותר מ-90 ימים מהיום");
                    stDeadlineDate = null;
                } else {
                    deadlineTextView.setError(null);
                }
            } catch (Exception e) {
                Log.e("AddNewEvent", "Error validating deadline date", e);
                deadlineTextView.setError("תאריך לא תקין");
                stDeadlineDate = null;
            }
        }
    }

    /**
     * Updates the explanation text for the current splitting method
     * @param method The selected splitting method
     */
    private void updateSplitExplanation(String method) {
        String explanation;
        switch (method) {
            case "חלוקה שווה":
                explanation = "הסכום יחולק שווה בשווה בין כל המשתתפים";
                break;
            case "חלוקה לפי אחוזים":
                explanation = "הזן אחוזים לכל משתתף (סה״כ חייב להיות 100%)";
                break;
            case "חלוקה מותאמת אישית":
                explanation = "הזן סכום מותאם לכל משתתף";
                break;
            default:
                explanation = "";
        }
        tvSplitExplanation.setText(explanation);
    }

    /**
     * Updates the list of user amounts based on selected splitting method
     * Recalculates amounts and updates UI
     */
    private void updateUserAmountsList() {
        String method = spSplittingMethod.getText().toString();
        userAmountAdapter.setUsers(usersSelected);
        userAmountAdapter.setTotalAmount(totalAmount);
        userAmountAdapter.setSplitMethod(method);
        userAmountAdapter.notifyDataSetChanged();
        updateTotalDisplay();
    }

    /**
     * Updates the total amount display
     * Shows remaining amount if not fully distributed
     */
    private void updateTotalDisplay() {
        double currentTotal = userAmountAdapter.getCurrentTotal();
        double remaining = totalAmount - currentTotal;
        
        tvTotalSplit.setVisibility(View.VISIBLE);
        if (Math.abs(remaining) < 0.01) {
            tvTotalSplit.setText(String.format("סה״כ: ₪%.2f", currentTotal));
            tvTotalSplit.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvTotalSplit.setText(String.format("סה״כ: ₪%.2f (נותר: ₪%.2f)", currentTotal, remaining));
            tvTotalSplit.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    /**
     * Loads users from the database
     * Populates user selection list
     */
    private void loadUsers() {
        databaseService.getAllUsers(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                userSelectionAdapter.setUsers(users);
                userSelectionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed(Exception e) {
                Log.e("AddNewEvent", "Failed to load users", e);
                Toast.makeText(AddNewEvent.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });

        databaseService.getUserById(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User u) {
                user = u;
            }

            @Override
            public void onFailed(Exception e) {
                Log.e("AddNewEvent", "Failed to load current user", e);
                Toast.makeText(AddNewEvent.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Handles click events for buttons
     * @param v The view that was clicked
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnCreateGroup) {
            if (validateInput()) {
                createNewGroup();
            }
        }
    }

    /**
     * Validates all input fields
     * Checks for required fields and valid values
     * @return true if all validation passes
     */
    private boolean validateInput() {
        boolean isValid = true;

        // Validate group name
        stGroupName = etGroupName.getText().toString().trim();
        if (stGroupName.isEmpty()) {
            etGroupName.setError("נא להזין שם לקבוצה");
            isValid = false;
        }

        // Validate event type
        stSPeventType = spEventType.getText().toString();
        if (stSPeventType.isEmpty()) {
            spEventType.setError("נא לבחור סוג אירוע");
            isValid = false;
        }

        // Validate total amount
        if (totalAmount <= 0) {
            etTotalAmount.setError("נא להזין סכום חיובי");
            isValid = false;
        }

        // Validate participants
        if (usersSelected.isEmpty()) {
            Toast.makeText(this, "נא לבחור לפחות משתתף אחד", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Validate payment distribution
        String method = spSplittingMethod.getText().toString();
        double currentTotal = userAmountAdapter.getCurrentTotal();
        if (Math.abs(currentTotal - totalAmount) > 0.01) {
            Toast.makeText(this, "סכום התשלומים חייב להיות שווה לסכום הכולל", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Validate deadline if enabled
        if (cbHasDeadline.isChecked() && stDeadlineDate == null) {
            deadlineTextView.setError("נא לבחור תאריך יעד");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Creates a new group with the provided information
     * Saves group data to Firebase
     */
    private void createNewGroup() {
        // Create group object
        Group group = new Group();
        group.setGroupName(stGroupName);
        group.setGroupDescription(etDescription.getText().toString().trim());
        group.setType(stSPeventType);
        group.setTotalAmount(totalAmount);
        group.setSplitMethod(spSplittingMethod.getText().toString());
        group.setCreator(user);
        group.setStatus("Active");
        group.setDeadlineDate(stDeadlineDate);

        // Create user payment list
        List<UserPay> userPayList = new ArrayList<>();
        for (User selectedUser : usersSelected) {
            UserPay userPay = new UserPay();
            userPay.setUser(selectedUser);
            userPay.setAmount(userAmountAdapter.getAmountForUser(selectedUser));
            userPay.setPaid(false);
            userPay.setPaymentStatus(UserPay.PaymentStatus.NOT_PAID);
            userPayList.add(userPay);
        }
        group.setUserPayList(userPayList);

        // Save group to database
        databaseService.createGroup(group, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                Toast.makeText(AddNewEvent.this, "Group created successfully", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailed(Exception e) {
                Log.e("AddNewEvent", "Failed to create group", e);
                Toast.makeText(AddNewEvent.this, "Failed to create group", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

// GroupDetailsActivity.java
// This activity displays detailed information about a group
// Handles group management, payment tracking, and user interactions
// Implements real-time updates and notifications
// Manages payment approvals, reminders, and group status updates

package com.example.yanivproject.screens;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.yanivproject.R;
import com.example.yanivproject.models.Group;
import com.example.yanivproject.models.User;
import com.example.yanivproject.models.UserPay;
import com.example.yanivproject.services.NotificationService;
import com.example.yanivproject.views.DeadlineCountdownView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.ChildEventListener;

import java.util.List;

/**
 * Activity for displaying and managing group details
 * Features:
 * - Real-time group information display
 * - Payment tracking and management
 * - User role-based interactions
 * - Payment approval workflow
 * - Deadline countdown visualization
 * - Group status updates
 * - Payment reminders
 */
public class GroupDetailsActivity extends NavActivity {
    // UI Components for displaying group information
    private TextView groupNameText;        // Displays the group name
    private TextView groupDescriptionText; // Displays the group description
    private TextView groupTypeText;        // Displays the group type
    private TextView groupTotalAmountText; // Displays the total amount
    private TextView groupStatusText;      // Displays the group status
    private TextView groupOwnerText;       // Displays the group creator
    private Button deleteButton;           // Button to delete the group
    private Group group;                   // Current group object
    private DatabaseReference groupRef;    // Firebase reference to the group
    private TextView userAmountDueText;    // Displays amount due by current user
    private String currentUserId;          // ID of the currently logged-in user
    private int numberOfParticipants;      // Number of participants in the group
    private double userOwedAmount;         // Amount owed by current user
    private NotificationService notificationService;  // Service for handling notifications
    private DeadlineCountdownView deadlineCountdownView;  // Custom view for deadline countdown
    private ValueEventListener valueEventListener;

    /**
     * Called when the activity is first created
     * Initializes UI components and sets up group data
     * @param savedInstanceState Bundle containing the activity's previously saved state
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);
        setupNavigationDrawer();

        // Initialize notification service for payment notifications
        notificationService = new NotificationService(this);

        // Initialize all UI components
        initializeViews();

        // Get group data passed from previous activity
        group = (Group) getIntent().getSerializableExtra("group");

        if (group != null) {
            // Set up Firebase reference for this group
            groupRef = FirebaseDatabase.getInstance().getReference("groups").child(group.getGroupId());

            // Set up real-time updates
            setupRealtimeUpdates();

            // Display group information
            displayGroupDetails();

            // Set up current user's payment information
            setupCurrentUserPayment();

            // Set up payment marking functionality
            setupPaymentMarking();

            // Update participant list
            updateParticipantsList();

            // Set up deadline countdown
            deadlineCountdownView.setGroup(group);
        } else {
            Log.e("GroupDetailsActivity", "Group is null. Could not retrieve group details.");
            Toast.makeText(this, "Failed to load group details.", Toast.LENGTH_SHORT).show();
        }

        // Set up delete functionality for group creator
        setupDeleteButton();
    }

    /**
     * Initializes all UI components
     * Finds and sets up views for user interaction
     */
    private void initializeViews() {
        groupNameText = findViewById(R.id.group_name);
        groupDescriptionText = findViewById(R.id.group_description);
        groupTypeText = findViewById(R.id.group_type);
        groupTotalAmountText = findViewById(R.id.group_totalAmount);
        groupStatusText = findViewById(R.id.group_status);
        groupOwnerText = findViewById(R.id.group_owner);
        deleteButton = findViewById(R.id.delete_button);
        deadlineCountdownView = findViewById(R.id.deadline_countdown);
        Button markCurrentUserPaidButton = findViewById(R.id.btnMarkCurrentUserPaid);
        userAmountDueText = findViewById(R.id.user_amount_due);
    }

    /**
     * Displays group details in the UI
     * Updates all text views with group information
     */
    private void displayGroupDetails() {
        groupNameText.setText(group.getGroupName());
        groupDescriptionText.setText(group.getGroupDescription() != null ? group.getGroupDescription() : "No description available");
        groupTypeText.setText(group.getType());
        groupTotalAmountText.setText(" " + getSplittingMethod(group.getTotalAmount()));
        groupStatusText.setText(group.getStatus());
        groupOwnerText.setText(group.getCreator().getName());
    }

    /**
     * Sets up current user's payment information
     * Retrieves user ID and displays owed amount
     */
    private void setupCurrentUserPayment() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
            displayUserOwedAmount();
        } else {
            Log.e("GroupDetailsActivity", "User not logged in");
        }
    }

    /**
     * Formats currency values with proper locale
     * @param amount The amount to format
     * @return Formatted currency string
     */
    private String getSplittingMethod(double amount) {
        return String.format(Locale.US, "$%.2f", amount);
    }

    /**
     * Calculates and displays the amount owed by current user
     * Handles different splitting methods (equal, percentage, custom)
     */
    private void displayUserOwedAmount() {
        double userShare = 0;
        String splitMethod = group.getSplitMethod();
        
        if (group.getUserPayListAsList() != null) {
            for (UserPay userPay : group.getUserPayListAsList()) {
                if (userPay.getUser().getId().equals(currentUserId)) {
                    // Calculate share based on split method
                    if (splitMethod.equals("חלוקה לפי אחוזים")) {
                        userShare = (userPay.getAmount() / group.getTotalAmount()) * 100;
                        userAmountDueText.setText(getCurrencyString(userPay.getAmount()) + " (" + String.format("%.1f%%", userShare) + ")");
                    } else if (splitMethod.equals("חלוקה מותאמת אישית")) {
                        userShare = userPay.getAmount();
                        userAmountDueText.setText(getCurrencyString(userShare) + " (Custom Amount)");
                    } else { // Equal split
                        userShare = userPay.getAmount();
                        userAmountDueText.setText(getCurrencyString(userShare) + " (Equal Share)");
                    }
                    break;
                }
            }
        } else {
            userAmountDueText.setText("Amount cannot be calculated");
        }
    }

    /**
     * Handles marking a payment as paid
     * Different behavior for creator and regular users
     * @param userPay The payment to mark as paid
     */
    private void markPaymentAsPaid(UserPay userPay) {
        boolean isCreator = group.getCreator().getId().equals(currentUserId);
        List<UserPay> userPayList = group.getUserPayListAsList();
        
        for (int i = 0; i < userPayList.size(); i++) {
            UserPay currentUserPay = userPayList.get(i);
            if (currentUserPay.getUser().getId().equals(userPay.getUser().getId())) {
                if (isCreator) {
                    // Creator can directly approve payments
                    handleCreatorPaymentApproval(currentUserPay, i, userPayList);
                } else {
                    // Regular user can only mark as pending
                    handleUserPaymentPending(currentUserPay, i, userPayList);
                }
                break;
            }
        }
    }

    /**
     * Handles payment approval by group creator
     * Updates payment status and sends notifications
     * @param currentUserPay The payment to approve
     * @param index Index of the payment in the list
     * @param userPayList List of all payments
     */
    private void handleCreatorPaymentApproval(UserPay currentUserPay, int index, List<UserPay> userPayList) {
        currentUserPay.setPaymentStatus(UserPay.PaymentStatus.PAID);
        currentUserPay.setPaid(true);
        currentUserPay.setPaymentDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        
        // Update payment status in Firebase
        userPayList.set(index, currentUserPay);
        groupRef.child("userPayList").setValue(userPayList)
            .addOnSuccessListener(aVoid -> {
                // Send notification to member
                notificationService.sendPaymentCompleteNotification(group, currentUserPay);
                
                // Check if all payments are complete
                checkAndUpdateGroupStatus();
                
                // Refresh UI
                updateParticipantsList();
            })
            .addOnFailureListener(e -> {
                Log.e("GroupDetailsActivity", "Failed to update payment status", e);
                Toast.makeText(this, "Failed to update payment status", Toast.LENGTH_SHORT).show();
            });
    }

    /**
     * Handles payment pending status for regular users
     * Updates payment status and sends notifications
     * @param currentUserPay The payment to mark as pending
     * @param index Index of the payment in the list
     * @param userPayList List of all payments
     */
    private void handleUserPaymentPending(UserPay currentUserPay, int index, List<UserPay> userPayList) {
        // Only proceed if the payment is not already pending
        if (currentUserPay.getPaymentStatus() != UserPay.PaymentStatus.PENDING_APPROVAL) {
            currentUserPay.setPaymentStatus(UserPay.PaymentStatus.PENDING_APPROVAL);
            currentUserPay.setPaid(false);
            currentUserPay.setPaymentDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
            
            // Update payment status in Firebase
            userPayList.set(index, currentUserPay);
            groupRef.child("userPayList").setValue(userPayList)
                .addOnSuccessListener(aVoid -> {
                    // Send notification to creator
                    notificationService.sendPaymentPendingNotification(group, currentUserPay);
                })
                .addOnFailureListener(e -> {
                    Log.e("GroupDetailsActivity", "Error updating payment status", e);
                    Toast.makeText(this, "Failed to update payment status", Toast.LENGTH_SHORT).show();
                });
        }
    }

    /**
     * Checks if all payments are complete and updates group status
     * Sends notifications when group is fully paid
     */
    private void checkAndUpdateGroupStatus() {
        boolean allPaid = group.getUserPayListAsList().stream().allMatch(UserPay::isPaid);
        if (allPaid) {
            group.setStatus("Paid");
            groupRef.child("status").setValue("Paid")
                .addOnSuccessListener(aVoid -> {
                    // Send notification to all members
                    notificationService.sendGroupPaidNotification(group);
                    
                    // Update UI
                    groupStatusText.setText("Paid");
                })
                .addOnFailureListener(e -> {
                    Log.e("GroupDetailsActivity", "Failed to update group status", e);
                    Toast.makeText(this, "Failed to update group status", Toast.LENGTH_SHORT).show();
                });
        }
    }

    /**
     * Formats currency values with proper locale
     * @param totalAmount The amount to format
     * @return Formatted currency string
     */
    private String getCurrencyString(double totalAmount) {
        return String.format(Locale.US, "$%.2f", totalAmount);
    }

    /**
     * Deletes the current group
     * Only available to group creator
     * Sends notifications to all members
     */
    private void deleteGroup() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Create a User object with the current user's information
            User user = new User();
            user.setId(currentUser.getUid());
            user.setEmail(currentUser.getEmail());
            user.setFname(currentUser.getDisplayName());
            
            // Send notification before deleting
            notificationService.sendGroupDeletedNotification(group, user);
            
            // Delete the group from Firebase
            groupRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Group deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("GroupDetailsActivity", "Error deleting group", e);
                    Toast.makeText(this, "Failed to delete group", Toast.LENGTH_SHORT).show();
                });
        }
    }

    /**
     * Handles back button press
     * Navigates back to the previous activity
     * @param view The view that triggered the back action
     */
    public void goBack(View view) {
        onBackPressed();
    }

    /**
     * Called when the activity is destroyed
     * Cleans up resources and listeners
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (groupRef != null && valueEventListener != null) {
            groupRef.removeEventListener(valueEventListener);
        }
    }

    /**
     * Updates the list of participants and their payment status
     * Displays payment information and approval buttons
     */
    private void updateParticipantsList() {
        LinearLayout participantsContainer = findViewById(R.id.participants_container);
        participantsContainer.removeAllViews();

        if (group.getUserPayListAsList() != null) {
            for (UserPay userPay : group.getUserPayListAsList()) {
                // Create participant view
                View participantView = getLayoutInflater().inflate(R.layout.item_participant, participantsContainer, false);
                
                // Set participant information
                TextView nameText = participantView.findViewById(R.id.participant_name);
                TextView amountText = participantView.findViewById(R.id.participant_amount);
                TextView statusText = participantView.findViewById(R.id.participant_status);
                Button approveButton = participantView.findViewById(R.id.btn_approve_payment);
                
                nameText.setText(userPay.getUser().getName());
                amountText.setText(getCurrencyString(userPay.getAmount()));
                
                // Set status text and state based on payment status
                switch (userPay.getPaymentStatus()) {
                    case PAID:
                        statusText.setText("שולם");
                        statusText.setActivated(true);
                        statusText.setSelected(false);
                        break;
                    case PENDING_APPROVAL:
                        statusText.setText("ממתין לאישור");
                        statusText.setActivated(false);
                        statusText.setSelected(true);
                        break;
                    case NOT_PAID:
                        statusText.setText("לא שולם");
                        statusText.setActivated(false);
                        statusText.setSelected(false);
                        break;
                }
                
                // Set up approval button for creator
                if (group.getCreator().getId().equals(currentUserId)) {
                    setupApprovalButton(approveButton, userPay);
                } else {
                    approveButton.setVisibility(View.GONE);
                }
                
                participantsContainer.addView(participantView);
            }
        }
    }

    /**
     * Sets up the approval button for a participant's payment
     * @param approveButton The button to set up
     * @param userPay The payment to approve
     */
    private void setupApprovalButton(Button approveButton, UserPay userPay) {
        if (userPay.getPaymentStatus() == UserPay.PaymentStatus.PENDING_APPROVAL) {
            approveButton.setVisibility(View.VISIBLE);
            approveButton.setOnClickListener(v -> {
                // Update payment status
                userPay.setPaymentStatus(UserPay.PaymentStatus.PAID);
                userPay.setPaid(true);
                userPay.setPaymentDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
                
                // Update in Firebase
                List<UserPay> userPayList = group.getUserPayListAsList();
                for (int i = 0; i < userPayList.size(); i++) {
                    if (userPayList.get(i).getUser().getId().equals(userPay.getUser().getId())) {
                        userPayList.set(i, userPay);
                        break;
                    }
                }
                
                groupRef.child("userPayList").setValue(userPayList)
                    .addOnSuccessListener(aVoid -> {
                        // Send notification to member
                        notificationService.sendPaymentCompleteNotification(group, userPay);
                        
                        // Check if all payments are complete
                        checkAndUpdateGroupStatus();
                        
                        // Refresh UI
                        updateParticipantsList();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("GroupDetailsActivity", "Failed to update payment status", e);
                        Toast.makeText(this, "Failed to update payment status", Toast.LENGTH_SHORT).show();
                    });
            });
        } else {
            approveButton.setVisibility(View.GONE);
        }
    }

    /**
     * Updates the payment status for a participant
     * Handles approval and rejection
     * @param userPay The payment to update
     */
    private void updatePaymentStatus(UserPay userPay) {
        TextView statusView = findViewById(R.id.participant_status);
        if (statusView != null) {
            switch (userPay.getPaymentStatus()) {
                case PAID:
                    statusView.setText("שולם");
                    statusView.setActivated(true);
                    statusView.setSelected(false);
                    break;
                case PENDING_APPROVAL:
                    statusView.setText("ממתין לאישור");
                    statusView.setActivated(false);
                    statusView.setSelected(true);
                    break;
                case NOT_PAID:
                    statusView.setText("לא שולם");
                    statusView.setActivated(false);
                    statusView.setSelected(false);
                    break;
            }
        }
    }

    /**
     * Called when the activity resumes
     * Refreshes group data and UI
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (group != null) {
            // Refresh group data
            groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Group updatedGroup = dataSnapshot.getValue(Group.class);
                    if (updatedGroup != null) {
                        group = updatedGroup;
                        displayGroupDetails();
                        setupCurrentUserPayment();
                        updateParticipantsList();
                        deadlineCountdownView.setGroup(group);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("GroupDetailsActivity", "Failed to refresh group data", error.toException());
                }
            });
        }
    }

    /**
     * Sends payment reminders to all unpaid participants
     * Only available to group creator
     */
    private void sendPaymentReminders() {
        if (group.getCreator().getId().equals(currentUserId)) {
            for (UserPay userPay : group.getUserPayListAsList()) {
                if (!userPay.isPaid()) {
                    notificationService.sendPaymentReminderNotification(group, userPay);
                }
            }
            Toast.makeText(this, "Payment reminders sent", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Only the group creator can send reminders", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Creates the options menu
     * @param menu The menu to create
     * @return true if menu was created successfully
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_details_menu, menu);
        return true;
    }

    /**
     * Handles menu item selection
     * @param item The selected menu item
     * @return true if item was handled successfully
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_send_reminders) {
            sendPaymentReminders();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets up the payment marking functionality
     * Configures the button to mark payments as paid
     */
    private void setupPaymentMarking() {
        Button markPaidButton = findViewById(R.id.btnMarkCurrentUserPaid);
        if (markPaidButton != null) {
            markPaidButton.setOnClickListener(v -> {
                List<UserPay> userPayList = group.getUserPayListAsList();
                for (UserPay userPay : userPayList) {
                    if (userPay.getUser().getId().equals(currentUserId)) {
                        markPaymentAsPaid(userPay);
                        break;
                    }
                }
            });
        }
    }

    /**
     * Sets up the delete button functionality
     * Only visible to group creator
     */
    private void setupDeleteButton() {
        if (deleteButton != null) {
            // Only show delete button to group creator
            if (group.getCreator().getId().equals(currentUserId)) {
                deleteButton.setVisibility(View.VISIBLE);
                deleteButton.setOnClickListener(v -> {
                    // Show confirmation dialog
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Delete Group")
                        .setMessage("Are you sure you want to delete this group?")
                        .setPositiveButton("Yes", (dialog, which) -> deleteGroup())
                        .setNegativeButton("No", null)
                        .show();
                });
            } else {
                deleteButton.setVisibility(View.GONE);
            }
        }
    }

    private void setupRealtimeUpdates() {
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Group updatedGroup = dataSnapshot.getValue(Group.class);
                if (updatedGroup != null) {
                    group = updatedGroup;
                    displayGroupDetails();
                    updateParticipantsList();
                    deadlineCountdownView.setGroup(group);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("GroupDetailsActivity", "Error listening for updates", error.toException());
            }
        };
        groupRef.addValueEventListener(valueEventListener);
    }
}

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

public class GroupDetailsActivity extends NavActivity {
    private TextView groupNameText, groupDescriptionText, groupTypeText, groupTotalAmountText, groupStatusText, groupOwnerText;
    private Button deleteButton;
    private Group group;
    private DatabaseReference groupRef;
    private TextView userAmountDueText;
    private String currentUserId;
    private int numberOfParticipants;
    private double userOwedAmount;
    private NotificationService notificationService;
    private DeadlineCountdownView deadlineCountdownView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);
        setupNavigationDrawer();

        // Initialize NotificationService
        notificationService = new NotificationService(this);

        // Initialize Views
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

        // Retrieve the group object passed from the adapter
        group = (Group) getIntent().getSerializableExtra("group");

        if (group != null) {
            groupRef = FirebaseDatabase.getInstance().getReference("groups").child(group.getGroupId());

            // Set group details to the views
            groupNameText.setText(group.getGroupName());
            groupDescriptionText.setText(group.getGroupDescription() != null ? group.getGroupDescription() : "No description available");
            groupTypeText.setText(group.getType());
            groupTotalAmountText.setText(" " + getSplittingMethod(group.getTotalAmount()));
            groupStatusText.setText(group.getStatus());
            groupOwnerText.setText(group.getCreator().getName());

            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                currentUserId = firebaseUser.getUid();
                // Calculate and display the user's owed amount
                displayUserOwedAmount();
            } else {
                Log.e("GroupDetailsActivity", "User not logged in");
            }

            // Set up mark current user paid button
            markCurrentUserPaidButton.setOnClickListener(v -> {
                // Find the current user's UserPay object
                for (UserPay userPay : group.getUserPayListAsList()) {
                    if (userPay.getUser().getId().equals(currentUserId)) {
                        if (!userPay.isPaid()) {
                            markPaymentAsPaid(userPay);
                        } else {
                            Toast.makeText(this, "You have already marked your payment as complete", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }
                }
            });

            // Prepare participant names
            updateParticipantsList();

            // Check if current user is creator and part of the group
            boolean isCreator = group.getCreator().getId().equals(currentUserId);
            boolean isPartOfGroup = false;
            for (UserPay userPay : group.getUserPayListAsList()) {
                if (userPay.getUser().getId().equals(currentUserId)) {
                    isPartOfGroup = true;
                    break;
                }
            }

            // Show mark payment button only if:
            // 1. User is not the creator, OR
            // 2. User is the creator AND is part of the group
            markCurrentUserPaidButton.setVisibility((!isCreator || (isCreator && isPartOfGroup)) ? View.VISIBLE : View.GONE);

            // Disable mark current user paid button if already paid
            if ("Paid".equals(group.getStatus())) {
                markCurrentUserPaidButton.setEnabled(false);
            }

            // Set up deadline countdown
            deadlineCountdownView.setGroup(group);
        } else {
            Log.e("GroupDetailsActivity", "Group is null. Could not retrieve group details.");
            Toast.makeText(this, "Failed to load group details.", Toast.LENGTH_SHORT).show();
        }

        // Set up delete button
        deleteButton.setOnClickListener(v -> deleteGroup());
        
        // Show delete button only for group creator
        boolean isCreator = group != null && group.getCreator().getId().equals(currentUserId);
        deleteButton.setVisibility(isCreator ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // Utility method to format currency
    private String getSplittingMethod(double amount) {
        return String.format(Locale.US, "$%.2f", amount);
    }

    private void displayUserOwedAmount() {
        double userShare = 0;
        String splitMethod = group.getSplitMethod();
        Log.d("DEBUG", "Split Method: " + splitMethod);
        Log.d("DEBUG", "Current User ID: " + currentUserId);
        Log.d("DEBUG", "UserPayList size: " + (group.getUserPayListAsList() != null ? group.getUserPayListAsList().size() : "null"));

        if (group.getUserPayListAsList() != null) {
            for (UserPay userPay : group.getUserPayListAsList()) {
                Log.d("DEBUG", "Checking user: " + userPay.getUser().getId() + " with amount: " + userPay.getAmount());
                if (userPay.getUser().getId().equals(currentUserId)) {
                    Log.d("DEBUG", "Matched current user: " + currentUserId);

                    if (splitMethod.equals("חלוקה לפי אחוזים")) {
                        userShare = (userPay.getAmount() / group.getTotalAmount()) * 100;
                        userAmountDueText.setText(getCurrencyString(userPay.getAmount()) + " (" + String.format("%.1f%%", userShare) + ")");
                        Log.d("DEBUG", "Percentage split - Amount: " + userPay.getAmount() + ", Share: " + userShare + "%");
                    } else if (splitMethod.equals("חלוקה מותאמת אישית")) {
                        userShare = userPay.getAmount();
                        userAmountDueText.setText(getCurrencyString(userShare) + " (Custom Amount)");
                        Log.d("DEBUG", "Custom split - Amount: " + userShare);
                    } else { // Equal split
                        userShare = userPay.getAmount();
                        userAmountDueText.setText(getCurrencyString(userShare) + " (Equal Share)");
                        Log.d("DEBUG", "Equal split - Amount: " + userShare);
                    }
                    break;
                }
            }
        } else {
            Log.e("DEBUG", "group.getUserPayListAsList() is null");
            userAmountDueText.setText("Amount cannot be calculated");
        }
    }

    private void markPaymentAsPaid(UserPay userPay) {
        // Check if current user is the group creator
        boolean isCreator = group.getCreator().getId().equals(currentUserId);
        
        // Find the correct user in the list
        List<UserPay> userPayList = group.getUserPayListAsList();
        for (int i = 0; i < userPayList.size(); i++) {
            UserPay currentUserPay = userPayList.get(i);
            if (currentUserPay.getUser().getId().equals(userPay.getUser().getId())) {
                if (isCreator) {
                    // Creator can directly approve payments
                    currentUserPay.setPaymentStatus(UserPay.PaymentStatus.PAID);
                    currentUserPay.setPaid(true);
                    currentUserPay.setPaymentDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
                    
                    // Update the entire userPayList in Firebase
                    userPayList.set(i, currentUserPay);
                    groupRef.child("userPayList").setValue(userPayList)
                        .addOnSuccessListener(aVoid -> {
                            // Send payment complete notification only to the member
                            notificationService.sendPaymentCompleteNotification(group, currentUserPay);
                            
                            // Check if all payments are complete
                            boolean allPaid = group.getUserPayListAsList().stream().allMatch(UserPay::isPaid);
                            if (allPaid) {
                                // Only update group status to "Paid" if all members have paid
                                groupRef.child("status").setValue("Paid")
                                    .addOnSuccessListener(statusVoid -> {
                                        group.setStatus("Paid");
                                        groupStatusText.setText("Paid");
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Error updating group status", Toast.LENGTH_SHORT).show();
                                        Log.e("GroupDetailsActivity", "Error updating group status", e);
                                    });
                            }
                            
                            // Refresh the UI
                            updateParticipantsList();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error updating payment status", Toast.LENGTH_SHORT).show();
                            Log.e("GroupDetailsActivity", "Error updating payment status", e);
                        });
                } else {
                    // Regular user can only mark as pending
                    currentUserPay.setPaymentStatus(UserPay.PaymentStatus.PENDING_APPROVAL);
                    currentUserPay.setPaymentDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
                    
                    // Update the entire userPayList in Firebase
                    userPayList.set(i, currentUserPay);
                    groupRef.child("userPayList").setValue(userPayList)
                        .addOnSuccessListener(aVoid -> {
                            // Send payment pending notification only to creator
                            notificationService.sendPaymentPendingNotification(group, currentUserPay);
                            Toast.makeText(this, "Payment request sent for approval", Toast.LENGTH_SHORT).show();
                            
                            // Refresh the UI
                            updateParticipantsList();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error sending payment request", Toast.LENGTH_SHORT).show();
                            Log.e("GroupDetailsActivity", "Error updating payment status", e);
                        });
                }
                break;
            }
        }
    }

    private void checkAndUpdateGroupStatus() {
        boolean allPaid = group.getUserPayListAsList().stream().allMatch(UserPay::isPaid);
        if (allPaid) {
            groupRef.child("status").setValue("Paid")
                .addOnSuccessListener(aVoid -> {
                    group.setStatus("Paid");
                    groupStatusText.setText("Paid");
                    Toast.makeText(this, "כל התשלומים הושלמו בהצלחה", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "שגיאה בעדכון סטטוס הקבוצה", Toast.LENGTH_SHORT).show();
                    Log.e("GroupDetailsActivity", "Error updating group status", e);
                });
        }
    }

    private void updateParticipantsList() {
        LinearLayout participantsContainer = findViewById(R.id.participants_container);
        participantsContainer.removeAllViews();
        
        boolean isCreator = group.getCreator().getId().equals(currentUserId);
        
        for (UserPay userPay : group.getUserPayListAsList()) {
            View participantView = getLayoutInflater().inflate(R.layout.item_participant_payment, participantsContainer, false);
            
            TextView nameText = participantView.findViewById(R.id.participant_name);
            TextView statusTextView = participantView.findViewById(R.id.payment_status);
            Button approveButton = participantView.findViewById(R.id.btn_approve);
            
            nameText.setText(userPay.getUser().getName());
            
            int statusColor;
            String statusText;
            
            switch (userPay.getPaymentStatus()) {
                case PAID:
                    statusColor = getResources().getColor(R.color.status_paid);
                    statusText = "שולם";
                    approveButton.setVisibility(View.GONE);
                    break;
                case PENDING_APPROVAL:
                    statusColor = getResources().getColor(R.color.status_pending);
                    statusText = "ממתין לאישור";
                    if (isCreator) {
                        approveButton.setVisibility(View.VISIBLE);
                        setupApprovalButton(approveButton, userPay);
                    } else {
                        approveButton.setVisibility(View.GONE);
                    }
                    break;
                default:
                    statusColor = getResources().getColor(R.color.status_not_paid);
                    statusText = "לא שולם";
                    approveButton.setVisibility(View.GONE);
            }
            
            statusTextView.setBackgroundTintList(ColorStateList.valueOf(statusColor));
            statusTextView.setText(statusText);
            
            participantsContainer.addView(participantView);
        }
    }

    private void setupApprovalButton(Button approveButton, UserPay userPay) {
        approveButton.setOnClickListener(v -> {
            userPay.setPaymentStatus(UserPay.PaymentStatus.PAID);
            userPay.setPaid(true);
            userPay.setPaymentDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
            
            updatePaymentStatus(userPay);
        });
    }

    private void updatePaymentStatus(UserPay userPay) {
        // Find the correct user in the list
        List<UserPay> userPayList = group.getUserPayListAsList();
        for (int i = 0; i < userPayList.size(); i++) {
            UserPay currentUserPay = userPayList.get(i);
            if (currentUserPay.getUser().getId().equals(userPay.getUser().getId())) {
                // Update the payment status at the correct index
                userPayList.set(i, userPay);
                groupRef.child("userPayList").setValue(userPayList)
                    .addOnSuccessListener(aVoid -> {
                        notificationService.sendPaymentCompleteNotification(group, userPay);
                        Toast.makeText(this, "Payment approved successfully", Toast.LENGTH_SHORT).show();
                        
                        // Check if all payments are complete
                        boolean allPaid = group.getUserPayListAsList().stream().allMatch(UserPay::isPaid);
                        if (allPaid) {
                            // Only update group status to "Paid" if all members have paid
                            groupRef.child("status").setValue("Paid")
                                .addOnSuccessListener(statusVoid -> {
                                    group.setStatus("Paid");
                                    groupStatusText.setText("Paid");
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error updating group status", Toast.LENGTH_SHORT).show();
                                    Log.e("GroupDetailsActivity", "Error updating group status", e);
                                });
                        }
                        
                        // Refresh the UI
                        updateParticipantsList();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error updating payment status", Toast.LENGTH_SHORT).show();
                        Log.e("GroupDetailsActivity", "Error updating payment status", e);
                    });
                break;
            }
        }
    }

    private String getCurrencyString(double totalAmount) {
        return String.format(Locale.US, "$%.2f", totalAmount);
    }

    private void deleteGroup() {
        if (group != null && group.getGroupId() != null) {
            // First, notify all participants about the deletion
            for (UserPay userPay : group.getUserPayListAsList()) {
                if (!userPay.getUser().getId().equals(currentUserId)) { // Don't notify the creator
                    notificationService.sendGroupDeletedNotification(group, userPay.getUser());
                }
            }

            // Then delete the group
            FirebaseDatabase.getInstance().getReference("groups")
                    .child(group.getGroupId())
                    .removeValue()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "הקבוצה נמחקה בהצלחה", Toast.LENGTH_SHORT).show();
                            finish();  // Close activity after deletion
                        } else {
                            Toast.makeText(this, "שגיאה במחיקת הקבוצה", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "קבוצה לא תקינה, לא ניתן למחוק", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateGroupStatus(String status) {
        groupRef.child("status").setValue(status).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(GroupDetailsActivity.this, "Status updated to " + status, Toast.LENGTH_SHORT).show();
                groupStatusText.setText(status);
            } else {
                Toast.makeText(GroupDetailsActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void goBack(View view) {
        onBackPressed();  // This will navigate back to the previous activity
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh group data and check status when activity resumes
        if (group != null && group.getGroupId() != null) {
            groupRef.get().addOnSuccessListener(snapshot -> {
                Group updatedGroup = snapshot.getValue(Group.class);
                if (updatedGroup != null) {
                    group = updatedGroup;
                    // Update UI with new data
                    groupStatusText.setText(group.getStatus());
                    deadlineCountdownView.setGroup(group);
                    // Refresh the amount display
                    displayUserOwedAmount();
                    // Check if all users have paid
                    boolean allPaid = group.getUserPayListAsList().stream().allMatch(UserPay::isPaid);
                    if (allPaid && !"Paid".equals(group.getStatus())) {
                        // Only update status if it's not already "Paid"
                        groupRef.child("status").setValue("Paid")
                            .addOnSuccessListener(aVoid -> {
                                group.setStatus("Paid");
                                groupStatusText.setText("Paid");
                            });
                    }
                }
            });
        }
    }

    // Add a method to manually trigger payment reminders
    private void sendPaymentReminders() {
        notificationService.sendPaymentReminder(group);
        Toast.makeText(this, "תזכורות נשלחו בהצלחה", Toast.LENGTH_SHORT).show();
    }

    // Add a menu item for sending reminders
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_details_menu, menu);
        // Show reminder button only to the creator
        boolean isCreator = group != null && group.getCreator().getId().equals(currentUserId);
        menu.findItem(R.id.action_send_reminders).setVisible(isCreator);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_send_reminders) {
            sendPaymentReminders();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

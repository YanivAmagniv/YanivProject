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
import com.example.yanivproject.models.Group;
import com.example.yanivproject.models.User;
import com.example.yanivproject.models.UserPay;
import com.example.yanivproject.services.AuthenticationService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class AddNewEvent extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private Spinner spCurrency, spEventType, spDivisionMethod;
    private EditText etGroupName, etDescription;
    private CalendarView cvEventDate;
    private ListView lvAllMembers, lvSelectedMembers;
    private Button btnCreateGroup;
    private ArrayList<User> allMembers = new ArrayList<>();
    private ArrayList<UserPay> selectedMembers = new ArrayList<>();
    private String groupName, groupDescription, groupType, eventDate;
    private int dividedBy;
    private double totalAmount;
    private User currentUser;
    private TextView dateTextView;
    private String divisionMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newgroup);

        etGroupName = findViewById(R.id.etGroupName);
        etDescription = findViewById(R.id.etDescription);
        spCurrency = findViewById(R.id.spCurrency);
        spEventType = findViewById(R.id.spEventType);
        cvEventDate = findViewById(R.id.cvEventDate);
        lvAllMembers = findViewById(R.id.lvallMembers);
        lvSelectedMembers = findViewById(R.id.lvSelectedMembers);
        btnCreateGroup = findViewById(R.id.btnCreateGroup);
        dateTextView = findViewById(R.id.dateTextView);
        spDivisionMethod = findViewById(R.id.spDivisionMethod);

        // Get the current user
        currentUser = AuthenticationService.getCurrentUser(auth);

        // Listen for the date selection
        cvEventDate.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            eventDate = year + "/" + (month + 1) + "/" + dayOfMonth;
            dateTextView.setText("תאריך שנבחר: " + eventDate);
        });

        spDivisionMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                divisionMethod = spDivisionMethod.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                divisionMethod = "Manual"; // Default method
            }
        });

        btnCreateGroup.setOnClickListener(view -> createGroup());
    }

    private void createGroup() {
        groupName = etGroupName.getText().toString();
        groupDescription = etDescription.getText().toString();
        groupType = spEventType.getSelectedItem().toString();

        // Validate input
        if (groupName.isEmpty() || groupDescription.isEmpty() || eventDate.isEmpty()) {
            Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate the total amount if needed
        totalAmount = 100.00; // You can replace this with input logic if necessary

        // Create the group object
        Group group = new Group();
        group.setGroupName(groupName);
        group.setGroupDescription(groupDescription);
        group.setType(groupType);
        group.setEventDate(eventDate);
        group.setStatus("Unpaid");
        group.setAdmin(currentUser);
        group.setDividedBy(dividedBy);
        group.setTotalAmount(totalAmount);
        group.setDivisionMethod(divisionMethod);

        // Save group to Firestore
        db.collection("groups").add(group)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "הקבוצה נוצרה בהצלחה", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "שגיאה בהוספת קבוצה", Toast.LENGTH_SHORT).show();
                    Log.e("AddGroupError", "Error adding group", e);
                });
    }
}

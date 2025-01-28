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
    Spinner spCurrency, spEventType,spDividedBy;

    CalendarView cvEventDate;
    TextView dateTextView;
    String stDate;

    EditText etGroupName, etDescription;
    String stGroupName, stDescription, stSPcurrency, stSPeventType,stspDividedBy;

    Button btnCreateGroup;

    DatabaseService databaseService;
    private AuthenticationService authenticationService;
    private String uid;

    User user;

    ListView lvMembers;
    ArrayList<User> users=new ArrayList<>();
    UserNamAdapter<User> adapter;

    ArrayList<User> usersSelected=new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_newgroup);
        databaseService = DatabaseService.getInstance();
        authenticationService = AuthenticationService.getInstance();
        uid=authenticationService.getCurrentUserId();

        initViews();


        users=new ArrayList<>();

        adapter = new UserNamAdapter<>(AddNewEvent.this, 0,0,users);
        lvMembers.setAdapter(adapter);
        lvMembers.setOnItemClickListener(this);

        databaseService.getUsers(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> object) {
                Log.d("TAG", "onCompleted: " + object);
                users.clear();
                users.addAll(object);
                /// notify the adapter that the data has changed
                /// this specifies that the data has changed
                /// and the adapter should update the view
                /// @see FoodSpinnerAdapter#notifyDataSetChanged()
                 // foodSpinnerAdapter.notifyDataSetChanged();

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

        spCurrency = findViewById(R.id.spCurrency);
        spEventType = findViewById(R.id.spEventType);
        etDescription = findViewById(R.id.etDescription);
        etGroupName = findViewById(R.id.etGroupName);

        lvMembers=findViewById(R.id.lvMembersSpit);

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
        /// get the values from the input fields
        stSPcurrency = spCurrency.getSelectedItem().toString();

       // stspDividedBy= spDividedBy.getSelectedItem().toString();

        stSPeventType = spEventType.getSelectedItem().toString();
        stDescription = etDescription.getText().toString();
        stGroupName = etGroupName.getText() + "";

        /// validate the input
        /// stop if the input is not valid
//        if (!isValid(name, priceText, imageBase64)) return;

        /// convert the price to double
//        double price = Double.parseDouble(priceText);

        /// generate a new id for the food
        String id = databaseService.generateMainSplitId();


// public MainSplit(String id, String status, String eventDate, String detail, String type, User admin, ArrayList<UserPay> users, int dividedBy, double totalAmount) {

        MainSplit mainSplit = new MainSplit(id, "not paid", stDate, stDescription,stSPeventType, user, users, 5, 1000.0);

        databaseService.createNewMainSplit(mainSplit, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {




            }

            @Override
            public void onFailed(Exception e) {

            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        usersSelected.add((User) parent.getItemAtPosition(position));
    }

    /// validate the input
//    private boolean isValid(String name, String priceText, String imageBase64) {
//        if (name.isEmpty()) {
//            Log.e(TAG, "Name is empty");
//            foodNameEditText.setError("Name is required");
//            foodNameEditText.requestFocus();
//            return false;
//        }
//
//        if (priceText.isEmpty()) {
//            Log.e(TAG, "Price is empty");
//            foodPriceEditText.setError("Price is required");
//            foodPriceEditText.requestFocus();
//            return false;
//        }
//
//        if (imageBase64 == null) {
//            Log.e(TAG, "Image is required");
//            Toast.makeText(this, "Image is required", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//
//        return true;
//    }
}
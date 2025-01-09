package com.example.yanivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yanivproject.R;
import com.example.yanivproject.models.MainSplit;
import com.example.yanivproject.models.User;
import com.example.yanivproject.models.UserPay;
import com.example.yanivproject.services.AuthenticationService;
import com.example.yanivproject.services.DatabaseService;

import java.util.ArrayList;

public class AddNewEvent extends AppCompatActivity implements View.OnClickListener {
    Spinner spCurrency, spEventType;

    EditText etGroupName, etDescription;
    String stGroupName, stDescription, stSPcurrency, stSPeventType;

    Button btnCreateGroup;

    DatabaseService databaseService;
    private AuthenticationService authenticationService;
    private String uid;

    User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_newgroup);
        databaseService = DatabaseService.getInstance();
        btnCreateGroup.setOnClickListener(this);
        authenticationService = AuthenticationService.getInstance();
        uid=authenticationService.getCurrentUserId();

        databaseService.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User u) {
                user = u;
                initViews();
            }

            @Override
            public void onFailed(Exception e) {

            }
        });

    }

    private void initViews() {
        btnCreateGroup = findViewById(R.id.btnCreateGroup);
        spCurrency = findViewById(R.id.spCurrency);
        spEventType = findViewById(R.id.spEventType);
        etDescription = findViewById(R.id.etDescription);
        etGroupName = findViewById(R.id.etGroupName);

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


// public MainSplit(String id, String status, String eventDate, String detail, String type, User admin, ArrayList< UserPay > users, int dividedBy, double totalAmount) {

        MainSplit mainSplit = new MainSplit(id, status, eventDate, stDescription, stSPcurrency,stSPeventType, admin, users, dividedBy, totalAmount);

        databaseService.createNewMainSplit(mainSplit, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {




            }

            @Override
            public void onFailed(Exception e) {

            }
        });
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
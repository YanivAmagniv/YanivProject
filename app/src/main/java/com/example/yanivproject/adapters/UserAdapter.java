// UserAdapter.java
// This adapter handles the display of users in a ListView
// Provides functionality to view and delete users
// Manages user data display and Firebase database operations

package com.example.yanivproject.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yanivproject.R;
import com.example.yanivproject.models.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

/**
 * Adapter class for displaying users in a ListView
 * Provides functionality to view user details and delete users
 * Manages Firebase database operations for user deletion
 */
public class UserAdapter extends ArrayAdapter<User> {
    // Application context for UI operations
    private Context context;
    // List of users to display
    private List<User> users;

    /**
     * Constructor for the adapter
     * @param context Application context
     * @param resource Resource ID for the layout of each item
     * @param users List of User objects to display
     */
    public UserAdapter(Context context, int resource, List<User> users) {
        super(context, resource, users);
        this.context = context;
        this.users = users;
    }

    /**
     * Creates and returns a view for each user item in the list
     * Handles view inflation, data binding, and delete functionality
     * @param position Position of the item in the data set
     * @param convertView The old view to reuse, if possible
     * @param parent The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate the view if it doesn't exist
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.user_item, parent, false);
        }

        // Get the user at the current position
        User user = users.get(position);

        // Initialize views for user information
        TextView tvFname = convertView.findViewById(R.id.tvfname);    // First name display
        TextView tvLname = convertView.findViewById(R.id.tvlname);    // Last name display
        TextView tvemail = convertView.findViewById(R.id.tvemail);    // Email display
        Button btnDelete = convertView.findViewById(R.id.btnDeleteUser); // Delete button

        // Set user information in the views
        tvFname.setText(user.getFname());
        tvLname.setText(user.getLname());
        tvemail.setText(user.getEmail());

        // Set up delete button click listener
        btnDelete.setOnClickListener(v -> {
            // Get reference to the user in Firebase database
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(user.getId());
            
            // Attempt to delete the user
            userRef.removeValue()
                    .addOnSuccessListener(aVoid -> {
                        // Remove user from local list and update UI
                        users.remove(position);
                        notifyDataSetChanged();
                        Toast.makeText(context, "User deleted", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Show error message if deletion fails
                        Toast.makeText(context, "Delete failed: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    });
        });

        return convertView;
    }
}

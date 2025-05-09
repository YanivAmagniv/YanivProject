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

public class UserAdapter extends ArrayAdapter<User> {
    private Context context;
    private List<User> users;

    public UserAdapter(Context context, int resource, List<User> users) {
        super(context, resource, users);
        this.context = context;
        this.users = users;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate the view
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.user_item, parent, false);
        }

        // Get current user
        User user = users.get(position);

        // Set views
        TextView tvFname = convertView.findViewById(R.id.tvfname);
        TextView tvLname = convertView.findViewById(R.id.tvlname);
        TextView tvemail = convertView.findViewById(R.id.tvemail);  // New TextView for email

        Button btnDelete = convertView.findViewById(R.id.btnDeleteUser);

        tvFname.setText(user.getFname());
        tvLname.setText(user.getLname());
        tvemail.setText(user.getEmail());

        // Delete user from Firebase
        btnDelete.setOnClickListener(v -> {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getId());
            userRef.removeValue().addOnSuccessListener(aVoid -> {
                users.remove(position);
                notifyDataSetChanged();
                Toast.makeText(context, "User deleted", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(context, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });

        return convertView;
    }
}

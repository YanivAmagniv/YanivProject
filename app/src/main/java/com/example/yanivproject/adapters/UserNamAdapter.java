// UserNamAdapter.java
// This adapter handles the display of user names in a ListView or Spinner
// Extends ArrayAdapter to provide custom view for user name display
// Used for displaying user names in dropdown menus or lists

package com.example.yanivproject.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.yanivproject.R;
import com.example.yanivproject.models.User;

import java.util.List;

/**
 * Adapter class for displaying user names in a ListView or Spinner
 * Provides custom view for each user name item
 * Extends ArrayAdapter for standard list functionality
 */
public class UserNamAdapter<P> extends ArrayAdapter<User> {
    // Application context for layout inflation
    private Context context;
    // List of users to display
    private List<User> objects;

    /**
     * Constructor for the adapter
     * @param context Application context
     * @param resource Resource ID for the layout of each item
     * @param textViewResourceId Resource ID for the TextView within the layout
     * @param objects List of User objects to display
     */
    public UserNamAdapter(Context context, int resource, int textViewResourceId, List<User> objects) {
        super(context, resource, textViewResourceId, objects);
        this.context = context;
        this.objects = objects;
    }

    /**
     * Creates and returns a view for each item in the list
     * Inflates the custom layout and sets the user's full name
     * @param position Position of the item in the data set
     * @param convertView The old view to reuse, if possible
     * @param parent The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the layout inflater from the activity context
        LayoutInflater layoutInflater = ((Activity)context).getLayoutInflater();
        // Inflate the custom layout for the item
        View view = layoutInflater.inflate(R.layout.username, parent, false);

        // Get the TextView for displaying the full name
        TextView tvfname = view.findViewById(R.id.tvfullName);

        // Get the user at the current position
        User temp = objects.get(position);
        // Set the full name by combining first and last name
        tvfname.setText(temp.getFname() + " " + temp.getLname());

        return view;
    }

    /**
     * Helper method to inflate the user row layout
     * @param parent The parent view group
     * @param layoutInflater The layout inflater to use
     * @return The inflated view
     */
    private View getInflate(ViewGroup parent, LayoutInflater layoutInflater) {
        return layoutInflater.inflate(R.layout.userraw, parent, false);
    }
}

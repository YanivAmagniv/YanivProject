// DatabaseService.java
// This service handles all Firebase Realtime Database operations for the app
// It provides methods for CRUD operations on users and groups
// Implements the Singleton pattern to ensure a single instance throughout the app

package com.example.yanivproject.services;

import android.util.Log;

import androidx.annotation.Nullable;


import com.example.yanivproject.models.Group;
import com.example.yanivproject.models.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class for handling Firebase Realtime Database operations
 * This class implements the Singleton pattern to ensure a single instance
 * throughout the application lifecycle
 */
public class DatabaseService {

    /// tag for logging
    /// @see Log
    private static final String TAG = "DatabaseService";

    /// callback interface for database operations
    /// @param <T> the type of the object to return
    /// @see DatabaseCallback#onCompleted(Object)
    /// @see DatabaseCallback#onFailed(Exception)
    public interface DatabaseCallback<T> {
        /// called when the operation is completed successfully
        void onCompleted(T object);

        /// called when the operation fails with an exception
        void onFailed(Exception e);
    }

    /// the instance of this class
    /// @see #getInstance()
    private static DatabaseService instance;

    /// the reference to the database
    /// @see DatabaseReference
    /// @see FirebaseDatabase#getReference()
    private final DatabaseReference databaseReference;

    /// use getInstance() to get an instance of this class
    /// @see DatabaseService#getInstance()
    private DatabaseService() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    /// get an instance of this class
    /// @return an instance of this class
    /// @see DatabaseService
    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }


    // private generic methods to write and read data from the database

    /// write data to the database at a specific path
    /// @param path the path to write the data to
    /// @param data the data to write (can be any object, but must be serializable, i.e. must have a default constructor and all fields must have getters and setters)
    /// @param callback the callback to call when the operation is completed
    /// @return void
    /// @see DatabaseCallback
    private void writeData(@NotNull final String path, @NotNull final Object data, final @Nullable DatabaseCallback<Void> callback) {
        databaseReference.child(path).setValue(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (callback == null) return;
                callback.onCompleted(task.getResult());
            } else {
                if (callback == null) return;
                callback.onFailed(task.getException());
            }
        });
    }

    /// read data from the database at a specific path
    /// @param path the path to read the data from
    /// @return a DatabaseReference object to read the data from
    /// @see DatabaseReference

    public DatabaseReference readData(@NotNull final String path) {
        return databaseReference.child(path);
    }


    /// get data from the database at a specific path
    /// @param path the path to get the data from
    /// @param clazz the class of the object to return
    /// @param callback the callback to call when the operation is completed
    /// @return void
    /// @see DatabaseCallback
    /// @see Class
    private <T> void getData(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull final DatabaseCallback<T> callback) {
        readData(path).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting data", task.getException());
                callback.onFailed(task.getException());
                return;
            }
            T data = task.getResult().getValue(clazz);
            callback.onCompleted(data);
        });
    }

    /// generate a new id for a new object in the database
    /// @param path the path to generate the id for
    /// @return a new id for the object
    /// @see String
    /// @see DatabaseReference#push()

    private String generateNewId(@NotNull final String path) {
        return databaseReference.child(path).push().getKey();
    }

    // end of private methods for reading and writing data

    // public methods to interact with the database

    /// create a new user in the database
    /// @param user the user object to create
    /// @param callback the callback to call when the operation is completed
    ///              the callback will receive void
    ///            if the operation fails, the callback will receive an exception
    /// @return void
    /// @see DatabaseCallback
    /// @see User
    public void createNewUser(@NotNull final User user, @Nullable final DatabaseCallback<Void> callback) {
        writeData("Users/" + user.getId(), user, callback);
    }


    /// get a user from the database
    /// @param uid the id of the user to get
    /// @param callback the callback to call when the operation is completed
    ///               the callback will receive the user object
    ///             if the operation fails, the callback will receive an exception
    /// @return void
    /// @see DatabaseCallback
    /// @see User
    public void getUser(@NotNull final String uid, @NotNull final DatabaseCallback<User> callback) {
        getData("Users/" + uid, User.class, callback);
    }

    /// generate a new id for a new group in the database
    /// @return a new id for the group
    public String generateGroupId() {
        return generateNewId("groups");
    }

    public void createNewGroup(@NotNull final Group group, @Nullable final DatabaseCallback<Void> callback) {
        writeData("groups/" + group.getGroupId(), group, callback);
    }
    /// get all the groups from the database
/// @param callback the callback to call when the operation is completed
///              the callback will receive a list of group objects
///            if the operation fails, the callback will receive an exception
/// @return void
/// @see DatabaseCallback
/// @see List
/// @see Group
    public void getGroups(@NotNull final DatabaseCallback<List<Group>> callback) {
        readData("groups").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting groups data", task.getException());
                callback.onFailed(task.getException());
                return;
            }
            List<Group> groups = new ArrayList<>();
            task.getResult().getChildren().forEach(dataSnapshot -> {
                Group group = dataSnapshot.getValue(Group.class);
                if (group != null) {
                    Log.d(TAG, "Fetched group: " + group.getGroupName()); // Log group name
                    groups.add(group);
                }
            });

            callback.onCompleted(groups);
        });
    }
    /// get all the users from the database
    /// @param callback the callback to call when the operation is completed
    ///              the callback will receive a list of food objects
    ///            if the operation fails, the callback will receive an exception
    /// @return void
    /// @see DatabaseCallback
    /// @see List
    /// @see Food
    /// @see #getData(String, Class, DatabaseCallback)
    public void getUsers(@NotNull final DatabaseCallback<List<User>> callback) {
        readData("Users").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting data", task.getException());
                callback.onFailed(task.getException());
                return;
            }
            List<User> users = new ArrayList<>();
            task.getResult().getChildren().forEach(dataSnapshot -> {
                User user = dataSnapshot.getValue(User.class);

                user=new User(user);
                Log.d(TAG, "Got user: " + user);
                users.add(user);
            });

            callback.onCompleted(users);
        });
    }

    /**
     * Get all users from the database
     * @param callback Callback to handle the result
     */
    public void getAllUsers(@NotNull final DatabaseCallback<List<User>> callback) {
        readData("Users").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting users", task.getException());
                callback.onFailed(task.getException());
                return;
            }
            List<User> users = new ArrayList<>();
            task.getResult().getChildren().forEach(dataSnapshot -> {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    users.add(user);
                }
            });
            callback.onCompleted(users);
        });
    }

    /**
     * Create a new group in the database
     * @param group The group to create
     * @param callback Callback to handle the result
     */
    public void createGroup(@NotNull final Group group, @Nullable final DatabaseCallback<Void> callback) {
        String groupId = generateGroupId();
        group.setGroupId(groupId);
        writeData("groups/" + groupId, group, callback);
    }

    /**
     * Gets a user by their ID
     * @param userId The ID of the user to retrieve
     * @param callback Callback to handle the result
     */
    public void getUserById(@NotNull final String userId, @NotNull final DatabaseCallback<User> callback) {
        readData("Users/" + userId).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting user data", task.getException());
                callback.onFailed(task.getException());
                return;
            }
            User user = task.getResult().getValue(User.class);
            if (user != null) {
                callback.onCompleted(user);
            } else {
                callback.onFailed(new Exception("User not found"));
            }
        });
    }

}

// User.java
// This class represents a user in the application
// Implements Serializable for easy data transfer between activities
// Contains user information and authentication details

package com.example.yanivproject.models;

import java.io.Serializable;

/**
 * Model class representing a user in the application
 * Contains user information, authentication details, and administrative status
 * Implements Serializable for easy data transfer between activities
 */
public class User implements Serializable {
    // Serialization version ID for compatibility
    private static final long serialVersionUID = 1L;

    // User identification and personal information
    protected String id;        // Unique identifier for the user
    protected String fname;     // User's first name
    protected String lname;     // User's last name
    protected String phone;     // User's phone number
    protected String email;     // User's email address
    protected String password;  // User's password (should be hashed in production)
    protected Boolean isAdmin;  // Flag indicating if user has administrative privileges

    /**
     * Constructor for creating a new user with all fields
     * @param id Unique identifier for the user
     * @param fname User's first name
     * @param lname User's last name
     * @param phone User's phone number
     * @param email User's email address
     * @param password User's password
     * @param isAdmin Flag indicating administrative privileges
     */
    public User(String id, String fname, String lname, String phone, String email, String password, Boolean isAdmin) {
        this.id = id;
        this.fname = fname;
        this.lname = lname;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
    }
    
    /**
     * Copy constructor for creating a new user from an existing one
     * Copies all fields except password and admin status
     * @param user The user to copy from
     */
    public User(User user) {
        this.id = user.id;
        this.fname = user.fname;
        this.lname = user.lname;
        this.phone = user.phone;
        this.email = user.email;
    }

    /**
     * Default constructor required for Firebase serialization
     */
    public User() {
    }

    // Getters and setters for all fields

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the user's full name by combining first and last name
     * @return The user's full name
     */
    public String getFullName() {
        return fname + " " + lname;
    }

    public Boolean getAdmin() {
        return isAdmin;
    }

    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }

    /**
     * Gets the user's full name (alias for getFullName)
     * @return The user's full name
     */
    public String getName() {
        return fname + " " + lname;
    }

    /**
     * Placeholder method for setting name
     * Currently not implemented as it would require splitting the name
     */
    public void setName(String name) {
        // This method is not used in the current implementation
    }

    /**
     * Returns a string representation of the user
     * @return String containing all user fields
     */
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", fname='" + fname + '\'' +
                ", lname='" + lname + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", isAdmin='" + isAdmin + '\'' +
                '}';
    }
}

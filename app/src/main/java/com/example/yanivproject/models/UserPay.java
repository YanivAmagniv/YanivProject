// UserPay.java
// This class represents a user's payment information in a group
// Tracks payment status, amounts, and payment history
// Implements Serializable for easy data transfer between activities

package com.example.yanivproject.models;

import java.io.Serializable;

/**
 * Model class representing a user's payment information in a group
 * Tracks payment status, amounts, and payment history
 * Implements Serializable for easy data transfer between activities
 */
public class UserPay implements Serializable {
    // Serialization version ID for compatibility
    private static final long serialVersionUID = 1L;

    /**
     * Enum representing the possible payment statuses
     * NOT_PAID: Payment has not been made
     * PENDING_APPROVAL: Payment has been made but needs approval
     * PAID: Payment has been made and approved
     */
    public enum PaymentStatus {
        NOT_PAID,
        PENDING_APPROVAL,
        PAID
    }

    // Payment information fields
    protected User user;                // The user associated with this payment
    protected double amount;           // The total amount to be paid
    protected boolean isPaid;          // Flag indicating if payment is complete
    protected double totalPaid;        // Total amount paid so far
    protected double remaining;        // Remaining amount to be paid
    protected String paymentDate;      // Date when payment was made
    protected PaymentStatus paymentStatus;  // Current status of the payment
    protected String paymentNote;      // Optional note about the payment

    /**
     * Default constructor
     * Initializes payment status to NOT_PAID
     */
    public UserPay() {
        this.paymentStatus = PaymentStatus.NOT_PAID;
    }

    /**
     * Constructor for creating a new payment record
     * @param user The user making the payment
     * @param amount The total amount to be paid
     */
    public UserPay(User user, double amount) {
        this.user = user;
        this.amount = amount;
        this.isPaid = false;
        this.totalPaid = 0;
        this.remaining = amount;
        this.paymentStatus = PaymentStatus.NOT_PAID;
    }

    /**
     * Constructor for creating a payment record with existing payment information
     * @param user The user making the payment
     * @param amount The total amount to be paid
     * @param isPaid Flag indicating if payment is complete
     * @param totalPaid Total amount paid so far
     * @param remaining Remaining amount to be paid
     */
    public UserPay(User user, double amount, boolean isPaid, double totalPaid, double remaining) {
        this.user = user;
        this.amount = amount;
        this.isPaid = isPaid;
        this.totalPaid = totalPaid;
        this.remaining = remaining;
        this.paymentStatus = isPaid ? PaymentStatus.PAID : PaymentStatus.NOT_PAID;
    }

    // Getters and setters for all fields

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public double getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(double totalPaid) {
        this.totalPaid = totalPaid;
    }

    public double getRemaining() {
        return remaining;
    }

    public void setRemaining(double remaining) {
        this.remaining = remaining;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    /**
     * Gets the current payment status
     * @return The current PaymentStatus
     */
    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    /**
     * Sets the payment status and updates the isPaid flag accordingly
     * @param paymentStatus The new payment status
     */
    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
        this.isPaid = (paymentStatus == PaymentStatus.PAID);
    }

    public String getPaymentNote() {
        return paymentNote;
    }

    public void setPaymentNote(String paymentNote) {
        this.paymentNote = paymentNote;
    }

    /**
     * Returns a string representation of the payment information
     * @return String containing all payment fields
     */
    @Override
    public String toString() {
        return "UserPay{" +
                "user=" + user +
                ", amount=" + amount +
                ", isPaid=" + isPaid +
                ", totalPaid=" + totalPaid +
                ", remaining=" + remaining +
                ", paymentDate=" + paymentDate +
                ", paymentStatus=" + paymentStatus +
                ", paymentNote=" + paymentNote +
                '}';
    }
}

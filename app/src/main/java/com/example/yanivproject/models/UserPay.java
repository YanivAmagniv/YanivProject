package com.example.yanivproject.models;

import java.io.Serializable;

public class UserPay implements Serializable {
    private static final long serialVersionUID = 1L;  // Optional

    public enum PaymentStatus {
        NOT_PAID,
        PENDING_APPROVAL,
        PAID
    }

    protected User user;
    protected double amount;
    protected boolean isPaid;
    protected double totalPaid;
    protected double remaining;
    protected String paymentDate;
    protected PaymentStatus paymentStatus;
    protected String paymentNote;

    public UserPay() {
        this.paymentStatus = PaymentStatus.NOT_PAID;
    }

    public UserPay(User user, double amount) {
        this.user = user;
        this.amount = amount;
        this.isPaid = false;
        this.totalPaid = 0;
        this.remaining = amount;
        this.paymentStatus = PaymentStatus.NOT_PAID;
    }

    public UserPay(User user, double amount, boolean isPaid, double totalPaid, double remaining) {
        this.user = user;
        this.amount = amount;
        this.isPaid = isPaid;
        this.totalPaid = totalPaid;
        this.remaining = remaining;
        this.paymentStatus = isPaid ? PaymentStatus.PAID : PaymentStatus.NOT_PAID;
    }

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

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

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

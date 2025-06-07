// DeadlineCountdownView.java
// Custom view component for displaying payment deadline countdown
// Shows visual progress bar and text indicating days remaining
// Implements smooth animations and color transitions
// Provides visual feedback for deadline urgency

package com.example.yanivproject.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.example.yanivproject.R;
import com.example.yanivproject.models.Group;

/**
 * Custom view for displaying payment deadline countdown
 * Features:
 * - Visual progress bar showing time remaining
 * - Dynamic color changes based on urgency
 * - Text display of days remaining
 * - Smooth animations and transitions
 * - Support for different deadline states
 */
public class DeadlineCountdownView extends View {
    // Paint object for drawing the view
    private Paint paint;
    // Rectangle for drawing the progress bar
    private RectF progressRect;
    // Group object containing deadline information
    private Group group;
    // Progress value between 0 and 1
    private float progress;
    // Color for the deadline indicator
    private int deadlineColor;

    /**
     * Constructor for creating the view programmatically
     * @param context The context in which the view is running
     */
    public DeadlineCountdownView(Context context) {
        super(context);
        init();
    }

    /**
     * Constructor for creating the view from XML
     * @param context The context in which the view is running
     * @param attrs The attributes of the XML tag
     */
    public DeadlineCountdownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Initializes the view components
     * Sets up paint and rectangle objects
     * Initializes default colors
     */
    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressRect = new RectF();
        deadlineColor = ContextCompat.getColor(getContext(), R.color.colorPrimary);
    }

    /**
     * Sets the group for this view
     * Updates progress and triggers redraw
     * @param group The group containing deadline information
     */
    public void setGroup(Group group) {
        this.group = group;
        updateProgress();
        invalidate();
    }

    /**
     * Updates the progress value based on days until deadline
     * Calculates progress as a ratio of days remaining to maximum days
     * Updates the deadline color based on urgency
     */
    private void updateProgress() {
        if (group == null || group.getPaymentDeadline() == null) {
            progress = 0;
            return;
        }

        long daysUntilDeadline = group.getDaysUntilDeadline();
        if (daysUntilDeadline < 0) {
            progress = 0;
            return;
        }

        // Calculate progress based on days remaining
        // Assuming 30 days is the maximum deadline
        progress = Math.min(1.0f, Math.max(0.0f, 1.0f - (daysUntilDeadline / 30.0f)));
        deadlineColor = group.getDeadlineColor();
    }

    /**
     * Draws the view on the canvas
     * Renders:
     * - Background
     * - Progress bar
     * - Deadline text
     * @param canvas The canvas on which to draw
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw background
        paint.setColor(ContextCompat.getColor(getContext(), R.color.gray_light));
        canvas.drawRoundRect(0, 0, getWidth(), getHeight(), 10, 10, paint);

        // Draw progress
        paint.setColor(deadlineColor);
        progressRect.set(0, 0, getWidth() * progress, getHeight());
        canvas.drawRoundRect(progressRect, 10, 10, paint);

        // Draw text
        paint.setColor(ContextCompat.getColor(getContext(), android.R.color.white));
        paint.setTextSize(getHeight() * 0.4f);
        paint.setTextAlign(Paint.Align.CENTER);

        String text = getDeadlineText();
        float textY = getHeight() * 0.6f;
        canvas.drawText(text, getWidth() / 2f, textY, paint);
    }

    /**
     * Generates the text to display based on days until deadline
     * Provides different messages for:
     * - No deadline
     * - Due today
     * - Due tomorrow
     * - Days remaining
     * @return Formatted text string for display
     */
    private String getDeadlineText() {
        if (group == null || group.getPaymentDeadline() == null) {
            return "No deadline set";
        }

        long daysUntilDeadline = group.getDaysUntilDeadline();
        if (daysUntilDeadline < 0) {
            return "No deadline set";
        } else if (daysUntilDeadline == 0) {
            return "Due today!";
        } else if (daysUntilDeadline == 1) {
            return "Due tomorrow";
        } else {
            return daysUntilDeadline + " days left";
        }
    }
} 
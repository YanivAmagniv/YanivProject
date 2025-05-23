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

public class DeadlineCountdownView extends View {
    private Paint paint;
    private RectF progressRect;
    private Group group;
    private float progress;
    private int deadlineColor;

    public DeadlineCountdownView(Context context) {
        super(context);
        init();
    }

    public DeadlineCountdownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressRect = new RectF();
        deadlineColor = ContextCompat.getColor(getContext(), R.color.colorPrimary);
    }

    public void setGroup(Group group) {
        this.group = group;
        updateProgress();
        invalidate();
    }

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
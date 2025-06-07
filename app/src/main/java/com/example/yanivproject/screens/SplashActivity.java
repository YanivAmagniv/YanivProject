// SplashActivity.java
// This activity serves as the entry point of the application
// Displays a splash screen with an animated logo
// Automatically transitions to MainActivity after a delay

package com.example.yanivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.yanivproject.R;

/**
 * Splash screen activity that displays when the app starts
 * Shows an animated logo and transitions to the main activity
 * Provides a smooth entry experience for users
 */
public class SplashActivity extends AppCompatActivity {
    // Duration to show the splash screen in milliseconds
    private static final int SPLASH_DELAY = 2000; // 2 seconds

    /**
     * Called when the activity is first created
     * Sets up the splash screen layout and animations
     * @param savedInstanceState Bundle containing the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Get the logo ImageView from the layout
        ImageView logoImageView = findViewById(R.id.splashLogo);

        // Load and start the fade-in animation for the logo
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.splash_fade_in);
        logoImageView.startAnimation(fadeIn);

        // Schedule navigation to MainActivity after the delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Create intent to start MainActivity
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            // Finish this activity to prevent going back to splash screen
            finish();
            // Add fade out animation for smooth transition to MainActivity
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, SPLASH_DELAY);
    }
} 
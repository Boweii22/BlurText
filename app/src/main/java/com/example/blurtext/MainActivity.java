package com.example.blurtext;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.MaskFilterSpan;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private LinearLayout container;
    private String text = "Isn't this so cool"; // Sentence to animate
    private int currentIndex = 0;
    private Handler handler = new Handler();

    // UI Elements
    private SeekBar speedSeekBar;
    private TextView speedTextView;

    // Toggle states
    private boolean isFromTop = true; // Floating direction
    private boolean isWordMode = true; // Toggle between word and letter animation
    private int animationSpeed = 390; // Default animation speed in ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Use XML layout

                // Find views
                container = findViewById(R.id.wordContainer);
        Button toggleDirectionButton = findViewById(R.id.toggleDirectionButton);
        Button toggleModeButton = findViewById(R.id.toggleModeButton);
        speedSeekBar = findViewById(R.id.speedSeekBar);
        speedTextView = findViewById(R.id.speedTextView);

        // SeekBar Change Listener
        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                animationSpeed = Math.max(progress, 100); // Minimum speed limit (100ms)
                speedTextView.setText("Speed: " + animationSpeed + "ms");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                resetAnimation();
            }
        });



// Set initial button text
        toggleDirectionButton.setText(isFromTop ? "Direction: Top" : "Direction: Bottom");
        toggleModeButton.setText(isWordMode ? "Mode: Words" : "Mode: Letters");

// Toggle floating direction
        toggleDirectionButton.setOnClickListener(v -> {
            isFromTop = !isFromTop;
            toggleDirectionButton.setText(isFromTop ? "Direction: Top" : "Direction: Bottom");
            resetAnimation();
        });

// Toggle between words and letters animation
        toggleModeButton.setOnClickListener(v -> {
            isWordMode = !isWordMode;
            toggleModeButton.setText(isWordMode ? "Mode: Words" : "Mode: Letters");
            resetAnimation();
        });


//        // Toggle floating direction
//        toggleDirectionButton.setOnClickListener(v -> {
//            isFromTop = !isFromTop;
//            resetAnimation();
//        });
//
//        // Toggle between words and letters animation
//        toggleModeButton.setOnClickListener(v -> {
//            isWordMode = !isWordMode;
//            resetAnimation();
//        });

        // Start animation
        animateNext();
    }

    private void resetAnimation() {
        container.removeAllViews();
        currentIndex = 0;
        animateNext();
    }

    private void animateNext() {
        String[] items = isWordMode ? text.split(" ") : text.split(""); // Words or letters

        if (currentIndex >= items.length) return; // Stop when all elements are animated

        final String currentItem = items[currentIndex];

        // Create a TextView for the word or letter
        final TextView textView = new TextView(this);
        textView.setTextSize(30);
        textView.setTextColor(Color.WHITE);
        textView.setLayerType(TextView.LAYER_TYPE_SOFTWARE, null); // Enable blur
        textView.setAlpha(0f); // Start invisible
        textView.setTranslationY(isFromTop ? 100f : -100f); // Set start position based on direction

        // Add margin (larger for words, smaller for letters)
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(isWordMode ? 10 : 2, 0, isWordMode ? 10 : 2, 0);
        textView.setLayoutParams(params);

        // Apply blur effect initially
        final float initialBlur = 25f;
        final SpannableString spannable = new SpannableString(currentItem);
        final MaskFilterSpan blurSpan = new MaskFilterSpan(new BlurMaskFilter(initialBlur, BlurMaskFilter.Blur.NORMAL));
        spannable.setSpan(blurSpan, 0, currentItem.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        textView.setText(spannable);

        container.addView(textView); // Add to layout

        // Floating animation
        ObjectAnimator moveAnim = ObjectAnimator.ofFloat(textView, "translationY", isFromTop ? 100f : -100f, 0f);
        moveAnim.setDuration(animationSpeed);
        moveAnim.setInterpolator(new AccelerateDecelerateInterpolator());

        // Fade in effect
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(textView, "alpha", 0f, 1f);
        fadeIn.setDuration(animationSpeed);
        fadeIn.setInterpolator(new AccelerateDecelerateInterpolator());

        // Blur animation
        ValueAnimator blurAnimator = ValueAnimator.ofFloat(initialBlur, 1f);
        blurAnimator.setDuration(animationSpeed);
        blurAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        blurAnimator.addUpdateListener(animation -> {
            float blurRadius = (float) animation.getAnimatedValue();
            spannable.setSpan(new MaskFilterSpan(new BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)),
                    0, currentItem.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            textView.setText(spannable);
        });

        // Start animations
        moveAnim.start();
        fadeIn.start();
        blurAnimator.start();

        // Delay before next animation
        handler.postDelayed(() -> {
            currentIndex++;
            animateNext();
        }, animationSpeed);
    }
}

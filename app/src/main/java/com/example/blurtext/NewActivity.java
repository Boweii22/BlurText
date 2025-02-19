package com.example.blurtext;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.MaskFilterSpan;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class NewActivity extends AppCompatActivity {
    private LinearLayout container;
    private String text = "Isn't this so cool"; //text to animate
    private int currentIndex = 0;
    private Handler handler = new Handler();

    //UI Elements
    private SeekBar speedSeekBar;
    private TextView speedTextView;

    //toggle states
    private boolean isFromTop = true; //floating direction
    private boolean isWordMode = true; //toggle between words and letters
    private int animationSpeed = 700; //default animation speed in ms


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        container = findViewById(R.id.wordContainer);
        TextView toggleDirection = findViewById(R.id.direction);
        TextView toggleMode = findViewById(R.id.animateWords);
        speedSeekBar = findViewById(R.id.delaySeekBar);
        speedTextView = findViewById(R.id.tvDelay);


        // SeekBar Change Listener
        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                animationSpeed = Math.max(progress, 100); // Minimum speed limit (100ms)
                speedTextView.setText(animationSpeed + "ms");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                resetAnimation();
            }
        });

        // Set initial button text
        toggleDirection.setText(isFromTop ? "Direction: Top" : "Direction: Bottom");
        toggleMode.setText(isWordMode ? "Mode: Words" : "Mode: Letters");

// Toggle floating direction
        toggleDirection.setOnClickListener(v -> {
            isFromTop = !isFromTop;
            toggleDirection.setText(isFromTop ? "Direction: Top" : "Direction: Bottom");
            resetAnimation();
        });

// Toggle between words and letters animation
        toggleMode.setOnClickListener(v -> {
            isWordMode = !isWordMode;
            toggleMode.setText(isWordMode ? "Mode: Words" : "Mode: Letters");
            resetAnimation();
        });


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
        final float initialBlur = 50f;
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

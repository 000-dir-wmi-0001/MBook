package com.example.mbook.utils;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;

import java.util.Timer;
import java.util.TimerTask;

public class AutoImageChangerView extends androidx.appcompat.widget.AppCompatImageView {
    private int[] imageResources;
    private int currentIndex = 0;
    private Handler handler;
    private Timer timer;

    public AutoImageChangerView(Context context) {
        super(context);
        init();
    }

    public AutoImageChangerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutoImageChangerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        handler = new Handler();
        timer = new Timer();
    }

    public void setImageResources(int[] imageResources) {
        this.imageResources = imageResources;
        if (imageResources.length > 0) {
            setImageResource(imageResources[0]); // Set the first image initially
        }
    }

    public void startImageChanger() {
        if (imageResources == null || imageResources.length == 0) {
            throw new IllegalStateException("Image resources are not set or empty.");
        }

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (imageResources != null && imageResources.length > 0) {
                            setImageResource(imageResources[currentIndex]);
                            currentIndex = (currentIndex + 1) % imageResources.length;
                        }
                    }
                });
            }
        }, 0, 3000); // Change image every 3 seconds
    }

    public void stopImageChanger() {
        if (timer != null) {
            timer.cancel();
            timer.purge(); // Clean up the Timer
            timer = new Timer(); // Reinitialize Timer to be used again
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopImageChanger(); // Stop the timer when the view is detached
    }
}

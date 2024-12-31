package com.example.mbook;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.mbook.models.UserProfile;
import com.example.mbook.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference db;
    private Handler inactivityHandler = new Handler();
    private Runnable inactivityRunnable;
    private DatabaseReference userRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Initialize Firebase Auth and Realtime Database
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();



        // Check if the user is logged in
        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
            userRef = db.child("users").child(currentUserId);
            getUserProfile(currentUserId);
        } else {
            // User is not logged in, proceed with the login flow
            setupLoginSignup();
        }
    }

    private void setupLoginSignup() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.nav_login).setOnClickListener(this::goToLogin);
        findViewById(R.id.nav_signup).setOnClickListener(this::goToSignup);
    }


    private void getUserProfile(String userId) {
        db.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                    if (userProfile != null) {
                        if ("admin".equals(userProfile.getRole())) {
                            showAdminFeatures();
                        } else {
                            showNormalUserFeatures();
                        }
                    } else {
                        showError("User profile data is null.");
                        Utils.goToLogin(MainActivity.this);
                        finish();
                    }
                } else {
                    showError("User profile not found.");
                    Utils.goToLogin(MainActivity.this);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MainActivity", "Database error", databaseError.toException());
                showError("Error retrieving user profile: " + databaseError.getMessage());
                Utils.goToLogin(MainActivity.this);
                finish();
            }
        });
    }

    private void showAdminFeatures() {
        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showNormalUserFeatures() {
        Intent intent = new Intent(MainActivity.this, home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void goToSignup(View view) {
        Utils.goToSignup(this);
    }

    public void goToLogin(View view) {
        Utils.goToLogin(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetInactivityTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        inactivityHandler.removeCallbacks(inactivityRunnable);
        updateUserActiveStatus(false); // Mark as inactive when paused
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        resetInactivityTimer(); // Reset timer on user interaction
    }

    private void resetInactivityTimer() {
        inactivityHandler.removeCallbacks(inactivityRunnable);
        updateUserActiveStatus(true); // Mark as active
        inactivityRunnable = new Runnable() {
            @Override
            public void run() {
                updateUserActiveStatus(false); // Mark as inactive after 5 minutes
            }
        };
        inactivityHandler.postDelayed(inactivityRunnable, 5 * 60 * 1000); // 5 minutes
    }

    private void updateUserActiveStatus(boolean isActive) {
        long currentTime = System.currentTimeMillis();
        if (userRef != null) {
            userRef.child("isActive").setValue(isActive);
            userRef.child("lastActive").setValue(currentTime); // Update last active time
        }
    }
}


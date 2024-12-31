package com.example.mbook;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
//import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mbook.models.UserProfile;
import com.example.mbook.utils.Utils;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private FirebaseAuth auth;
    private DatabaseReference db;

    private EditText f_name, l_name, s_email, p_number, pass, c_pass;
//    private ImageButton select_image_button;
    private Button signup_button, select_image_button;
    private ImageView profile_image;
    private CircularProgressIndicator progressIndicator;
    private View blurOverlay;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        initializeViews();

        // Set up image selection
        select_image_button.setOnClickListener(v -> openFileChooser());

        signup_button.setOnClickListener(v -> registerUser());
    }

    private void initializeViews() {
        f_name = findViewById(R.id.first_name);
        l_name = findViewById(R.id.last_name);
        s_email = findViewById(R.id.email);
        p_number = findViewById(R.id.phone);
        pass = findViewById(R.id.signup_password);
        c_pass = findViewById(R.id.confirm_password);
        signup_button = findViewById(R.id.signup);
        select_image_button = findViewById(R.id.select_image_button);
        profile_image = findViewById(R.id.profile_image);
        progressIndicator = findViewById(R.id.progress_bar);
        blurOverlay = findViewById(R.id.blur_overlay);

        progressIndicator.setIndeterminate(true);
    }

    private void registerUser() {
        String firstName = f_name.getText().toString().trim();
        String lastName = l_name.getText().toString().trim();
        String email = s_email.getText().toString().trim();
        String phone = p_number.getText().toString().trim();
        String password = pass.getText().toString().trim();
        String confirmPassword = c_pass.getText().toString().trim();

        if (!validateInputs(email, password, confirmPassword, firstName, lastName, phone)) return;

        showProgressIndicator(true);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showProgressIndicator(false);
                    if (task.isSuccessful()) {
                        createUserProfile(firstName, lastName, phone, email);
                    } else {
                        showToast(task.getException() != null ? task.getException().getMessage() : "Authentication Failed");
                    }
                });
    }

    private boolean validateInputs(String email, String password, String confirmPassword, String firstName, String lastName, String phone) {
        if (email.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || imageUri == null) {
            showToast("Please fill all fields and select a profile image");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Please enter a valid email address");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            showToast("Both passwords must match!");
            return false;
        }
        if (password.length() < 6) {
            showToast("Password must be at least 6 characters");
            return false;
        }
        return true;
    }

    private void createUserProfile(String firstName, String lastName, String phone, String email) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid(); // Get user ID from Firebase Auth
            long currentTime = System.currentTimeMillis();
            UserProfile userProfile = new UserProfile(userId, firstName, lastName, phone, email, "user", currentTime, true, imageUri.toString(), "");

            db.child("users").child(userId).setValue(userProfile)
                    .addOnCompleteListener(profileTask -> {
                        if (profileTask.isSuccessful()) {
                            showToast("Signup Successful");
                            Utils.goToLogin(this);
                        } else {
                            showToast(profileTask.getException() != null ? profileTask.getException().getMessage() : "Failed to save user profile");
                        }
                    });
        } else {
            showToast("Failed to get user details");
        }
    }

    private void showProgressIndicator(boolean show) {
        blurOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profile_image.setImageURI(imageUri);
        }
    }

    public void goToHome(View view) {
        Utils.goToLanding(this);
    }

    public void goToLogin(View view) {
        Utils.goToLogin(this);
    }
}


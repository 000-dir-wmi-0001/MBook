package com.example.mbook;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.Toolbar;

import com.example.mbook.fragments.Add_Contents_Fragment;
import com.example.mbook.fragments.Admin_Analytics_Fragment;
import com.example.mbook.fragments.Admin_Manage_Fragment;
import com.example.mbook.fragments.Admin_Profile_Fragment;
import com.example.mbook.fragments.Admin_home_Fragment;
import com.example.mbook.fragments.Content_manage_Fragment;
import com.example.mbook.fragments.Content_update_Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import android.view.View;
import android.widget.TextView;

public class DashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference db; // Realtime Database instance
    private FragmentManager fragmentManager;
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize FirebaseAuth and Realtime Database
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference("users"); // Adjust the path as needed

        // Initialize FragmentManager
        fragmentManager = getSupportFragmentManager();

        // Initialize UI components
        drawerLayout = findViewById(R.id.drawer_layout);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Handle Drawer Toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Handle Bottom Navigation View item clicks
        bottomNavigationView.setOnItemSelectedListener(item -> {
            handleBottomNavigationSelection(item.getItemId());
            return true;
        });

        // Handle Navigation View item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            handleNavigationSelection(item.getItemId());
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Set default fragment if no saved state
        if (savedInstanceState == null) {
            loadFragment(new Admin_home_Fragment());
        }

        // Access header views
        View headerView = navigationView.getHeaderView(0);
        TextView headerTitle = headerView.findViewById(R.id.header_title);
        TextView headerSubtitle = headerView.findViewById(R.id.header_subtitle);

        // Set dynamic values for your header views
        String userId = mAuth.getCurrentUser().getUid();
        db.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    String userRole = dataSnapshot.child("role").getValue(String.class); // Adjust based on your data structure
//                    headerTitle.setText(mAuth.getCurrentUser().getDisplayName()); // Set user's name
//                    headerSubtitle.setText(userRole != null ? userRole : "User Role"); // Set user role
//                } else {
//                    Toast.makeText(DashboardActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
//                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DashboardActivity.this, "Error getting user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleBottomNavigationSelection(int itemId) {
        handleNavigationSelection(itemId);
    }

    private void handleNavigationSelection(int itemId) {
        Fragment fragment = null;

        if (itemId == R.id.nav_home || itemId == R.id.nav_home_) {
            fragment = new Admin_home_Fragment();
        } else if (itemId == R.id.nav_content_update) {
            fragment = new Content_update_Fragment();
        } else if (itemId == R.id.nav_analytics) {
            fragment = new Admin_Analytics_Fragment();
        } else if (itemId == R.id.nav_content) {
            fragment = new Add_Contents_Fragment();
        } else if (itemId == R.id.nav_User_manage) {
            fragment = new Admin_Manage_Fragment();
        } else if (itemId == R.id.nav_content_manage) {
            fragment = new Content_manage_Fragment();
        } else if (itemId == R.id.nav_account) {
            fragment = new Admin_Profile_Fragment();
        } else if (itemId == R.id.nav_logout) {
            handleLogout();
            return; // Skip fragment loading for logout
        }

        // Load the fragment if not null
        if (fragment != null) {
            loadFragment(fragment);
        }
    }

    public void loadFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void handleLogout() {
        // Create a confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_logout_title)
                .setMessage(R.string.confirm_logout_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    // Sign out the user
                    mAuth.signOut();
                    // Redirect to login activity
                    Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish(); // Close the current activity
                    Toast.makeText(DashboardActivity.this, R.string.logged_out_successfully, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.no, null) // Dismiss the dialog
                .show();
    }
}
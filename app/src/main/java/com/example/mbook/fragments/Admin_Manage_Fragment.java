package com.example.mbook.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mbook.R;
import com.example.mbook.adapters.UserAdapter;
import com.example.mbook.models.UserProfile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Admin_Manage_Fragment extends Fragment implements UserAdapter.OnUserActionListener {

    private RecyclerView userRecyclerView;
    private UserAdapter userAdapter;
    private List<UserProfile> userList = new ArrayList<>();
    private DatabaseReference userRef;
    private SearchView searchView;

    public Admin_Manage_Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userRef = FirebaseDatabase.getInstance().getReference("users");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin__manage_, container, false);
        initializeViews(view);
        loadUsers();
        setupSearchView();
        return view;
    }

    private void initializeViews(View view) {
        userRecyclerView = view.findViewById(R.id.user_recycler_view);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchView = view.findViewById(R.id.search_view);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return true;
            }
        });
    }

    private void loadUsers() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    UserProfile user = userSnapshot.getValue(UserProfile.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }
                updateUserAdapter(userList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Failed to load users.");
            }
        });
    }

    private void updateUserAdapter(List<UserProfile> users) {
        userAdapter = new UserAdapter(users, getContext(), this);
        userRecyclerView.setAdapter(userAdapter);
    }

    private void filterUsers(String query) {
        List<UserProfile> filteredUsers = new ArrayList<>();
        if (TextUtils.isEmpty(query)) {
            filteredUsers.addAll(userList);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (UserProfile user : userList) {
                if (matchesFilter(user, filterPattern)) {
                    filteredUsers.add(user);
                }
            }
        }
        updateUserAdapter(filteredUsers);
    }

    private boolean matchesFilter(UserProfile user, String filterPattern) {
        return user.getFirstName().toLowerCase().contains(filterPattern) ||
                user.getLastName().toLowerCase().contains(filterPattern) ||
                user.getEmail().toLowerCase().contains(filterPattern);
    }

    @Override
    public void onEditClick(UserProfile user) {
        showEditDialog(user);
    }

    @Override
    public void onDeleteClick(UserProfile user) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete this user?")
                .setPositiveButton("Yes", (dialog, which) -> deleteUser(user))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteUser(UserProfile user) {
        userRef.child(user.getId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    showToast("User deleted successfully");
                    loadUsers(); // Refresh the user list
                })
                .addOnFailureListener(e -> showToast("Failed to delete user: " + e.getMessage()));
    }

    private void showEditDialog(UserProfile user) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_user, null);
        populateEditDialog(dialogView, user);

        new AlertDialog.Builder(getContext())
                .setTitle("Edit User Information")
                .setView(dialogView)
                .setPositiveButton("Update", (dialogInterface, i) -> updateUser(user, dialogView))
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void populateEditDialog(View dialogView, UserProfile user) {
        ((EditText) dialogView.findViewById(R.id.edit_first_name)).setText(user.getFirstName());
        ((EditText) dialogView.findViewById(R.id.edit_last_name)).setText(user.getLastName());
        ((EditText) dialogView.findViewById(R.id.edit_phone)).setText(user.getPhone());
        ((EditText) dialogView.findViewById(R.id.edit_email)).setText(user.getEmail());
        ((EditText) dialogView.findViewById(R.id.edit_role)).setText(user.getRole());
    }

    private void updateUser(UserProfile user, View dialogView) {
        String updatedFirstName = ((EditText) dialogView.findViewById(R.id.edit_first_name)).getText().toString().trim();
        String updatedLastName = ((EditText) dialogView.findViewById(R.id.edit_last_name)).getText().toString().trim();
        String updatedPhone = ((EditText) dialogView.findViewById(R.id.edit_phone)).getText().toString().trim();
        String updatedEmail = ((EditText) dialogView.findViewById(R.id.edit_email)).getText().toString().trim();
        String updatedRole = ((EditText) dialogView.findViewById(R.id.edit_role)).getText().toString().trim();

        UserProfile updatedUser = new UserProfile(
                user.getId(),
                updatedFirstName,
                updatedLastName,
                updatedPhone,
                updatedEmail,
                updatedRole,
                System.currentTimeMillis(),
                user.isActive(),
                user.getProfileImage(),
                user.getUserProfile()
        );

        userRef.child(user.getId()).setValue(updatedUser)
                .addOnSuccessListener(aVoid -> {
                    showToast("User updated successfully");
                    loadUsers(); // Refresh the user list
                })
                .addOnFailureListener(e -> showToast("Failed to update user: " + e.getMessage()));
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}

package com.example.powersaver.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.powersaver.activity.dashboard.DashboardActivity;
import com.example.powersaver.activity.devicemanager.DeviceManagerActivity;
import com.example.powersaver.controllers.DatabaseHelper;
import com.example.powersaver.R;
import com.example.powersaver.objects.User;

public class UpdateDetailsActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etName, etEmail;
    private Button btnSaveChanges;
    private DatabaseHelper dbHelper;
    private String currentUsername;  // Store the current username
    private Integer currentUserID;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_details);

        // Set up the toolbar for the 3-dot menu
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set the toolbar title to "Account Details"
        getSupportActionBar().setTitle("Account Details");

        // Initialize views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etName = findViewById(R.id.etName);  // Add name input
        etEmail = findViewById(R.id.etEmail); // Add email input
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        dbHelper = new DatabaseHelper(this);

        // Retrieve the current username from the intent
        currentUsername = getIntent().getStringExtra("username");
        currentUserID = getIntent().getIntExtra("userID", 0);
        user = dbHelper.getUserByUsername(currentUsername);

        //populates textviews with current data
        etEmail.setText(user.getEmail());
        etUsername.setText(user.getUsername());
        etName.setText(user.getName());
        etPassword.setText(user.getPassword());
        //Will check contents of fields and update user details
        btnSaveChanges.setOnClickListener(v -> {
            String newUsername = etUsername.getText().toString().trim();
            String newPassword = etPassword.getText().toString().trim();
            String newName = etName.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();

            if (!newUsername.isEmpty() && !newPassword.isEmpty() && !newName.isEmpty() && !newEmail.isEmpty()) {
                // Update user details (username, password, name, and email)
                User updatedUser=new User(user.getUserID(),newUsername,newPassword,newName,newEmail,0);
                boolean isUpdated = dbHelper.updateUserAccountDetails(
                        currentUsername,updatedUser
                );

                if (isUpdated) {
                    Toast.makeText(UpdateDetailsActivity.this, "Details updated successfully", Toast.LENGTH_SHORT).show();
                    finish();  // Close the activity and return to the dashboard
                } else {
                    Toast.makeText(UpdateDetailsActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(UpdateDetailsActivity.this, "No fields can be left blank.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu for the 3-dot menu in Account Details
        getMenuInflater().inflate(R.menu.menu_account_details, menu);
        return true;
    }
    //Handles top menu options
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_dashboard) {
            // Navigate to the Dashboard and pass the current username
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("username", currentUsername);  // Pass the current username to Dashboard
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_manage_devices) {
            // Navigate to the Device Manager
            Intent intent = new Intent(this, DeviceManagerActivity.class);
            intent.putExtra("username", currentUsername);  // Pass the current username
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_logout) {
            // Handle Logout
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }

        if (id == R.id.action_close) {
            // Handle Close App action
            finishAffinity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

package com.example.powersaver.activity.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.powersaver.controllers.DatabaseHelper;
import com.example.powersaver.R;

public class AdminRegisterActivity extends AppCompatActivity {

    private EditText etAdminUsername, etAdminPassword, etAdminName, etAdminEmail;
    private Button registerAdminButton, btnBack;  // Back button
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_register);

        // Initialize views
        etAdminUsername = findViewById(R.id.adminUsername);
        etAdminPassword = findViewById(R.id.adminPassword);
        etAdminName = findViewById(R.id.adminName);  // Initialize admin name field
        etAdminEmail = findViewById(R.id.adminEmail);  // Initialize admin email field
        registerAdminButton = findViewById(R.id.registerAdminButton);
        btnBack = findViewById(R.id.btnBack);  // Initialize the back button

        // Initialize the database helper
        dbHelper = new DatabaseHelper(this);

        // Set OnClickListener for registerAdminButton
        registerAdminButton.setOnClickListener(v -> {
            String username = etAdminUsername.getText().toString().trim();
            String password = etAdminPassword.getText().toString().trim();
            String name = etAdminName.getText().toString().trim();  // Get name
            String email = etAdminEmail.getText().toString().trim();  // Get email

            if (!username.isEmpty() && !password.isEmpty() && !name.isEmpty() && !email.isEmpty()) {
                // Insert admin account into the database
                boolean isInserted = dbHelper.addUser(username, password, true, name, email);  // 'true' for isAdmin

                if (isInserted) {
                    Toast.makeText(AdminRegisterActivity.this, "Admin account created successfully", Toast.LENGTH_SHORT).show();
                    // Redirect to admin dashboard
                    Intent intent = new Intent(AdminRegisterActivity.this, AdminDashboardActivity.class);
                    startActivity(intent);
                    finish();  // Close registration page
                } else {
                    Toast.makeText(AdminRegisterActivity.this, "Error creating admin account", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(AdminRegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });

        // Back button click listener
        btnBack.setOnClickListener(v -> finish());  // Go back to the previous page
    }
}

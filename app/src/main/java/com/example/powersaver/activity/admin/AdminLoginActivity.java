package com.example.powersaver.activity.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.powersaver.activity.login.LoginSelectionActivity;
import com.example.powersaver.controllers.DatabaseHelper;
import com.example.powersaver.R;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText etAdminUsername, etAdminPassword;
    private Button adminLoginButton, registerAdminButton, backButton, btnAdminForgotPassword;
    private TextView adminLoginLabel;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        // Initialize views
        etAdminUsername = findViewById(R.id.adminUsername);
        etAdminPassword = findViewById(R.id.adminPassword);
        adminLoginButton = findViewById(R.id.adminLoginButton);
        registerAdminButton = findViewById(R.id.registerAdminButton);
        backButton = findViewById(R.id.back_button);
        btnAdminForgotPassword = findViewById(R.id.btnAdminForgotPassword); // Forgot Password Button
        adminLoginLabel = findViewById(R.id.adminLoginLabel);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Check if an admin account already exists
        boolean isAdminExists = dbHelper.isAdminExists();

        if (isAdminExists) {
            adminLoginLabel.setText("Admin account already exists. Please contact admin for modifications.");
            registerAdminButton.setVisibility(View.GONE);  // Hide register button
        } else {
            adminLoginLabel.setText("Please create an admin account for the first user.");
            registerAdminButton.setVisibility(View.VISIBLE);  // Show register button
        }

        // Set OnClickListener for admin login button
        adminLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etAdminUsername.getText().toString().trim();
                String password = etAdminPassword.getText().toString().trim();

                if (!username.isEmpty() && !password.isEmpty()) {
                    boolean isAuthenticated = dbHelper.checkAdminLogin(username, password);
                    if (isAuthenticated) {
                        // Admin login successful, navigate to AdminDashboardActivity
                        Intent intent = new Intent(AdminLoginActivity.this, AdminDashboardActivity.class);
                        intent.putExtra("username", username);
                        startActivity(intent);
                    } else {
                        Toast.makeText(AdminLoginActivity.this, "Invalid Admin Credentials", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AdminLoginActivity.this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set OnClickListener for registerAdminButton (Create Admin Account)
        registerAdminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminLoginActivity.this, AdminRegisterActivity.class);
                startActivity(intent);
            }
        });

        // Set OnClickListener for Forgot Password Button
        btnAdminForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to Admin Forgot Password Page
                Intent intent = new Intent(AdminLoginActivity.this, AdminForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        // Set OnClickListener for backButton (Go back to login selection)
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminLoginActivity.this, LoginSelectionActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}

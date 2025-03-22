package com.example.powersaver.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.powersaver.activity.dashboard.DashboardActivity;
import com.example.powersaver.controllers.DatabaseHelper;
import com.example.powersaver.R;

public class LoginActivity extends AppCompatActivity {

    private EditText etLoginUsername, etLoginPassword;
    private Button loginButton, registerButton, backButton, btnForgotPassword;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        etLoginUsername = findViewById(R.id.username);
        etLoginPassword = findViewById(R.id.password);
        loginButton = findViewById(R.id.login);
        registerButton = findViewById(R.id.register);
        backButton = findViewById(R.id.backButton);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);  // Initialize Forgot Password button

        // Initialize the database helper
        dbHelper = new DatabaseHelper(this);

        // Set OnClickListener for login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etLoginUsername.getText().toString().trim();
                String password = etLoginPassword.getText().toString().trim();
//
                // Check if username and password are not empty
                if (!username.isEmpty() && !password.isEmpty()) {
                    boolean isAuthenticated = dbHelper.checkUser(username, password);
                    if (isAuthenticated) {
                        int UserID=dbHelper.getCheckedUserID(username,password);
                        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                        intent.putExtra("username", username);
                        intent.putExtra("isAdmin", false);
                        intent.putExtra("userID",UserID);
                        startActivity(intent);
                        finish();  // Prevent going back to login page after logging in
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set OnClickListener for register button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // Set OnClickListener for back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, LoginSelectionActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Set OnClickListener for Forgot Password button
        btnForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }
}

package com.example.powersaver.activity.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.powersaver.controllers.DatabaseHelper;
import com.example.powersaver.R;

public class AdminForgotPasswordActivity extends AppCompatActivity {

    private EditText etUsername, etEmail;
    private Button btnValidateInfo, btnBack;  // Added a Back button
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_forgot_password);

        // Initialize views
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        btnValidateInfo = findViewById(R.id.btnValidateInfo);
        btnBack = findViewById(R.id.btnBack);  // Initialize the back button

        dbHelper = new DatabaseHelper(this);

        // Handle the Validate Info button click
        btnValidateInfo.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            if (!username.isEmpty() && !email.isEmpty()) {
                //Handles check if userInfo i validated
                boolean isValid = dbHelper.validateUserInfo(username, email);

                if (isValid) {
                    Intent intent = new Intent(AdminForgotPasswordActivity.this, AdminResetPasswordActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                } else {
                    Toast.makeText(AdminForgotPasswordActivity.this, "Incorrect information", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(AdminForgotPasswordActivity.this, "Please fill in all details", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle the Back button click
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(AdminForgotPasswordActivity.this, AdminLoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();  // Close the current activity
        });
    }
}

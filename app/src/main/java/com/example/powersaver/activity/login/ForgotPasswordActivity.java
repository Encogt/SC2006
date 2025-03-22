package com.example.powersaver.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.powersaver.controllers.DatabaseHelper;
import com.example.powersaver.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etUsername, etEmail;
    private Button btnValidateInfo, btnBack;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize views
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        btnValidateInfo = findViewById(R.id.btnValidateInfo);
        btnBack = findViewById(R.id.btnBack);  // Initialize back button
        dbHelper = new DatabaseHelper(this);
        //Trigger and handle validation of user input via dbhelper
        btnValidateInfo.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            if (!username.isEmpty() && !email.isEmpty()) {
                boolean isValid = dbHelper.validateUserInfo(username, email);

                if (isValid) {
                    Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Incorrect information", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ForgotPasswordActivity.this, "Please fill in all details", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle Back button click
        btnBack.setOnClickListener(v -> {
            // Go back to login page
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}

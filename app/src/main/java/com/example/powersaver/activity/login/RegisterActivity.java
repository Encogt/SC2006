package com.example.powersaver.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.powersaver.controllers.DatabaseHelper;
import com.example.powersaver.R;
import com.example.powersaver.objects.User;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etName, etEmail;
    private Button btnRegister, btnBack;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etName = findViewById(R.id.etName);  // Initialize name field
        etEmail = findViewById(R.id.etEmail);  // Initialize email field
        btnRegister = findViewById(R.id.btnRegister);
        btnBack = findViewById(R.id.btnBack);

        // Initialize the database helper
        dbHelper = new DatabaseHelper(this);

        // Set OnClickListener for register button
        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String name = etName.getText().toString().trim();  // Get name
            String email = etEmail.getText().toString().trim();  // Get email

            if (!username.isEmpty() && !password.isEmpty() && !name.isEmpty() && !email.isEmpty()) {
                // Insert user account into the database
                User newUser= new User(username, password, name, email,0,"","");// 'false' for non-admin
                boolean isInserted = dbHelper.addUser(newUser);

                if (isInserted) {
                    Toast.makeText(RegisterActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                    // Redirect to login page
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();  // Close registration page
                } else {
                    Toast.makeText(RegisterActivity.this, "Error creating account", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });

        // Back button click listener
        btnBack.setOnClickListener(v -> finish());  // Go back to the previous page
    }
}

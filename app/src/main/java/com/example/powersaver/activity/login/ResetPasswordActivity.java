package com.example.powersaver.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.powersaver.controllers.DatabaseHelper;
import com.example.powersaver.R;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText etNewPassword;
    private Button btnResetPassword;
    private DatabaseHelper dbHelper;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Initialize views
        etNewPassword = findViewById(R.id.etNewPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        dbHelper = new DatabaseHelper(this);

        // Retrieve username from intent
        username = getIntent().getStringExtra("username");

        // Set OnClickListener for reset password button
        btnResetPassword.setOnClickListener(v -> {
            String newPassword = etNewPassword.getText().toString().trim();

            if (!newPassword.isEmpty()) {
                boolean isUpdated = dbHelper.updatePassword(username, newPassword);

                if (isUpdated) {
                    Toast.makeText(ResetPasswordActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                    // Redirect to login page
                    Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();  // Close reset password page
                } else {
                    Toast.makeText(ResetPasswordActivity.this, "Error updating password", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ResetPasswordActivity.this, "Please enter a new password", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

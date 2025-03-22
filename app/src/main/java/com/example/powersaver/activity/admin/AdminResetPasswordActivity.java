package com.example.powersaver.activity.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.powersaver.controllers.DatabaseHelper;
import com.example.powersaver.R;

public class AdminResetPasswordActivity extends AppCompatActivity {

    private EditText etNewPassword, etConfirmPassword;
    private Button btnResetPassword;
    private DatabaseHelper dbHelper;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reset_password);  // Link to the new XML layout for reset password

        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        dbHelper = new DatabaseHelper(this);

        // Retrieve the username from the Intent
        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        btnResetPassword.setOnClickListener(v -> {
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (!newPassword.isEmpty() && !confirmPassword.isEmpty()) {
                if (newPassword.equals(confirmPassword)) {
                    // Update password in the database
                    boolean isUpdated = dbHelper.updatePassword(username, newPassword);

                    if (isUpdated) {
                        Toast.makeText(AdminResetPasswordActivity.this, "Password reset successfully", Toast.LENGTH_SHORT).show();
                        // Redirect to admin login page after successful reset
                        Intent loginIntent = new Intent(AdminResetPasswordActivity.this, AdminLoginActivity.class);
                        startActivity(loginIntent);
                        finish();
                    } else {
                        Toast.makeText(AdminResetPasswordActivity.this, "Password reset failed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AdminResetPasswordActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(AdminResetPasswordActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

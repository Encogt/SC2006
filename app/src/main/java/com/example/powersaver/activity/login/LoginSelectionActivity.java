package com.example.powersaver.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.powersaver.R;
import com.example.powersaver.activity.admin.AdminLoginActivity;

public class LoginSelectionActivity extends AppCompatActivity {

    private Button userLoginButton, adminLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_selection);

        userLoginButton = findViewById(R.id.user_login_button);
        adminLoginButton = findViewById(R.id.admin_login_button);

        // Navigate to User Login
        userLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginSelectionActivity.this, LoginActivity.class);  // For user login
                startActivity(intent);
            }
        });
        //Navigate to Admin Login
        adminLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginSelectionActivity.this, AdminLoginActivity.class);  // For admin login
                startActivity(intent);
            }
        });
    }
}

package com.example.powersaver.activity.devicemanager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.example.powersaver.R;
import com.example.powersaver.activity.dashboard.DashboardActivity;
import com.example.powersaver.activity.login.LoginActivity;
import com.example.powersaver.activity.login.UpdateDetailsActivity;
import com.example.powersaver.databinding.ActivityDevicemanagerBinding;

public class DeviceManagerActivity extends AppCompatActivity {
    private ActivityDevicemanagerBinding binding;
    private String currentUsername;  // Pass username from DashboardActivity or other activities
    private int currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the username from the intent that started this activity
        currentUsername = getIntent().getStringExtra("username");
        currentUserID=getIntent().getIntExtra("userID",0);
        //launches devicemanager in the form of a fragment
        binding = ActivityDevicemanagerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.toolbar.setBackgroundColor(Color.WHITE);
        binding.toolbar.setTitleTextColor(Color.BLACK);
        binding.toolbar.setTitle("Device Manager");
        setSupportActionBar(binding.toolbar);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device_manager, menu);
        return true;
    }
//Handles menu options
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_dashboard) {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("username", currentUsername);  // Pass the username back to the DashboardActivity
            intent.putExtra("userID",currentUserID);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_account_details) {
            Intent intent = new Intent(this, UpdateDetailsActivity.class);
            intent.putExtra("username", currentUsername);  // Pass the username to the Account Details
            intent.putExtra("userID",currentUserID);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_logout) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        if (id == R.id.action_close) {
            this.finishAffinity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

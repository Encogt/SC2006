package com.example.powersaver.activity.admin;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.powersaver.controllers.DatabaseHelper;
import com.example.powersaver.R;

import java.util.ArrayList;

public class AdminDashboardActivity extends AppCompatActivity {

    private ListView userListView;
    private Button logoutButton, editApplianceButton; // Removed dashboardButton, added editApplianceButton
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize views
        userListView = findViewById(R.id.user_list_view);
        logoutButton = findViewById(R.id.logout_button);
        editApplianceButton = findViewById(R.id.edit_appliance_button); // Initialize the edit appliance button

        //Initialize DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Fetch user data from SQLite and display it in ListView
        ArrayList<String> userList = new ArrayList<>();
        Cursor cursor = dbHelper.getAllUsers();

        try { //Retrieve user list
            if (cursor != null && cursor.moveToFirst()) {
                int usernameIndex = cursor.getColumnIndex("username");
                if (usernameIndex >= 0) {
                    do {
                        String username = cursor.getString(usernameIndex);
                        userList.add(username);
                    } while (cursor.moveToNext());
                } else {
                    Toast.makeText(this, "Username column not found!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No users found!", Toast.LENGTH_SHORT).show();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        // Display the list of users in the ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userList);
        userListView.setAdapter(adapter);

        // Set OnClickListener for the Edit/Set Appliance button
        editApplianceButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, EditApplianceActivity.class);
            startActivity(intent);  // Navigate to EditApplianceActivity for appliance management
        });

        // Set OnClickListener for the Logout button
        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            startActivity(intent);
            finish();
        });
    }
}

package com.example.powersaver.activity.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.powersaver.controllers.ApplianceAdapter;
import com.example.powersaver.controllers.DatabaseHelper;
import com.example.powersaver.R;
import com.example.powersaver.objects.Appliance;

import java.util.ArrayList;

public class EditApplianceActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private EditText applianceNameEditText, powerRatingEditText;
    private Button saveButton, addButton;
    private RecyclerView applianceRecyclerView;
    private ApplianceAdapter applianceAdapter;
    private ArrayList<Appliance> applianceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_appliance);

        // Initialize UI elements
        applianceNameEditText = findViewById(R.id.appliance_name_edit_text);
        powerRatingEditText = findViewById(R.id.power_rating_edit_text);
        saveButton = findViewById(R.id.save_button);
        addButton = findViewById(R.id.add_button);
        applianceRecyclerView = findViewById(R.id.appliance_list_view);

        // Initialize DatabaseHelper and fetch appliances
        dbHelper = new DatabaseHelper(this);
        applianceList = dbHelper.getAllAppliances();

        // Set up RecyclerView with the appliance adapter
        applianceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        applianceAdapter = new ApplianceAdapter(this, applianceList, dbHelper);
        applianceRecyclerView.setAdapter(applianceAdapter);

        // Set click listeners
        addButton.setOnClickListener(v -> addNewAppliance());
        saveButton.setOnClickListener(v -> saveAppliances());
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish()); // Finishes the current activity, going back to the previous one
    }

    private void addNewAppliance() {
        String name = applianceNameEditText.getText().toString().trim();
        String powerRatingStr = powerRatingEditText.getText().toString().trim();

        // Validation check for appliance name and power rating input
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(powerRatingStr)) {
            Toast.makeText(this, "Please enter appliance name and power rating", Toast.LENGTH_SHORT).show();
            return;
        }

        int powerRating;
        try {
            powerRating = Integer.parseInt(powerRatingStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Power rating must be a number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new appliance and add it to the database and list
        Appliance appliance = new Appliance(0, name, powerRating);
        dbHelper.addOrUpdateAppliance(appliance);
        applianceList.add(appliance);
        applianceAdapter.notifyDataSetChanged();

        // Clear input fields after adding the appliance
        applianceNameEditText.setText("");
        powerRatingEditText.setText("");
    }

    //Calls db helper to save all appliances in the list
    private void saveAppliances() {
        for (Appliance appliance : applianceList) {
            dbHelper.addOrUpdateAppliance(appliance);
        }
        Toast.makeText(this, "Appliances saved successfully!", Toast.LENGTH_SHORT).show();
    }
}

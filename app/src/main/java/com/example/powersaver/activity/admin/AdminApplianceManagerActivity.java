package com.example.powersaver.activity.admin;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.powersaver.controllers.ApplianceAdapter;
import com.example.powersaver.controllers.DatabaseHelper;
import com.example.powersaver.R;
import com.example.powersaver.objects.Appliance;

import java.util.ArrayList;

public class AdminApplianceManagerActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ApplianceAdapter adapter;
    private DatabaseHelper dbHelper;
    private ArrayList<Appliance> applianceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_appliance_manager);

        dbHelper = new DatabaseHelper(this);
        applianceList = dbHelper.getAllAppliances();  // Fetch appliances from DB

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter with context, appliance list, and dbHelper
        adapter = new ApplianceAdapter(this, applianceList, dbHelper);
        recyclerView.setAdapter(adapter);

        // Add Appliance Button
        Button addApplianceButton = findViewById(R.id.addApplianceButton);
        addApplianceButton.setOnClickListener(v -> showAddOrEditDialog(null)); // Add new appliance
    }

    // Show dialog for adding or editing an appliance
    private void showAddOrEditDialog(Appliance appliance) {
        // Dialog logic for adding or editing appliance
    }
}

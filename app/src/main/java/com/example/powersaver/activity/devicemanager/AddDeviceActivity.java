package com.example.powersaver.activity.devicemanager;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.powersaver.controllers.DatabaseHelper;
import com.example.powersaver.R;
import com.example.powersaver.objects.Appliance;
import com.example.powersaver.objects.Device;

import java.util.ArrayList;
import java.util.List;

public class AddDeviceActivity extends AppCompatActivity {

    private Spinner deviceSpinner;
    private EditText customDeviceNameEditText;
    private EditText powerUsageEditText;
    private EditText durationHoursEditText;   // Hours input field
    private EditText durationMinutesEditText; // Minutes input field
    private Button addButton, discardButton;
    private DatabaseHelper dbHelper;
    private List<Appliance> applianceList;
    private ArrayAdapter<String> deviceAdapter;
    private boolean isEditMode = false;  // Flag to track if we're in edit mode
    private int deviceID;  // Track device ID for editing
    private int userID;
    private Appliance editSavedApplianceData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);

        // Initialize UI elements
        deviceSpinner = findViewById(R.id.deviceSpinner);
        customDeviceNameEditText = findViewById(R.id.customDeviceNameEditText);
        powerUsageEditText = findViewById(R.id.powerUsageEditText);
        durationHoursEditText = findViewById(R.id.durationHoursEditText);
        durationMinutesEditText = findViewById(R.id.durationMinutesEditText);
        addButton = findViewById(R.id.addButton);
        discardButton = findViewById(R.id.discardButton);

        dbHelper = new DatabaseHelper(this);

        //Gets user id
        userID=getIntent().getIntExtra("userID",0);

        // Check if we are in edit mode
        if (getIntent().getBooleanExtra("isEditMode", false)) {
            isEditMode = true;
            deviceID = getIntent().getIntExtra("deviceID", -1);
            setUpEditMode();
        }

        // Load appliances from the database and set up the spinner
        loadAppliancesFromDatabase();



        // Handle appliance selection in Spinner
        deviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    Appliance selectedAppliance = applianceList.get(position);
                    powerUsageEditText.setText(String.valueOf(selectedAppliance.getPowerRating()));

                if(position==applianceList.size()-1) //Custom will always be at the bottom
                 {
                     powerUsageEditText.setEnabled(true);
                 }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });
//Adds action to the button to either add or update device
        addButton.setOnClickListener(v -> {
            if (isEditMode) {
                updateDeviceInDatabase();
            } else {
                addDeviceToDatabase();
            }
        });

        discardButton.setOnClickListener(v -> finish());
    }
//populates spinner with appliances from database
    private void loadAppliancesFromDatabase() {
        applianceList = dbHelper.getAllAppliances();
        List<String> applianceNames = new ArrayList<>();

        if(isEditMode&& !(editSavedApplianceData ==null))
        {
            applianceList.add(editSavedApplianceData);
        }
        else {
            //Creates a custom Appliance to add to the list that serves as a custom device
            Appliance custom = new Appliance(0, "Custom Device", 0);
            applianceList.add(custom);
        }
        for (Appliance appliance : applianceList) {
            applianceNames.add(appliance.getName());
        }

        deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, applianceNames);
        deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deviceSpinner.setAdapter(deviceAdapter);
        if(isEditMode)
        {
            deviceSpinner.setSelection(applianceList.size()-1); //Default it to custom
        }
    }
//inputs edit device into text edits to be ready for modifying
    private void setUpEditMode() {
        // Retrieve and populate fields for editing
        String deviceName = getIntent().getStringExtra("deviceName");
        int duration = getIntent().getIntExtra("duration", 0);
        int hours = duration / 60;
        int minutes = duration % 60;
        int usage=getIntent().getIntExtra("usage",0);

        customDeviceNameEditText.setText(deviceName);
        durationHoursEditText.setText(String.valueOf(hours));
        durationMinutesEditText.setText(String.valueOf(minutes));

        editSavedApplianceData=new Appliance(0,"Previous Data",usage);
        // Disable editing for power usage
        //powerUsageEditText.setVisibility(View.GONE);
        //deviceSpinner.setEnabled(false);
        addButton.setText("Save Device");
    }
//verify contents of entry before adding device to db Via dbhelper
    private void addDeviceToDatabase() {
        String selectedDeviceName = deviceSpinner.getSelectedItem().toString();  // Original appliance name
        String customDeviceName = customDeviceNameEditText.getText().toString().trim();  // Custom name entered by user

        // Use custom name if provided, otherwise fallback to selected device name
        String deviceNameToSave = customDeviceName.isEmpty() ? selectedDeviceName : customDeviceName;

        String hours = durationHoursEditText.getText().toString();
        String minutes = durationMinutesEditText.getText().toString();

        if (hours.isEmpty() && minutes.isEmpty()) {
            Toast.makeText(this, "Please enter duration for hours or minutes", Toast.LENGTH_SHORT).show();
            return;
        }


        int durationHours = hours.isEmpty() ? 0 : Integer.parseInt(hours);
        int durationMinutes = minutes.isEmpty() ? 0 : Integer.parseInt(minutes);
        int totalMinutes = (durationHours * 60) + durationMinutes;
        if (totalMinutes>1440)
        {
            Toast.makeText(this, "Please enter duration less than 24 hours", Toast.LENGTH_SHORT).show();
            return;
        }

        //Appliance selectedAppliance = applianceList.get(deviceSpinner.getSelectedItemPosition());
        if(powerUsageEditText.getText().toString().isEmpty())
        {
            Toast.makeText(this, "Device power usage cannot be blank", Toast.LENGTH_SHORT).show();
            return;
        }
        int powerUsage = Integer.parseInt(powerUsageEditText.getText().toString());


        // Save the device with the custom name or the selected name
        Device newDevice = new Device(deviceNameToSave, powerUsage, totalMinutes,userID);

        boolean success = dbHelper.addDevice(newDevice);
        if (success) {
            Toast.makeText(this, "Device added successfully!", Toast.LENGTH_SHORT).show();
            finish();  // Close the activity after saving the device
        } else {
            Toast.makeText(this, "Error adding device", Toast.LENGTH_SHORT).show();
        }
    }
    //verify contents of entry before updating device in db Via dbhelper
    private void updateDeviceInDatabase() {
        String customDeviceName = customDeviceNameEditText.getText().toString();
        String hours = durationHoursEditText.getText().toString();
        String minutes = durationMinutesEditText.getText().toString();
        String usage = powerUsageEditText.getText().toString();

        if (hours.isEmpty() && minutes.isEmpty()) {
            Toast.makeText(this, "Please enter duration for hours or minutes", Toast.LENGTH_SHORT).show();
            return;
        }



        int durationHours = hours.isEmpty() ? 0 : Integer.parseInt(hours);
        int durationMinutes = minutes.isEmpty() ? 0 : Integer.parseInt(minutes);
        int totalMinutes = (durationHours * 60) + durationMinutes;
        int powerUsage = Integer.parseInt(usage);
        if (totalMinutes>1440)
        {
            Toast.makeText(this, "Please enter duration less than 24 hours", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = dbHelper.updateDeviceDuration(deviceID, customDeviceName, totalMinutes,powerUsage);
        if (success) {
            Toast.makeText(this, "Device updated successfully!", Toast.LENGTH_SHORT).show();
            finish();  // Close the activity after saving the device
        } else {
            Toast.makeText(this, "Error updating device", Toast.LENGTH_SHORT).show();
        }
    }
}
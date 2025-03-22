package com.example.powersaver.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.powersaver.controllers.DatabaseHelper;
import com.example.powersaver.R;
import com.example.powersaver.activity.devicemanager.AddDeviceActivity;
import com.example.powersaver.databinding.FragmentDevicemanagerBinding;
import com.example.powersaver.objects.Device;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class DeviceManagerFragment extends Fragment {

    private FragmentDevicemanagerBinding binding;
    private ArrayList<Device> deviceList;
    private DeviceInfoAdapter deviceAdapter;
    private int userID;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentDevicemanagerBinding.inflate(inflater, container, false);
        deviceList = new ArrayList<>();
        //getsUserID
        userID=getActivity().getIntent().getIntExtra("userID",0);
        // Initialize the RecyclerView
        RecyclerView recyclerView = binding.deviceManagerTableBody;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize and set the DeviceInfoAdapter
        deviceAdapter = new DeviceInfoAdapter(deviceList, getContext());
        // Load devices from the database
        loadDevicesFromDatabase();
        recyclerView.setAdapter(deviceAdapter);

        // Load devices from the database
        //loadDevicesFromDatabase();



        // Set up Floating Action Button for adding new devices
        FloatingActionButton addDeviceFab = binding.fab;
        addDeviceFab.setAlpha(0.75f);
        addDeviceFab.setOnClickListener(view -> {
            // Open AddDeviceActivity when the FAB is clicked
            Intent intent = new Intent(getContext(), AddDeviceActivity.class);
            intent.putExtra("userID",userID);
            startActivityForResult(intent, 1);
        });

        return binding.getRoot();
    }

    // Reload the device list every time the fragment is resumed
    @Override
    public void onResume() {
        super.onResume();
        loadDevicesFromDatabase();  // Re-fetch devices from the database
    }

    // Load devices from the database and display them in the RecyclerView
    private void loadDevicesFromDatabase() {
        deviceList.clear();  // Clear the list before reloading

        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        Cursor cursor = dbHelper.getAllDevices();  // Fetch all devices from the database
        ArrayList<Device> deviceListNew = new ArrayList<>();
        deviceListNew=dbHelper.getDevicesByUser(userID);
        deviceList.addAll(deviceListNew);
        // Notify the adapter that the data has changed so the RecyclerView can update
        deviceAdapter.notifyDataSetChanged();

    }

    // Handle result when a new device is added
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == getActivity().RESULT_OK) {
            // Reload the devices from the database after adding a new one
            loadDevicesFromDatabase();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // RecyclerView Adapter for displaying devices
    public class DeviceInfoAdapter extends RecyclerView.Adapter<DeviceInfoAdapter.ViewHolder> {

        private final ArrayList<Device> deviceList;
        private final Context context;

        public DeviceInfoAdapter(ArrayList<Device> deviceList, Context context) {
            this.deviceList = deviceList;
            this.context = context;
        }


        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_devicemanager_layout, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Device deviceInfo = deviceList.get(position);

            // Set device name and usage
            holder.deviceNameTextView.setText(deviceInfo.getDeviceName());
            holder.deviceNameTextView.setEllipsize(TextUtils.TruncateAt.END);
            holder.usageTextView.setText(deviceInfo.getUsage() + "");

            // Get total minutes from deviceInfo (assuming duration is stored in minutes)
            int totalMinutes = deviceInfo.getDuration();

            // Calculate hours and minutes from the total minutes
            int hours = totalMinutes / 60;
            int minutes = totalMinutes % 60;

            // Display hours and minutes in the correct format
            String durationText;
            if (hours > 0 && minutes > 0) {
                durationText = hours + " H " + minutes + " M";
            } else if (hours > 0) {
                durationText = hours + " H";
            } else if (minutes == 1) {
                durationText = minutes + " M";
            } else if (minutes > 1) {
                durationText = minutes + " M";
            } else {
                durationText = "0 M";  // If neither, just show 0 minutes
            }

            holder.durationTextView.setText(durationText);  // Make sure this is set correctly


            // Handle remove button click
            holder.removeButton.setOnClickListener(v -> {
                String deviceName = deviceInfo.getDeviceName();  // Get the device name to delete
                int deviceID=deviceInfo.getDeviceID();
                DatabaseHelper dbHelper = new DatabaseHelper(context);
                boolean success = dbHelper.removeDevice(deviceID);  // Delete from the database
                if (success) {
                    // Remove the device from the list and update the adapter
                    deviceList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, deviceList.size());
                    Toast.makeText(context, "Device removed: " + deviceName, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to remove device: " + deviceName, Toast.LENGTH_SHORT).show();
                }
            });

            //Handle edit button click
            holder.editButton.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), AddDeviceActivity.class);
                intent.putExtra("deviceID", deviceInfo.getDeviceID());
                intent.putExtra("deviceName", deviceInfo.getDeviceName());
                intent.putExtra("duration", deviceInfo.getDuration());
                intent.putExtra("usage", deviceInfo.getUsage());
                intent.putExtra("isEditMode", true);  // Flag to indicate edit mode
                startActivityForResult(intent, 1);  // Open AddDeviceActivity in edit mode
            });
        }

        @Override
        public int getItemCount() {
            return deviceList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView deviceNameTextView;
            private final TextView usageTextView;
            private final TextView durationTextView;
            private final Button removeButton;
            private final Button editButton;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                deviceNameTextView = itemView.findViewById(R.id.deviceNameTextView);
                usageTextView = itemView.findViewById(R.id.UsageTextView);
                durationTextView = itemView.findViewById(R.id.durationTextView);
                removeButton = itemView.findViewById(R.id.removeButton);  // Make sure this ID matches your layout
                editButton=itemView.findViewById(R.id.editDeviceBtn);
            }
        }
    }
}
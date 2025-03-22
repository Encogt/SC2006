package com.example.powersaver.controllers;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.powersaver.R;
import com.example.powersaver.objects.Device;

import java.util.List;

public class DeviceInfoAdapter extends RecyclerView.Adapter<DeviceInfoAdapter.ViewHolder> {

    private final List<Device> deviceList;

    public DeviceInfoAdapter(List<Device> deviceList) {
        this.deviceList = deviceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_devicemanager_layout, parent, false);
        return new ViewHolder(view);
    }
//Creates every item displayed at DeviceManager table
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Device deviceInfo = deviceList.get(position);

        // Set device name and usage
        holder.deviceNameTextView.setText(deviceInfo.getDeviceName());
        holder.usageTextView.setText(deviceInfo.getUsage() + " kWh");

        // Calculate hours and minutes from the duration stored in total minutes
        int totalMinutes = deviceInfo.getDuration();
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;

        // Format the duration to display hours and minutes
        String durationText;
        if (hours > 0 && minutes > 0) {
            durationText = hours + " hours " + minutes + " minutes";
        } else if (hours > 0) {
            durationText = hours + " hours";
        } else {
            durationText = minutes + " minutes";
        }
        holder.durationTextView.setText(durationText);

        // Remove button click listener
        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("RemoveButton", "Remove button clicked for device: " + deviceInfo.getDeviceName());

                // Call remove logic
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    removeDevice(deviceInfo.getDeviceName(), adapterPosition, holder.itemView.getContext());
                }
            }
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceNameTextView = itemView.findViewById(R.id.deviceNameTextView);
            usageTextView = itemView.findViewById(R.id.UsageTextView);
            durationTextView = itemView.findViewById(R.id.durationTextView);
            removeButton = itemView.findViewById(R.id.removeButton);  // Ensure this ID is in your XML
        }
    }

    private void removeDevice(String deviceName, int position, Context context) {
        // Remove the item from the database and the list
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        boolean success = dbHelper.removeDevice(deviceName);

        if (success) {
            // Remove the device from the list and notify the adapter
            deviceList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, deviceList.size());  // Refresh remaining items
            Log.d("RemoveDevice", "Device removed: " + deviceName);
            Toast.makeText(context, "Device removed: " + deviceName, Toast.LENGTH_SHORT).show();
        } else {
            Log.d("RemoveDevice", "Failed to remove device: " + deviceName);
            Toast.makeText(context, "Failed to remove device: " + deviceName, Toast.LENGTH_SHORT).show();
        }
    }
}

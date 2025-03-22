package com.example.powersaver.controllers;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.powersaver.R;
import com.example.powersaver.objects.Appliance;

import java.util.List;

public class ApplianceAdapter extends RecyclerView.Adapter<ApplianceAdapter.ViewHolder> {

    private final Context context;
    private final List<Appliance> applianceList;
    private final DatabaseHelper dbHelper;

    public ApplianceAdapter(Context context, List<Appliance> applianceList, DatabaseHelper dbHelper) {
        this.context = context;
        this.applianceList = applianceList;
        this.dbHelper = dbHelper;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_appliance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appliance appliance = applianceList.get(position);
        holder.applianceNameTextView.setText(appliance.getName());
        holder.powerRatingEditText.setText(String.valueOf(appliance.getPowerRating()));

        // Listen for changes to the power rating
        holder.powerRatingEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int powerRating = Integer.parseInt(s.toString());
                    appliance.setPowerRating(powerRating);
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "Enter a valid number", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Edit button functionality
        holder.editButton.setOnClickListener(v -> {
            String powerRatingStr = holder.powerRatingEditText.getText().toString().trim();
            try {
                int newPowerRating = Integer.parseInt(powerRatingStr);
                appliance.setPowerRating(newPowerRating);
                boolean success = dbHelper.addOrUpdateApplianceRating(appliance.getName(), newPowerRating);  // Update in database
                if (success) {
                    Toast.makeText(context, "Appliance updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to update appliance", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Please enter a valid power rating", Toast.LENGTH_SHORT).show();
            }
        });

        // Remove button functionality
        holder.removeButton.setOnClickListener(v -> {
            boolean success = dbHelper.deleteAppliance(appliance.getName());  // Remove from database
            if (success) {
                applianceList.remove(position);  // Remove from list
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, applianceList.size());
                Toast.makeText(context, "Appliance removed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to remove appliance", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return applianceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView applianceNameTextView;
        private final EditText powerRatingEditText;
        private final Button editButton;
        private final Button removeButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            applianceNameTextView = itemView.findViewById(R.id.appliance_name_text_view);
            powerRatingEditText = itemView.findViewById(R.id.power_rating_edit_text);
            editButton = itemView.findViewById(R.id.edit_button);    // Make sure these IDs are in your item layout XML
            removeButton = itemView.findViewById(R.id.remove_button);
        }
    }
}

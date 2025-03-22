package com.example.powersaver.fragments;

import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log; // Added for logging
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.powersaver.controllers.DatabaseHelper;
import com.example.powersaver.controllers.NotificationHelper;
import com.example.powersaver.R;
import com.example.powersaver.activity.devicemanager.DeviceManagerActivity;
import com.example.powersaver.databinding.FragmentPowerusageBinding;
import com.example.powersaver.objects.Device;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.components.Legend;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PowerUsageFragment extends Fragment {

    private FragmentPowerusageBinding binding;
    private String currentUsername;  // Store username from Dashboard
    private int currentUserID;
    private ArrayList<Device> deviceList=new ArrayList<Device>();
    private DatabaseHelper dbHelper;
    private double NationalAveragePowerConsumption=14.67;//Hardcoded backup value based on overall 2024 average
    private boolean onlineDataSuccess=false;
    private NotificationHelper notificationHelper;
    private boolean notificationSent = true; // Flag to prevent multiple notifications //Initially set to false to prevent notif before online data is fetched
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1003;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentPowerusageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //TODO TEST
        CsvDataListener CsvListen=new CsvDataListener() {
            @Override
            public void onDataFetched(ArrayList<String[]> data) {
                notificationSent=false;
                NationalAveragePowerConsumption= calculateAverage(data);
                onlineDataSuccess=true;
                refreshData();
                Toast.makeText(binding.getRoot().getContext(), "Latest Average Data calculated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(binding.getRoot().getContext(), "Failed to retrieve average usage data, using default value", Toast.LENGTH_SHORT).show();
            }
        };
        fetchData(CsvListen);
        // Initialize NotificationHelper
        notificationHelper = new NotificationHelper(requireContext());
        // Retrieve the username from the parent activity (DashboardActivity)
        Intent intentTest=getActivity().getIntent();
        currentUsername = getActivity().getIntent().getStringExtra("username");
        currentUserID=getActivity().getIntent().getIntExtra("userID",0);
        dbHelper = new DatabaseHelper(binding.getRoot().getContext());
        //deviceList=dbHelper.getAllDevicesObj();
        deviceList=dbHelper.getDevicesByUser(currentUserID);
        sortDeviceList();
        TextView totalTextview=binding.PowerUsageMainReadout;
        double total=0;

        TableLayout tableLayout = binding.powerUsageExpandedText;
        if (deviceList != null && !deviceList.isEmpty()) {
            for (Device d : deviceList) {
                TableRow row = new TableRow(binding.getRoot().getContext());
                TextView device = new TextView(binding.getRoot().getContext());
                device.setText(d.getDeviceName());
                device.setPadding(15, 15, 15, 15);
                device.setGravity(Gravity.CENTER);
                device.setEllipsize(TextUtils.TruncateAt.END);
                device.setTextColor(Color.BLACK);
                device.setMaxWidth(60);
                device.setMaxLines(1);
                device.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.background_outline_sharp, getActivity().getTheme()));
                TextView usage = new TextView(binding.getRoot().getContext());
                double calculatedConsumptionDouble=(d.getUsage()*((double) d.getDuration() /60));
                DecimalFormat df = new DecimalFormat("###.#");
                String calculatedConsumption=(df.format(calculatedConsumptionDouble));
                usage.setText(calculatedConsumption + " W / day");
                usage.setPadding(15, 15, 15, 15);
                usage.setGravity(Gravity.CENTER);
                usage.setTextColor(Color.BLACK);
                usage.setMaxWidth(60);
                usage.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.background_outline_sharp, getActivity().getTheme()));
                row.addView(device);
                row.addView(usage);
                tableLayout.addView(row);
                total+=d.getUsage()*(double)(d.getDuration()/60);
            }
            DecimalFormat totalDf = new DecimalFormat("###.##");
            totalTextview.setText(totalDf.format(total/1000)+"kWh");
            updateStatCompare(total/1000);
            //refreshData();
            if(total==0.0)
            {
                totalTextview.setText("No Devices");
            }
        }
        else
        {
            totalTextview.setText("No Devices");
            refreshData();
        }
      //  displayBarChart();

        binding.powerUsageExpandToggle.setOnClickListener(v -> {
            if (binding.powerUsageExpandedText.getVisibility() != View.GONE) {
                binding.powerUsageExpandedText.setVisibility(View.GONE);
                binding.devicePieChart.setVisibility(View.GONE);
                binding.powerUsageExpandToggle.setImageResource(android.R.drawable.arrow_down_float);
            } else {
                binding.powerUsageExpandedText.setVisibility(View.VISIBLE);
                binding.devicePieChart.setVisibility(View.VISIBLE);
                binding.powerUsageExpandToggle.setImageResource(android.R.drawable.arrow_up_float);
                refreshData();
            }
        });

        // Request Notification Permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
        //Handles opening the device Manager via the fragment button
        binding.manageDevicesButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DeviceManagerActivity.class);
            intent.putExtra("username", currentUsername);  // Pass the username to DeviceManagerActivity
            intent.putExtra("userID",currentUserID);
            startActivity(intent);
        });
    }

    //sets up textblurb on fragment
    public void updateStatCompare(double userTotal)
    {
        //Get the blurb
        TextView blurb=binding.PowerUsageBlurb;
        Log.d("PowerUsageFragment", "User Total: " + userTotal + " kWh");
        Log.d("PowerUsageFragment", "National Average: " + NationalAveragePowerConsumption + " kWh");
        double threshold = NationalAveragePowerConsumption * 1.15; // 15% higher
        //Compare if userTotal Higher or lower than national average
        if(userTotal==0.0)
        {
            blurb.setText("No decives have been added, you can add your home devices via the button below or the option menu"+
                    " taking you to the device manager!");
        }
        else if(userTotal>NationalAveragePowerConsumption)
        {
            double percentageDiff=(1-(NationalAveragePowerConsumption/userTotal))*100;
            DecimalFormat df = new DecimalFormat("#.#");
            String roundedPercentDiffString = Double.toString(Double.parseDouble(df.format(percentageDiff)));
            // rounds off then saves percentage as string
            String blurbTextFull="You have used "+roundedPercentDiffString+"% more power than the current national daily average of "
                    +NationalAveragePowerConsumption+"kWh. \n" ;
            blurbTextFull+="\n"+getBlurbSuggestions(percentageDiff,true);
            blurb.setText(blurbTextFull);
            // Send notification if not already sent
            if (!notificationSent) {
                String title = "Warning!";
                String message = "Check your Home Power Usage.";
                notificationHelper.sendHighPowerUsageNotification(title, message);
                notificationSent = true;
                Log.d("PowerUsageFragment", "Notification sent: " + message);
            }
        }
        else if(userTotal<NationalAveragePowerConsumption)
        {
            double percentageDiff=(1-(userTotal/NationalAveragePowerConsumption))*100;
            DecimalFormat df = new DecimalFormat("#.#");
            String roundedPercentDiffString = Double.toString(Double.parseDouble(df.format(percentageDiff)));
            // rounds off then saves percentage as string
            String blurbTextFull="You have used "+roundedPercentDiffString+"% less power than the current national daily average of "
                    +NationalAveragePowerConsumption+"kWh. \n" ;
            blurbTextFull+="\n"+getBlurbSuggestions(percentageDiff,false);
            blurb.setText(blurbTextFull);

            // Optionally, send a positive notification
            /*
            if (!notificationSent) {
                String title = "Good Power Usage";
                String message = "Your current power usage is " + roundedPercentDiffString + "% below the national average of " + NationalAveragePowerConsumption + "kWh.";
                notificationHelper.sendHighPowerUsageNotification(title, message);
                notificationSent = true;
                Log.d("PowerUsageFragment", "Good usage notification sent: " + message);
            }
            */
        }
        else if(userTotal==NationalAveragePowerConsumption)
        {
            blurb.setText("You are using EXACTLY the national avarage power consumption per day!" +
                    " That's astondingly rare! However, you could always cut down and help reduce our nation-wide power usage" +
                    "and save yourself some money too!");
        }

        //Calculate percentage difference and change blurb depending on higher or lower
        //I guess a catch if it was exactly the same

    }
    //Give a string of feedback to add to blurb
    public String getBlurbSuggestions(double percentageDiff,boolean OverAvg)
    {
        boolean suggestionGiven=false;
        String suggestion="";
        if(OverAvg)
        {
            if(percentageDiff>10) {
                suggestion += "Since your consumption is significantly higher than the national average you may want to consider the following suggestion(s): ";
            }
            else
            {
                suggestion +="You are not too far off the national average power consumption, try adopting the following"
                        +" suggestions to help you reduce your energy usage:";
            }
            if(getDeviceListSize()>=15)
                {
                //Suggest reduce amount of devices
                    suggestion+="\n- You currently have a total of "+getDeviceListSize()+
                            " devices, consider reducing the amount of electrical devices in the household.\n";
                    suggestionGiven=true;
                }

            if(getAverageDuration()>=900) //if average is about 60% on daily
            {
                //Suggest reduce Device-on-time
                double avgDuration=getAverageDuration();
                String avgDuraHours=String.valueOf((int)avgDuration/60);
                String avgDuraMin=String.valueOf((int)avgDuration%60);
                suggestion+="\n- The average in-use time for many of your devices is rather high at: "+
                        avgDuraHours+" Hours and "+avgDuraMin+" Min."
                        +" Consider switching off some of your devices that are left on the longest\n";
                suggestionGiven=true;

            }
            if(getAvergeUsage()>=500) //if average is greater than 500Wh
            {
                //Suggest reduction of high consumption devices
                //Suggest reduce time-on of high consumption devices
                int avgUsage=(int)getAvergeUsage();//round off to make avg look cleaner
                suggestion+="\n- The average consumption for many of your devices is rather high"
                        +" at "+avgUsage+"Wh daily usage. Consider reducing the use of the high consumption devices"
                        +" or seek alternate devices with reduced energy consumption\n";
                suggestionGiven=true;

            }
            if(!suggestionGiven) //in case no suggestions are added
            {
                //Default suggestion?
                suggestion+="\n- While nothing stands out significantly, try reducing the amount of use time of"
                        +" your devices across the board and try seeking out more efficient devices for your home.";
            }
        }
        else
        {
            suggestion+="- Good work on keeping a daily energy usage below the national average,"
                    +" that being said you can always seek out more efficient devices for your home "
                    +"or simply reduce the time using some of your devices.";
        }
        return suggestion;
    }
    public int getDeviceListSize()
    {
        return deviceList.size();
    }
    //returns average duration of current deviceList
    public double getAverageDuration()
    {
        int total=0;
        for(Device d:deviceList)
        {
            total+=d.getDuration();
        }
        return (double) total/getDeviceListSize();
    }
    //returns average power usage of current deviceList
    public double getAvergeUsage()
    {
        int total=0;
        for(Device d:deviceList)
        {
            total+=d.getUsage();
        }
        return (double) total/getDeviceListSize();
    }
    //Sorts deviceList to have largest at the top
    public void sortDeviceList()
    {
        //sorts device list by highest consumption (total based off both usage and duration)
        Collections.sort(deviceList, new Comparator<Device>() {
            @Override
            public int compare(Device device1, Device device2) {
                int totalConsumption1 = device1.getDuration() * device1.getUsage();
                int totalConsumption2 = device2.getDuration()  * device2.getUsage();
                return Integer.compare(totalConsumption2, totalConsumption1); // Descending order
            }
        });
    }
    //retries latest DB data and force refresh of tables and graphs in fragment
    public void refreshData()
    {
        TextView totalTextview=binding.PowerUsageMainReadout;
        double total=0;
        removeExistingTable();
        deviceList=dbHelper.getDevicesByUser(currentUserID);
        sortDeviceList();
        TableLayout tableLayout = binding.powerUsageExpandedText;
        for (Device d:deviceList) {
            TableRow row = new TableRow(binding.getRoot().getContext());
            TextView device = new TextView(binding.getRoot().getContext());
            device.setText(d.getDeviceName());
            device.setPadding(15, 15, 15, 15);
            device.setGravity(Gravity.CENTER);
            device.setEllipsize(TextUtils.TruncateAt.END);
            device.setTextColor(Color.BLACK);
            device.setMaxWidth(60);
            device.setMaxLines(1);
            device.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.background_outline_sharp, getActivity().getTheme()));
            TextView usage = new TextView(binding.getRoot().getContext());
            double calculatedConsumptionDouble=(d.getUsage()*((double) d.getDuration() /60));
            DecimalFormat df = new DecimalFormat("###.#");
            String calculatedConsumption=(df.format(calculatedConsumptionDouble));
            usage.setText(calculatedConsumption + " W / day");
            usage.setPadding(15, 15, 15, 15);
            usage.setGravity(Gravity.CENTER);
            usage.setTextColor(Color.BLACK);
            usage.setMaxWidth(60);
            usage.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.background_outline_sharp, getActivity().getTheme()));
            row.addView(device);
            row.addView(usage);
            tableLayout.addView(row);
            total+=d.getUsage()*(d.getDuration()/60);
        }
        DecimalFormat totalDf = new DecimalFormat("###.##");
        totalTextview.setText(totalDf.format(total/1000)+"kWh");
        displayPieChart();
        displayBarChart();
        if(total==0.0)
        {
            totalTextview.setText("No Devices");
        }
        if(!onlineDataSuccess)
        {
               //only execute if never successful before, to avoid constant calling
            CsvDataListener CsvListen=new CsvDataListener() {
                @Override
                public void onDataFetched(ArrayList<String[]> data) {
                    NationalAveragePowerConsumption= calculateAverage(data);
                    onlineDataSuccess=true;
                 //   Toast.makeText(binding.getRoot().getContext(), "Latest Average Data calculated", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(binding.getRoot().getContext(), "Failed to retrieve average usage data, using default value", Toast.LENGTH_SHORT).show();
                }
            };
            fetchData(CsvListen);
        }
        updateStatCompare(total/1000);
    }
//Force removes the device tables in the fragment
    public void removeExistingTable()
    {
        int tableSize=binding.powerUsageExpandedText.getChildCount();
        for(int i=1;i<tableSize;i++)//skip heading row
        {
            binding.powerUsageExpandedText.removeViewAt(1);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        refreshData();
     //   displayPieChart();
     //   displayBarChart();
    }
    // Handle permission request results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can send notifications if needed
                Toast.makeText(getContext(), "Notification permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                Toast.makeText(getContext(), "Notification permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //CSV Request from Datasite and gets the latest average



        public void fetchData(final CsvDataListener listener) {
            String url = "https://www.ema.gov.sg/content/dam/corporate/resources/singapore-energy-statistics/chapter-3/chart/electricity-map-4.csv";

            int currentYear = Calendar.getInstance().get(Calendar.YEAR);

            // Create a StringRequest to fetch the CSV data
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String
                                response) {
                            // Parse the CSV data
                            ArrayList<String[]> data = parseCsv(response,currentYear);
                            if (listener != null) {
                                listener.onDataFetched(data);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (listener != null) {
                                listener.onError(error.getMessage());

                            }
                        }
                    });

            // Add the request to the RequestQueue
            RequestQueue requestQueue = Volley.newRequestQueue(binding.getRoot().getContext());
            requestQueue.add(stringRequest);

        }
//used when receiving CSV data
    private ArrayList<String[]> parseCsv(String csvData, int currentYear) {
        ArrayList<String[]> filteredData = new ArrayList<>();
        String[] lines = csvData.split("\n");

        for (String line : lines) {
            String[] fields = line.split(",");

            if (fields.length >= 4 && fields[0].equals("Overall") && Integer.parseInt(fields[1]) == currentYear) {
                filteredData.add(fields);
            }
        }

        return filteredData;
    }
//Manually reads through provided CSV data and calculates average
    private double calculateAverage(ArrayList<String[]> data)
    {
        double total = 0.0;
        for (String[] row : data)
        {
            total+=Double.parseDouble(row[3]);
        }
        double average=total/data.size(); //this gets average monthly
        double averageDaily=average/30; //this gets average daily
        DecimalFormat df = new DecimalFormat("#.##");
        return Double.parseDouble(df.format(averageDaily)); //rounds averageDaily and returns
    }

        public interface CsvDataListener {
            void onDataFetched(ArrayList<String[]> data);
            void onError(String message);
        }
//Displays user deviceList and other information in pie chart
    private void displayPieChart() {
        List<PieEntry> pieEntries = new ArrayList<>();

        // Add each device's usage to the pie entries
        for (Device device : deviceList) {
            pieEntries.add(new PieEntry(device.getUsage(), device.getDeviceName())); // Usage as value, Device name as label
        }

        if (pieEntries.isEmpty()) {
            // Clear any existing data
            binding.devicePieChart.clear();

            // Set the "No devices found" message as center text
            binding.devicePieChart.setCenterText("No devices found");
            binding.devicePieChart.setCenterTextSize(16f);
            binding.devicePieChart.setCenterTextColor(Color.GRAY); // Set the text color for better readability
            binding.devicePieChart.getDescription().setEnabled(false);

            // Add a dummy slice with a grey color for visual effect
            pieEntries.add(new PieEntry(1f, "")); // Dummy entry to create a single-color pie chart
            PieDataSet dataSet = new PieDataSet(pieEntries, "");
            dataSet.setColor(Color.LTGRAY); // Use a light grey color for the empty pie slice

            PieData pieData = new PieData(dataSet);
            pieData.setDrawValues(false); // Don't show values for the dummy entry
            binding.devicePieChart.setData(pieData);

            // Set legend to default
            Legend legend = binding.devicePieChart.getLegend();
            legend.setEnabled(false); // Enable legend
            legend.setForm(Legend.LegendForm.CIRCLE); // Optional: set the shape of the legend icon
            legend.setWordWrapEnabled(true);

            // Refresh the chart to display changes
            binding.devicePieChart.invalidate();
            return;
        }
        int[] colors = generateColors(deviceList.size());
        // Create a PieDataSet
        PieDataSet pieDataSet = new PieDataSet(pieEntries, ""); // Empty label to remove the legend text
        pieDataSet.setColors(colors); // Set colors for each slice
        pieDataSet.setSliceSpace(3f); // Space between slices
        pieDataSet.setValueTextSize(12f); // Size of the text that will display on the slices
        pieDataSet.setValueTextColor(Color.BLACK); // Text color for the percentage and usage

        // Create PieData with PieDataSet
        PieData pieData = new PieData(pieDataSet);

        // Set the value formatter for PieData (percentage values on the slices)
        pieData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1f%%", value, value); // Format the percentage to 1 decimal place
            }
        });

        // Set the data for PieChart
        binding.devicePieChart.setData(pieData);
        binding.devicePieChart.setUsePercentValues(true); // Enable percentage display
        binding.devicePieChart.setCenterText("");

        // Enable drawing of values on the slices
        pieDataSet.setDrawValues(true);

        // Set properties for PieChart
        binding.devicePieChart.setDrawHoleEnabled(false); // Disable hole in the middle of the pie
        binding.devicePieChart.getDescription().setEnabled(false); // Disable description text
        binding.devicePieChart.setDrawEntryLabels(false);

        // Set the legend with the device names and their usage values
        List<LegendEntry> legendEntries = new ArrayList<>();

        for (int i = 0; i < deviceList.size(); i++) {
            Device device = deviceList.get(i);
            LegendEntry entry = new LegendEntry();
            entry.label = device.getDeviceName() + ": " + device.getUsage() + " Wh"; // Add usage to legend label
            int testSize=pieDataSet.getColors().size();
            entry.formColor = colors[i]; // Set the color for the legend entry
            legendEntries.add(entry);
        }

        // Set custom legend entries
        binding.devicePieChart.getLegend().setCustom(legendEntries);

        // Move the legend to the left side of the pie chart
        Legend legend = binding.devicePieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER); // Center the legend vertically
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT); // Align the legend to the left
        legend.setOrientation(Legend.LegendOrientation.VERTICAL); // Make the legend vertical
        legend.setEnabled(true); // Enable the legend

        // Refresh the chart to apply changes
        binding.devicePieChart.animateXY(1400,1400);
        binding.devicePieChart.invalidate(); // Invalidate to update the chart
    }
    //Displays user deviceList and other information in bar chart
    public void displayBarChart() {
        BarChart barChart = binding.barChart;
        barChart.clear();
        List<BarEntry> barChartEntries = new ArrayList<>();
        String[] deviceNames;
        BarDataSet dataSet;
        float[] usageValues;
        float maxUsage = 0f;
        float deviceTotal= 0f; // compare to national average if higher needs to be expanded.
        if(!deviceList.isEmpty())
        {
            barChart.setVisibility(View.VISIBLE);
            if (deviceList.size() == 1) {
                // Workaround for a single device: add a dummy entry
                deviceNames = new String[] { deviceList.get(0).getDeviceName()}; // Second entry to trigger the legend
               // barChartEntries.add(new BarEntry(0, new float[] { deviceList.get(0).getUsage(), 0f })); // Add a dummy value of 0
                // Preparing data for the stacked bar chart
                // float maxUsage = 0f;
                usageValues = new float[deviceList.size()];
                usageValues[0] = (deviceList.get(0).getUsage()*(float)(deviceList.get(0).getDuration()/60)/1000);
                deviceTotal=usageValues[0];
                // Add a single BarEntry containing all usage values for the stacked bar
                barChartEntries.add(new BarEntry(0, usageValues));

                // Create the dataset with the device usage values
                dataSet = new BarDataSet(barChartEntries, deviceNames[0]);

                dataSet.setStackLabels(deviceNames); // Set labels from device names
                dataSet.resetColors();
                int[] colors = generateColors(deviceNames.length);
                dataSet.setColors(colors);// Set colors for each segment
                dataSet.setValueTextSize(12f);
                dataSet.setValueTextColor(Color.BLACK);
            } else {
                // Normal behavior for multiple devices
                deviceNames = new String[deviceList.size()];
                usageValues = new float[deviceList.size()];
                for (int i = 0; i < deviceList.size(); i++) {
                    deviceNames[i] = deviceList.get(i).getDeviceName();
                    usageValues[i] = (deviceList.get(i).getUsage()*(float)(deviceList.get(i).getDuration()/60)/1000);
                    deviceTotal+=usageValues[i];
                }

                barChartEntries.add(new BarEntry(0, usageValues));
                // Preparing data for the stacked bar chart
                // float maxUsage = 0f;
                // usageValues = new float[deviceList.size()];
            /*for (int i = 0; i < deviceList.size(); i++) {
                usageValues[i] = (deviceList.get(i).getUsage()*(float)(deviceList.get(i).getDuration()/60)/1000);
            }*/
                // Add a single BarEntry containing all usage values for the stacked bar
                barChartEntries.add(new BarEntry(0, usageValues));

                // Create the dataset with the device usage values
                dataSet = new BarDataSet(barChartEntries, "");

                dataSet.setStackLabels(deviceNames); // Set labels from device names
                dataSet.resetColors();
                int[] colors = generateColors(deviceNames.length);
                dataSet.setColors(colors);
                //  dataSet.setColors(ColorTemplate.COLORFUL_COLORS); // Set colors for each segment
                dataSet.setValueTextSize(12f);
                dataSet.setValueTextColor(Color.BLACK);
            }

            // Set a custom ValueFormatter to add "kWh" unit to the usage values
            dataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getBarStackedLabel(float value, BarEntry entry) {
                    return value + " kWh"; // Append "kWh" to each label
                }
            });

            BarData barData = new BarData(dataSet);
           // barData.setBarWidth(1f);
            barChart.setData(barData);

            // ==================================================

            float averageConsumption = (float) NationalAveragePowerConsumption; // change this to CSV value

            // ==================================================
            float maxAxisValue = Math.max(maxUsage, averageConsumption) * 1.1f; // Adding 10% padding
            // Determine the max Y-axis value to fit the average consumption line
            if(maxAxisValue<deviceTotal)
            {
                maxAxisValue=deviceTotal*1.1f; //expand the axis so it will always fit full graph
            }


            // Add average consumption limit line
            YAxis leftAxis = barChart.getAxisLeft();
            leftAxis.setAxisMaximum(maxAxisValue);
            leftAxis.setAxisMinimum(0f);

            LimitLine averageLine = new LimitLine(averageConsumption, "National Average");
            averageLine.setLineWidth(2f);
            averageLine.enableDashedLine(10f, 10f, 0f);
            averageLine.setLineColor(Color.RED);
            averageLine.setTextSize(10f);
            averageLine.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP); // Positions label on the right and above the line

            leftAxis.removeAllLimitLines();
            leftAxis.addLimitLine(averageLine);

            // Customize x-axis to show only one label
            XAxis xAxis = barChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setDrawLabels(false); // Hide x-axis label as there's only one bar
            xAxis.setLabelRotationAngle(-45);

            // Customize the bar chart appearance
            barChart.getDescription().setEnabled(false);
            barChart.getAxisRight().setEnabled(false); // Hide right y-axis
            barChart.setFitBars(true); // Make the bars fit into the chart view
            barChart.animateY(1000); // Animate on load

            barChart.getLegend().setWordWrapEnabled(true);

            // Refresh the chart to display changes
            // barChart.notifyDataSetChanged();
            barChart.invalidate();
        }
        else
        {
            barChart.setVisibility(View.GONE);
        }
    }


//Used to provide infinite colors for pie chart and bar chart
    private int[] generateColors(int count) {
        int[] colors = new int[count];
        float hue = 0f;
        float saturation = 1f;
        float value = 1f;

        for (int i = 0; i < count; i++) {
            colors[i] = Color.HSVToColor(new float[]{hue, saturation, value});
            if(count!=0)
            {
                hue += 360.0f / count; //splits entire color spectrum into equal parts apart
            }

        }

        return colors;
    }

}

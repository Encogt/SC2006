package com.example.powersaver.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.powersaver.controllers.DatabaseHelper;
import com.example.powersaver.objects.Device;
import com.github.mikephil.charting.charts.LineChart;

import com.example.powersaver.databinding.FragmentBillBinding;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BillFragment extends Fragment{
    private FragmentBillBinding binding;
    private String currentUsername;  // Store username from Dashboard
    private int currentUserID;
    private ArrayList<Device> deviceList=new ArrayList<Device>();
    private DatabaseHelper dbHelper;
    private double energyTariff;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentBillBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boolean expanded=false;

        currentUsername = getActivity().getIntent().getStringExtra("username");
        currentUserID=getActivity().getIntent().getIntExtra("userID",0);
        dbHelper = new DatabaseHelper(binding.getRoot().getContext());

        getEnergyTariff(new TariffCallback() {
            @Override
            public void onTariffReceived(double tariff) {
                calculateBill(tariff);
                binding.BillBlurb.setText(String.format("Current Electricity Tariffs is:\n%.2f cents/kWh (with GST)", tariff));
            }

            // onError not needed...
            @Override
            public void onError(String errorMessage) {
                // Handle error case, e.g., show a Toast message
            }
        });

        // fetch and display historical tariff data
        getBillGraphData();

        /*
        LineChart lineChart = binding.billExpandedGraph;
        List<Entry> entries = new ArrayList<Entry>();
        for (int i = 0; i < 10; i++) {
            float value = (float) (Math.random() * 100); // Generate random values
            entries.add(new Entry(i, value));
        }
        LineDataSet dataSet = new LineDataSet(entries, "Dummy Data");
        dataSet.setColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        //lineChart.zoom(1.5f, 1.5f, 0f, 0f);
        //lineChart.getAxisLeft().setAxisMinimum(100f);
        //lineChart.getAxisLeft().setAxisMaximum(200f); // Increased range
        //lineChart.getAxisRight().setAxisMinimum(100f);
        //lineChart.getAxisRight().setAxisMaximum(200f);
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate();
        */
//Button handlers for expanding and minimising the fragment
        binding.billExpandToggle.setOnClickListener
                (new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(binding.billExpandedText.getVisibility()!=View.GONE){
                            binding.billExpandedText.setVisibility(View.GONE);
                            binding.billExpandToggle.setImageResource(android.R.drawable.arrow_down_float);
                        }
                        else{
                            binding.billExpandedText.setVisibility(View.VISIBLE);
                            binding.billExpandToggle.setImageResource(android.R.drawable.arrow_up_float);
                        }
                    }});


    }

    @Override
    public void onResume() {
        super.onResume();

        // Re-fetch the energy tariff every time the fragment is resumed
        getEnergyTariff(new TariffCallback() {
            @Override
            public void onTariffReceived(double tariff) {
                calculateBill(tariff);
                binding.BillBlurb.setText(String.format("Current Electricity Tariffs is:\n%.2f cents/kWh (with GST)", tariff));
            }

            @Override
            public void onError(String errorMessage) {
                // Handle error case, e.g., show a Toast message
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void calculateBill(double tariff)
    {
        //will pull data of all user devices from database and  calculate total daily consumption,
        //add bill as text into the Textview
        deviceList=dbHelper.getDevicesByUser(currentUserID);
        TextView billTextView=binding.BillMainReadout;
        double totalUsage=0;
        for(Device d:deviceList)
        {
            totalUsage+=d.getUsage()*(double)(d.getDuration()/60);
        }
        double totalBill=(totalUsage/1000)*(tariff/100);
        updateBillSubtext(totalBill);
        billTextView.setText(String.format("$%.2f",totalBill));
    }
    //Gives 30 day expected bill for fragment display
public void updateBillSubtext(double DailyBill)
{
    TextView sub=binding.BillMainReadoutSubtext;
    String BillSubtext="Expected Daily Bill\n";
    BillSubtext+="Estimated Monthly (30 day) Bill: \n";
    BillSubtext+=String.format("$%.2f",DailyBill*30);
    sub.setText(BillSubtext);
    sub.setGravity(Gravity.CENTER);
}

//Callback handler for Tariff info API call
    public interface TariffCallback {
        void onTariffReceived(double tariff);
        void onError(String errorMessage);
    }

    // method call to get current tariff from SingStats API
    public void getEnergyTariff(TariffCallback callback) {

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonthIndex = calendar.get(Calendar.MONTH);

        String currentMonth = new DateFormatSymbols().getShortMonths()[currentMonthIndex];

        String url = String.format("https://tablebuilder.singstat.gov.sg/api/table/tabledata/M890991?isTestApi=true&timeFilter=%d%%20%s", currentYear, currentMonth);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // Start from the main JSON object
                    JSONObject dataObject = response.getJSONObject("Data");

                    // Get the "row" array from within "Data"
                    JSONArray rowsArray = dataObject.getJSONArray("row");

                    // Iterate over each item in the "row" array
                    for (int i = 0; i < rowsArray.length(); i++) {
                        JSONObject rowObject = rowsArray.getJSONObject(i);

                        // Check if "rowText" equals "Low Tension Supplies - Domestic"
                        String rowText = rowObject.getString("rowText");
                        if ("Low Tension Supplies - Domestic".equals(rowText)) {

                            // Get the "columns" array inside this specific "row" object
                            JSONArray columnsArray = rowObject.getJSONArray("columns");

                            // Assuming we want the first entry in the "columns" array
                            JSONObject columnObject = columnsArray.getJSONObject(0);

                            // Retrieve the "value"
                            String value = columnObject.getString("value");
                            Double tariff = Double.parseDouble(value);
                            callback.onTariffReceived(tariff);
                        }
                    }
                } catch (Exception e) {

                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        Volley.newRequestQueue(requireContext()).add(request);
    }

    // method to get past Annual Average Electricity Tariffs to display on graph
    public void getBillGraphData() {

        String url = String.format("https://tablebuilder.singstat.gov.sg/api/table/tabledata/M891001?isTestApi=true");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // Start from the main JSON object
                    JSONObject dataObject = response.getJSONObject("Data");

                    // Get the "row" array from within "Data"
                    JSONArray rowsArray = dataObject.getJSONArray("row");

                    List<Entry> entries = new ArrayList<>();
                    List<String> years = new ArrayList<>();

                    // Iterate over each item in the "row" array
                    for (int i = 0; i < rowsArray.length(); i++) {
                        JSONObject rowObject = rowsArray.getJSONObject(i);

                        // Check if "rowText" equals "Low Tension Supplies - Domestic"
                        String rowText = rowObject.getString("rowText");
                        if ("Low Tension Supplies - Domestic".equals(rowText)) {

                            // Get the "columns" array inside this specific "row" object
                            JSONArray columnsArray = rowObject.getJSONArray("columns");

                            // Assuming we want the first entry in the "columns" array
                            for (int j = 0; j < columnsArray.length(); j++) {
                                JSONObject columnObject = columnsArray.getJSONObject(j);
                                String year = columnObject.getString("key");
                                float tariff = Float.parseFloat(columnObject.getString("value"));

                                entries.add(new Entry(j, tariff));
                                years.add(year);
                            }
                            break;
                        }
                    }

                    showLineChart(entries, years);

                } catch (Exception e) {

                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        Volley.newRequestQueue(getContext()).add(request);
    }

    // Method to display data on the line chart
    private void showLineChart(List<Entry> entries, List<String> years) {
        LineDataSet dataSet = new LineDataSet(entries, "Annual Average Electricity Tariff (cents/kWh)");
        dataSet.setColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.RED);
        dataSet.setCircleRadius(3f);

        // set data to the chart
        LineData lineData = new LineData(dataSet);
        LineChart lineChart = binding.billExpandedGraph;
        lineChart.setData(lineData);
        lineChart.getDescription().setEnabled(false);

        // customized x-axis to show the years
        lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return index >= 0 && index < years.size() ? years.get(index) : "";
            }
        });
        lineChart.getXAxis().setGranularity(1f);
        lineChart.getXAxis().setTextColor(Color.BLACK);
        lineChart.getXAxis().setLabelRotationAngle(-45);

        lineChart.getAxisLeft().setTextColor(Color.BLACK);
        lineChart.getAxisRight().setEnabled(false);

        lineChart.getLegend().setTextColor(Color.BLACK);
        lineChart.invalidate(); // Refresh chart
    }
}
package com.example.mbook.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mbook.R;
import com.example.mbook.models.Library;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Admin_Analytics_Fragment extends Fragment {

    private TextView tvTotalUsers, tvDailyActiveUsers, tvMonthlyActiveUsers;
    private BarChart barChartMostLiked;
    private LineChart lineChart, lineChartDailyActiveUsers; // Added new line chart
    private PieChart pieChart;

    private DatabaseReference userReference;
    private DatabaseReference libraryReference;

    public Admin_Analytics_Fragment() {
        // Required empty public constructor
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin__analytics_, container, false);

        // Initialize Views
        tvTotalUsers = view.findViewById(R.id.tv_total_users);
        tvDailyActiveUsers = view.findViewById(R.id.tv_daily_active_users);
        tvMonthlyActiveUsers = view.findViewById(R.id.tv_monthly_active_users);
        barChartMostLiked = view.findViewById(R.id.bar_chart_most_liked);
        lineChart = view.findViewById(R.id.line_chart);
        lineChartDailyActiveUsers = view.findViewById(R.id.line_chart_daily_active_users); // New line chart
        pieChart = view.findViewById(R.id.pie_chart);

        // Initialize Firebase Database References
        userReference = FirebaseDatabase.getInstance().getReference("users");
        libraryReference = FirebaseDatabase.getInstance().getReference("libraries");

        fetchUserData();
        fetchMostLikedLibraries();

        return view;
    }

    private void fetchUserData() {
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalUsers = (int) dataSnapshot.getChildrenCount();
                int dailyActiveUsers = 0; // Replace with actual calculation if available
                int monthlyActiveUsers = 0; // Replace with actual calculation if available

                // Set total users
                tvTotalUsers.setText(String.valueOf(totalUsers));

                // Set daily active users and monthly active users
                tvDailyActiveUsers.setText(String.valueOf(dailyActiveUsers));
                tvMonthlyActiveUsers.setText(String.valueOf(monthlyActiveUsers));

                // Update charts with total user data
                updateCharts(totalUsers, dailyActiveUsers, monthlyActiveUsers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }

    private void updateCharts(int totalUsers, int dailyActiveUsers, int monthlyActiveUsers) {
        // Update Line Chart for Users Over Time
        ArrayList<Entry> lineEntries = new ArrayList<>();
        lineEntries.add(new Entry(0, totalUsers));
        lineEntries.add(new Entry(1, dailyActiveUsers));
        lineEntries.add(new Entry(2, monthlyActiveUsers));

        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Users Over Time");
        lineDataSet.setColor(Color.BLUE);
        lineDataSet.setValueTextColor(Color.BLACK); // Value text color
        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        lineChart.invalidate(); // Refresh

        // Update the new Line Chart for Daily Active Users (sample data for demonstration)
        ArrayList<Entry> dailyActiveEntries = new ArrayList<>();
        dailyActiveEntries.add(new Entry(0, dailyActiveUsers));
        // Add more entries here based on your actual daily data

        LineDataSet dailyActiveDataSet = new LineDataSet(dailyActiveEntries, "Daily Active Users");
        dailyActiveDataSet.setColor(Color.GREEN);
        dailyActiveDataSet.setValueTextColor(Color.BLACK); // Value text color
        LineData dailyActiveData = new LineData(dailyActiveDataSet);
        lineChartDailyActiveUsers.setData(dailyActiveData);
        lineChartDailyActiveUsers.invalidate(); // Refresh

        // Update Pie Chart for User Engagement Breakdown
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(totalUsers, "Total Users"));
        pieEntries.add(new PieEntry(dailyActiveUsers, "Daily Active Users"));
        pieEntries.add(new PieEntry(monthlyActiveUsers, "Monthly Active Users"));

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "User Engagement Distribution");
        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate(); // Refresh

        // Additional pie chart styling
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleColor(Color.WHITE);
        pieChart.setTransparentCircleAlpha(100);
        pieChart.setHoleRadius(40f);
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText("User Engagement");
    }

    private void fetchMostLikedLibraries() {
        libraryReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Library> libraries = new HashMap<>();
                for (DataSnapshot librarySnapshot : dataSnapshot.getChildren()) {
                    Library library = librarySnapshot.getValue(Library.class);
                    if (library != null) {
                        libraries.put(librarySnapshot.getKey(), library);
                    }
                }
                setMostLikedLibraries(libraries);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }

    private void setMostLikedLibraries(HashMap<String, Library> libraries) {
        // Sort libraries by likes
        List<Library> sortedLibraries = new ArrayList<>(libraries.values());
        Collections.sort(sortedLibraries, (lib1, lib2) -> Integer.compare(lib2.getLikes(), lib1.getLikes()));

        // Get top 5 libraries
        int topCount = Math.min(5, sortedLibraries.size());
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        for (int i = 0; i < topCount; i++) {
            Library library = sortedLibraries.get(i);
            barEntries.add(new BarEntry(i, library.getLikes())); // Use index as x-value
        }

        // Update the Bar Chart
        BarDataSet barDataSet = new BarDataSet(barEntries, "Top 5 Most Liked Libraries");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        BarData barData = new BarData(barDataSet);
        barChartMostLiked.setData(barData);
        barChartMostLiked.invalidate(); // Refresh
    }
}

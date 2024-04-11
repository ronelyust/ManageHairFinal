package com.example.managehairfinal.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.managehairfinal.R;
import com.example.managehairfinal.model.scheduleData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WorkingHours#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WorkingHours extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    String[] daysOfWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    final String[] hourLabels = {"00:00", "01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00", "10:00", "11:00",
            "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"};
    private int numBreaks = 0;

    List<List<String>> breakTimes = new ArrayList<List<String>>();



    public WorkingHours() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WorkingHours.
     */
    // TODO: Rename and change types and number of parameters
    public static WorkingHours newInstance(String param1, String param2) {
        WorkingHours fragment = new WorkingHours();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private int getHourIndex(String hourLabel) {
        for (int i = 0; i < hourLabels.length; i++) {
            if (hourLabels[i].equals(hourLabel)) {
                return i;
            }
        }
        return -1; // Hour label not found
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_working_hours, container, false);

        Button addBreakButton = view.findViewById(R.id.addBreakButton);
        Button confirmHoursButton = view.findViewById(R.id.confirmHoursButton);
        Button backfromScheduleButton = view.findViewById(R.id.backScheduleButton);

        Spinner daySpinner = view.findViewById(R.id.daySpinner);
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, daysOfWeek);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdapter);
        String day = daySpinner.getSelectedItem().toString();

        NumberPicker fromHourPicker = view.findViewById(R.id.fromHourPicker);
        fromHourPicker.setMinValue(0);
        fromHourPicker.setMaxValue(hourLabels.length - 1);
        fromHourPicker.setDisplayedValues(hourLabels);

        NumberPicker toHourPicker = view.findViewById(R.id.toHourPicker);
        toHourPicker.setMinValue(0);
        toHourPicker.setMaxValue(hourLabels.length - 1);
        toHourPicker.setDisplayedValues(hourLabels);

        confirmHoursButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String day = daySpinner.getSelectedItem().toString();
                int fromHourIndex = fromHourPicker.getValue();
                int toHourIndex = toHourPicker.getValue();

                List<String> workingHoursList = new ArrayList<>();

                if (fromHourIndex == toHourIndex) {
                    if (fromHourIndex == 0) {
                        for (String hourLabel : hourLabels) {
                            workingHoursList.add(hourLabel);
                        }
                    } else{
                        Toast.makeText(getActivity(), "Invalid hours", Toast.LENGTH_LONG).show();
                    }
                } else if (fromHourIndex < toHourIndex) {
                    for (int i = fromHourIndex; i < toHourIndex; i++) {
                        workingHoursList.add(hourLabels[i]);
                    }
                } else {
                    Toast.makeText(getActivity(), "Invalid hours", Toast.LENGTH_LONG).show();
                }


                DatabaseReference scheduleRef = FirebaseDatabase.getInstance().getReference("schedule").child(day);
                scheduleData schedule = new scheduleData(day, workingHoursList);
                scheduleRef.setValue(schedule);
                Toast.makeText(getActivity(), "Working hours for " + day + " have been changed", Toast.LENGTH_LONG).show();

            }
        });

        addBreakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBreak(daySpinner, fromHourPicker, toHourPicker);
            }
        });

        backfromScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_workingHours_to_menuScreen);
            }
        });


        return view;
    }

    private void addBreak(Spinner daySpinner, NumberPicker fromHourPicker,NumberPicker toHourPicker) {
        int fromHour = fromHourPicker.getValue();
        int toHour = toHourPicker.getValue();
        String day = daySpinner.getSelectedItem().toString();

        Log.d("WorkingHours", "From Hour: " + fromHour + ", To Hour: " + toHour);

        DatabaseReference scheduleRef = FirebaseDatabase.getInstance().getReference("schedule").child(day);

        if (fromHour == toHour){
            if (fromHour == 0) {
                for (String hourLabel : hourLabels) {
                    scheduleRef.removeValue();
                    Toast.makeText(getActivity(), "Working hours for " + day + " have been changed", Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(getActivity(), "Please make sure the break hours stay on the same day", Toast.LENGTH_LONG).show();
            }
        }else if (fromHour < toHour){
            for (int i = fromHour; i < toHour; i++) {
                scheduleRef.child("hours").orderByValue().equalTo(hourLabels[i]).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            childSnapshot.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                    }
                });
            }
            Toast.makeText(getActivity(), "Working hours for " + day + " have been changed", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getActivity(), "Invalid hours", Toast.LENGTH_LONG).show();
        }
    }
}
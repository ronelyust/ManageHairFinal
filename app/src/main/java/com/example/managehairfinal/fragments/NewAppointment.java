package com.example.managehairfinal.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.managehairfinal.activities.MainActivity;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;

import com.example.managehairfinal.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewAppointment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewAppointment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private CompactCalendarView compactCalendarView;
    private MaterialTextView textSelectedDate;
    private MaterialButton buttonScheduleAppointment;
    private MaterialTextView textMonthYear;

    private FirebaseAuth mAuth;

    public NewAppointment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NewAppointment.
     */
    // TODO: Rename and change types and number of parameters
    public static NewAppointment newInstance(String param1, String param2) {
        NewAppointment fragment = new NewAppointment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_new_appointment, container, false);

        compactCalendarView = view.findViewById(R.id.calendar_view);
        textSelectedDate = view.findViewById(R.id.text_selected_date);
        ImageButton buttonScheduleAppointment = view.findViewById(R.id.button_schedule_appointment);
        textMonthYear = view.findViewById(R.id.text_month_year);
        mAuth = ((MainActivity) requireActivity()).getFirebaseAuth();
        ImageButton backbutton = view.findViewById(R.id.scheduleBack_button);

        // Set initial month and year text
        updateMonthYearText(compactCalendarView.getFirstDayOfCurrentMonth());

        // Set the current date
        Date currentDate = new Date();
        textSelectedDate.setText(currentDate.toString());

        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                // Formatting the dates.
                Date currentDate = new Date();
                SimpleDateFormat ymd = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                String clickedDateString = ymd.format(dateClicked);
                String currentDateString = ymd.format(currentDate);

                // If the date selected is in the past set it as N/A.
                if (clickedDateString.compareTo(currentDateString) < 0) {
                    textSelectedDate.setText("N/A");
                } else {
                    textSelectedDate.setText(dateClicked.toString());
                }
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                // Handle month scroll event
                updateMonthYearText(firstDayOfNewMonth);
            }
        });

        buttonScheduleAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Taking the selected date and sending it to the Schedule Appointment fragment in a bundle.
                String selectedDate = textSelectedDate.getText().toString();

                // If the date is in the past, you can't select it.
                if (textSelectedDate.getText().equals("N/A"))
                {
                    Toast.makeText(requireContext(), "Cannot select past dates", Toast.LENGTH_SHORT).show();
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString("selectedDate", selectedDate);
                    Navigation.findNavController(view).navigate(R.id.action_newAppointment_to_scheduleAppointment, bundle);
                }
            }
        });

        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Moving back to the Menu fragment.
                Navigation.findNavController(view).navigate(R.id.action_newAppointment_to_menuScreen);
            }
        });

        return view;
    }

    private void updateMonthYearText(Date date) {
        // Setting the text of each Month & Year.
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        String monthYearString = sdf.format(date);
        textMonthYear.setText(monthYearString);
    }
}
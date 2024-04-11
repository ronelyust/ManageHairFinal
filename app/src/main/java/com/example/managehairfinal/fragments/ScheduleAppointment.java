package com.example.managehairfinal.fragments;

import static com.example.managehairfinal.activities.MainActivity.getAdminUid;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.Button;

import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import com.example.managehairfinal.activities.MainActivity;
import com.example.managehairfinal.classes.AppointmentHourAdapter;
import com.example.managehairfinal.R;
import com.example.managehairfinal.model.appointmentData;
import com.example.managehairfinal.model.scheduleData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScheduleAppointment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScheduleAppointment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private RecyclerView recyclerView;
    private AppointmentHourAdapter adapter;
    private FirebaseAuth mAuth;



    public ScheduleAppointment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ScheduleAppointment.
     */
    // TODO: Rename and change types and number of parameters
    public static ScheduleAppointment newInstance(String param1, String param2) {
        ScheduleAppointment fragment = new ScheduleAppointment();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_schedule_appointment, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_appointment_hours);
        mAuth = ((MainActivity) requireActivity()).getFirebaseAuth();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String selectedDate = getArguments().getString("selectedDate");

        // Initialize RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);

        // Getting user information.
        FirebaseUser user = mAuth.getCurrentUser();
        String uid = user.getUid();

        // Set adapter with appointment hours and appointment status
        adapter = new AppointmentHourAdapter(selectedDate, mAuth, this, getAdminUid().equals(uid));
        recyclerView.setAdapter(adapter);

        Button clearDayButton = view.findViewById(R.id.button_clear_day);

        // Admin gets a button to clear a day making it unavailable (or undo the process)
        if (getAdminUid().equals(uid)){
            clearDayButton.setEnabled(true);
            clearDayButton.setVisibility(View.VISIBLE);
            checkifCleared(selectedDate,new ClearDayCallback(){
                @Override
                public void onCheckCompleted(boolean wasCleared) {
                    if (wasCleared) {
                        clearDayButton.setText("Unclear Day");
                    } else {
                        clearDayButton.setText("Clear Day");
                    }
                }
            });

            clearDayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clearDayButton.getText().equals("Clear Day")) {
                        clearAppointmentsForDay(selectedDate, true);
                        clearDayButton.setText("Unclear Day");
                    } else {
                        clearAppointmentsForDay(selectedDate, false);
                        clearDayButton.setText("Clear Day");
                    }
                }
            });

        }

        Button backButton = view.findViewById(R.id.button_back_to_calendar);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the calendar fragment
                Navigation.findNavController(view).navigate(R.id.action_scheduleAppointment_to_newAppointment);
            }
        });

        getAvailableAppointmentHours(selectedDate);

    }

    private void checkifCleared(String selectedDate, ClearDayCallback callback){
        // Connecting to the appointment database and getting all the appointments for the day to check if they were cleared (cleared = not available but without user id).
        DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");
        String formattedDate = getFormattedDate(selectedDate);
        Query query = appointmentsRef.orderByKey().startAt(formattedDate + "_").endAt(formattedDate + "_\uf8ff");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean wasCleared = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    appointmentData appointment = snapshot.getValue(appointmentData.class);
                    if (appointment != null) {
                        String uid = appointment.getUid();
                        if (uid == null || uid.equals("")) {
                            wasCleared = true;
                            callback.onCheckCompleted(wasCleared);
                        }
                    }
                }
                callback.onCheckCompleted(wasCleared);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onCheckCompleted(false);
            }
        });
    }

    public interface ClearDayCallback {
        void onCheckCompleted(boolean wasCleared);
    }

    private void clearAppointmentsForDay(String selectedDate, boolean state) {
        // Connecting to the user and appointment databases and removing all the appointments for the selected date.
        DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");
        String formattedDate = getFormattedDate(selectedDate);
        Query query = appointmentsRef.orderByKey().startAt(formattedDate + "_").endAt(formattedDate + "_\uf8ff");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String appointmentKey = snapshot.getKey();
                    appointmentData appointment = snapshot.getValue(appointmentData.class);
                    if (appointment != null) {
                        String uid = appointment.getUid();
                        snapshot.getRef().removeValue();
                        if (uid != null) {
                            DatabaseReference userAppointmentsRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("appointments");
                            userAppointmentsRef.child(appointmentKey).removeValue();
                            snapshot.getRef().removeValue();
                        }
                    }
                }

                if(state) {
                    getAllPossibleAppointmentHours(selectedDate, new AppointmentHoursCallback() {
                        @Override
                        public void onAppointmentHoursReceived(List<String> allHours) {
                            markAllHoursAsUnavailable(selectedDate, allHours);

                            reloadAvailableAppointmentHours(selectedDate);
                        }
                    });
                } else {
                    reloadAvailableAppointmentHours(selectedDate);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled
            }
        });
    }


    private void markAllHoursAsUnavailable(String selectedDate, List<String> allHours) {
        String formattedDate = getFormattedDate(selectedDate);
        DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");

        for (String hour : allHours) {
            String appointmentKey = formattedDate + "_" + hour;
            appointmentData appointment = new appointmentData(formattedDate, hour, false, "", "", "");
            appointmentsRef.child(appointmentKey).setValue(appointment);
        }
    }

    public void reloadAvailableAppointmentHours(String selectedDate) {
        // Reload available appointment hours
        getAvailableAppointmentHours(selectedDate);
    }

    private void getAvailableAppointmentHours(String selectedDate) {

        String formattedSelectedDate = getFormattedDate(selectedDate);

        DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");
        appointmentsRef.orderByChild("date").equalTo(formattedSelectedDate).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                getAllPossibleAppointmentHours(selectedDate, new AppointmentHoursCallback() {
                    @Override
                    public void onAppointmentHoursReceived(List<String> appointmentHours) {
                        List<Boolean> isAvailableList = new ArrayList<>(Collections.nCopies(appointmentHours.size(), true));

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            appointmentData appointment = snapshot.getValue(appointmentData.class);
                            if (appointment != null) {
                                String appointmentTime = appointment.getTime();
                                int index = appointmentHours.indexOf(appointmentTime);
                                if (index != -1) {
                                    isAvailableList.set(index, false);
                                }
                            }
                        }

                        adapter.setAvailableAppointmentHours(appointmentHours, isAvailableList);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }

        });
    }

    private String getFormattedDate(String date) {
        SimpleDateFormat ymd = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);

        Date parsedDate = null;

        try {
            if (date.equals(getContext().getString(R.string.selected_date_placeholder))) {
                String selectedDate = ymd.format(new Date());
                parsedDate = ymd.parse(selectedDate);
            } else {
                parsedDate = inputFormat.parse(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String formattedDate = ymd.format(parsedDate);
        return formattedDate;

    }


    private void getAllPossibleAppointmentHours(String selectedDate, AppointmentHoursCallback callback) {
        String formattedSelectedDate = getFormattedDate(selectedDate);
        String day = getDayOfWeek(formattedSelectedDate);

        DatabaseReference scheduleRef = FirebaseDatabase.getInstance().getReference("schedule").child(day);

        scheduleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> workingHoursList = new ArrayList<>();
                scheduleData schedule = dataSnapshot.getValue(scheduleData.class);
                if (schedule != null && schedule.getHours() != null) {
                    for (String hour : schedule.getHours()) {
                        if (hour != null) {
                            workingHoursList.add(hour);
                        }
                    }
                }
                callback.onAppointmentHoursReceived(workingHoursList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    public interface AppointmentHoursCallback {
        void onAppointmentHoursReceived(List<String> appointmentHours);
    }


    public static String getDayOfWeek(String dateString) {
        SimpleDateFormat ymd = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        try {
            Date date = ymd.parse(dateString);
            Calendar calendar = Calendar.getInstance();
            if (date != null) {
                calendar.setTime(date);
            }
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            String[] daysOfWeek = new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
            return daysOfWeek[dayOfWeek - 1];
        } catch (ParseException e) {
            e.printStackTrace();
            return ""; // Return empty string or handle the error as needed
        }
    }
}

package com.example.managehairfinal.fragments;
import static com.example.managehairfinal.activities.MainActivity.getAdminUid;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.managehairfinal.R;
import com.example.managehairfinal.activities.MainActivity;
import com.example.managehairfinal.classes.UserAppointmentsAdapter;
import com.example.managehairfinal.model.appointmentData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserAppointments#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserAppointments extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerView recyclerView;
    private UserAppointmentsAdapter adapter;
    private FirebaseAuth mAuth;

    public UserAppointments() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserAppointments.
     */
    // TODO: Rename and change types and number of parameters
    public static UserAppointments newInstance(String param1, String param2) {
        UserAppointments fragment = new UserAppointments();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public void reloadAppointment(FirebaseUser user) {

        if (user != null) {
            String uid = user.getUid();
            DatabaseReference userAppointmentsRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("appointments");
            List<String> appointmentKeys = new ArrayList<>();
            DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");

            if (getAdminUid().equals(uid)){
                appointmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<String> appointmentKeys = new ArrayList<>();
                        for (DataSnapshot appointmentSnapshot : dataSnapshot.getChildren()) {
                            String appointmentKey = appointmentSnapshot.getKey();
                            appointmentKeys.add(appointmentKey);
                        }
                        fetchAppointmentData(appointmentKeys,true);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle onCancelled
                    }
                });
            } else {
                userAppointmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot appointmentSnapshot : dataSnapshot.getChildren()) {
                            String appointmentKey = appointmentSnapshot.getKey();
                            appointmentKeys.add(appointmentKey);
                        }

                        fetchAppointmentData(appointmentKeys, false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            adapter = new UserAppointmentsAdapter(new ArrayList<>(), mAuth, this);
            recyclerView.setAdapter(adapter);
        }
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
        View view = inflater.inflate(R.layout.fragment_user_appointments, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        mAuth = ((MainActivity) requireActivity()).getFirebaseAuth();

        adapter = new UserAppointmentsAdapter(new ArrayList<>(), mAuth, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseUser user = mAuth.getCurrentUser();

        reloadAppointment(user);

        Button backButton = view.findViewById(R.id.button_back_to_menu);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the calendar fragment
                Navigation.findNavController(view).navigate(R.id.action_userAppointments_to_menuScreen);
            }
        });

        return view;
    }


    private void fetchAppointmentData(List<String> appointmentKeys, Boolean isAdmin) {
        // Initialize a list to store appointment information
        List<appointmentData> appointmentDataList = new ArrayList<>();
        DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");

        // Retrieve relevant appointment data using the keys obtained from user data
        for (String appointmentKey : appointmentKeys) {
            appointmentsRef.child(appointmentKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Retrieve appointment data from dataSnapshot
                    appointmentData appointmentData = dataSnapshot.getValue(appointmentData.class);
                    if (appointmentData != null) {
                        if ((isAdmin) && (appointmentData.getUid() == null || appointmentData.getUid().isEmpty())) {
                            return;
                        }
                        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd");
                        SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy");
                        try {
                            Date date = inputFormat.parse(appointmentData.getDate());
                            String formattedDate = outputFormat.format(date);
                            appointmentData.setDate(formattedDate);
                        } catch (ParseException e) {
                            Log.e("FetchAppointments", "Error parsing date: " + e.getMessage());
                        }

                        appointmentDataList.add(appointmentData);
                    }

                    // Check if all appointments have been fetched
                    if (appointmentDataList.size() == appointmentKeys.size()) {
                        // Pass the appointmentDataList to your RecyclerView adapter
                        adapter = new UserAppointmentsAdapter(appointmentDataList, mAuth, UserAppointments.this);
                        recyclerView.setAdapter(adapter);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("FetchAppointments", "Failed to fetch appointment: " + databaseError.getMessage());
                }
            });
        }
    }
}
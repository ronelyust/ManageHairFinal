package com.example.managehairfinal.fragments;

import static android.content.ContentValues.TAG;

import com.example.managehairfinal.model.appointmentData;
import com.example.managehairfinal.model.userData;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.managehairfinal.R;
import com.example.managehairfinal.activities.MainActivity;
import com.example.managehairfinal.model.userData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserInfo#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserInfo extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FirebaseAuth mAuth;

    public UserInfo() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserInfo.
     */
    // TODO: Rename and change types and number of parameters
    public static UserInfo newInstance(String param1, String param2) {
        UserInfo fragment = new UserInfo();
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


    private void checkPhoneNumberAvailability(String phoneNumber, String password, String name, View view, userData user) {
        Query query = FirebaseDatabase.getInstance().getReference("users").orderByChild("phone_num").equalTo(phoneNumber);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Phone number is already in use
                    Toast.makeText(getActivity(), "Phone number already in use", Toast.LENGTH_SHORT).show();
                } else {
                    // Phone number is available, proceed with user registration
                    user.setPhone_num(phoneNumber);
                    updateUser(password, name, phoneNumber, view, user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(getActivity(), "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updateUser(String password, String name, String phoneNumber, View view, userData user) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users").child(mAuth.getCurrentUser().getUid());

        myRef.updateChildren(user.toMap())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "User information updated successfully", Toast.LENGTH_SHORT).show();

                            FirebaseUser firebaseUser = mAuth.getCurrentUser();

                            firebaseUser.updatePassword(password)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> passwordTask) {
                                            if (passwordTask.isSuccessful()) {
                                                // Password updated successfully in Firebase Authentication
                                                Toast.makeText(getActivity(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                                            } else {
                                                // Password update failed, handle the error
                                                Exception exception = passwordTask.getException();
                                                Toast.makeText(getActivity(), "Failed to update password: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                            updateAppointments(name, phoneNumber);

                        } else {
                            // Data update failed, handle the error
                            Exception exception = task.getException();
                            Toast.makeText(getActivity(), "Failed to update user information: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_info, container, false);
        mAuth = ((MainActivity) requireActivity()).getFirebaseAuth();

        Button changeInfoButton = view.findViewById(R.id.changeInfo_Button);
        Button backfrominfoButton = view.findViewById(R.id.backInfo_button);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users").child(mAuth.getCurrentUser().getUid());

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                userData user = snapshot.getValue(userData.class);
                if (user != null) {
                    // Populate EditText fields with user data
                    EditText infoPassword = view.findViewById(R.id.infoPassword);
                    EditText infoName = view.findViewById(R.id.infoName);
                    EditText infoPhone = view.findViewById(R.id.infoPhone);

                    infoPassword.setText(user.getPassword());
                    infoName.setText(user.getFull_name());
                    infoPhone.setText(user.getPhone_num());

                    changeInfoButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Get updated values from EditText fields
                            String newPassword = infoPassword.getText().toString().trim();
                            String newName = infoName.getText().toString().trim();
                            String newPhone = infoPhone.getText().toString().trim();

                            // Update user object with new values
                            user.setPassword(newPassword);
                            user.setFull_name(newName);

                            if (newPhone.equals(user.getPhone_num())) {
                                updateUser(newPassword, newName, newPhone, view, user);
                            } else {

                                if (newPassword.isEmpty()) {
                                    Toast.makeText(getActivity(), "Please enter your password", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (newPhone.isEmpty()) {
                                    Toast.makeText(getActivity(), "Please enter your phone number", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (newName.isEmpty()) {
                                    Toast.makeText(getActivity(), "Please enter your full name", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                checkPhoneNumberAvailability(newPhone, newPassword, newName, view, user);
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Empty implementation since we don't need to handle any logic for cancellation
            }
        });

        backfrominfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_userInfo_to_menuScreen);
            }
        });

        return view;
    }

    private void updateAppointments(String userName, String phoneNumber) {
        String uid = mAuth.getCurrentUser().getUid();
        DatabaseReference userAppointmentsRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("appointments");
        List<String> appointmentKeys = new ArrayList<>();

        userAppointmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot appointmentSnapshot : dataSnapshot.getChildren()) {
                    appointmentKeys.add(appointmentSnapshot.getKey());
                }

                if(appointmentKeys!=null){
                updateAppointmentDetailsInDatabase(appointmentKeys, userName, phoneNumber);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled
                Log.e(TAG, "Error retrieving user appointments: " + databaseError.getMessage());
            }
        });
    }

    private void updateAppointmentDetailsInDatabase(List<String> appointmentKeys, String userName, String phoneNumber) {

        DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");

        for (String appointmentKey : appointmentKeys) {
            DatabaseReference appointmentRef = appointmentsRef.child(appointmentKey);

            appointmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        appointmentData appointment = dataSnapshot.getValue(appointmentData.class);
                        if (appointment != null) {
                            appointment.setUserName(userName);
                            appointment.setPhoneNumber(phoneNumber);
                            appointmentRef.setValue(appointment); // Update appointment in the appointment database
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle onCancelled
                    Log.e(TAG, "Error updating appointment: " + databaseError.getMessage());
                }
            });
        }
    }
}
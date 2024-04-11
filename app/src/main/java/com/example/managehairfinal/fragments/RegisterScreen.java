package com.example.managehairfinal.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Patterns;

import com.example.managehairfinal.R;
import com.example.managehairfinal.activities.MainActivity;
import com.example.managehairfinal.model.userData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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
 * Use the {@link RegisterScreen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterScreen extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FirebaseAuth mAuth;

    public RegisterScreen() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RegisterScreen.
     */
    // TODO: Rename and change types and number of parameters
    public static RegisterScreen newInstance(String param1, String param2) {
        RegisterScreen fragment = new RegisterScreen();
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
        mAuth = ((MainActivity) requireActivity()).getFirebaseAuth();
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        //Checking if the number is valid.
        String phoneRegex = "^[+]?[0-9]{10,13}$";
        return phoneNumber.matches(phoneRegex);
    }


    private void checkPhoneNumberAvailability(String phoneNumber, String email, String password, String name, View view) {
        //Going through the database and checking if a user already registered with the number.
        Query query = FirebaseDatabase.getInstance().getReference("users").orderByChild("phone_num").equalTo(phoneNumber);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Phone number is already in use
                    Toast.makeText(getActivity(), "Phone number already in use", Toast.LENGTH_SHORT).show();
                } else {
                    // Phone number is available, proceed with user registration
                    registerUser(email, password, name, phoneNumber, view);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(getActivity(), "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void registerUser(String email, String password, String name, String phoneNumber, View view) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(getActivity(), "Registration successful", Toast.LENGTH_LONG).show();
                            String uid = user.getUid();
                            userData data = new userData();

                            // admin@gmail.com can be changed to other email, marking it as admin.

                            if (email.equals("admin@gmail.com")) {
                                data = new userData(name, phoneNumber, email, password, uid, true);
                            } else {
                                data = new userData(name, phoneNumber, email, password, uid, false);
                            }

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("users").child(data.uid);

                            myRef.setValue(data);

                            //moving to the Menu fragment after registration.
                            Navigation.findNavController(view).navigate(R.id.action_registerScreen_to_menuScreen);

                        } else {
                            // If sign in fails, display a message to the user.
                            Exception exception = task.getException();
                            Toast.makeText(getActivity(), "Registration failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_screen, container, false);

        Button completeRegButton = view.findViewById(R.id.completeReg_button);
        Button backfromReg = view.findViewById(R.id.backReg_button);

        completeRegButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = ((EditText) view.findViewById(R.id.regEmail)).getText().toString().trim();
                String password = ((EditText) view.findViewById(R.id.regPassword)).getText().toString().trim();
                String phone = ((EditText) view.findViewById(R.id.regPhone)).getText().toString().trim();
                String name = ((EditText) view.findViewById(R.id.regName)).getText().toString();

                // Validate email/password/phone number/name.
                if (email.isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter your email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter your password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (phone.isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter your phone number", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isValidPhoneNumber(phone)) {
                    Toast.makeText(getActivity(), "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (name.isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter your full name", Toast.LENGTH_SHORT).show();
                    return;
                }

                checkPhoneNumberAvailability(phone, email, password, name, view);
            }
        });

        // Moving back to the Login fragment.
        backfromReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_registerScreen_to_loginScreen);
            }
        });

        return view;
    }
}
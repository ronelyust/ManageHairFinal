package com.example.managehairfinal.fragments;
import static com.example.managehairfinal.activities.MainActivity.getAdminUid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.managehairfinal.R;
import com.example.managehairfinal.activities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MenuScreen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MenuScreen extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FirebaseAuth mAuth;

    public MenuScreen() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MenuScreen.
     */
    // TODO: Rename and change types and number of parameters
    public static MenuScreen newInstance(String param1, String param2) {
        MenuScreen fragment = new MenuScreen();
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
        View view = inflater.inflate(R.layout.fragment_menu_screen, container, false);

        mAuth = ((MainActivity) requireActivity()).getFirebaseAuth();
        FirebaseUser user = mAuth.getCurrentUser();
        String uid = user.getUid();
        Button newAppButton = view.findViewById(R.id.newappButton);
        Button userAppButton = view.findViewById(R.id.userappButton);
        Button userInfoButton = view.findViewById(R.id.userinfoButton);
        Button logOutButton = view.findViewById(R.id.menulogoutButton);
        Button workingHoursButton = view.findViewById(R.id.workhoursButton);

        // Moving to the Calendar fragment.
        newAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_menuScreen_to_newAppointment);
            }
        });

        // Moving to the User Appointments fragment.
        userAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_menuScreen_to_userAppointments);
            }
        });

        // Moving to the User Info fragment.
        userInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_menuScreen_to_userInfo);
            }
        });

        // Moving back to the Login fragment, singing out of the current user.
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Navigation.findNavController(view).navigate(R.id.action_menuScreen_to_loginScreen);
            }
        });

        // If a user is an admin he can move to the Working Hours fragment and set his work hours.
        if (getAdminUid().equals(uid)){
            workingHoursButton.setVisibility(View.VISIBLE);
            workingHoursButton.setEnabled(true);

            workingHoursButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Navigation.findNavController(view).navigate(R.id.action_menuScreen_to_workingHours);
                }
            });
        }

        return view;
    }
}
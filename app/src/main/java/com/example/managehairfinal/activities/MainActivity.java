package com.example.managehairfinal.activities;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.managehairfinal.R;
import com.example.managehairfinal.fragments.LoginScreen;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private static String adminUid = "3qzJTDjVctYduQGnrS4mCuw0PjQ2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
    }

    public static String getAdminUid() {
        return adminUid;
    }

    // Provide a method to access FirebaseAuth instance
    public FirebaseAuth getFirebaseAuth() {
        return mAuth;
    }
}
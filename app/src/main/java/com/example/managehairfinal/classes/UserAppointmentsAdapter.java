package com.example.managehairfinal.classes;
import static com.example.managehairfinal.activities.MainActivity.getAdminUid;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.managehairfinal.R; // Replace this with your actual package name
import com.example.managehairfinal.fragments.UserAppointments;
import com.example.managehairfinal.model.appointmentData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserAppointmentsAdapter extends RecyclerView.Adapter<UserAppointmentsAdapter.ViewHolder> {

    private List<appointmentData> appointmentList; // Replace String with your actual appointment model class
    private OnItemClickListener itemClickListener;
    private FirebaseAuth mAuth;
    private UserAppointments userAppointmentsFragment;


    public UserAppointmentsAdapter(List<appointmentData> appointmentList, FirebaseAuth mAuth, UserAppointments fragment) {
        this.appointmentList = appointmentList;
        this.mAuth = mAuth;
        this.userAppointmentsFragment = fragment;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_appointments, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        appointmentData appointment = appointmentList.get(position);
        holder.bind(appointment, userAppointmentsFragment);
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView appointmentDateTextView;
        private TextView appointmentTimeTextView;
        private TextView appointmentNameTextView;
        private TextView appointmentPhoneTextView;

        private Button removeButton;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appointmentDateTextView = itemView.findViewById(R.id.text_appointment_date);
            appointmentTimeTextView = itemView.findViewById(R.id.text_appointment_time);
            appointmentNameTextView = itemView.findViewById(R.id.text_appointment_fullName);
            appointmentPhoneTextView = itemView.findViewById(R.id.text_appointment_phoneNum);
            removeButton = itemView.findViewById(R.id.button_remove_appointment);
        }

        public void bind(appointmentData appointment, UserAppointments fragment) {
            FirebaseUser user = mAuth.getCurrentUser();
            String uid = user.getUid();
            boolean isAdmin = false;
            if (getAdminUid().equals(uid)){
                isAdmin = true;
            }
            String date = appointment.getDate();
            String hour = appointment.getTime();
            appointmentDateTextView.setText(date);
            appointmentTimeTextView.setText(hour);

            if (isAdmin){
                String name = appointment.getUserName();
                String phone = appointment.getPhoneNumber();
                appointmentNameTextView.setVisibility(View.VISIBLE);
                appointmentPhoneTextView.setVisibility(View.VISIBLE);
                appointmentNameTextView.setText(name);
                appointmentPhoneTextView.setText(phone);
            }

            String formattedDate;
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd.MM.yyyy");
            try {
                Date oridate = inputFormat.parse(date);
                formattedDate = outputFormat.format(oridate);

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");
                String appointmentKey = formattedDate + "_" + hour;

                removeButton.setOnClickListener(v -> {
                    // Handle removing appointment
                    DatabaseReference formattedAppRef = appointmentsRef.child(appointmentKey);
                    DatabaseReference myRef = database.getReference("users").child(appointment.getUid()).child("appointments");

                    formattedAppRef.removeValue()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    myRef.child(appointmentKey).removeValue();
                                    Toast.makeText(itemView.getContext(), "Appointment for " + date + " at " + hour + " has been canceled", Toast.LENGTH_SHORT).show();
                                    fragment.reloadAppointment(user);
                                } else {

                                }
                            });
                });

            } catch (ParseException e) {
                Log.e("FetchAppointments", "Error parsing date: " + e.getMessage());
            }
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(getAdapterPosition());
            }
        }
    }

    // Interface for handling item click events
    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}

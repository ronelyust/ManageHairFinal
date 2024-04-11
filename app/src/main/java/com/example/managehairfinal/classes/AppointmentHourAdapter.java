                package com.example.managehairfinal.classes;
                import static com.example.managehairfinal.activities.MainActivity.getAdminUid;
                import static java.sql.Types.NULL;

                import com.example.managehairfinal.fragments.ScheduleAppointment;

                import android.util.Log;
                import android.view.LayoutInflater;
                import android.widget.Button;
                import android.widget.Toast;
                import android.view.View;
                import android.view.ViewGroup;
                import android.widget.TextView;
                import androidx.annotation.NonNull;
                import androidx.recyclerview.widget.RecyclerView;

                import com.example.managehairfinal.R;
                import com.example.managehairfinal.activities.MainActivity;
                import com.example.managehairfinal.model.appointmentData;
                import com.example.managehairfinal.model.userData;
                import com.google.android.gms.tasks.OnCompleteListener;
                import com.google.android.gms.tasks.Task;
                import com.google.firebase.auth.FirebaseAuth;
                import com.google.firebase.auth.FirebaseUser;
                import com.google.firebase.database.DataSnapshot;
                import com.google.firebase.database.DatabaseError;
                import com.google.firebase.database.DatabaseReference;
                import com.google.firebase.database.FirebaseDatabase;
                import com.google.firebase.database.Query;
                import com.google.firebase.database.ValueEventListener;

                import java.text.ParseException;
                import java.text.SimpleDateFormat;
                import java.util.ArrayList;
                import java.util.Collections;
                import java.util.Date;
                import java.util.List;
                import java.util.Locale;
                import java.util.concurrent.atomic.AtomicBoolean;


                public class AppointmentHourAdapter extends RecyclerView.Adapter<AppointmentHourAdapter.ViewHolder> {

                    private String appointmentDate;

                    private List<String> appointmentHours;
                    private List<Boolean> availabilityStatus;

                    private FirebaseAuth mAuth;

                    private ScheduleAppointment scheduleAppointment;

                    public AppointmentHourAdapter(String appointmentDate, FirebaseAuth mAuth, ScheduleAppointment scheduleAppointment, boolean isAdmin) {
                        this.appointmentDate = appointmentDate;
                        this.mAuth = mAuth;
                        this.appointmentHours = new ArrayList<>();
                        this.availabilityStatus = new ArrayList<>();
                        this.scheduleAppointment = scheduleAppointment;
                    }

                    @NonNull
                    @Override
                    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.item_appointment_hour, parent, false);

                        return new ViewHolder(view, scheduleAppointment);
                    }

                    @Override
                    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                        String hour = appointmentHours.get(position);
                        String date = appointmentDate;
                        boolean isAvailable = availabilityStatus.get(position);
                        holder.bind(date, hour, isAvailable);
                    }

                    // Setter method to set available appointment hours
                    public void setAvailableAppointmentHours(List<String> appointmentHours, List<Boolean> availabilityStatus) {
                        this.appointmentHours.clear(); // Clear existing data
                        this.availabilityStatus.clear(); // Clear existing data
                        this.appointmentHours.addAll(appointmentHours); // Add new data
                        this.availabilityStatus.addAll(availabilityStatus); // Add new data

                        notifyDataSetChanged(); // Notify adapter about the data change
                    }

                    @Override
                    public int getItemCount() {
                        return appointmentHours.size();
                    }

                    public class ViewHolder extends RecyclerView.ViewHolder {

                        private TextView textAppointmentHour;
                        private TextView textAppointmentName;
                        private TextView textAppointmentPhone;

                        private Button buttonAddAppointment;
                        private Button buttonRemoveAppointment;
                        private ScheduleAppointment scheduleAppointment;

                        public ViewHolder(@NonNull View itemView, ScheduleAppointment scheduleAppointment) {
                            super(itemView);
                            textAppointmentHour = itemView.findViewById(R.id.text_appointment_hour);
                            textAppointmentName = itemView.findViewById(R.id.text_appointment_name);
                            textAppointmentPhone = itemView.findViewById(R.id.text_appointment_phone);
                            buttonAddAppointment = itemView.findViewById(R.id.button_add_appointment);
                            buttonRemoveAppointment = itemView.findViewById(R.id.button_remove_appointment);
                            this.scheduleAppointment = scheduleAppointment;
                        }

                        public void bind(String date, String hour, boolean isAvailable) {
                            textAppointmentHour.setText(hour);
                            FirebaseUser user = mAuth.getCurrentUser();
                            String uid = user.getUid();
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("users").child(uid);
                            SimpleDateFormat ymd = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

                            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            Date currentDate = new Date();

                            String formattedDate = getFormattedDate(date);
                            String appointmentKey = formattedDate + "_" + hour;

                            if (formattedDate.equals(ymd.format(currentDate)) && hour.compareTo(timeFormat.format(currentDate)) < 0) {
                                buttonAddAppointment.setEnabled(false);
                                buttonAddAppointment.setVisibility(View.GONE);
                                buttonRemoveAppointment.setEnabled(false);
                                buttonRemoveAppointment.setVisibility(View.VISIBLE);
                                buttonRemoveAppointment.setText("Unavailable");
                                buttonRemoveAppointment.setOnClickListener(null);
                            } else if (isAvailable) {
                                buttonAddAppointment.setEnabled(true);
                                buttonAddAppointment.setVisibility(View.VISIBLE);
                                buttonRemoveAppointment.setEnabled(false);
                                buttonRemoveAppointment.setVisibility(View.GONE);

                                buttonAddAppointment.setOnClickListener(v -> {
                                    // Handle adding appointment
                                    Toast.makeText(itemView.getContext(), "Appointment added for " + date + " at " + hour, Toast.LENGTH_SHORT).show();

                                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists() && dataSnapshot.hasChild("full_name") && dataSnapshot.hasChild("phone_num")) {
                                                // Get the value of the full name child
                                                String fullName = dataSnapshot.child("full_name").getValue(String.class);
                                                String phoneNum = dataSnapshot.child("phone_num").getValue(String.class);

                                                if (fullName != null && phoneNum != null) {
                                                    appointmentData appointment = new appointmentData(formattedDate, hour, false, fullName, phoneNum, uid);
                                                    myRef.child("appointments").child(appointmentKey).setValue(true);
                                                    DatabaseReference appRef = database.getReference("appointments").child(appointmentKey);
                                                    appRef.setValue(appointment);
                                                    scheduleAppointment.reloadAvailableAppointmentHours(date);

                                                } else {
                                                    Toast.makeText(itemView.getContext(), "Error", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(itemView.getContext(), "Error", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            // Handle any errors that may occur during the data retrieval process
                                        }
                                    });
                                });

                            } else {
                                DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");

                                appointmentsRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.exists()) {
                                            for (DataSnapshot appointmentSnapshot : dataSnapshot.getChildren()) {
                                                appointmentData appointment = appointmentSnapshot.getValue(appointmentData.class);

                                                if (appointment != null && appointment.getUid() != null) {
                                                    if (appointment.getTime().equals(hour)) {
                                                        if ((appointment.getUid().equals(uid)) || (getAdminUid().equals(uid))) {
                                                            buttonAddAppointment.setEnabled(false);
                                                            buttonAddAppointment.setVisibility(View.GONE);
                                                            buttonRemoveAppointment.setEnabled(true);
                                                            buttonRemoveAppointment.setVisibility(View.VISIBLE);
                                                            buttonRemoveAppointment.setText("Remove Appointment");

                                                            buttonRemoveAppointment.setOnClickListener(v -> {
                                                                // Handle removing appointment
                                                                DatabaseReference formattedAppRef = appointmentsRef.child(appointmentKey);
                                                                DatabaseReference myRef = database.getReference("users").child(appointment.getUid()).child("appointments");

                                                                formattedAppRef.removeValue()
                                                                        .addOnCompleteListener(task -> {
                                                                            if (task.isSuccessful()) {
                                                                                myRef.child(appointmentKey).removeValue();
                                                                                Toast.makeText(itemView.getContext(), "Appointment for " + date + " at " + hour + " has been canceled", Toast.LENGTH_SHORT).show();
                                                                                scheduleAppointment.reloadAvailableAppointmentHours(date);
                                                                            } else {
                                                                                // Handle error
                                                                            }
                                                                        });
                                                            });

                                                            if (getAdminUid().equals(uid)) {
                                                                textAppointmentName.setText(appointment.getUserName());
                                                                textAppointmentName.setVisibility(View.VISIBLE);
                                                                textAppointmentPhone.setText(appointment.getPhoneNumber());
                                                                textAppointmentPhone.setVisibility(View.VISIBLE);
                                                            }
                                                        } else {
                                                            buttonAddAppointment.setEnabled(false);
                                                            buttonAddAppointment.setVisibility(View.GONE);
                                                            buttonRemoveAppointment.setEnabled(false);
                                                            buttonRemoveAppointment.setVisibility(View.VISIBLE);
                                                            buttonRemoveAppointment.setText("Unavailable");
                                                            buttonRemoveAppointment.setOnClickListener(null);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e("AppointmentHourAdapter", "Error reading appointments: " + error.getMessage());
                                    }
                                });
                            }
                        }

                        private String getFormattedDate(String date) {
                            SimpleDateFormat ymd = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);

                            Date parsedDate = null;

                            try {
                                if (date.equals(itemView.getContext().getString(R.string.selected_date_placeholder))) {
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

                    }
                }
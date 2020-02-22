package com.example.empat.Fragments;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.empat.Model.CheckInCheckOutTime;
import com.example.empat.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class SubmitAttendanceFragment extends Fragment
{

    private static final String CURRENT_EMPLOYEE_CODE = "current_employee_code";
    private String employeeCode;
    private Button checkInAttendance, checkOutAttendance;
    private FirebaseAuth firebaseAuth;
    String currentTime, currentDate;
    private DatabaseReference employeeCheckInCheckOutDatabaseReference;
    private SimpleDateFormat presentTime, presentDate;
    private ProgressBar progressBar;
    private TextView checkInCheckOutMessage;
    CheckInCheckOutTime checkInCheckOutTime = null;
    public SubmitAttendanceFragment()
    {
        // Required empty public constructor
    }

    public static SubmitAttendanceFragment newInstance(String employeeCode)
    {
        SubmitAttendanceFragment submitAttendanceFragment = new SubmitAttendanceFragment();
        Bundle args = new Bundle();
        args.putString(CURRENT_EMPLOYEE_CODE, employeeCode);
        submitAttendanceFragment.setArguments(args);
        return submitAttendanceFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_submit_attendance, container, false);
        if(view != null)
        {
            init(view);
            checkInAttendance.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    //Check in Function
                    checkIn(employeeCode, currentDate, "Pending Check Out", currentTime, "Pending Check Out");
                }
            });


            checkOutAttendance.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    //Check out Function
                    checkOut(employeeCode, checkInCheckOutTime.getCheckInDate(), currentDate, checkInCheckOutTime.getCheckInTime(), currentTime);
                }
            });

        }
        return view;
    }

    @SuppressLint("SimpleDateFormat")
    private void init(View view)
    {
        if(getArguments() != null)
        {
            employeeCode = getArguments().getString(CURRENT_EMPLOYEE_CODE);
        }
        checkInAttendance = view.findViewById(R.id.check_in_attendance);
        checkOutAttendance = view.findViewById(R.id.check_out_attendance);
        firebaseAuth = FirebaseAuth.getInstance();
        presentTime = new SimpleDateFormat("hh:mm a");
        presentDate = new SimpleDateFormat("dd-MMM-YYYY");
        progressBar = view.findViewById(R.id.progress_bar);
        checkInCheckOutMessage = view.findViewById(R.id.checkInCheckOutMessage);

        initialiseDateAndTime();
        getAttendanceStatus(employeeCode);
    }

    private void initialiseDateAndTime()
    {
        Date date = new Date();
        currentTime = presentTime.format(date);
        currentDate = presentDate.format(date);
    }

    private void getAttendanceStatus(String employeeCode)
    {
        //Change Below DB reference to the new astrologers one

        employeeCheckInCheckOutDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Employee Attendance")
                .child(employeeCode);//The Root reference for each employee
        employeeCheckInCheckOutDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener()
        {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.hasChild(currentDate))
                {
                    try
                    {
                        for (DataSnapshot userSnapshot: dataSnapshot.getChildren())
                        {
                            if(userSnapshot.getKey().equals(currentDate))
                            {
                                checkInCheckOutTime = userSnapshot.getValue(CheckInCheckOutTime.class);// Assigning the database data to the model object
                            }
                        }
                        updateViewsForCheckOut();
                    } catch(Exception e)
                    {
                        Log.e("Attendance Status Check", "Data interchange failed. Exception: <<< " + e.getMessage() + " >>>.");
                    }
                }
                else
                {
                    updateViewsForCheckIn();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.w("Attendance Status Check", "Database error : " + databaseError.toException() + " >>>");
            }
        });
    }

    private void updateViewsForCheckOut()
    {
        progressBar.setVisibility(View.GONE);
        checkInCheckOutMessage.setText("Hello\n\n Your last check in was on : !\n\n" + checkInCheckOutTime.getCheckInDate() + " " + checkInCheckOutTime.getCheckInTime());
        checkOutAttendance.setVisibility(View.VISIBLE);
        checkInAttendance.setVisibility(View.GONE);
    }

    private void updateViewsForCheckIn()
    {
        progressBar.setVisibility(View.GONE);
        checkInCheckOutMessage.setText("Hello\n\n You are yet to check in today!\n\n");
        checkOutAttendance.setVisibility(View.GONE);
        checkInAttendance.setVisibility(View.VISIBLE);
    }

    private void checkIn(String employeeCode, String checkInDate, String checkOutDate, String checkInTime, String checkOutTime)
    {
        progressBar.setVisibility(View.VISIBLE);
        final CheckInCheckOutTime checkInCheckOutTime = new CheckInCheckOutTime(checkInDate, checkOutDate, checkInTime, checkOutTime);

        employeeCheckInCheckOutDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Employee Attendance")
                .child(employeeCode).child(currentDate);
        employeeCheckInCheckOutDatabaseReference.setValue(checkInCheckOutTime).addOnSuccessListener(new OnSuccessListener<Void>()
        {
            public void onSuccess(Void aVoid) // If the task is successful i. e registration successful
            {
                checkInAttendance.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Check In Successful", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() // If after the task fails after initiation then either connectivity issue or FireBase down or node not found
        {
            public void onFailure(@NonNull Exception e)
            {
                Toast.makeText(getContext(), "Check In Failed", Toast.LENGTH_SHORT).show(); // If registration fails
            }
        });//The Root reference for each employee

    }

    private void checkOut(String employeeCode, String checkInDate, String checkOutDate, String checkInTime, String checkOutTime)
    {
        progressBar.setVisibility(View.VISIBLE);
        CheckInCheckOutTime checkInCheckOutTime = new CheckInCheckOutTime(checkInDate, checkOutDate, checkInTime, checkOutTime);

        employeeCheckInCheckOutDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Employee Attendance")
                .child(employeeCode).child(currentDate);
        employeeCheckInCheckOutDatabaseReference.setValue(checkInCheckOutTime).addOnSuccessListener(new OnSuccessListener<Void>()
        {
            public void onSuccess(Void aVoid) // If the task is successful i. e registration successful
            {
                progressBar.setVisibility(View.GONE);
                checkOutAttendance.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Check Out Successful", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() // If after the task fails after initiation then either connectivity issue or FireBase down or node not found
        {
            public void onFailure(@NonNull Exception e)
            {
                Toast.makeText(getContext(), "Check Out Failed", Toast.LENGTH_SHORT).show(); // If registration fails
            }
        });//The Root reference for each employee

    }
}

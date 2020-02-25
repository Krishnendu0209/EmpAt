package com.example.empat.Fragments;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.empat.Model.CheckInCheckOutTime;
import com.example.empat.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class AttendanceModificationFragment extends Fragment
{
    private EditText checkInDate, checkInTime, checkOutDate, checkOutTime, employeeCode;
    private TextView attendanceDate;
    private LinearLayout attendanceDetailsView, attendanceRequiredDetails;
    private Button fetchRecord, deleteRecord, confirmModification, unlockRecord;
    private ProgressBar progressBar;
    private DatabaseReference employeeAttendance;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private String currentDate, currentTime, tempEmployeeCode, checkInDateText, checkInTimeText, checkOutDateText, checkOutTimeText;
    private FrameLayout operationCompletionFrame;
    private TextView successfulMessage;

    public AttendanceModificationFragment()
    {
        // Required empty public constructor
    }

    public static AttendanceModificationFragment newInstance(String employeeCode)
    {
        AttendanceModificationFragment attendanceModificationFragment = new AttendanceModificationFragment();
        return attendanceModificationFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_attendance_modification, container, false);
        init(view);

        attendanceDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                datePicker(1);
            }
        });
        checkInDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                datePicker(2);
            }
        });
        checkOutDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                datePicker(3);
            }
        });
        checkInTime.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                timePicker(1);
            }
        });
        checkOutTime.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                timePicker(2);
            }
        });
        fetchRecord.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                tempEmployeeCode = employeeCode.getText().toString();
                if(tempEmployeeCode.equals(""))
                {
                    Toast.makeText(getContext(), "Enter Employee Code", Toast.LENGTH_LONG).show();
                    return;
                }
                if(attendanceDate.getText().equals("Click To Select Transaction Date"))
                {
                    Toast.makeText(getContext(), "Enter Attendance date", Toast.LENGTH_LONG).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                fetchAndDisplayEmployeeDetails(tempEmployeeCode, currentDate);

            }
        });

        unlockRecord.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                unlockUI();
            }
        });
        deleteRecord.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                deleteEmployeeDetailsAndAttendanceRecord(tempEmployeeCode, currentDate);
                progressBar.setVisibility(View.GONE);
            }
        });
        confirmModification.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                detailsViewToStringConversion(); // converting all the view data to string data
                if(validations()) //if the data provided is valid then proceed with registration of the user
                {
                    progressBar.setVisibility(View.VISIBLE);
                    modifyEmployeeAttendance(tempEmployeeCode, checkInDateText, checkOutDateText, checkInTimeText, checkOutTimeText);
                }
            }
        });
        return view;
    }

    private void init(View view)
    {
        checkInDate = view.findViewById(R.id.checkInDate);
        checkInTime = view.findViewById(R.id.checkInTime);
        checkOutDate = view.findViewById(R.id.checkOutDate);
        checkOutTime = view.findViewById(R.id.checkoutTime);
        attendanceDate = view.findViewById(R.id.attendanceDate);
        successfulMessage = view.findViewById(R.id.successfulMessage);
        employeeCode = view.findViewById(R.id.employeeCode);
        progressBar = view.findViewById(R.id.progress_bar);
        attendanceDetailsView = view.findViewById(R.id.attendanceDetailsView);
        attendanceRequiredDetails = view.findViewById(R.id.attendanceRequiredDetails);
        fetchRecord = view.findViewById(R.id.fetch_record);
        confirmModification = view.findViewById(R.id.confirm_modification);
        deleteRecord = view.findViewById(R.id.delete_record);
        unlockRecord = view.findViewById(R.id.unlock_record);
        operationCompletionFrame = view.findViewById(R.id.operation_completion_frame);
    }

    private void fetchAndDisplayEmployeeDetails(final String employeeCode, final String currentDate) // Function is responsible for fetching details corresponding to employee code form FireBase
    {
        employeeAttendance = FirebaseDatabase.getInstance().getReference()
                .child("Employee Attendance").child(employeeCode);
        employeeAttendance.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.hasChild(currentDate))
                {
                    try
                    {
                        CheckInCheckOutTime checkInCheckOutTime = null;
                        for(DataSnapshot userSnapshot : dataSnapshot.getChildren())
                        {
                            if(userSnapshot.getKey().equals(currentDate))
                            {
                                Toast.makeText(getContext(), "Heloooo", Toast.LENGTH_LONG).show();
                                checkInCheckOutTime = userSnapshot.getValue(CheckInCheckOutTime.class);// Assigning the database data to the model object
                            }
                        }
                        if(checkInCheckOutTime != null)
                        {
                            setUpUIOnDetailsFetched(checkInCheckOutTime);
                        }
                        else
                        {
                            progressBar.setVisibility(View.GONE);
                        }
                    } catch(Exception e)
                    {
                        Log.e("fetDisEmpAttenData", "Data interchange failed. Exception: <<< " + e.getMessage() + " >>>.");
                    }
                }
                else
                {
                    //Employee Code not found
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Date Not Found!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.w("fetDisEmpAttenData", "Database error : " + databaseError.toException() + " >>>");
            }
        });
    }

    private void setUpUIOnDetailsFetched(CheckInCheckOutTime checkInCheckOutTime) //When details are fetched
    {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(getContext(), "Employee Record Fetched", Toast.LENGTH_LONG).show();

        attendanceDetailsView.setVisibility(View.VISIBLE);
        attendanceRequiredDetails.setVisibility(View.GONE);

        unlockRecord.setVisibility(View.VISIBLE);
        deleteRecord.setVisibility(View.VISIBLE);

        checkInDate.setText(checkInCheckOutTime.checkInDate);
        checkInDate.setEnabled(false);

        checkInTime.setText(checkInCheckOutTime.checkInTime);
        checkInTime.setEnabled(false);

        checkOutDate.setText(checkInCheckOutTime.checkOutDate);
        checkOutDate.setEnabled(false);

        checkOutTime.setText(checkInCheckOutTime.checkOutTime);
        checkOutTime.setEnabled(false);
    }

    private void datePicker(final int viewSelector)
    {
        // Get Current Date
        final Calendar calendar = Calendar.getInstance();
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                new DatePickerDialog.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth)
                    {
                        calendar.set(year, monthOfYear, dayOfMonth);
                        SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
                        currentDate = format.format(calendar.getTime());
                        if(viewSelector == 1)
                        {
                            attendanceDate.setText(currentDate);
                        }
                        else if(viewSelector == 2) //Check in Date
                        {
                            checkInDate.setText(currentDate);
                        }
                        else if(viewSelector == 3) //Check in Date
                        {
                            checkOutDate.setText(currentDate);
                        }
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    private void timePicker(final int viewSelector)
    {
        // Get Current Time
        final Calendar calendar = Calendar.getInstance();
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                new TimePickerDialog.OnTimeSetListener()
                {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute)
                    {
                        if(viewSelector == 1) //Check in Time
                        {
                            int hour = hourOfDay % 12;
                            checkInTime.setText(String.format("%02d:%02d %s", hour == 0 ? 12 : hour,
                                    minute, hourOfDay < 12 ? "am" : "pm"));
                        }
                        else if(viewSelector == 2) //Check out Time
                        {
                            int hour = hourOfDay % 12;
                            checkOutTime.setText(String.format("%02d:%02d %s", hour == 0 ? 12 : hour,
                                    minute, hourOfDay < 12 ? "am" : "pm"));
                        }
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    private void unlockUI()
    {
        deleteRecord.setVisibility(View.GONE);
        checkInDate.setEnabled(true);
        checkInDate.setFocusable(false);

        checkInTime.setEnabled(true);
        checkInTime.setFocusable(false);

        checkOutDate.setEnabled(true);
        checkOutDate.setFocusable(false);

        checkOutTime.setEnabled(true);
        checkOutTime.setFocusable(false);

        unlockRecord.setVisibility(View.GONE);
        confirmModification.setVisibility(View.VISIBLE);
    }

    private void deleteEmployeeDetailsAndAttendanceRecord(String employeeCode, String currentDate)
    {
        progressBar.setVisibility(View.VISIBLE);
        employeeAttendance = FirebaseDatabase.getInstance().getReference(); // Add the reference
        employeeAttendance.child("Employee Attendance").child(employeeCode).child(currentDate).removeValue();// Complete attendance record of employee deleted
        operationCompletion(2);
    }

    private void modifyEmployeeAttendance(final String employeeCode, String checkInDate, String checkOutDate, String checkInTime, String checkOutTime)
    {
        CheckInCheckOutTime checkInCheckOutTime = new CheckInCheckOutTime(checkInDate, checkOutDate, checkInTime, checkOutTime);
        employeeAttendance = FirebaseDatabase.getInstance().getReference(); // Add the reference
        employeeAttendance.child("Employee Attendance").child(employeeCode).child(currentDate).setValue(checkInCheckOutTime).addOnSuccessListener(new OnSuccessListener<Void>()
        {
            public void onSuccess(Void aVoid) // If the task is successful i. e registration successful
            {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Modification Successful", Toast.LENGTH_SHORT).show();
                operationCompletion(1);
            }
        }).addOnFailureListener(new OnFailureListener() // If after the task fails after initiation then either connectivity issue or FireBase down or node not found
        {
            public void onFailure(@NonNull Exception e)
            {
                Toast.makeText(getContext(), "Modification Failed", Toast.LENGTH_SHORT).show(); // If registration fails
            }
        });
    }

    private boolean validations()
    {
        if(checkInDateText.equals("")) // Checks if employee code field is empty or not if yes then should not proceed else proceed
        {
            if(getContext() != null)
            {
                Toast.makeText(getContext(), "Check In Date!", Toast.LENGTH_SHORT).show();
                checkInDate.requestFocus();
                return false;
            }
        }
        if(checkInTimeText.equals("")) // Checks if name field is empty or not if yes then should not proceed else proceed
        {
            if(getContext() != null)
            {
                Toast.makeText(getContext(), "Check In Time!", Toast.LENGTH_SHORT).show();
                checkInTime.requestFocus();
                return false;
            }
        }

        if(checkOutDateText.equals(""))//If phone number field is empty then don't proceed
        {
            if(getContext() != null)
            {
                Toast.makeText(getContext(), "Check Out Date!", Toast.LENGTH_SHORT).show();
                checkOutDate.requestFocus();
                return false;
            }
        }

        if(checkOutTimeText.equals("")) // Checks if email field is empty or not if yes then should not proceed
        {
            if(getContext() != null)
            {
                Toast.makeText(getContext(), "Check Out Time!", Toast.LENGTH_SHORT).show();
                checkOutTime.requestFocus();
                return false;
            }
        }
        return true;
    }

    private void detailsViewToStringConversion()
    {
        tempEmployeeCode = employeeCode.getText().toString();
        checkInDateText = checkInDate.getText().toString();
        checkInTimeText = checkInTime.getText().toString();
        checkOutDateText = checkOutDate.getText().toString();
        checkOutTimeText = checkOutTime.getText().toString();
    }
    private void operationCompletion(int opCode) //Only frame layout to be visible
    {
        attendanceRequiredDetails.setVisibility(View.GONE);
        attendanceDetailsView.setVisibility(View.GONE);
        operationCompletionFrame.setVisibility(View.VISIBLE);
        if(opCode == 1)
        {
            successfulMessage.setText("Modification Successful!");
        }
        else if(opCode == 2)
        {
            successfulMessage.setText("Deletion Successful!");
        }
    }
}

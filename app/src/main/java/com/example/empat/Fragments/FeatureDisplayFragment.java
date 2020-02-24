package com.example.empat.Fragments;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.empat.Model.CheckInCheckOutTime;
import com.example.empat.Model.EmployeeProfileDetails;
import com.example.empat.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class FeatureDisplayFragment extends Fragment
{
    private static final String CURRENT_USER_TYPE = "current_user_type";
    private static final String CURRENT_EMPLOYEE_CODE = "current_employee_code";
    private int userType;
    private TextView test;
    private DatabaseReference userDataBase, employeeAttendance;
    private String employeeCode, employeeCodeText;
    private Button registerEmployee, submitAttendance, modify_employee_record, modify_employee_transactions, getAttendanceReport;
    private ProgressBar progressBar;
    CheckInCheckOutTime checkInCheckOutTime = null;
    EmployeeProfileDetails employeeProfileDetails = null;
    Boolean transactionAttendance;
    public FeatureDisplayFragment()
    {
        // Required empty public constructor
    }

    public static FeatureDisplayFragment newInstance(int userType, String employeeCode)
    {
        FeatureDisplayFragment featureDisplayFragment = new FeatureDisplayFragment();
        Bundle args = new Bundle();
        args.putInt(CURRENT_USER_TYPE, userType); // 1- denotes admin rights and 2- denotes general employee
        args.putString(CURRENT_EMPLOYEE_CODE, employeeCode);
        featureDisplayFragment.setArguments(args);
        return featureDisplayFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feature_display, container, false);
        if(view != null)
        {
            init(view);
            if(userType == 1) //Admin user has got all rights
            {
                registerEmployee.setVisibility(View.VISIBLE);
                registerEmployee.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if(getFragmentManager() != null)
                        {
                            getFragmentManager().beginTransaction().replace(R.id.fragment_placeholder, EmployeeRegisterModificationDeletionFragment.newInstance(1, ""))
                                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                                    .addToBackStack("User Register").commit();
                        }
                    }
                });
            }
            else if(userType == 2) //Other type user
            {
                registerEmployee.setVisibility(View.GONE);
                modify_employee_record.setVisibility(View.GONE);
                modify_employee_transactions.setVisibility(View.GONE);
            }
            submitAttendance.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_placeholder, SubmitAttendanceFragment.newInstance(employeeCode)) // launch the home fragment if login is successful
                            .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).commit();

                }
            });
            modify_employee_record.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    //Employee Details can be modified only by the Admin user
                    setupAlertDialog();
                }
            });
            modify_employee_transactions.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    //Employee attendance transactions can be modified by the Admin
                    Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_placeholder, AttendanceModificationFragment.newInstance(employeeCodeText)) // launch the home fragment if login is successful
                            .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).addToBackStack("Employee Attendance Modifications").commit();
                }
            });
            getAttendanceReport.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    //Yet to be implemented
                    if(isWriteStoragePermissionGranted())
                    {
                        if(userType == 2)//Employee of type : other can get only their respective details of attendance
                        {
                            fetchAndDisplayEmployeeDetails(employeeCode);
                            fetchAttendanceReport(employeeCode);
                        }
                        else //Admin type employee can see others' details
                        {
                            transactionAttendance = true;
                            setupAlertDialog();
                        }

                    }
                }
            });
        }
        return view;
    }

    private void fetchAttendanceReport(final String employeeCode)
    {
        employeeAttendance = FirebaseDatabase.getInstance().getReference()
                .child("Employee Attendance");
        employeeAttendance.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.hasChild(employeeCode))
                {
                    try
                    {
                        progressBar.setVisibility(View.VISIBLE);
                        String finalData = "";
                        for(DataSnapshot employeeDateSnapshot : dataSnapshot.getChildren())
                        {
                            if(employeeDateSnapshot.getKey().equals(employeeCode)) //Pointer now at employee code
                            {
                                for(DataSnapshot userSnapshot : employeeDateSnapshot.getChildren()) //All the dates of that employee will be the childres
                                {
                                    String tempData = userSnapshot.getKey().toString() + " ";
                                    checkInCheckOutTime = userSnapshot.getValue(CheckInCheckOutTime.class);// Assigning the database data to the model object
                                    tempData = formatAttendanceReport(checkInCheckOutTime, tempData);
                                    finalData = finalData + tempData + "\n\n";
                                }
                            }
                        }
                        finalData = formatEmployeeDetails(employeeProfileDetails) + finalData;
                        test.setText(finalData);
                        writeToFile(finalData, employeeCode);
                        progressBar.setVisibility(View.GONE);
                        if(checkInCheckOutTime != null)
                        {
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
                    Toast.makeText(getContext(), "Employee Code Not Found", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.w("fetDisEmpAttenData", "Database error : " + databaseError.toException() + " >>>");
            }
        });
    }

    private void fetchAndDisplayEmployeeDetails(final String employeeCode) // Function is responsible for fetching details corresponding to employee code form FireBase
    {
        userDataBase = FirebaseDatabase.getInstance().getReference()
                .child("Employee Profiles");
        userDataBase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.hasChild(employeeCode))
                {
                    try
                    {
                        for(DataSnapshot userSnapshot : dataSnapshot.getChildren())
                        {
                            if(userSnapshot.getKey().equals(employeeCode))
                            {
                                employeeProfileDetails = userSnapshot.getValue(EmployeeProfileDetails.class);// Assigning the database data to the model object
                            }
                        }
                    } catch(Exception e)
                    {
                        Log.e("fetchAndDisplayEmpDet", "Data interchange failed. Exception: <<< " + e.getMessage() + " >>>.");
                    }
                }
                else
                {
                    //Employee Code not found
                    Toast.makeText(getContext(), "Employee Not Found!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.w("fetchAndDisplayEmpDet", "Database error : " + databaseError.toException() + " >>>");
            }
        });
    }

    private String formatAttendanceReport(CheckInCheckOutTime checkInCheckOutTime, String tempDate) //Method handles the formatting of the attendance data
    {
        String attendanceReport = "";
        attendanceReport = attendanceReport + "Details For Date : ---------- : " + tempDate + "\n\n";
        attendanceReport = attendanceReport + "Check In Date  :     " + checkInCheckOutTime.checkInDate + "\n\n";
        attendanceReport = attendanceReport + "Check In Time  :     " + checkInCheckOutTime.checkInTime + "\n\n";
        attendanceReport = attendanceReport + "Check Out Date :     " + checkInCheckOutTime.checkOutDate + "\n\n";
        attendanceReport = attendanceReport + "Check Out Time :     " + checkInCheckOutTime.checkOutTime + "\n\n";
        return attendanceReport;
    }

    private String formatEmployeeDetails(EmployeeProfileDetails employeeProfileDetails) // Method handles the employee details display
    {
        String employeeDetails = "";
        employeeDetails = employeeDetails + "Employee Code  :     " + employeeProfileDetails.employeeCode + "\n\n";
        employeeDetails = employeeDetails + "Employee Name  :     " + employeeProfileDetails.employeeName + "\n\n";
        employeeDetails = employeeDetails + "Employee Phone :     " + employeeProfileDetails.employeePhoneNumber + "\n\n";
        employeeDetails = employeeDetails + "Employee Email :     " + employeeProfileDetails.userEmail + "\n\n";
        employeeDetails = employeeDetails + "Employee Type  :     " + employeeProfileDetails.userType + "\n\n";
        return employeeDetails;
    }

    public void writeToFile(String data, String empCode)
    {
        //Downloading to file
        Toast.makeText(getContext(), "Downloading Attendance Report", Toast.LENGTH_LONG).show();
        // Get the directory for the user's public pictures directory.
        final File path =
                Environment.getExternalStoragePublicDirectory
                        (
                                //Environment.DIRECTORY_PICTURES
                                Environment.DIRECTORY_DOCUMENTS + "/EmpAt_Attendance_reports/"
                        );

        // Make sure the path directory exists.
        if(!path.exists())
        {
            // Make it, if it doesn't exit
            path.mkdirs();
        }
        String fileName = empCode + "_attendance.txt";
        final File file = new File(path, fileName);

        // Save your stream, don't forget to flush() it before closing it.

        try
        {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);

            myOutWriter.close();
            fOut.flush();
            fOut.close();
        } catch(FileNotFoundException e)
        {
            e.printStackTrace();
        } catch(IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public boolean isWriteStoragePermissionGranted()
    {
        if(Build.VERSION.SDK_INT >= 23)
        {
            if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            {
                return true;
            }
            else
            {
                Log.v("WritePermission", "Permission is revoked");
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        }
        else
        { //permission is automatically granted on sdk<23 upon installation
            Log.v("WritePermission", "Permission is granted");
            return true;
        }
    }

    private void setupAlertDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Attendance Modifications");

        final View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.employee_details_dialog, (ViewGroup) getView(), false);
        // Set up the employeeCode
        final EditText employeeCode = (EditText) viewInflated.findViewById(R.id.employeeCode);
        builder.setView(viewInflated);
        // Set up the buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                employeeCodeText = employeeCode.getText().toString();

                if(employeeCodeText.equals(""))
                {
                    Toast.makeText(getContext(), "Enter Employee Code", Toast.LENGTH_LONG).show();
                }
                else
                {
                    if(transactionAttendance)
                    {
                        fetchAndDisplayEmployeeDetails(employeeCodeText);
                        fetchAttendanceReport(employeeCodeText);
                    }
                    else
                    {
                        Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_placeholder, EmployeeRegisterModificationDeletionFragment.newInstance(2, employeeCodeText)) // launch the home fragment if login is successful
                                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).addToBackStack("Employee Details Modifications").commit();
                    }
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        builder.show();

    }

    private void init(View view)
    {
        if(getArguments() != null)
        {
            userType = getArguments().getInt(CURRENT_USER_TYPE);
            employeeCode = getArguments().getString(CURRENT_EMPLOYEE_CODE);
        }
        transactionAttendance = false;
        test = view.findViewById(R.id.test);
        registerEmployee = view.findViewById(R.id.register_employee_button);
        submitAttendance = view.findViewById(R.id.submit_attendance_button);
        modify_employee_record = view.findViewById(R.id.modify_employee_record);
        modify_employee_transactions = view.findViewById(R.id.modify_employee_transactions);
        getAttendanceReport = view.findViewById(R.id.get_attendance_report);
        progressBar = view.findViewById(R.id.progress_bar);
    }
}

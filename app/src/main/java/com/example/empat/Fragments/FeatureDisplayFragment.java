package com.example.empat.Fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.empat.R;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class FeatureDisplayFragment extends Fragment
{
    private static final String CURRENT_USER_TYPE = "current_user_type";
    private static final String CURRENT_EMPLOYEE_CODE = "current_employee_code";
    private int userType;
    private String employeeCode, employeeCodeText;
    private Button registerEmployee, submitAttendance, modify_employee_record, modify_employee_transactions, getAttendanceReport;

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
                }
            });
        }
        return view;
    }

    private void setupAlertDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Record Modifications");

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
                    Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_placeholder, EmployeeRegisterModificationDeletionFragment.newInstance(2, employeeCodeText)) // launch the home fragment if login is successful
                            .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).addToBackStack("Employee Details Modifications").commit();
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
        registerEmployee = view.findViewById(R.id.register_employee_button);
        submitAttendance = view.findViewById(R.id.submit_attendance_button);
        modify_employee_record = view.findViewById(R.id.modify_employee_record);
        modify_employee_transactions = view.findViewById(R.id.modify_employee_transactions);
        getAttendanceReport = view.findViewById(R.id.get_attendance_report);
    }
}

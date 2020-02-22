package com.example.empat.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
    private String employeeCode;
    private Button registerEmployee,submitAttendance, getAttendanceReport;
    public FeatureDisplayFragment()
    {
        // Required empty public constructor
    }

    public static FeatureDisplayFragment newInstance(int userType, String employeeCode)
    {
        FeatureDisplayFragment featureDisplayFragment = new FeatureDisplayFragment();
        Bundle args = new Bundle();
        args.putInt(CURRENT_USER_TYPE, userType); // 1- denotes admin rights and 2- denotes general employee
        args.putString(CURRENT_EMPLOYEE_CODE,employeeCode);
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
            if(userType == 1)
            {
                registerEmployee.setVisibility(View.VISIBLE);
                registerEmployee.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if(getFragmentManager() != null)
                        {
                            getFragmentManager().beginTransaction().replace(R.id.fragment_placeholder, EmployeeRegisterFragment.newInstance())
                                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                                    .addToBackStack("User Register").commit();
                        }
                    }
                });
            }
            else if(userType == 2)
            {
                registerEmployee.setVisibility(View.INVISIBLE);
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
    private void init(View view)
    {
        if(getArguments() != null)
        {
            userType = getArguments().getInt(CURRENT_USER_TYPE);
            employeeCode = getArguments().getString(CURRENT_EMPLOYEE_CODE);
        }
        registerEmployee = view.findViewById(R.id.register_employee_button);
        submitAttendance = view.findViewById(R.id.submit_attendance_button);
        getAttendanceReport = view.findViewById(R.id.get_attendance_report);
    }
}

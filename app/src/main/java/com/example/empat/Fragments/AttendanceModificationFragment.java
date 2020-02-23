package com.example.empat.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.empat.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AttendanceModificationFragment extends Fragment
{

    private static final String ATTENDANCE_DATE = "current_user_type";
    private static final String EMPLOYEE_CODE = "current_employee_code";
    public AttendanceModificationFragment()
    {
        // Required empty public constructor
    }

    public static AttendanceModificationFragment newInstance(String attendanceDate, String employeeCode)
    {
        AttendanceModificationFragment attendanceModificationFragment = new AttendanceModificationFragment();
        Bundle args = new Bundle();
        args.putString(ATTENDANCE_DATE, attendanceDate); // 1- denotes admin rights and 2- denotes general employee
        args.putString(EMPLOYEE_CODE, employeeCode);
        attendanceModificationFragment.setArguments(args);
        return attendanceModificationFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_attendance_modification, container, false);
    }

}

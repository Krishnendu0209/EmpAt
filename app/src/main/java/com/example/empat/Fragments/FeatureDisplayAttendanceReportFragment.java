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
import android.widget.RadioButton;
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

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class FeatureDisplayAttendanceReportFragment extends Fragment
{
    private static final String CURRENT_USER_TYPE = "current_user_type";
    private static final String CURRENT_EMPLOYEE_CODE = "current_employee_code";
    private int userType;
    private TextView test;
    private ArrayList<CheckInCheckOutTime> checkInCheckOutTimeList = new ArrayList<>();
    private ArrayList<String> attendanceDatesList = new ArrayList<>();
    private DatabaseReference userDataBase, employeeAttendance;
    private String employeeCode, employeeCodeText;
    private Button registerEmployee, submitAttendance, modifyEmployeeRecord, modifyEmployeeTransactions, getAttendanceReport;
    private ProgressBar progressBar;
    CheckInCheckOutTime checkInCheckOutTime = null;
    EmployeeProfileDetails employeeProfileDetails = null;
    Boolean attendanceReport; // To understand dialog is for attendance report fetch or modifying employee record
    private RadioButton txtFile, excelFile;
    public FeatureDisplayAttendanceReportFragment()
    {
        // Required empty public constructor
    }

    public static FeatureDisplayAttendanceReportFragment newInstance(int userType, String employeeCode)
    {
        FeatureDisplayAttendanceReportFragment featureDisplayAttendanceReportFragment = new FeatureDisplayAttendanceReportFragment();
        Bundle args = new Bundle();
        args.putInt(CURRENT_USER_TYPE, userType); // 1- denotes admin rights and 2- denotes general employee
        args.putString(CURRENT_EMPLOYEE_CODE, employeeCode);
        featureDisplayAttendanceReportFragment.setArguments(args);
        return featureDisplayAttendanceReportFragment;
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
            if(employeeCode.equals("2526")) //This user is hardcoded in the previous screen and will be able to only register users
            {
                registerEmployee.setVisibility(View.VISIBLE);
                modifyEmployeeRecord.setVisibility(View.GONE);
                modifyEmployeeTransactions.setVisibility(View.GONE);
                submitAttendance.setVisibility(View.GONE);
                getAttendanceReport.setVisibility(View.GONE);
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
            else //Not the prime admin user
            {
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
                    modifyEmployeeRecord.setVisibility(View.GONE);
                    modifyEmployeeTransactions.setVisibility(View.GONE);
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
                modifyEmployeeRecord.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        //Employee Details can be modified only by the Admin user
                        setupAlertDialog();
                    }
                });
                modifyEmployeeTransactions.setOnClickListener(new View.OnClickListener()
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
                                attendanceReport = true;
                                setupAlertDialog();
                            }
                            else //Admin type employee can see others' details
                            {
                                attendanceReport = true;
                                setupAlertDialog();
                            }

                        }
                    }
                });
            }
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
                        for(DataSnapshot employeeDateSnapshot : dataSnapshot.getChildren())
                        {
                            if(employeeDateSnapshot.getKey().equals(employeeCode)) //Pointer now at employee code
                            {
                                for(DataSnapshot userSnapshot : employeeDateSnapshot.getChildren()) //All the dates of that employee will be the childres
                                {
                                    String tempData = userSnapshot.getKey().toString() + " ";
                                    checkInCheckOutTime = userSnapshot.getValue(CheckInCheckOutTime.class);// Assigning the database data to the model object
                                    attendanceDatesList.add(tempData);//Will contain the parent date nodes
                                    checkInCheckOutTimeList.add(checkInCheckOutTime); //Will contain the check in check out data nodes
                                }
                            }
                        }
                        if(checkInCheckOutTimeList != null)
                        {
                            if(excelFile.isChecked())
                            {
                                writeToFile(2);
                            }
                            else if(txtFile.isChecked())
                            {
                                writeToFile(1);
                            }
                        }
                        else
                        {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "No record found", Toast.LENGTH_LONG).show();
                        }
                    } catch(Exception e)
                    {
                        Log.e("fetDisEmpAttenData", "Data interchange failed. Exception: <<< " + e.getMessage() + " >>>.");
                    }
                }
                else
                {
                    //Employee Code not found
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
                    fetchingDataUISet();
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

    public void writeToFile(int choice)
    {
        //Downloading to file
        String tempEmployeeDetails = formatEmployeeDetails(employeeProfileDetails);
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

        if(choice == 1)
        {
            String fileName = employeeProfileDetails.employeeCode + employeeProfileDetails.employeeName + "_attendance.txt";
            final File file = new File(path, fileName);

            // Save your stream, don't forget to flush() it before closing it.

            try
            {
                file.createNewFile();
                FileOutputStream fOut = new FileOutputStream(file);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append(tempEmployeeDetails);
                for(int i = 0; i < checkInCheckOutTimeList.size(); i++)
                {
                    String tempCheckInCheckOutData = formatAttendanceReport(checkInCheckOutTimeList.get(i), attendanceDatesList.get(i));
                    myOutWriter.append(tempCheckInCheckOutData);
                }

                myOutWriter.close();
                fOut.flush();
                fOut.close();
                Toast.makeText(getContext(), "Download Completed", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                fetchingDataUIReset();
            } catch(FileNotFoundException e)
            {
                e.printStackTrace();
            } catch(IOException e)
            {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }
        else //If choice is excel format
        {
            //New Workbook
            Workbook wb = new HSSFWorkbook();
            Cell cell = null;

            //Cell style for header row
            CellStyle headingCellStyle = wb.createCellStyle();
            headingCellStyle.setFillForegroundColor(HSSFColor.LIME.index);
            headingCellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

            //New Sheet
            Sheet sheet1 = null;
            sheet1 = wb.createSheet("Attendance Report");

            // Generate column headings
            Row row = sheet1.createRow(0);

            String employeeDetails = "Employee Details : " +
                    "\nEmployee Code : " + employeeProfileDetails.employeeCode +
                    "\nEmployee Name : " + employeeProfileDetails.employeeName +
                    "\nEmployee Phone : " + employeeProfileDetails.employeePhoneNumber +
                    "\nEmployee Email : " + employeeProfileDetails.userEmail;

            cell = row.createCell(0);
            cell.setCellValue(employeeDetails);
            cell.setCellStyle(headingCellStyle);

            cell = row.createCell(1);
            cell.setCellValue("Attendance Transaction Date");
            cell.setCellStyle(headingCellStyle);

            cell = row.createCell(2);
            cell.setCellValue("Check In Date");
            cell.setCellStyle(headingCellStyle);

            cell = row.createCell(3);
            cell.setCellValue("Check In Time");
            cell.setCellStyle(headingCellStyle);

            cell = row.createCell(4);
            cell.setCellValue("Check Out Date");
            cell.setCellStyle(headingCellStyle);

            cell = row.createCell(5);
            cell.setCellValue("Check Out Time");
            cell.setCellStyle(headingCellStyle);

            sheet1.setColumnWidth(0, (15 * 500));
            sheet1.setColumnWidth(1, (15 * 500));
            sheet1.setColumnWidth(2, (15 * 500));
            for(int counter = 0; counter < checkInCheckOutTimeList.size();counter++)
            {
                Row dataRows = sheet1.createRow(counter + 1);//One new row created

                cell = dataRows.createCell(1);
                cell.setCellValue(attendanceDatesList.get(counter)); //First column will have the transaction date

                cell = dataRows.createCell(2);
                cell.setCellValue(checkInCheckOutTimeList.get(counter).checkInDate); //Second column will have check in date

                cell = dataRows.createCell(3);
                cell.setCellValue(checkInCheckOutTimeList.get(counter).checkInTime); //Third column will have check in time

                cell = dataRows.createCell(4);
                cell.setCellValue(checkInCheckOutTimeList.get(counter).checkOutDate); //Fourth column will have check out date

                cell = dataRows.createCell(5);
                cell.setCellValue(checkInCheckOutTimeList.get(counter).checkOutTime); //Fifth column will have check out time
            }

            // Create a path where we will place our List of objects on external storage
            String fileName = employeeProfileDetails.employeeCode + employeeProfileDetails.employeeName + "_attendance.xls";
            final File file = new File(path, fileName);
            FileOutputStream os = null;

            try
            {
                os = new FileOutputStream(file);
                wb.write(os);
                Log.w("FileUtils", "Writing file" + file);
                Toast.makeText(getContext(), "Download Completed", Toast.LENGTH_SHORT).show();
            } catch(IOException e)
            {
                Log.w("FileUtils", "Error writing " + file, e);
            } catch(Exception e)
            {
                Log.w("FileUtils", "Failed to save file", e);
            } finally
            {
                try
                {
                    if(os != null)
                    {
                        os.close();
                    }
                } catch(Exception ex)
                {
                }
            }
            fetchingDataUIReset();
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
        builder.setTitle("Details Required!");

        final View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.employee_details_dialog, (ViewGroup) getView(), false);

        // Set up the employeeCode
        final EditText employeeCodeEt =  viewInflated.findViewById(R.id.employeeCode);
        excelFile = viewInflated.findViewById(R.id.excelFile);
        txtFile = viewInflated.findViewById(R.id.txtFile);

        if(!attendanceReport)
        {
            excelFile.setVisibility(View.GONE);
            txtFile.setVisibility(View.GONE);
        }
        if(userType == 2)
        {
            employeeCodeEt.setVisibility(View.GONE); //Other type employee can't input employee code
        }

        builder.setView(viewInflated);
        // Set up the buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                if(userType == 1) // Admin user
                {
                    employeeCodeText = employeeCodeEt.getText().toString();
                    if(employeeCodeText.equals("")) //Employee code compulsory
                    {
                        Toast.makeText(getContext(), "Enter Employee Code", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        if(attendanceReport) //operation for attendance Report
                        {
                            fetchingDataUISet();
                            fetchAndDisplayEmployeeDetails(employeeCodeText);//Employee Details will be fetched
                            fetchAttendanceReport(employeeCodeText); //Attendance report of the employee wil be fetched
                        }
                        else //For modifying employee details data
                        {
                            Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_placeholder, EmployeeRegisterModificationDeletionFragment.newInstance(2, employeeCodeText)) // launch the home fragment if login is successful
                                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).addToBackStack("Employee Details Modifications").commit();
                        }
                    }
                }
                else //Non admin user will not be able to give any employee code system by default will take his logged in code
                {
                    fetchAndDisplayEmployeeDetails(employeeCode);//Employee Details will be fetched
                    fetchAttendanceReport(employeeCode); //Attendance report of the employee wil be fetched
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
        attendanceReport = false;
        registerEmployee = view.findViewById(R.id.register_employee_button);
        submitAttendance = view.findViewById(R.id.submit_attendance_button);
        modifyEmployeeRecord = view.findViewById(R.id.modify_employee_record);
        modifyEmployeeTransactions = view.findViewById(R.id.modify_employee_transactions);
        getAttendanceReport = view.findViewById(R.id.get_attendance_report);
        progressBar = view.findViewById(R.id.progress_bar);
    }
    private void fetchingDataUISet()
    {
        Toast.makeText(getContext(), "Downloading Attendance Report", Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.VISIBLE);
        submitAttendance.setVisibility(View.GONE);
        registerEmployee.setVisibility(View.GONE);
        modifyEmployeeTransactions.setVisibility(View.GONE);
        modifyEmployeeRecord.setVisibility(View.GONE);
        getAttendanceReport.setVisibility(View.GONE);
    }
    private void fetchingDataUIReset()
    {
        if(userType == 1)
        {
            progressBar.setVisibility(View.INVISIBLE);
            submitAttendance.setVisibility(View.VISIBLE);
            registerEmployee.setVisibility(View.VISIBLE);
            modifyEmployeeTransactions.setVisibility(View.VISIBLE);
            modifyEmployeeRecord.setVisibility(View.VISIBLE);
            getAttendanceReport.setVisibility(View.VISIBLE);
        }
        else
        {
            progressBar.setVisibility(View.INVISIBLE);
            submitAttendance.setVisibility(View.VISIBLE);
            getAttendanceReport.setVisibility(View.VISIBLE);
        }
    }
}

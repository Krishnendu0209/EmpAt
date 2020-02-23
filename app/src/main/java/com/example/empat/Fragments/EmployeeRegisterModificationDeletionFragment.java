package com.example.empat.Fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.empat.Model.EmployeeProfileDetails;
import com.example.empat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class EmployeeRegisterModificationDeletionFragment extends Fragment
{
    private FrameLayout operationCompletionFrame;
    private TextView successfulMessage;
    String emailRegexValidator = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
    private EditText employeeCodeEditText,
            employeeNameEditText,
            phoneNumberEditText,
            employeeEmailEditText,
            passwordEditText,
            confirmPasswordEditText;
    private Button registerButton, confirmModifications, unlockRecord, deleteRecord;
    private ProgressBar progressBar;
    private static final String CALLING_FRAGMENT = "calling fragment";
    private static final String EMPLOYEE_CODE_DETAILS = "employee code details";
    private int callingFragmentFlag;
    private String employeeCode, employeeFullName, employeePhoneNumber, employeeEmail, userPassword, confirmUserPassword, userType;
    private DatabaseReference userDataBase, employeeAttendance;
    private FirebaseAuth firebaseAuth;
    private RadioButton admin, other;
    private RadioGroup rgUserType;

    public EmployeeRegisterModificationDeletionFragment()
    {
        // Required empty public constructor
    }

    public static EmployeeRegisterModificationDeletionFragment newInstance(int callingFragment, String employeeCode)
    {
        EmployeeRegisterModificationDeletionFragment employeeRegisterModificationDeletionFragment = new EmployeeRegisterModificationDeletionFragment();
        Bundle args = new Bundle();
        // 1- denotes fragment called from registration of new employee and 2- denotes employee record being modified
        args.putInt(CALLING_FRAGMENT, callingFragment);
        args.putString(EMPLOYEE_CODE_DETAILS,employeeCode);
        employeeRegisterModificationDeletionFragment.setArguments(args);
        return employeeRegisterModificationDeletionFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_employee_register_modification_deletion, container, false);
        if(view != null)
        {
            init(view);
            if(callingFragmentFlag == 1)
            {
                registerButton.setOnClickListener(new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        detailsViewToStringConversion(); // converting all the view data to string data
                        if(validations()) //if the data provided is valid then proceed with registration of the user
                        {
                            progressBar.setVisibility(View.VISIBLE);
                            registerUser(employeeCode, employeeFullName, employeePhoneNumber, employeeEmail, userPassword, userType);
                            operationCompletion(1);
                        }
                    }
                });
            }
            else //Calling fragment for employee details modifications
            {
                progressBar.setVisibility(View.VISIBLE);
                fetchAndDisplayEmployeeDetails(employeeCode);
                unlockRecord.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        unlockUI();
                    }
                });
                confirmModifications.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        detailsViewToStringConversion(); // converting all the view data to string data
                        if(validations()) //if the data provided is valid then proceed with registration of the user
                        {
                            progressBar.setVisibility(View.VISIBLE);
                            modifyEmployeeRecord(employeeCode, employeeFullName, employeePhoneNumber, employeeEmail, userPassword, userType);
                            operationCompletion(2);
                        }
                    }
                });
                deleteRecord.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        progressBar.setVisibility(View.VISIBLE);
                        deleteEmployeeDetailsAndAttendanceRecord(employeeCode);
                        Toast.makeText(getContext(), "Deletion Successful", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        operationCompletion(3);
                    }
                });
            }
        }
        return view;
    }


    private void init(View view) //Function is handling the view initialisation
    {
        employeeCodeEditText = view.findViewById(R.id.employee_code);
        employeeNameEditText = view.findViewById(R.id.full_name);
        phoneNumberEditText = view.findViewById(R.id.phone_number);
        employeeEmailEditText = view.findViewById(R.id.email);
        passwordEditText = view.findViewById(R.id.password);
        confirmPasswordEditText = view.findViewById(R.id.confirm_password);
        registerButton = view.findViewById(R.id.register_button);
        confirmModifications = view.findViewById(R.id.confirm_modification);
        unlockRecord = view.findViewById(R.id.unlock_record);
        deleteRecord = view.findViewById(R.id.delete_record);
        progressBar = view.findViewById(R.id.progress_bar);
        admin = view.findViewById(R.id.admin);
        other = view.findViewById(R.id.other);
        rgUserType = view.findViewById(R.id.rgUserType);

        operationCompletionFrame = view.findViewById(R.id.operation_completion_frame);
        successfulMessage = view.findViewById(R.id.successfulMessage);
        firebaseAuth = FirebaseAuth.getInstance();

        if(getArguments() != null)
        {
            callingFragmentFlag = getArguments().getInt(CALLING_FRAGMENT);
            employeeCode = getArguments().getString(EMPLOYEE_CODE_DETAILS);
        }
    }

    //Function is responsible for converting data from editText to string
    private void detailsViewToStringConversion()
    {
        employeeCode = employeeCodeEditText.getText().toString();
        employeeFullName = employeeNameEditText.getText().toString();
        employeePhoneNumber = phoneNumberEditText.getText().toString();
        employeeEmail = employeeEmailEditText.getText().toString();
        userPassword = passwordEditText.getText().toString();
        confirmUserPassword = confirmPasswordEditText.getText().toString();

        if(admin.isChecked())
        {
            userType = "Admin";
        }
        else if(other.isChecked())
        {
            userType = "Other";
        }
    }

    /*
        Below function is implemented to validate data in all UI fields i.e. all validation are implemented
        in this function
    */
    private boolean validations()
    {
        if(employeeCode.equals("")) // Checks if employee code field is empty or not if yes then should not proceed else proceed
        {
            if(getContext() != null)
            {
                Toast.makeText(getContext(), "Enter Code!", Toast.LENGTH_SHORT).show();
                employeeNameEditText.requestFocus();
                return false;
            }
        }
        if(employeeFullName.equals("")) // Checks if name field is empty or not if yes then should not proceed else proceed
        {
            if(getContext() != null)
            {
                Toast.makeText(getContext(), "Enter Name!", Toast.LENGTH_SHORT).show();
                employeeNameEditText.requestFocus();
                return false;
            }
        }

        if(employeePhoneNumber.equals(""))//If phone number field is empty then don't proceed
        {
            if(getContext() != null)
            {
                Toast.makeText(getContext(), "Enter Phone Number!", Toast.LENGTH_SHORT).show();
                passwordEditText.requestFocus();
                return false;
            }
        }
        else if(employeePhoneNumber.length() < 10) //else proceed and check if phone number field is not empty then check for number of characters as FireBase supports min 6 characters password
        {
            if(getContext() != null)
            {
                Toast.makeText(getContext(), "Phone Number must have 10 digits!", Toast.LENGTH_SHORT).show();
                passwordEditText.requestFocus();
                return false;
            }
        }
        if(employeeEmail.equals("")) // Checks if email field is empty or not if yes then should not proceed
        {
            if(getContext() != null)
            {
                Toast.makeText(getContext(), "Enter Email Id!", Toast.LENGTH_SHORT).show();
                employeeEmailEditText.requestFocus();
                return false;
            }
        }
        else if(!employeeEmail.matches(emailRegexValidator)) // else proceed and check if the email id is a valid one or not by matching with regular expression
        {
            if(getContext() != null)
            {
                Toast.makeText(getContext(), "Invalid Email Id!", Toast.LENGTH_SHORT).show();
                employeeEmailEditText.requestFocus();
                return false;
            }
        }
        if(userPassword.equals(""))//If password field is empty then don't proceed
        {
            if(getContext() != null)
            {
                Toast.makeText(getContext(), "Enter Password!", Toast.LENGTH_SHORT).show();
                passwordEditText.requestFocus();
                return false;
            }
        }
        else if(userPassword.length() < 6) //else proceed and check if password field is not empty then check for number of characters as FireBase supports min 6 characters password
        {
            if(getContext() != null)
            {
                Toast.makeText(getContext(), "Password must contain 6 characters!", Toast.LENGTH_SHORT).show();
                passwordEditText.requestFocus();
                return false;
            }
        }
        if(callingFragmentFlag == 1)
        {
            if(confirmUserPassword.equals("")) // Confirm password field is empty then don't proceed
            {
                if(getContext() != null)
                {
                    Toast.makeText(getContext(), "Confirm Password!", Toast.LENGTH_SHORT).show();
                    confirmPasswordEditText.requestFocus();
                    return false;
                }
            }
            else if(!userPassword.equals(confirmUserPassword)) //proceed and check whether password and confirm password bot fields are same or not.
            {
                if(getContext() != null)
                {
                    Toast.makeText(getContext(), "Passwords doesn't match!", Toast.LENGTH_SHORT).show();
                    passwordEditText.requestFocus();
                    confirmPasswordEditText.requestFocus();
                    return false;
                }
            }
        }
        return true;
    }

    private void registerUser(final String employeeCode, final String employeeFullName, final String employeePhoneNumber, final String employeeEmail, final String userPassword, final String userType)
    {
        firebaseAuth.createUserWithEmailAndPassword(employeeEmail, userPassword).addOnCompleteListener(Objects.requireNonNull(getActivity()), new OnCompleteListener<AuthResult>()
        {
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if(!task.isSuccessful()) // if task fails before begining then there is problem with FireBase or internet
                {
                    if(getContext() != null)
                    {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Registration Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    EmployeeProfileDetails employeeProfileDetails = new EmployeeProfileDetails(employeeCode, employeeFullName, employeePhoneNumber, employeeEmail, userPassword, userType);
                    userDataBase = FirebaseDatabase.getInstance().getReference(); // Add the reference
                    employeeAttendance = FirebaseDatabase.getInstance().getReference();
                    userDataBase.child("Employee Profiles").child(employeeCode).setValue(employeeProfileDetails).addOnSuccessListener(new OnSuccessListener<Void>()
                    {
                        public void onSuccess(Void aVoid) // If the task is successful i. e registration successful
                        {
                            employeeAttendance.child("Employee Attendance").child(employeeCode).setValue(true);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Registration Successful", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() // If after the task fails after initiation then either connectivity issue or FireBase down or node not found
                    {
                        public void onFailure(@NonNull Exception e)
                        {
                            Toast.makeText(getContext(), "Registration Failed", Toast.LENGTH_SHORT).show(); // If registration fails
                        }
                    });
                }
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
                        EmployeeProfileDetails employeeProfileDetails = null;
                        for (DataSnapshot userSnapshot: dataSnapshot.getChildren())
                        {
                            if(userSnapshot.getKey().equals(employeeCode))
                            {
                                employeeProfileDetails = userSnapshot.getValue(EmployeeProfileDetails.class);// Assigning the database data to the model object
                            }
                        }
                        setUpUIOnDetailsFetched(employeeProfileDetails);
                    } catch(Exception e)
                    {
                        Log.e("fetchAndDisplayEmpDet", "Data interchange failed. Exception: <<< " + e.getMessage() + " >>>.");
                    }
                }
                else
                {
                    //Employee Code not found
                    Toast.makeText(getContext(),"Employee Not Found!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.w("fetchAndDisplayEmpDet", "Database error : " + databaseError.toException() + " >>>");
            }
        });
    }

    private void modifyEmployeeRecord(final String employeeCode, String employeeFullName, String employeePhoneNumber, String employeeEmail, String userPassword, String userType)
    {

        EmployeeProfileDetails employeeProfileDetails = new EmployeeProfileDetails(employeeCode, employeeFullName, employeePhoneNumber, employeeEmail, userPassword, userType);
        userDataBase = FirebaseDatabase.getInstance().getReference(); // Add the reference
        userDataBase.child("Employee Profiles").child(employeeCode).setValue(employeeProfileDetails).addOnSuccessListener(new OnSuccessListener<Void>()
        {
            public void onSuccess(Void aVoid) // If the task is successful i. e registration successful
            {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Modification Successful", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() // If after the task fails after initiation then either connectivity issue or FireBase down or node not found
        {
            public void onFailure(@NonNull Exception e)
            {
                Toast.makeText(getContext(), "Modification Failed", Toast.LENGTH_SHORT).show(); // If registration fails
            }
        });
    }

    private void deleteEmployeeDetailsAndAttendanceRecord(String employeeCode)
    {
        userDataBase = FirebaseDatabase.getInstance().getReference(); // Add the reference
        userDataBase.child("Employee Profiles").child(employeeCode).removeValue(); //Record deleted from the employee record node
        userDataBase.child("Employee Attendance").child(employeeCode).removeValue();// Complete attendance record of employee deleted
    }

    private void setUpUIOnDetailsFetched(EmployeeProfileDetails employeeProfileDetails)
    {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(getContext(),"Employee Record Fetched", Toast.LENGTH_LONG).show();
        confirmPasswordEditText.setVisibility(View.GONE);
        registerButton.setVisibility(View.GONE);
        unlockRecord.setVisibility(View.VISIBLE);
        deleteRecord.setVisibility(View.VISIBLE);

        employeeCodeEditText.setText(employeeProfileDetails.employeeCode);
        employeeCodeEditText.setEnabled(false);

        employeeNameEditText.setText(employeeProfileDetails.employeeName);
        employeeNameEditText.setEnabled(false);

        phoneNumberEditText.setText(employeeProfileDetails.employeePhoneNumber);
        phoneNumberEditText.setEnabled(false);

        employeeEmailEditText.setText(employeeProfileDetails.userEmail);
        employeeEmailEditText.setEnabled(false);

        passwordEditText.setText(employeeProfileDetails.userPassword);
        passwordEditText.setEnabled(false);

        if(employeeProfileDetails.userType.equals("Admin"))
        {
            admin.setChecked(true);
        }
        else
        {
            other.setChecked(true);
        }
        admin.setEnabled(false);
        other.setEnabled(false);
    }
    private void unlockUI()
    {
        deleteRecord.setVisibility(View.GONE);
        employeeCodeEditText.setEnabled(false);
        employeeNameEditText.setEnabled(true);
        phoneNumberEditText.setEnabled(true);
        employeeEmailEditText.setEnabled(true);
        passwordEditText.setEnabled(true);
        admin.setEnabled(true);
        other.setEnabled(true);
        unlockRecord.setVisibility(View.GONE);
        confirmModifications.setVisibility(View.VISIBLE);
    }
    private void operationCompletion(int opCode) //Only frame layout to be visible
    {
        deleteRecord.setVisibility(View.GONE);
        employeeCodeEditText.setVisibility(View.GONE);
        employeeNameEditText.setVisibility(View.GONE);
        phoneNumberEditText.setVisibility(View.GONE);
        employeeEmailEditText.setVisibility(View.GONE);
        passwordEditText.setVisibility(View.GONE);
        rgUserType.setVisibility(View.GONE);
        registerButton.setVisibility(View.GONE);
        confirmModifications.setVisibility(View.GONE);
        unlockRecord.setVisibility(View.GONE);
        deleteRecord.setVisibility(View.GONE);
        operationCompletionFrame.setVisibility(View.VISIBLE);
        if(opCode == 1)
        {
            successfulMessage.setText("Registration Successful!");
        }
        else if(opCode == 2)
        {
            successfulMessage.setText("Modification Successful!");
        }
        else if(opCode == 3)
        {
            successfulMessage.setText("Deletion Successful!");
        }
    }
}

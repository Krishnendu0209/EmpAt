package com.example.empat.Fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.empat.Model.EmployeeProfileDetails;
import com.example.empat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class EmployeeRegisterFragment extends Fragment
{

    String emailRegexValidator = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
    private EditText employeeCodeEditText,
            employeeNameEditText,
            phoneNumberEditText,
            employeeEmailEditText,
            passwordEditText,
            confirmPasswordEditText;
    private Button registerButton;
    private ProgressBar progressBar;
    public static final String EMPTY_STRING = "";
    private String employeeCode, employeeFullName,employeePhoneNumber, employeeEmail, userPassword, confirmUserPassword;
    private DatabaseReference userDataBase;
    private FirebaseAuth firebaseAuth;

    public EmployeeRegisterFragment()
    {
        // Required empty public constructor
    }

    public static EmployeeRegisterFragment newInstance()
    {
        EmployeeRegisterFragment employeeRegisterFragment = new EmployeeRegisterFragment();
        Bundle args = new Bundle();

        employeeRegisterFragment.setArguments(args);
        return employeeRegisterFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_employee_register, container, false);
        if(view != null)
        {
            init(view);
            registerButton.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View view)
                {
                    detailsViewToStringConversion(); // converting all the view data to string data
                    if(validations()) //if the data provided is valid then proceed with registration of the user
                    {
                        progressBar.setVisibility(View.VISIBLE);
                        registerUser(employeeCode, employeeFullName, employeePhoneNumber, employeeEmail, userPassword);
                    }
                }
            });
        }
        return view;
    }

    private void init(View view)
    {
        employeeCodeEditText = view.findViewById(R.id.employee_code);
        employeeNameEditText = view.findViewById(R.id.full_name);
        phoneNumberEditText = view.findViewById(R.id.phone_number);
        employeeEmailEditText = view.findViewById(R.id.email);
        passwordEditText = view.findViewById(R.id.password);
        confirmPasswordEditText = view.findViewById(R.id.confirm_password);
        registerButton = view.findViewById(R.id.register_button);
        progressBar = view.findViewById(R.id.progress_bar);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void detailsViewToStringConversion()
    {
        employeeCode = employeeCodeEditText.getText().toString();
        employeeFullName = employeeNameEditText.getText().toString();
        employeePhoneNumber = phoneNumberEditText.getText().toString();
        employeeEmail = employeeEmailEditText.getText().toString();
        userPassword = passwordEditText.getText().toString();
        confirmUserPassword = confirmPasswordEditText.getText().toString();
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
                Toast.makeText(getContext(), "Passwords don't match!", Toast.LENGTH_SHORT).show();
                passwordEditText.requestFocus();
                confirmPasswordEditText.requestFocus();
                return false;
            }
        }
        return true;
    }

    private void registerUser(final String employeeCode, final String employeeFullName, final String employeePhoneNumber, final String employeeEmail, final String userPassword)
    {
        firebaseAuth.createUserWithEmailAndPassword(employeeEmail, userPassword).addOnCompleteListener(Objects.requireNonNull(getActivity()), new OnCompleteListener<AuthResult>()
        {
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if(!task.isSuccessful()) // if task fails before begining then there is problem with FireBase or internet
                {
                    if(getContext() != null)
                    {
                        Toast.makeText(getContext(), "Registration Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    String user_id = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
                    EmployeeProfileDetails employeeProfileDetails = new EmployeeProfileDetails(employeeCode, employeeFullName, employeePhoneNumber, employeeEmail, userPassword);
                    userDataBase = FirebaseDatabase.getInstance().getReference(); // Add the reference
                    userDataBase.child("Employee Profiles").child(employeeCode).setValue(employeeProfileDetails).addOnSuccessListener(new OnSuccessListener<Void>()
                    {
                        public void onSuccess(Void aVoid) // If the task is successful i. e registration successful
                        {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Registration Successful", Toast.LENGTH_SHORT).show();
//                            Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_placeholder, HomeFragment.newInstance()) // launch the home fragment if login is successful
//                                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).commit();
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

}

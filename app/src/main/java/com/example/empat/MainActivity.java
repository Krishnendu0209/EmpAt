package com.example.empat;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.empat.Fragments.FeatureDisplayFragment;
import com.example.empat.Model.EmployeeProfileDetails;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity
{
    private Button loginButton;
    private EditText employeeCodeEditText, passwordEditText;
    private ProgressBar progressBar;
    public static final String EMPTY_STRING = "";
    private String employeeCode, userPassword;
    private FrameLayout fragment_placeholder;
    private DatabaseReference userDataBase;
    EmployeeProfileDetails employeeProfileDetails = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginButton = findViewById(R.id.login_button);
        employeeCodeEditText = findViewById(R.id.employeeCode);
        passwordEditText = findViewById(R.id.employeePassword);
        progressBar = findViewById(R.id.progress_bar);
        fragment_placeholder = findViewById(R.id.fragment_placeholder);


        loginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                detailsViewToStringConversion();
                if(validations())
                {
                    if(employeeCode.equals("2526") && userPassword.equals("2526")) // if code is 2526 then user is of admin category hence can register other users
                    {
                        updateUI();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_placeholder, FeatureDisplayFragment.newInstance(1, "2526"))
                                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                                .addToBackStack("Admin Features").commit();
                    }
                    else
                    {
                        progressBar.setVisibility(View.VISIBLE);
                        validateUser(employeeCode, userPassword);
                    }
                }
            }
        });
    }

    private void detailsViewToStringConversion()
    {
        employeeCode = employeeCodeEditText.getText().toString();
        userPassword = passwordEditText.getText().toString();
    }

    private boolean validations()
    {
        if(employeeCode.equals(""))
        {
            Toast toast = Toast.makeText(MainActivity.this, "Enter Code", Toast.LENGTH_LONG);
            toast.show();
            return false;
        }
        if(userPassword.equals(""))
        {
            Toast toast = Toast.makeText(MainActivity.this, "Enter Password", Toast.LENGTH_LONG);
            toast.show();
            return false;
        }
        return true;
    }

    private void updateUI()
    {
        fragment_placeholder.setVisibility(View.VISIBLE);
        loginButton.setVisibility(View.GONE);
        employeeCodeEditText.setVisibility(View.GONE);
        passwordEditText.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }


    private void validateUser(final String employeeCode, final String userPassword)
    {
        userDataBase = FirebaseDatabase.getInstance().getReference().child("Employee Profiles");

        userDataBase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
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
                        if(employeeProfileDetails != null && employeeProfileDetails.userPassword.equals(userPassword))//Valid User
                        {
                            updateUI();
                            if(employeeProfileDetails.userType.equals("Admin"))
                            {
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_placeholder, FeatureDisplayFragment.newInstance(1, employeeCode))
                                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                                        .addToBackStack("Admin Features").commit();
                            }
                            else
                            {
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_placeholder, FeatureDisplayFragment.newInstance(2, employeeCode))
                                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                                        .addToBackStack("Other Features").commit();
                            }
                        }
                        else
                        {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, "Invalid Credentials!", Toast.LENGTH_LONG).show();
                        }
                    } catch(Exception e)
                    {
                        Log.e("Attendance Status Check", "Data interchange failed. Exception: <<< " + e.getMessage() + " >>>.");
                    }
                }
                else if (employeeProfileDetails == null)
                {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Invalid Credentials!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }
}

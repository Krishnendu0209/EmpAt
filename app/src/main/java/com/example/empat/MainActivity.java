package com.example.empat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{
    private Button continueButton;
    private EditText employeeCode;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        continueButton = findViewById(R.id.register_button);
        employeeCode = findViewById(R.id.employeeCode);
        continueButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(employeeCode.getText().toString().equals(""))
                {
                    Toast toast = Toast.makeText(MainActivity.this,"Enter Code",Toast.LENGTH_LONG);
                    toast.show();
                }
                else
                {
                    employeeCode.setVisibility(View.GONE);
                    continueButton.setVisibility(View.GONE);
                    if (employeeCode.getText().toString().equals("2526")) // if code is 2526 then user is of admin category hence can register other users
                    {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_placeholder, EmployeeRegisterFragment.newInstance(1,"2526"))
                                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                                .addToBackStack("Admin Register").commit();
                    }
                    else
                    {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_placeholder, EmployeeRegisterFragment.newInstance(2,employeeCode.getText().toString()))
                                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                                .addToBackStack("Employee Register").commit();
                    }
                }
            }
        });
    }
}

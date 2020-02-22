package com.example.empat.Model;

public class EmployeeProfileDetails
{
    public String employeeCode;
    public String employeeName;
    public String employeePhoneNumber;
    public String userEmail;
    public String userPassword;
    public String userType;

    public String getUserType()
    {
        return userType;
    }


    public EmployeeProfileDetails()
    {

    }

    public EmployeeProfileDetails(String employeeCode, String employeeName, String employeePhoneNumber, final String userEmail, String userPassword, String userType)
    {
        this.employeeCode = employeeCode;
        this.employeeName = employeeName;
        this.employeePhoneNumber = employeePhoneNumber;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
        this.userType = userType;
    }
}

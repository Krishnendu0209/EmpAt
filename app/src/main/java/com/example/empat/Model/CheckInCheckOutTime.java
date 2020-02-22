package com.example.empat.Model;

public class CheckInCheckOutTime
{
    public String checkInDate;
    public String checkOutDate;
    public String checkInTime;
    public String checkOutTime;
    public CheckInCheckOutTime()
    {

    }

    public CheckInCheckOutTime(String checkInDate, String checkOutDate, String checkInTime, String checkOutTime)
    {
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
    }
    public String getCheckInDate()
    {
        return checkInDate;
    }

    public void setCheckInDate(String checkInDate)
    {
        this.checkInDate = checkInDate;
    }

    public String getCheckOutDate()
    {
        return checkOutDate;
    }

    public void setCheckOutDate(String checkOutDate)
    {
        this.checkOutDate = checkOutDate;
    }

    public String getCheckInTime()
    {
        return checkInTime;
    }

    public void setCheckInTime(String checkInTime)
    {
        this.checkInTime = checkInTime;
    }

    public String getCheckOutTime()
    {
        return checkOutTime;
    }

    public void setCheckOutTime(String checkOutTime)
    {
        this.checkOutTime = checkOutTime;
    }

}

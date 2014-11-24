package com.example.cloud_quake_sensor;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import android.annotation.SuppressLint;

public class AData {
    private long timestamp;
    private double Ax;
    private double Ay;
    private double Az;
 
    public AData(long timestamp, double x, double y, double z) {
        this.timestamp = timestamp;
        this.Ax = x;
        this.Ay = y;
        this.Az = z;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public double getX() {
        return Ax;
    }
    public void setX(double x) {
        this.Ax = x;
    }
    public double getY() {
        return Ay;
    }
    public void setY(double y) {
        this.Ay = y;
    }
    public double getZ() {
        return Az;
    }
    public void setZ(double z) {
        this.Az = z;
    }
    @SuppressLint("SimpleDateFormat")
	public String getTime(){
    	Date date = new Date(timestamp);
    	Calendar cal = new GregorianCalendar();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss S");
        sdf.setCalendar(cal);
        cal.setTime(date);
        return sdf.format(date);
    }
	public int getMilisecond(){
    	Date date = new Date(timestamp);
    	Calendar cal = new GregorianCalendar();
        SimpleDateFormat sdf = new SimpleDateFormat("S");
        sdf.setCalendar(cal);
        cal.setTime(date);
        return Integer.parseInt(sdf.format(date));
    }
    public String toString()
    {
        return getTime()+","+String.format("%.4f", Ax)+","+String.format("%.4f", Ay)+","+String.format("%.4f", Az)+"\r\n";
    }
}

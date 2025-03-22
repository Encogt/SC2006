package com.example.powersaver.objects;

public class Device {
    private String deviceName;
    private Integer deviceID;
    private Integer usage;
    private Integer duration;
    //private int powerUsage;
    private int userID;

    // Constructor for adding a device without ID
    public Device(String deviceName, int use, int duration) {
        this.deviceName = deviceName;
        this.usage = use;
        this.duration = duration;
    }

    // Constructor for device with ID, device name, usage, and duration
    public Device(int deviceID, String deviceName, int usage, int duration) {
        this.deviceID = deviceID;
        this.deviceName = deviceName;
        this.usage = usage;
        this.duration = duration;
    }



    // Empty constructor
    public Device() {}

    // Full constructor for creating a device with all fields
    public Device(Integer deviceID, String deviceName, Integer usage, Integer duration, int user) {
        this.deviceID = deviceID;
        this.deviceName = deviceName;
        this.usage = usage;
        this.duration = duration;
        this.userID=user;
    }

    // Full constructor for creating a device with all fields
    public Device(Integer deviceID, String deviceName, Integer usage, Integer duration, int powerUsage,int user) {
        this.deviceID = deviceID;
        this.deviceName = deviceName;
        this.usage = usage;
        this.duration = duration;
        this.userID=user;
    }

    //Full contructor sans the deviceID(Auto generated upon created and saved)
    public Device(String deviceName, Integer usage, Integer duration, int user) {

        this.deviceName = deviceName;
        this.usage = usage;
        this.duration = duration;
        this.userID=user;
    }

    // Getters and Setters
    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Integer getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(Integer deviceID) {
        this.deviceID = deviceID;
    }

    public Integer getUsage() {
        return usage;
    }

    public void setUsage(Integer usage) {
        this.usage = usage;
    }


    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getUserID()
    {
        return userID;
    }
    public void setUserID(int id)
    {
        this.userID=id;
    }

    // Additional methods to get hours and minutes from total duration
    public Integer getDurationHour() {
        return duration / 60;
    }

    public Integer getDurationMin() {
        return duration % 60;
    }
}
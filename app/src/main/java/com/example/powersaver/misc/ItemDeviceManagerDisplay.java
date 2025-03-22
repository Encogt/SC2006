package com.example.powersaver.misc;

public class ItemDeviceManagerDisplay {

        private String deviceName;
        private int usage;
        private int duration;

        // Getters and Setters for each field
    ItemDeviceManagerDisplay(String deviceName, int usage, int duration)
    {
        setDuration(duration);
        setDeviceName(deviceName);
        setUsage(usage);
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getUsage() {
        return usage;
    }

    public void setUsage(int usage) {
        this.usage = usage;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}

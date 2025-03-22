package com.example.powersaver.objects;

public class Appliance {

    private int id;
    private String name;
    private int powerRating;

    // Constructor
    public Appliance(int id, String name, int powerRating) {
        this.id = id;
        this.name = name;
        this.powerRating = powerRating;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPowerRating() {
        return powerRating;
    }

    public void setPowerRating(int powerRating) {
        this.powerRating = powerRating;
    }
}

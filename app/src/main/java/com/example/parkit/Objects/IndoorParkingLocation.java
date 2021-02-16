package com.example.parkit.Objects;

public class IndoorParkingLocation {
    private String color;
    private String number;
    private String level;
    private String description;
    private String uriString;

    public IndoorParkingLocation(){}

    public IndoorParkingLocation(String color,String number,String level,String description,String uriString){
        this.color = color;
        this.number = number;
        this.level = level;
        this.description = description;
        this.uriString = uriString;
    }

    public String getUriString() {
        return uriString;
    }

    public void setUriString(String uriString) {
        this.uriString = uriString;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

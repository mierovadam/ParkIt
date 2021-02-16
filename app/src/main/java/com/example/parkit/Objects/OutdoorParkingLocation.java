package com.example.parkit.Objects;

public class OutdoorParkingLocation {
    private String userEmail;
    private double latitude;
    private double longitude;
    private String description;
    private String uriString;

    public OutdoorParkingLocation(){ }

    public OutdoorParkingLocation(double latitude,double longitude,String description,String uriString,String userEmail){
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.uriString = uriString;
        this.userEmail = userEmail;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUriString() {
        return uriString;
    }

    public void setUriString(String uriString) {
        this.uriString = uriString;
    }
}

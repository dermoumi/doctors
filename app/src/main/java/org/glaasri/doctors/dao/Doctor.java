package org.glaasri.doctors.dao;

/**
 * Created by sid on 1/1/2017.
 */

public class Doctor
{
    private String name;
    private String address;
    private String telephone;
    private String specialty;
    private double latitude;
    private double longitude;

    public Doctor() {

    }

    public Doctor(
        String name, String address, String telephone, String specialty,
        double latitude, double longitude
    ) {
        this.name = name;
        this.address = address;
        this.telephone = telephone;
        this.specialty = specialty;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getSpecialty() {
        return specialty;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}

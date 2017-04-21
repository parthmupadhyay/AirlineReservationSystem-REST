package edu.sjsu.cmpe275.lab2.model;

import org.json.JSONObject;

import javax.persistence.*;

/**
 * Created by parth on 4/16/2017.
 */

@Embeddable
@Table(name = "Plane")
public class Plane
{
    private int capacity;
    private String model;
    private String manufacturer;
    private int yearOfManufacture;

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public int getYearOfManufacture() {
        return yearOfManufacture;
    }

    public void setYearOfManufacture(int yearOfManufacture) {
        this.yearOfManufacture = yearOfManufacture;
    }

    public JSONObject getJSON()
    {
        JSONObject plane=new JSONObject();
        plane.put("capacity",this.getCapacity());
        plane.put("model",this.getModel());
        plane.put("manufacturer",this.getManufacturer());
        plane.put("yearOfManufacture",this.getYearOfManufacture());
        return plane;
    }

    public Plane(int capacity, String model, String manufacturer, int yearOfManufacture) {
        this.capacity = capacity;
        this.model = model;
        this.manufacturer = manufacturer;
        this.yearOfManufacture = yearOfManufacture;
    }

    public Plane()
    {
        this.capacity=0;
        this.model="";
        this.manufacturer="";
        this.yearOfManufacture=0;
    }
}
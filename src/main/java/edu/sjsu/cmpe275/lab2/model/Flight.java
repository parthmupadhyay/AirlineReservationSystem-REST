package edu.sjsu.cmpe275.lab2.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "flight")
public class Flight
{
    @Id
    @Column(name="flight_number",unique = true)
    private String number;


    @ManyToMany(mappedBy="flights")
    private List<Passenger> passengers;

    @ManyToMany(mappedBy="flights")
    private List<Reservation> reservations;

    private int price;

    public Plane getPlane() {
        return plane;
    }

    public void setPlane(Plane plane) {
        this.plane = plane;
    }

    @Embedded

    private Plane plane;

    private String source;

    private String destination;

    private Date departureTime;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public List<Passenger> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<Passenger> passengers) {
        this.passengers = passengers;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Date getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(Date departureTime) {
        this.departureTime = departureTime;
    }

    public Date getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Date arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public int getSeatsLeft() {
        return seatsLeft;
    }

    public void setSeatsLeft(int seatsLeft) {
        this.seatsLeft = seatsLeft;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    private Date arrivalTime;

    private int seatsLeft;

    private String description;

    public JSONObject getJSON()
    {
        JSONObject flightJson=new JSONObject();
        flightJson.put("number",this.getNumber());
        flightJson.put("price",this.getPrice());
        flightJson.put("from",this.getSource());
        flightJson.put("to",this.getDestination());
        flightJson.put("departureTime",this.getDepartureTime());
        flightJson.put("arrivalTime",this.getArrivalTime());
        flightJson.put("seatsLeft",this.getSeatsLeft());
        flightJson.put("description",this.getDescription());
        flightJson.put("plane",this.getPlane().getJSON());
        return flightJson;
    }

    public JSONObject getFullJson()
    {
        JSONObject flight=this.getJSON();
        JSONObject passengers=new JSONObject();
        JSONArray passengerArray=new JSONArray();
        for(Passenger passenger:this.getPassengers())
        {
            JSONObject pass=passenger.getJSON();
            passengerArray.put(pass);
        }
        passengers.put("passenger",passengerArray);
        flight.put("passengers",passengers);
        return flight;
    }

    public String getXML()
    {
        return XML.toString(this.getFullJson());
    }

    public Flight(String number, int price, Plane plane, String source, String destination, Date departureTime, Date arrivalTime, int seatsLeft, String description) {
        this.number = number;
        this.price = price;
        this.plane = plane;
        this.source = source;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.seatsLeft = seatsLeft;
        this.description = description;
    }

    public Flight() {
        this.number = "";
        this.price = 0;
        this.plane = null;
        this.source = "";
        this.destination = "";
        this.departureTime = new Date();
        this.arrivalTime = new Date();
        this.seatsLeft = 0;
        this.description = "";

    }
}
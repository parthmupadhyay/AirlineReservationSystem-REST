package edu.sjsu.cmpe275.lab2.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.persistence.*;
import java.util.List;


@Entity
@Table(name = "Reservation")
public class Reservation
{

    @Id
    @Column(name="reservation_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id")
    private Passenger passenger;


    @ManyToMany
    @JoinTable(
            name="reservation_flights",
            joinColumns=@JoinColumn(name="reservation_id", referencedColumnName="reservation_id"),
            inverseJoinColumns=@JoinColumn(name="flight_id", referencedColumnName="flight_number"))
    private List<Flight> flights;

    private int price;

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    @JsonIgnore
    public Passenger getPassenger() {
        return passenger;
    }

    public List<Flight> getFlights() {
        return flights;
    }

    public void setFlights(List<Flight> flights) {
        this.flights = flights;
    }

    public void setPassenger(Passenger passenger) {
        this.passenger = passenger;

    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public JSONObject getFullJSON()
    {
        JSONObject result = new JSONObject();
        JSONObject reserv=new JSONObject();
        reserv.put("orderNumber",this.getOrderNumber());
        reserv.put("price",this.getPrice());
        JSONArray flights = new JSONArray();
        for (Flight flight:this.getFlights())
        {
            flights.put(flight.getJSON());
        }
        reserv.put("flights",new JSONObject().put("flight",flights));
        reserv.put("passenger",this.getPassenger().getJSON());
        result.put("reservation",reserv);
        return result;
    }

    public Reservation()
    {

    }
    public Reservation(Passenger passenger, int price) {
        this.passenger = passenger;
        this.price = price;
    }
}
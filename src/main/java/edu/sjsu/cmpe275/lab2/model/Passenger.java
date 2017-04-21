package edu.sjsu.cmpe275.lab2.model;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(name="Passenger")
public class Passenger
{
    @Id
    @Column(name="passenger_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @OneToMany(mappedBy = "passenger",cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    private List<Reservation> reservations;

    @ManyToMany
    @JoinTable(
            name ="passenger_flights",
            joinColumns = @JoinColumn(name="passenger_id",referencedColumnName = "passenger_id"),
            inverseJoinColumns = @JoinColumn(name="flight_number",referencedColumnName = "flight_number"))

    private List<Flight> flights;

    @NotNull
    private String firstname;

    @NotNull
    private String lastname;

    private int age;

    private String gender;

    @Column(unique = true)
    private String phone;

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Passenger()
    {}

    public Passenger(String first_name, String last_name, int age, String gender, String phone) {
        this.firstname = first_name;
        this.lastname = last_name;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
    }

    public JSONObject getFullJSON()
    {
        JSONObject passenger=this.getJSON();
        JSONArray reservationArray=new JSONArray();
        for(Reservation res:this.getReservations())
        {
            JSONObject reservation=res.getFullJSON();
            JSONObject reserv=reservation.getJSONObject("reservation");
            reserv.remove("passenger");
            reservation.put("reservation",reserv);
            reservationArray.put(reservation);
        }
        passenger.put("reservations",reservationArray);
        return passenger;
    }

    public String getXML()
    {
        return XML.toString(this.getFullJSON());
    }
    public JSONObject getJSON()
    {
        JSONObject passenger = new JSONObject();
        passenger.put("id",this.getId());
        passenger.put("firstname",this.getFirstname());
        passenger.put("lastname",this.getLastname());
        passenger.put("age",this.getAge());
        passenger.put("gender",this.getGender());
        passenger.put("phone",this.getPhone());
        return passenger;
    }

    public List<Flight> getFlights() {
        return flights;
    }

    public void setFlights(List<Flight> flights) {
        this.flights = flights;
    }
}
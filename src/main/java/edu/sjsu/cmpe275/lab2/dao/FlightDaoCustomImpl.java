package edu.sjsu.cmpe275.lab2.dao;

import edu.sjsu.cmpe275.lab2.model.Flight;
import edu.sjsu.cmpe275.lab2.model.Passenger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * Created by parth on 4/18/2017.
 */
public class FlightDaoCustomImpl implements  FlightDaoCustom
{
    @Autowired
    FlightDao flightDao;

    @Override
    public void updateFlight(Flight flight)
    {
        Date arrival=flight.getArrivalTime();
        Date departure=flight.getDepartureTime();
        List<Passenger> passengerList=flight.getPassengers();
        for(Passenger passenger:passengerList)
        {

        }
    }
}

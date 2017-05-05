package edu.sjsu.cmpe275.lab2.controller;

import edu.sjsu.cmpe275.lab2.dao.FlightDao;
import edu.sjsu.cmpe275.lab2.dao.PassengerDao;
import edu.sjsu.cmpe275.lab2.dao.ReservationDao;
import edu.sjsu.cmpe275.lab2.main.Message;
import edu.sjsu.cmpe275.lab2.model.Flight;
import edu.sjsu.cmpe275.lab2.model.Passenger;
import edu.sjsu.cmpe275.lab2.model.Plane;
import edu.sjsu.cmpe275.lab2.model.Reservation;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by parth on 4/17/2017.
 */
@RestController
@ComponentScan(value ="edu.sjsu.cmpe275.lab2.dao")
public class FlightController
{
    @Autowired
    private FlightDao flightDao;

    @Autowired
    private PassengerDao passengerDao;

    @Autowired
    private ReservationDao reservationDao;

    /**
     * Get Flight as JSON
     * @param flightNumber Flight Number
     */
    @RequestMapping(value = "/flight/{flightNumber}",
            method = RequestMethod.GET,
            produces = "application/json")
    @ResponseBody
    public ResponseEntity<Object> getFlightJSON(@PathVariable("flightNumber") String flightNumber)
    {
        Message error=new Message("Sorry, the requested flight with number "+flightNumber+" does not exist","404");
        if (flightDao.exists(flightNumber))
        {
            Flight flight = flightDao.findOne(flightNumber);
            return new ResponseEntity(flight.getFullJson().toString(), HttpStatus.OK);
        }

        return new ResponseEntity(error.getMessageJSON().toString(),HttpStatus.NOT_FOUND);

    }

    /**
     * Get Flight back as XML ,if xml boolean parameter is true
     * @param flightNumber Flight Number
     * @param xml boolean
     */
    @RequestMapping(value = "/flight/{flightNumber}",
            params = "xml",
            method = RequestMethod.GET,
            produces = "application/xml")
    @ResponseBody
    public ResponseEntity<Object> getFlightXML(@PathVariable("flightNumber") String flightNumber,
                                               @RequestParam(value="xml")boolean xml)
    {
        if(xml==true)
        {
            if (flightDao.exists(flightNumber)) {
                Flight flight = flightDao.findOne(flightNumber);
                return new ResponseEntity(flight.getXML(), HttpStatus.OK);
            }
            else
            {
                Message error=new Message("Sorry, the requested flight with number "+flightNumber+" does not exist","404");
                return new ResponseEntity(error.getXML(),HttpStatus.NOT_FOUND);
            }
        }
        else
        {
            Message error=new Message("xml param should be set to true","400");
            return new ResponseEntity(error.getXML(),HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Create or Update flight
     * @param flightNumber Flight Number
     * @param price Ticket Price for Flight
     * @param from Source
     * @param to Destination
     * @param departureTime Departure Time of Flight
     * @param arrivalTime Arrival Time of Flight
     * @param description Description of Flight
     * @param capacity Passenger Capacity of Flight
     * @param model Plane Model
     * @param manufacturer Plane Manufacturer
     * @param yearOfManufacture Plane Manufacture Year
     * @param response for redirection to get Flight
     */
    @RequestMapping(value="flight/{flightNumber}",
                    method=RequestMethod.POST,
                    produces = "application/json")
    @ResponseBody
    public ResponseEntity<Object> createOrUpdateFlight(@PathVariable("flightNumber") String flightNumber,
                                                       @RequestParam(value="price")int price,
                                                       @RequestParam(value="from")String from,
                                                       @RequestParam(value="to")String to,
                                                       @RequestParam(value="departureTime")String departureTime,
                                                       @RequestParam(value="arrivalTime")String arrivalTime,
                                                       @RequestParam(value="description")String description,
                                                       @RequestParam(value="capacity")int capacity,
                                                       @RequestParam(value="model")String model,
                                                       @RequestParam(value="manufacturer")String manufacturer,
                                                       @RequestParam(value="yearOfManufacture")int yearOfManufacture,
                                                       HttpServletResponse response)
    {
        try
        {
            Plane plane = new Plane(capacity, model, manufacturer, yearOfManufacture);
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd-HH");
            Date depTime=sdf.parse(departureTime);
            Date arrTime=sdf.parse(arrivalTime);
            Flight newFlight = new Flight(flightNumber,
                    price, plane, from, to,
                    depTime , arrTime,
                    capacity, description);

            if (!flightDao.exists(flightNumber))
            {
                flightDao.save(newFlight);
                response.sendRedirect("/flight/"+flightNumber+"?xml=true");
            }
            else
            {
                Flight existingFlight=flightDao.findOne(flightNumber);
                if(!checkFlightCapacity(existingFlight,capacity))
                {
                    Message capacityMessage=new Message("Cannot reduce capacity,active reservation count for this flight is higher than the target capacity ","400");
                    return new ResponseEntity<Object>(capacityMessage.getMessageJSON().toString(),HttpStatus.BAD_REQUEST);
                }
                List<Passenger> passengers=existingFlight.getPassengers();
                for(Passenger passenger:passengers)
                {
                    List<Flight> temp=new ArrayList<Flight>();
                    temp.add(newFlight);
                    if(ReservationController.checkOverlap(passenger,temp))
                    {
                        Message error= new Message("Flight overlap occurred","400");
                        return new ResponseEntity<Object>(error,HttpStatus.BAD_REQUEST);
                    }
                    List<Flight> passengerFlights=passenger.getFlights();
                    passengerFlights.remove(existingFlight);
                    passengerFlights.add(newFlight);
                    passenger.setFlights(passengerFlights);
                    passengerDao.save(passenger);
                }
                List<Reservation> reservations=existingFlight.getReservations();
                for(Reservation reservation:reservations)
                {
                    List<Flight> reservedFlights=reservation.getFlights();
                    reservedFlights.remove(existingFlight);
                    reservedFlights.add(newFlight);
                    reservation.setFlights(reservedFlights);
                    reservationDao.save(reservation);
                }
                int oldPlaneCap=existingFlight.getPlane().getCapacity();
                int newPlaneCap=newFlight.getPlane().getCapacity();
                int changeInCapacity=Math.abs(oldPlaneCap-newPlaneCap);
                if(oldPlaneCap>newPlaneCap)
                    newFlight.setSeatsLeft(existingFlight.getSeatsLeft()-changeInCapacity);
                else
                    newFlight.setSeatsLeft(existingFlight.getSeatsLeft()+changeInCapacity);
                flightDao.save(newFlight);
                response.sendRedirect("/flight/"+flightNumber+"?xml=true");

            }
        }
        catch(Exception e)
        {
            return new ResponseEntity<Object>(e,HttpStatus.NOT_FOUND);
        }
        Message error=new Message("Cannot create/update flight","400");
        return new ResponseEntity<Object>(error.getMessageJSON().toString(),HttpStatus.BAD_REQUEST);
    }

    /**
     * Delete an exisiting flight
     * @param flightNumber Flight Number
     */
    @RequestMapping(value="airline/{flightNumber}",
            method=RequestMethod.DELETE,
            produces = "application/json")
    public ResponseEntity<Object> deleteFlight(@PathVariable("flightNumber") String flightNumber)
    {
        Message message=new Message("","404");
        if(!flightDao.exists(flightNumber))
        {
            message.setMessage("Flight does not exist");
            return new ResponseEntity<Object>(message.getMessageJSON().toString(),HttpStatus.NOT_FOUND);
        }
        else
        {
            Flight flight=flightDao.findOne(flightNumber);
            if(flight.getReservations().size()>0)
            {
                message.setMessage("Cannot delete flight, has one or more reservations");
                message.setCode("400");
                return new ResponseEntity<Object>(message.getMessageJSON().toString(),HttpStatus.BAD_REQUEST);
            }
            else
            {
                message.setCode("200");
                message.setMessage("Flight with number "+flightNumber+" is deleted successfully ");
                flightDao.delete(flight);
                return new ResponseEntity<Object>(message.getMessageJSON().toString(),HttpStatus.OK);
            }
        }
    }

    /**
     * Check if new flight capacity is less than total reservation of flight
     * @param flight Flight Object
     * @param newCap New seat capacity
     * @return false if capacity is lesser than total reservations
     */
    private boolean checkFlightCapacity(Flight flight,int newCap)
    {
        int currCap=flight.getPlane().getCapacity();

        if(currCap<newCap)
            return true;
        else
        {
            if(newCap<(currCap-flight.getSeatsLeft()))
                return false;
        }
        return true;
    }
}

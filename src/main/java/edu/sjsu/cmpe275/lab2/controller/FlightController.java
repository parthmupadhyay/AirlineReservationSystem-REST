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
                return new ResponseEntity(XML.toString(error.getMessageJSON()),HttpStatus.NOT_FOUND);
            }
        }
        else
        {
            Message error=new Message("xml param should be set to true","400");
            return new ResponseEntity(XML.toString(error.getMessageJSON()),HttpStatus.BAD_REQUEST);
        }

    }

    @RequestMapping(value="flight/{flightNumber}",
                    method=RequestMethod.POST)
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
        String error="{\n" +
                "\t\"BadRequest\": {\n" +
                "\t\t\"code\": \" 404 \",\n" +
                "\t\t\"msg\": \" Cannot create flight \"\n" +
                "\t}\n" +
                "}\n";
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
                List<Passenger> passengers=existingFlight.getPassengers();
                for(Passenger passenger:passengers)
                {
                    List<Flight> temp=new ArrayList<Flight>();
                    temp.add(newFlight);
                    if(ReservationController.checkOverlap(passenger,temp))
                    {
                        return new ResponseEntity<Object>(error,HttpStatus.NOT_FOUND);
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
        return new ResponseEntity<Object>(error,HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value="flight/{flightNumber}",
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
}

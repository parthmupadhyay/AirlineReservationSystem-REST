package edu.sjsu.cmpe275.lab2.controller;

import edu.sjsu.cmpe275.lab2.dao.FlightDao;
import edu.sjsu.cmpe275.lab2.dao.PassengerDao;
import edu.sjsu.cmpe275.lab2.dao.ReservationDao;
import edu.sjsu.cmpe275.lab2.model.Flight;
import edu.sjsu.cmpe275.lab2.model.Passenger;
import edu.sjsu.cmpe275.lab2.model.Reservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;

/**
 * Created by parth on 4/17/2017.
 */

@RestController
@ComponentScan(value ="edu.sjsu.cmpe275.lab2.dao")
public class ReservationController
{
    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private FlightDao flightDao;

    @Autowired
    private PassengerDao passengerDao;

    @RequestMapping(value = "/reservation/{id}",
                    method = RequestMethod.GET,
                    produces = "application/json")
    @ResponseBody
    public ResponseEntity<Object> getReservation(@PathVariable("id") int id)
    {
        String error="{\n" + "\t\"BadRequest\": {\n" + "\t\t\"code\": \" 404 \",\n" +
                "\t\t\"msg\": \" Reserveration with number "+id+" does not exist \"\n" + "\t}\n" + "}\n";
        if (reservationDao.exists(id))
        {
            Reservation reservation=reservationDao.findOne(id);
            return new ResponseEntity(reservation.getFullJSON().toString(), HttpStatus.OK);
        }
        return new ResponseEntity(error,HttpStatus.NOT_FOUND);

    }


    @RequestMapping(value = "/reservation",
            method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> makeReservation(@RequestParam int passengerId,
                                  @RequestParam List<String> flightLists,
                                  HttpServletResponse response)
    {
        String msg="";
        if(flightLists!=null&&!flightLists.isEmpty()) {
            try {
                if (validatePassenger(passengerId) && validateFlights(flightLists)) {
                    int total = 0;
                    List<Flight> flights = new ArrayList<Flight>();
                    Passenger passenger = passengerDao.findOne(passengerId);
                    for (String flightNumber : flightLists) {
                        Flight tempFlight = flightDao.findOne(flightNumber);
                        total += tempFlight.getPrice();
                        flights.add(tempFlight);
                    }
                    if (checkOverlap(passenger, flights) && checkSeatsLeft(flights)) {
                        List<Flight> passengerFlights = passenger.getFlights();
                        passengerFlights.addAll(flights);
                        passenger.setFlights(passengerFlights);
                        passengerDao.save(passenger);
                        Reservation newReservation = new Reservation(passenger, total);
                        newReservation.setFlights(flights);
                        reservationDao.save(newReservation);
                        for (Flight flight : flights) {
                            flight.setSeatsLeft(flight.getSeatsLeft() - 1);
                            flightDao.save(flight);
                        }
                        response.sendRedirect("/reservation/" + newReservation.getOrderNumber());
                    } else
                        msg = "Flights Overlap occurred";
                } else
                    msg = "Flight or Passenger doesnt exist";
            } catch (Exception e) {
                msg = e.getMessage();
            }
        }
        else
        {
            msg= "Please specify flights";
        }
        String error="{\n" +
                "\t   \"BadRequest\": {\n" +
                "\t\t  \"code\": \"404 \",\n" +
                "\t\t   \"msg\": \""+msg+"\"\n" +
                "\t   }\n" +
                "}\n";

        return new ResponseEntity<Object>(error,HttpStatus.NOT_FOUND);
    }


    @RequestMapping(value = "/reservation/{id}",
                    method = RequestMethod.DELETE)
    public ResponseEntity<Object> deleteReservation(@PathVariable int id)
    {
        String error="{\n" + "\t\"BadRequest\": {\n" + "\t\t\"code\": \" 404 \",\n" +
                "\t\t\"msg\": \" Reservation with number "+id+" does not exist \"\n" + "\t}\n" + "}\n";
        String success="<Response>\n" +
                "           <code> 200 </code>\n" +
                "           <msg> Reservation with number "+id+" is canceled successfully  </msg>\n" +
                "</Response>";
        if(!reservationDao.exists(id))
        {
            return new ResponseEntity(error,HttpStatus.NOT_FOUND);
        }
        else
        {
            Reservation reservation=reservationDao.findOne(id);
            Passenger passenger=reservation.getPassenger();
            List<Flight> passengerFlights=passenger.getFlights();
            List<Flight> flights=reservation.getFlights();
            for(Flight flight:flights)
            {
                flight.setSeatsLeft(flight.getSeatsLeft()+1);
                flightDao.save(flight);
                passengerFlights.remove(flight);
            }
            passenger.setFlights(passengerFlights);
            passengerDao.save(passenger);
            reservationDao.delete(reservation);
            return new ResponseEntity(success,HttpStatus.OK);
        }
    }

    @RequestMapping(value="/reservation/{id}",
                    method = RequestMethod.POST)
    public ResponseEntity<Object> updateReservation(@PathVariable int id,
                                                    @RequestParam(required = false) List<String> flightsAdded ,
                                                    @RequestParam(required = false) List<String> flightsRemoved,
                                                    HttpServletResponse response)
    {
        String error="{\n" + "\t   \"BadRequest\": {\n" + "\t\t  \"code\": \"404 \",\n" +
                "\t\t   \"msg\": \"FAILED\"\n" + "\t   }\n" + "}\n";

        try
        {

        if((flightsAdded==null||flightsAdded.isEmpty())&&(flightsRemoved==null||flightsRemoved.isEmpty()))
        {
            String msg="Both flightsAdded and flightsRemoved parameters are empty";
            return new ResponseEntity(error.replace("FAILED",msg),HttpStatus.NOT_FOUND);
        }
        else if(flightsAdded.equals(flightsRemoved))
        {
            String msg="Both flightsAdded and flightsRemoved parameters are identical";
            return new ResponseEntity(error.replace("FAILED",msg),HttpStatus.NOT_FOUND);

        }
        else if(!reservationDao.exists(id))
        {
            String msg="Reservation with number "+id+" does not exist";
            return new ResponseEntity(error.replace("FAILED",msg),HttpStatus.NOT_FOUND);
        }
        else
        {
            Reservation reservation=reservationDao.findOne(id);
            Passenger passenger=reservation.getPassenger();
            if(!flightsRemoved.isEmpty())
            {
                List<Flight> flightsToBeRemoved=getFlights(flightsRemoved);
                if(!validateFlights(flightsRemoved))
                {
                    String msg="Flights to be removed does'nt exist";
                    return new ResponseEntity(error.replace("FAILED",msg),HttpStatus.NOT_FOUND);
                }
                else if(checkReservationForExistingFlights(reservation,flightsToBeRemoved))
                {
                    List<Flight> oldFlights=reservation.getFlights();
                    passenger=reservation.getPassenger();
                    List<Flight> passengerFlights=passenger.getFlights();
                    for(Flight flight:flightsToBeRemoved)
                    {
                        flight.setSeatsLeft(flight.getSeatsLeft()+1);
                        flightDao.save(flight);
                        oldFlights.remove(flight);
                        passengerFlights.remove(flight);
                    }
                    passenger.setFlights(passengerFlights);
                    reservation.setFlights(oldFlights);
                }
            }
            if(!flightsAdded.isEmpty())
            {
                if(!validateFlights(flightsAdded))
                {
                    String msg="Flights to be added does'nt exist";
                    return new ResponseEntity(error.replace("FAILED",msg),HttpStatus.NOT_FOUND);
                }
                else
                {
                    List<Flight> flightsToBeAdded=getFlights(flightsAdded);
                    passenger=reservation.getPassenger();
                    List<Flight> passengerFlights=passenger.getFlights();
                    if(checkOverlap(passenger,flightsToBeAdded))
                    {
                        List<Flight> flightList=reservation.getFlights();
                        for(Flight flight:flightsToBeAdded)
                        {
                            flight.setSeatsLeft(flight.getSeatsLeft()-1);
                            flightDao.save(flight);
                            flightList.add(flight);
                            passengerFlights.add(flight);
                        }
                        passenger.setFlights(passengerFlights);
                        reservation.setFlights(flightList);
                    }
                }
            }
            passengerDao.save(passenger);
            reservationDao.save(reservation);
            response.sendRedirect("/reservation/" + reservation.getOrderNumber());
        }
        }
        catch (Exception e)
        {
            return new ResponseEntity(error.replace("FAILED",e.getMessage()),HttpStatus.NOT_FOUND);

        }
        return new ResponseEntity(error,HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/reservation",
                    method = RequestMethod.GET)
    public ResponseEntity<Object> searchReservation(@RequestParam(required = false) String passengerId,
                                                    @RequestParam(required = false) String from,
                                                    @RequestParam(required = false) String to,
                                                    @RequestParam(required = false) String flightNumber)
    {
        //if(from.isEmpty() && to.isEmpty() && flightNumber.isEmpty())
        //{
            return new ResponseEntity<Object>(passengerId,HttpStatus.NOT_FOUND);
        //}
    }

    private List<Flight> getFlights(List<String> flightIds)
    {
        List<Flight> flights=new ArrayList<Flight>();
        for(String flightId:flightIds)
        {
            flights.add(flightDao.findOne(flightId));
        }
        return  flights;
    }
    private boolean checkReservationForExistingFlights(Reservation reservation,List<Flight> flightList)
    {
        List<Flight> existingFlights=reservation.getFlights();
        for(Flight flight:flightList)
        {
            if(!existingFlights.contains(flight))
                return false;
        }
        return true;
    }

    private boolean validatePassenger(int passengerId)
    {
        return passengerDao.exists(passengerId);
    }
    private boolean validateFlights(List<String> flights)
    {
        for(String flightId:flights)
        {
            if(!flightDao.exists(flightId))
                return false;
        }
        return true;
    }

    private boolean checkOverlap(Passenger passenger,List<Flight> flights)
    {
        for(Flight flight:flights)
        {
            Date departureDate=flight.getDepartureTime();
            Date arrivalDate=flight.getArrivalTime();
            List<Flight> bookedFlights=passenger.getFlights();
            for(Flight booked:bookedFlights)
            {
                Date bookedDeparture=booked.getDepartureTime();
                Date bookedArrival=booked.getArrivalTime();
                if(checkFlightDatesClash(bookedDeparture,bookedArrival,departureDate,arrivalDate))
                    return false;
            }
        }
        return true;
    }

    private boolean checkFlightDatesClash(Date oldDep,Date oldArr,Date newDep,Date newArr)
    {
        boolean case1=oldDep.compareTo(newDep) * newDep.compareTo(oldArr) >= 0;
        boolean case2=oldDep.compareTo(newArr) * newArr.compareTo(oldArr) >= 0;
        return case1&&case2;
    }

    private boolean checkSeatsLeft(List<Flight> flights)
    {
        for(Flight flight:flights)
        {
            if(flight.getSeatsLeft()<1)
                return false;
        }
        return true;
    }
}

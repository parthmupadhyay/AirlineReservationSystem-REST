package edu.sjsu.cmpe275.lab2.controller;

import edu.sjsu.cmpe275.lab2.dao.FlightDao;
import edu.sjsu.cmpe275.lab2.dao.PassengerDao;
import edu.sjsu.cmpe275.lab2.dao.ReservationDao;
import edu.sjsu.cmpe275.lab2.main.Message;
import edu.sjsu.cmpe275.lab2.model.Flight;
import edu.sjsu.cmpe275.lab2.model.Passenger;
import edu.sjsu.cmpe275.lab2.model.Reservation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    public ResponseEntity<Object> getReservation(@PathVariable("id") String id)
    {
        Message errorMessage=new Message(" Reservation with number "+id+" does not exist","404");
        if (reservationDao.exists(id))
        {
            Reservation reservation=reservationDao.findOne(id);
            return new ResponseEntity(reservation.getFullJSON().toString(), HttpStatus.OK);
        }
        return new ResponseEntity(errorMessage.getMessageJSON().toString(),HttpStatus.NOT_FOUND);

    }


    @RequestMapping(value = "/reservation",
            method = RequestMethod.POST,
            produces = "application/json")
    @ResponseBody
    public ResponseEntity<Object> makeReservation(@RequestParam String passengerId,
                                  @RequestParam List<String> flightLists,
                                  HttpServletResponse response)
    {
        Message message=new Message("","400");
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
                        message.setMessage("Flights Overlap occurred");
                } else
                    message.setMessage("Flight or Passenger doesnt exist");
            } catch (Exception e) {
                message.setMessage(e.getMessage());
            }
        }
        else
        {
            message.setMessage("Please specify flights");
        }
        return new ResponseEntity<Object>(message.getMessageJSON().toString(),HttpStatus.BAD_REQUEST);
    }


    @RequestMapping(value = "/reservation/{id}",
                    method = RequestMethod.DELETE,
                    produces = "application/xml")
    public ResponseEntity<Object> deleteReservation(@PathVariable String id)
    {
        Message success=new Message("Reservation with number "+id+" is canceled successfully","200");
        Message error=new Message("Reservation with number "+id+" does not exist","404");
        if(!reservationDao.exists(id))
        {
            return new ResponseEntity(XML.toString(error.getMessageJSON()),HttpStatus.NOT_FOUND);
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
            return new ResponseEntity(XML.toString(success.getMessageJSON()),HttpStatus.OK);
        }
    }

    @RequestMapping(value="/reservation/{id}",
                    method = RequestMethod.POST)
    public ResponseEntity<Object> updateReservation(@PathVariable String id,
                                                    @RequestParam(required = false) List<String> flightsAdded ,
                                                    @RequestParam(required = false) List<String> flightsRemoved,
                                                    HttpServletResponse response)
    {
        Message error=new Message("","404");
        try
        {

        if((flightsAdded==null||flightsAdded.isEmpty())&&(flightsRemoved==null||flightsRemoved.isEmpty()))
        {
            String msg="Both flightsAdded and flightsRemoved parameters are empty";
            error.setMessage(msg);
        }
        else if(flightsAdded.equals(flightsRemoved))
        {
            String msg="Both flightsAdded and flightsRemoved parameters are identical";
            error.setMessage(msg);

        }
        else if(!reservationDao.exists(id))
        {
            String msg="Reservation with number "+id+" does not exist";
            error.setMessage(msg);
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
                    error.setMessage(msg);
                }
                else if(checkReservationForExistingFlights(reservation,flightsToBeRemoved))
                {
                    List<Flight> oldFlights=reservation.getFlights();
                    passenger=reservation.getPassenger();
                    List<Flight> passengerFlights=passenger.getFlights();
                    int oldTotal=reservation.getPrice();
                    int newTotal=oldTotal;
                    for(Flight flight:flightsToBeRemoved)
                    {
                        flight.setSeatsLeft(flight.getSeatsLeft()+1);
                        flightDao.save(flight);
                        oldFlights.remove(flight);
                        passengerFlights.remove(flight);
                        newTotal-=flight.getPrice();
                    }
                    passenger.setFlights(passengerFlights);
                    reservation.setPrice(newTotal);
                    reservation.setFlights(oldFlights);
                }
            }
            if(!flightsAdded.isEmpty())
            {
                if(!validateFlights(flightsAdded))
                {
                    String msg="Flights to be added does'nt exist";
                    error.setMessage(msg);
                }
                else
                {
                    List<Flight> flightsToBeAdded=getFlights(flightsAdded);
                    passenger=reservation.getPassenger();
                    List<Flight> passengerFlights=passenger.getFlights();

                    if(checkOverlap(passenger,flightsToBeAdded))
                    {
                        int newTotal=reservation.getPrice();
                        List<Flight> flightList=reservation.getFlights();
                        for(Flight flight:flightsToBeAdded)
                        {
                            flight.setSeatsLeft(flight.getSeatsLeft()-1);
                            flightDao.save(flight);
                            flightList.add(flight);
                            passengerFlights.add(flight);
                            newTotal+=flight.getPrice();
                        }
                        passenger.setFlights(passengerFlights);
                        reservation.setFlights(flightList);
                        reservation.setPrice(newTotal);
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
            error.setMessage(e.getMessage());
        }
        return new ResponseEntity(error.getMessageJSON().toString(),HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/reservation",
                    method = RequestMethod.GET,
                    produces = "application/xml")
    public ResponseEntity<Object> searchReservationByAllParam(@RequestParam(required = false) String passengerId,
                                                    @RequestParam(required = false) String from,
                                                    @RequestParam(required = false) String to,
                                                    @RequestParam(required = false) String flightNumber)
    {
        List<Reservation> reservations=null;
        JSONObject result=new JSONObject();
        JSONArray reservationArray=new JSONArray();

        boolean passengerIdExists=isEmpty(passengerId);
        boolean toExists=isEmpty(to);
        boolean fromExists=isEmpty(from);
        boolean flightNumberExists=isEmpty(flightNumber);
        if(!passengerIdExists&&!toExists&&!fromExists&&!flightNumberExists)
        {
            Message message=new Message("Please pass valid search params","400");
            return new ResponseEntity<Object>(message.getMessageJSON().toString(),HttpStatus.BAD_REQUEST);
        }
        else
        {
            if (passengerIdExists && fromExists && toExists && flightNumberExists)
                reservations = reservationDao.findByAllParam(flightNumber, to, from, passengerId);
            else if (passengerIdExists)
            {
                if (toExists && fromExists)
                    reservations = reservationDao.findByPassengerSourceDestination(passengerId, from, to);
                else if (toExists && flightNumberExists)
                    reservations = reservationDao.findByPassengerDestinationFlight(passengerId, to, flightNumber);
                else if (fromExists && flightNumberExists) {
                    System.out.println(passengerId);
                    System.out.println(from);
                    System.out.println(flightNumber);
                    reservations = reservationDao.findByPassengerSourceFlight(passengerId, from, flightNumber);
                }
                else if (toExists)
                    reservations = reservationDao.findByPassengerDestination(passengerId, to);
                else if (fromExists)
                    reservations = reservationDao.findByPassengerSource(passengerId, from);
                else if (flightNumberExists)
                    reservations = reservationDao.findByPassengerFlight(passengerId, flightNumber);
                else
                    reservations = reservationDao.findByPassengerId(passengerId);
            }
            else if (flightNumberExists)
            {
                if (fromExists && toExists)
                    reservations = reservationDao.findByFlightSourceDestination(flightNumber, from, to);
                else if (fromExists)
                    reservations = reservationDao.findBySourceFlight(from, flightNumber);
                else if (toExists)
                    reservations = reservationDao.findByDestinationFlight(to, flightNumber);
                else
                    reservations = reservationDao.findByFlightNumber(flightNumber);
            }
            else if (fromExists)
            {
                if (toExists)
                    reservations = reservationDao.findBySourceDestination(from, to);
                else
                    reservations = reservationDao.findBySource(from);
            } else if (toExists)
            {
                reservations = reservationDao.findByDestination(to);
            }

            for(Reservation res:reservations)
                reservationArray.put(res.getFullJSON());
            result.put("reservations",reservationArray);
            if(reservations.size()<1||reservations==null||reservations.isEmpty())
            {
                Message message=new Message("No results found","200");
                return new ResponseEntity<Object>(message.getMessageJSON().toString(),HttpStatus.OK);

            }
            else
                return new ResponseEntity<Object>(XML.toString(result),HttpStatus.OK);
        }

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

    private boolean validatePassenger(String passengerId)
    {
        return passengerDao.exists(passengerId);
    }
    private boolean validateFlights(List<String> flights)
    {
        /*for(String flightId:flights)
        {
            if(!flightDao.exists(flightId))
                return false;

        }*/
        for(int i=0;i<flights.size()-1;i++)
        {
            for(int j=i+1;j<flights.size();j++)
            {
                if(flightDao.exists(flights.get(i))&&flightDao.exists(flights.get(j)))
                {
                    Flight one=flightDao.findOne(flights.get(i));
                    Flight two=flightDao.findOne(flights.get(j));
                    if(checkFlightDatesClash(one.getDepartureTime(),one.getArrivalTime(),two.getDepartureTime(),two.getArrivalTime()))
                    {
                        return false;
                    }
                }
                else
                    return false;
            }
        }

        return true;
    }

    public static boolean checkOverlap(Passenger passenger,List<Flight> flights)
    {
        for(Flight flight:flights)
        {
            Date departureDate=flight.getDepartureTime();
            Date arrivalDate=flight.getArrivalTime();
            List<Flight> bookedFlights=passenger.getFlights();
            for(Flight booked:bookedFlights)
            {
                if(!booked.equals(flight)) {
                    Date bookedDeparture = booked.getDepartureTime();
                    Date bookedArrival = booked.getArrivalTime();
                    System.out.println("bookedD:"+bookedDeparture.toString());
                    System.out.println("bookedA:"+bookedArrival.toString());
                    System.out.println("newD:"+departureDate.toString());
                    System.out.println("newA:"+arrivalDate.toString());
                    System.out.println(checkFlightDatesClash(bookedDeparture, bookedArrival, departureDate, arrivalDate));
                    if (checkFlightDatesClash(bookedDeparture, bookedArrival, departureDate, arrivalDate))
                        return false;
                }
            }
        }
        return true;
    }

    private static boolean checkFlightDatesClash(Date oldDep,Date oldArr,Date newDep,Date newArr)
    {
        return (oldDep.compareTo(newArr)<=0)&&(newDep.compareTo(oldArr)<=0);
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

    private boolean isEmpty(String param)
    {
        if(param==null)
            return false;
        else
            return !param.isEmpty();
    }
}

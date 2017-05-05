package edu.sjsu.cmpe275.lab2.controller;

import edu.sjsu.cmpe275.lab2.dao.FlightDao;
import edu.sjsu.cmpe275.lab2.dao.PassengerDao;
import edu.sjsu.cmpe275.lab2.main.Message;
import edu.sjsu.cmpe275.lab2.model.Flight;
import edu.sjsu.cmpe275.lab2.model.Passenger;
import edu.sjsu.cmpe275.lab2.model.Reservation;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


/**
 * Created by parth on 4/16/2017.
 */

@RestController
@ComponentScan(value ="edu.sjsu.cmpe275.lab2.dao")
public class PassengerController
{
    @Autowired
    PassengerDao passengerDao;

    @Autowired
    FlightDao flightDao;

    /**
     * Create a new passenger using passed parameters
     * @param firstname First Name
     * @param lastname Last Name
     * @param age Age
     * @param gender Gender
     * @param phone Phone Number
     * @param response for redirection to get Passenger
     */
    @RequestMapping(value = "passenger",method = RequestMethod.POST,produces = "application/json")
    public ResponseEntity<Object> createPassenger(@RequestParam(value="firstname")String firstname,
                                  @RequestParam(value="lastname")String lastname,
                                  @RequestParam(value="age")int age,
                                  @RequestParam(value="gender")String gender,
                                  @RequestParam(value="phone")String phone,
                                  HttpServletResponse response)
    {
        Message message = new Message("","400");
        Passenger passenger=null;
        try
        {
            passenger = new Passenger(firstname, lastname, age, gender, phone);
            passengerDao.save(passenger);
            response.sendRedirect("/passenger/"+passenger.getId());
        }
        catch (DataIntegrityViolationException e)
        {
            message.setMessage("Another passenger with the same Phone number already exists");
        }
        catch (IOException e)
        {
            message.setMessage(e.toString());
        }

        return new ResponseEntity<Object>(message.getMessageJSON().toString(),HttpStatus.BAD_REQUEST);
    }

    /**
     * Update a existing Passenger
     * @param id Passenger Id
     * @param firstname First Name
     * @param lastname Last Name
     * @param age Age
     * @param gender Gender
     * @param phone Phone Number
     * @param response for redirection to get Passenger
     */
    @RequestMapping(value = "passenger/{id}",method = RequestMethod.PUT,produces = "application/json")
    public ResponseEntity<Object> updatePassenger(@PathVariable("id") String id,
                                  @RequestParam(value="firstname")String firstname,
                                  @RequestParam(value="lastname")String lastname,
                                  @RequestParam(value="age")int age,
                                  @RequestParam(value="gender")String gender,
                                  @RequestParam(value="phone")String phone,
                                  HttpServletResponse response)
    {
        Message message =new Message("","404");
        try
        {
            Passenger passenger = passengerDao.findOne(id);
            if (passenger != null) {
                passenger.setFirstname(firstname);
                passenger.setLastname(lastname);
                passenger.setAge(age);
                passenger.setGender(gender);
                passenger.setPhone(phone);
                passengerDao.save(passenger);
                response.sendRedirect("/passenger/"+id);
            }
        }
        catch(DataIntegrityViolationException e)
        {
            message.setMessage("Cannot update , passenger with same phone number already exists");
        }
        catch(IOException e)
        {
            message.setMessage(e.getMessage());
        }
        return new ResponseEntity(message.getMessageJSON().toString(),HttpStatus.NOT_FOUND);
    }

    /**
     * Get Passenger as JSON
     * @param id PassengerId
     */
    @RequestMapping(value = "passenger/{id}",
            method = RequestMethod.GET,
            produces = "application/json")
    @ResponseBody
    public ResponseEntity<Object> getPassengerJson(@PathVariable("id") String id)
    {
        if(passengerDao.exists(id))
        {
            Passenger passenger=passengerDao.findOne(id);
            return new ResponseEntity(passenger.getFullJSON().toString(),HttpStatus.OK);
        }
        else
        {
            Message error=new Message("Sorry, the requested passenger with id " + id + " does not exist","404");
            return new ResponseEntity(error.getMessageJSON().toString(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Get Passenger as XML if xml param is true
     * @param id PassengerId
     * @param xml boolean
     */
    @RequestMapping(value = "passenger/{id}",
            params = "xml",
            method = RequestMethod.GET,
            produces = "application/xml")
    @ResponseBody
    public ResponseEntity<Object> getPassengerXml(@PathVariable("id") String id,
                                                   @RequestParam(value="xml")boolean xml)
    {
        if(xml)
        {
            if(passengerDao.exists(id))
            {
                Passenger passenger=passengerDao.findOne(id);
                return new ResponseEntity(passenger.getXML(),HttpStatus.OK);
            }
            else
            {
                Message error=new Message("Sorry, the requested passenger with id " + id + " does not exist","404");
                return new ResponseEntity(error.getXML(), HttpStatus.NOT_FOUND);
            }
        }
        else
        {
            Message error=new Message("Please check xml param","404");
            return new ResponseEntity(error.getXML(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Delete a Passenger
     * @param id Passenger Id
     */
    @RequestMapping(value = "passenger/{id}",
            method = RequestMethod.DELETE,
            produces = "application/xml")
    public ResponseEntity<Object> deletePassenger(@PathVariable("id") String id)
    {
        Message success=new Message("Passenger with id "+id+" is deleted successfully ","200");
        Message error=new Message("Passenger with id "+id+" does not exist","404");
        if(passengerDao.exists(id))
        {
            Passenger passenger=passengerDao.findOne(id);
            List<Flight> flights= passenger.getFlights();
            for(Flight flight:flights)
            {
                flight.setSeatsLeft(flight.getSeatsLeft()+1);
                flightDao.save(flight);
            }
            passengerDao.delete(id);
            return new ResponseEntity(success.getXML(),HttpStatus.OK);
        }
        else
        {
            return new ResponseEntity(error.getXML(),HttpStatus.NOT_FOUND);
        }
    }
}

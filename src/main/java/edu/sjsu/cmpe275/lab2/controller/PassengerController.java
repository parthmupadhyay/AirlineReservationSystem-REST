package edu.sjsu.cmpe275.lab2.controller;

import edu.sjsu.cmpe275.lab2.dao.PassengerDao;
import edu.sjsu.cmpe275.lab2.main.Message;
import edu.sjsu.cmpe275.lab2.model.Passenger;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * Created by parth on 4/16/2017.
 */

@RestController
@ComponentScan(value ="edu.sjsu.cmpe275.lab2.dao")
public class PassengerController
{
    @Autowired
    PassengerDao passengerDao;

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
        String passengerId="";
        try
        {
            passenger = new Passenger(firstname, lastname, age, gender, phone);
            passengerDao.save(passenger);
            passengerId=passenger.getId();
            response.sendRedirect("/passenger/"+passengerId);
        }
        catch (DataIntegrityViolationException e)
        {
            message.setMessage("Another passenger with the same Phone number already exists");
        }
        catch (IOException e)
        {
            message.setMessage(e.getMessage());
        }

        return new ResponseEntity<Object>(message.getMessageJSON().toString(),HttpStatus.BAD_REQUEST);
    }

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
                return new ResponseEntity(XML.toString(error.getMessageJSON()), HttpStatus.NOT_FOUND);
            }

        }
        else {
            Message error=new Message("Please check xml param","404");
            return new ResponseEntity(XML.toString(error.getMessageJSON()), HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "passenger/{id}",
            method = RequestMethod.DELETE,
            produces = "application/xml")
    public ResponseEntity<Object> deletePassenger(@PathVariable("id") String id)
    {
        Message success=new Message("Passenger with id "+id+" is deleted successfully ","200");
        Message error=new Message("Passenger with id "+id+" does not exist","404");
        if(passengerDao.exists(id))
        {
            passengerDao.delete(id);
            return new ResponseEntity(XML.toString(success.getMessageJSON()),HttpStatus.OK);
        }
        else
        {
            return new ResponseEntity(XML.toString(error.getMessageJSON()),HttpStatus.NOT_FOUND);
        }
    }
}

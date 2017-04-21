package edu.sjsu.cmpe275.lab2.controller;

import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import edu.sjsu.cmpe275.lab2.dao.PassengerDao;
import edu.sjsu.cmpe275.lab2.model.Passenger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
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

    @RequestMapping(value = "passenger",method = RequestMethod.POST)
    public ResponseEntity<Object> createPassenger(@RequestParam(value="firstname")String firstname,
                                  @RequestParam(value="lastname")String lastname,
                                  @RequestParam(value="age")int age,
                                  @RequestParam(value="gender")String gender,
                                  @RequestParam(value="phone")String phone,
                                  HttpServletResponse response)
    {
        String msg="";
        Passenger passenger=null;
        String passengerId="";
        try
        {
            passenger = new Passenger(firstname, lastname, age, gender, phone);
            passengerDao.save(passenger);
            passengerId=passenger.getId();
            response.sendRedirect("/passenger/"+passengerId+"?json=true");
        }
        catch (DataIntegrityViolationException e)
        {
            msg="Another passenger with the same number already exists";
        }
        catch (IOException e)
        {
            msg=e.getMessage();
        }
        String error="{\n" + "       \"BadRequest\": {\n" + "              \"code\": \"400\",\n" +
                "               \"msg\": \""+msg+"”\n" + "       }\n" + "}\n";

        return new ResponseEntity<Object>(error,HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "passenger/{id}",method = RequestMethod.PUT)
    public ResponseEntity<Object> updatePassenger(@PathVariable("id") String id,
                                  @RequestParam(value="firstname")String firstname,
                                  @RequestParam(value="lastname")String lastname,
                                  @RequestParam(value="age")int age,
                                  @RequestParam(value="gender")String gender,
                                  @RequestParam(value="phone")String phone,
                                  HttpServletResponse response)
    {
        String msg="";

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
                response.sendRedirect("/passenger/"+id+"?json=true");
            }
        }
        catch(DataIntegrityViolationException e)
        {
            msg="Cannot update , passenger with same phone number already exists";
        }
        catch(IOException e)
        {
            msg=e.getMessage();
        }
        String error="{\n" + "       \"BadRequest\": {\n" + "              \"code\": \"404 \",\n" +
                "              \"msg\": \""+msg+"\"\n" + "       }\n" + "}\n";
        return new ResponseEntity(error,HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "passenger/{id}",
            params = "json",
            method = RequestMethod.GET,
            produces = "application/json")
    @ResponseBody
    public ResponseEntity<Object> getPassengerJson(@PathVariable("id") String id,
                                               @RequestParam(value="json")boolean json)
    {
        if(json)
        {
            Passenger passenger=passengerDao.findOne(id);
            return new ResponseEntity(passenger.getFullJSON().toString(),HttpStatus.OK);

        }
        else {
            String error = "{\n" + "\t\"BadRequest\":\n {\n" + "\t\t\"code\": \" 404 \",\n" +
                    "\t\t\"msg\": \" Sorry, the requested passenger with id " + id + " does not exist\"\n" + "\t}\n" + "}\n";

            return new ResponseEntity(error, HttpStatus.NOT_FOUND);
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
            Passenger passenger=passengerDao.findOne(id);
            return new ResponseEntity(passenger.getXML(),HttpStatus.OK);

        }
        else {
            String error = "{\n" + "\t\"BadRequest\":\n {\n" + "\t\t\"code\": \" 404 \",\n" +
                    "\t\t\"msg\": \" Sorry, the requested passenger with id " + id + " does not exist\"\n" + "\t}\n" + "}\n";

            return new ResponseEntity(error, HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "passenger/{id}",
            method = RequestMethod.DELETE,
            produces = "application/xml")
    public ResponseEntity<Object> deletePassenger(@PathVariable("id") String id)
    {
        String error="{\n" + "       \"BadRequest\": {\n" + "              \"code\": \"404 \",\n" +
                "              \"msg\": \"Passenger with id "+id+" does not exist\"\n" + "       }\n" + "}\n";
        String success="<Response>\n" + "           <code> 200 </code>\n" +
                "           <msg> Passenger with id "+id+" is deleted successfully  </msg>\n" + "</Response>\n";
        if(passengerDao.exists(id))
        {
            passengerDao.delete(id);
            return new ResponseEntity(success,HttpStatus.OK);
        }
        else
        {
            return new ResponseEntity(error,HttpStatus.NOT_FOUND);
        }
    }






}

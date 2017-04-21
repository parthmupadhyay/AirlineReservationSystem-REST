package edu.sjsu.cmpe275.lab2.controller;

import edu.sjsu.cmpe275.lab2.dao.FlightDao;
import edu.sjsu.cmpe275.lab2.model.Flight;
import edu.sjsu.cmpe275.lab2.model.Plane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by parth on 4/17/2017.
 */
@RestController
@ComponentScan(value ="edu.sjsu.cmpe275.lab2.dao")
public class FlightController
{
    @Autowired
    private FlightDao flightDao;

    @RequestMapping(value = "/flight/{flightNumber}",
            params = "json",
            method = RequestMethod.GET,
            produces = "application/json")
    @ResponseBody
    public ResponseEntity<Object> getFlightJSON(@PathVariable("flightNumber") String flightNumber,
                                                @RequestParam(value="json")boolean json)
    {
        String error="{\n" + "\t\"BadRequest\": {\n" + "\t\t\"code\": \" 404 \",\n" +
                "\t\t\"msg\": \" Sorry, the requested flight with number "+flightNumber+" does not exist \"\n" + "\t}\n" + "}\n";
        if(json)
        {
            if (flightDao.exists(flightNumber)) {
                Flight flight = flightDao.findOne(flightNumber);
                return new ResponseEntity(flight.getFullJson().toString(), HttpStatus.OK);
            }
        }
        return new ResponseEntity(error,HttpStatus.NOT_FOUND);

    }

    @RequestMapping(value = "/flight/{flightNumber}",
            params = "xml",
            method = RequestMethod.GET,
            produces = "application/xml")
    @ResponseBody
    public ResponseEntity<Object> getFlightXML(@PathVariable("flightNumber") String flightNumber,
                                               @RequestParam(value="xml")boolean xml)
    {
        String error="{\n" + "\t\"BadRequest\": {\n" + "\t\t\"code\": \" 404 \",\n" +
                "\t\t\"msg\": \" Sorry, the requested flight with number "+flightNumber+" does not exist \"\n" + "\t}\n" + "}\n";
        if(xml)
        {
            if (flightDao.exists(flightNumber)) {
                Flight flight = flightDao.findOne(flightNumber);
                return new ResponseEntity(flight.getXML(), HttpStatus.OK);
            }
        }
        return new ResponseEntity(error,HttpStatus.NOT_FOUND);

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
            if (!flightDao.exists(flightNumber)) {
                Plane plane = new Plane(capacity, model, manufacturer, yearOfManufacture);
                SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd-HH");
                Flight newFlight = new Flight(flightNumber,
                        price, plane, from, to,
                        sdf.parse(departureTime), sdf.parse(arrivalTime),
                        capacity, description);
                flightDao.save(newFlight);
                response.sendRedirect("/flight/"+flightNumber+"?xml=true");

            }
        }
        catch(Exception e)
        {

            return new ResponseEntity<Object>(e,HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Object>("nhi hua create",HttpStatus.NOT_FOUND);
    }

}

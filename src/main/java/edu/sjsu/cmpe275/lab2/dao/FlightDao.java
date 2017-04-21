package edu.sjsu.cmpe275.lab2.dao;

import edu.sjsu.cmpe275.lab2.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by parth on 4/17/2017.
 */
public interface FlightDao extends JpaRepository<Flight,String> {
}

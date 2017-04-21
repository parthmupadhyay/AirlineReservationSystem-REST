package edu.sjsu.cmpe275.lab2.dao;

import edu.sjsu.cmpe275.lab2.model.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;

/**
 * Created by parth on 4/16/2017.
 */

public interface PassengerDao extends JpaRepository<Passenger,Integer>
{
}

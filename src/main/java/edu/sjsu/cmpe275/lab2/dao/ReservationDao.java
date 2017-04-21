package edu.sjsu.cmpe275.lab2.dao;

import edu.sjsu.cmpe275.lab2.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by parth on 4/17/2017.
 */
public interface ReservationDao extends JpaRepository<Reservation,Integer>
{
}

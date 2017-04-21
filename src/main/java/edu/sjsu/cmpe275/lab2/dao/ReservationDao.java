package edu.sjsu.cmpe275.lab2.dao;

import edu.sjsu.cmpe275.lab2.model.Passenger;
import edu.sjsu.cmpe275.lab2.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by parth on 4/17/2017.
 */
public interface ReservationDao extends JpaRepository<Reservation,String>
{
    List<Reservation> findByPassenger(Passenger passenger);

    @Query("select r from Reservation r inner join r.flights f where f.number=(:flightNumber)")
    public List<Reservation> findByFlightNumber(@Param("flightNumber") String flightNumber);

    @Query("select r from Reservation r inner join r.flights f where f.source=(:source)")
    public List<Reservation> findBySource(@Param("source") String source);

    @Query("select r from Reservation r inner join r.flights f where f.destination=(:destination)")
    public List<Reservation> findByDestination(@Param("destination") String destination);

    @Query("select r from Reservation r inner join r.flights f where f.destination=(:destination)" +
            " and f.source=(:source) and f.number=(:flightNumber) and r.passenger.id=(:passengerId)")
    public List<Reservation> find(@Param("flightNumber") String flightNumber,
                                  @Param("destination") String destination,
                                  @Param("source") String source ,
                                  @Param("passengerId")String passengerId );

}

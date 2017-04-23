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
    @Query("select r from Reservation r where r.passenger.id=(:passengerId)")
    List<Reservation> findByPassengerId(@Param("passengerId")String passengerId);

    @Query("select r from Reservation r inner join r.flights f where f.number=(:flightNumber)")
    List<Reservation> findByFlightNumber(@Param("flightNumber") String flightNumber);

    @Query("select r from Reservation r inner join r.flights f where f.source=(:source)")
    List<Reservation> findBySource(@Param("source") String source);

    @Query("select r from Reservation r inner join r.flights f where f.destination=(:destination)")
    List<Reservation> findByDestination(@Param("destination") String destination);

    @Query("select r from Reservation r inner join r.flights f where f.destination=(:destination)" +
            " and f.source=(:source) and f.number=(:flightNumber) and r.passenger.id=(:passengerId)")
    List<Reservation> findByAllParam(@Param("flightNumber") String flightNumber,
                                  @Param("destination") String destination,
                                  @Param("source") String source ,
                                  @Param("passengerId")String passengerId );

    @Query("select r from Reservation r inner join r.flights f where f.number=(:flightNumber) and r.passenger.id=(:passengerId)")
    List<Reservation> findByPassengerFlight(@Param("passengerId")String passengerId,@Param("flightNumber")String flightNumber);

    @Query("select r from Reservation r inner join r.flights f where f.source=(:source)and r.passenger.id=(:passengerId)")
    List<Reservation> findByPassengerSource(@Param("passengerId")String passengerId,@Param("source")String source);

    @Query("select r from Reservation r inner join r.flights f where f.destination=(:destination)and r.passenger.id=(:passengerId)")
    List<Reservation> findByPassengerDestination(@Param("passengerId")String passengerId,@Param("destination")String destination);

    @Query("select r from Reservation r inner join r.flights f where f.source=(:source) and f.destination=(:destination)")
    List<Reservation> findBySourceDestination(@Param("source")String source,@Param("destination")String destination);

    @Query("select r from Reservation r inner join r.flights f where f.source=(:source) and f.number=(:flightNumber)")
    List<Reservation> findBySourceFlight(@Param("source")String source,@Param("flightNumber")String flightNumber);

    @Query("select r from Reservation r inner join r.flights f where f.destination=(:destination) and f.number=(:flightNumber)")
    List<Reservation> findByDestinationFlight(@Param("destination")String destination,@Param("flightNumber")String flightNumber);

    @Query("select r from Reservation r inner join r.flights f where r.passenger.id=(:passengerId) and f.destination=(:destination) and f.number=(:flightNumber)")
    List<Reservation> findByPassengerDestinationFlight(@Param("passengerId")String passengerId,@Param("destination")String destination,@Param("flightNumber")String flightNumber);

    @Query("select r from Reservation r inner join r.flights f where r.passenger.id=(:passengerId) and f.source=(:source) and f.number=(:flightNumber)")
    List<Reservation> findByPassengerSourceFlight(@Param("passengerId")String passengerId,@Param("source")String source,@Param("flightNumber")String flightNumber);

    @Query("select r from Reservation r inner join r.flights f where r.passenger.id=(:passengerId) and f.source=(:source) and f.destination=(:destination)")
    List<Reservation> findByPassengerSourceDestination(@Param("passengerId")String passengerId,@Param("source")String source,@Param("destination")String destination);

    @Query("select r from Reservation r inner join r.flights f where f.number=(:flightNumber) and f.source=(:source) and f.destination=(:destination)")
    List<Reservation> findByFlightSourceDestination(@Param("flightNumber")String flightNumber,@Param("source")String source,@Param("destination")String destination);


}

package com.klu.service;

import com.klu.entity.Booking;

import java.util.List;

public interface BookingService {

    Booking createBooking(Booking booking);

    List<Booking> getAllBookings();

    Booking getBookingById(Long id);

    List<Booking> getBookingsByUser(Long userId);

    List<Booking> getBookingsByVehicle(Long vehicleId);

    Booking updateBooking(Long id, Booking booking);

    Booking cancelBooking(Long id);

    Booking startRental(Long id);

    Booking returnVehicle(Long id);

    void deleteBooking(Long id);
}

package com.klu.service.impl;

import com.klu.entity.Booking;
import com.klu.repo.BookingRepository;
import com.klu.service.BookingService;
import com.klu.service.UserClient;
import com.klu.service.VehicleClient;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserClient userClient;

    @Autowired
    private VehicleClient vehicleClient;

    @Override
    public Booking createBooking(Booking booking) {
        // Validate dates
        if (booking.getEndDate().isBefore(booking.getStartDate()) ||
            booking.getEndDate().isEqual(booking.getStartDate())) {
            throw new RuntimeException("End date must be after start date");
        }

        // Verify user exists via User Service
        try {
            userClient.getUserById(booking.getUserId());
        } catch (FeignException e) {
            throw new RuntimeException("User not found with id: " + booking.getUserId());
        }

        // Verify vehicle exists and check availability via Vehicle Service
        Map<String, Object> vehicle;
        try {
            vehicle = vehicleClient.getVehicleById(booking.getVehicleId());
        } catch (FeignException e) {
            throw new RuntimeException("Vehicle not found with id: " + booking.getVehicleId());
        }

        String availabilityStatus = (String) vehicle.get("availabilityStatus");
        if (!"AVAILABLE".equals(availabilityStatus)) {
            throw new RuntimeException("Vehicle is not available");
        }

        // Check for overlapping bookings
        List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                booking.getVehicleId(), booking.getStartDate(), booking.getEndDate());

        if (!overlapping.isEmpty()) {
            throw new RuntimeException("Vehicle is already booked for the selected dates");
        }

        // Set status to CONFIRMED and save
        booking.setStatus("CONFIRMED");
        Booking savedBooking = bookingRepository.save(booking);

        // Update vehicle status to BOOKED
        vehicleClient.updateVehicleStatus(booking.getVehicleId(), Map.of("status", "BOOKED"));

        return savedBooking;
    }

    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    @Override
    public List<Booking> getBookingsByUser(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    @Override
    public List<Booking> getBookingsByVehicle(Long vehicleId) {
        return bookingRepository.findByVehicleId(vehicleId);
    }

    @Override
    public Booking updateBooking(Long id, Booking booking) {
        Booking existingBooking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if ("ACTIVE".equals(existingBooking.getStatus()) ||
            "COMPLETED".equals(existingBooking.getStatus()) ||
            "CANCELLED".equals(existingBooking.getStatus())) {
            throw new RuntimeException("Cannot update booking with status: " + existingBooking.getStatus());
        }

        // If dates changed, validate no overlap
        if (booking.getStartDate() != null && booking.getEndDate() != null) {
            if (booking.getEndDate().isBefore(booking.getStartDate()) ||
                booking.getEndDate().isEqual(booking.getStartDate())) {
                throw new RuntimeException("End date must be after start date");
            }

            List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                    existingBooking.getVehicleId(), booking.getStartDate(), booking.getEndDate());

            // Remove self from overlap check
            overlapping.removeIf(b -> b.getBookingId().equals(id));

            if (!overlapping.isEmpty()) {
                throw new RuntimeException("Vehicle is already booked for the selected dates");
            }

            existingBooking.setStartDate(booking.getStartDate());
            existingBooking.setEndDate(booking.getEndDate());
        }

        return bookingRepository.save(existingBooking);
    }

    @Override
    public Booking cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!"CONFIRMED".equals(booking.getStatus()) && !"PENDING".equals(booking.getStatus())) {
            throw new RuntimeException("Cannot cancel booking with status: " + booking.getStatus());
        }

        booking.setStatus("CANCELLED");
        Booking savedBooking = bookingRepository.save(booking);

        // Update vehicle status back to AVAILABLE
        vehicleClient.updateVehicleStatus(booking.getVehicleId(), Map.of("status", "AVAILABLE"));

        return savedBooking;
    }

    @Override
    public Booking startRental(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!"CONFIRMED".equals(booking.getStatus())) {
            throw new RuntimeException("Cannot start rental for booking with status: " + booking.getStatus());
        }

        booking.setStatus("ACTIVE");
        Booking savedBooking = bookingRepository.save(booking);

        // Update vehicle status to RENTED
        vehicleClient.updateVehicleStatus(booking.getVehicleId(), Map.of("status", "RENTED"));

        return savedBooking;
    }

    @Override
    public Booking returnVehicle(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!"ACTIVE".equals(booking.getStatus())) {
            throw new RuntimeException("Cannot return vehicle for booking with status: " + booking.getStatus());
        }

        booking.setStatus("COMPLETED");
        Booking savedBooking = bookingRepository.save(booking);

        // Update vehicle status back to AVAILABLE
        vehicleClient.updateVehicleStatus(booking.getVehicleId(), Map.of("status", "AVAILABLE"));

        return savedBooking;
    }

    @Override
    public void deleteBooking(Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new RuntimeException("Booking not found");
        }
        bookingRepository.deleteById(id);
    }
}

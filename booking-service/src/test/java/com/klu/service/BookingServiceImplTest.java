package com.klu.service;

import com.klu.entity.Booking;
import com.klu.repo.BookingRepository;
import com.klu.service.impl.BookingServiceImpl;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private VehicleClient vehicleClient;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Booking sampleBooking;

    @BeforeEach
    void setUp() {
        sampleBooking = new Booking(1L, 1L, 1L,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 5),
                "CONFIRMED");
    }

    @Test
    void testCreateBookingSuccess() {
        Booking requestBooking = new Booking(null, 1L, 1L,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 5),
                null);

        when(userClient.getUserById(1L)).thenReturn(Map.of("userId", 1, "name", "Alice"));
        when(vehicleClient.getVehicleById(1L)).thenReturn(Map.of("vehicleId", 1, "availabilityStatus", "AVAILABLE"));
        when(bookingRepository.findOverlappingBookings(eq(1L), any(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);

        Booking created = bookingService.createBooking(requestBooking);

        assertNotNull(created);
        assertEquals("CONFIRMED", created.getStatus());
        verify(vehicleClient, times(1)).updateVehicleStatus(1L, Map.of("status", "BOOKED"));
        verify(bookingRepository, times(1)).save(requestBooking);
    }

    @Test
    void testCreateBookingInvalidDatesThrows() {
        Booking invalidBooking = new Booking(null, 1L, 1L,
                LocalDate.of(2026, 10, 5),
                LocalDate.of(2026, 10, 1),
                null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> bookingService.createBooking(invalidBooking));
        assertEquals("End date must be after start date", ex.getMessage());
    }

    @Test
    void testCreateBookingVehicleNotAvailableThrows() {
        Booking requestBooking = new Booking(null, 1L, 1L,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 5),
                null);

        when(userClient.getUserById(1L)).thenReturn(Map.of("userId", 1));
        when(vehicleClient.getVehicleById(1L)).thenReturn(Map.of("vehicleId", 1, "availabilityStatus", "BOOKED"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> bookingService.createBooking(requestBooking));
        assertEquals("Vehicle is not available", ex.getMessage());
    }

    @Test
    void testCreateBookingOverlappingThrows() {
        Booking requestBooking = new Booking(null, 1L, 1L,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 5),
                null);

        when(userClient.getUserById(1L)).thenReturn(Map.of("userId", 1));
        when(vehicleClient.getVehicleById(1L)).thenReturn(Map.of("vehicleId", 1, "availabilityStatus", "AVAILABLE"));
        when(bookingRepository.findOverlappingBookings(eq(1L), any(), any()))
                .thenReturn(List.of(sampleBooking));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> bookingService.createBooking(requestBooking));
        assertEquals("Vehicle is already booked for the selected dates", ex.getMessage());
    }

    @Test
    void testStartRental() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(sampleBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);

        Booking started = bookingService.startRental(1L);

        assertEquals("ACTIVE", started.getStatus());
        verify(vehicleClient, times(1)).updateVehicleStatus(1L, Map.of("status", "RENTED"));
    }

    @Test
    void testReturnVehicle() {
        sampleBooking.setStatus("ACTIVE");
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(sampleBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);

        Booking returned = bookingService.returnVehicle(1L);

        assertEquals("COMPLETED", returned.getStatus());
        verify(vehicleClient, times(1)).updateVehicleStatus(1L, Map.of("status", "AVAILABLE"));
    }

    @Test
    void testCancelBooking() {
        sampleBooking.setStatus("CONFIRMED");
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(sampleBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);

        Booking cancelled = bookingService.cancelBooking(1L);

        assertEquals("CANCELLED", cancelled.getStatus());
        verify(vehicleClient, times(1)).updateVehicleStatus(1L, Map.of("status", "AVAILABLE"));
    }

    @Test
    void testDeleteBooking() {
        when(bookingRepository.existsById(1L)).thenReturn(true);
        doNothing().when(bookingRepository).deleteById(1L);

        bookingService.deleteBooking(1L);
        verify(bookingRepository, times(1)).deleteById(1L);
    }
}

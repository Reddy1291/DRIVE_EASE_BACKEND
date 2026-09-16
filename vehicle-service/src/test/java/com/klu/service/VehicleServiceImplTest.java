package com.klu.service;

import com.klu.entity.Vehicle;
import com.klu.repo.VehicleRepository;
import com.klu.service.impl.VehicleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private Vehicle sampleVehicle;

    @BeforeEach
    void setUp() {
        sampleVehicle = new Vehicle(1L, "Sedan", "Tesla Model 3", "AVAILABLE", 85.0);
    }

    @Test
    void testAddVehicle() {
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(sampleVehicle);

        Vehicle added = vehicleService.addVehicle(sampleVehicle);
        assertNotNull(added);
        assertEquals("Tesla Model 3", added.getModel());
        verify(vehicleRepository, times(1)).save(sampleVehicle);
    }

    @Test
    void testGetAllVehicles() {
        when(vehicleRepository.findAll()).thenReturn(List.of(sampleVehicle));

        List<Vehicle> list = vehicleService.getAllVehicles();
        assertEquals(1, list.size());
        assertEquals("Sedan", list.get(0).getType());
    }

    @Test
    void testGetVehicleByIdSuccess() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(sampleVehicle));

        Vehicle found = vehicleService.getVehicleById(1L);
        assertNotNull(found);
        assertEquals(1L, found.getVehicleId());
    }

    @Test
    void testGetVehicleByIdNotFoundThrows() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> vehicleService.getVehicleById(99L));
    }

    @Test
    void testGetAvailableVehicles() {
        when(vehicleRepository.findByAvailabilityStatus("AVAILABLE")).thenReturn(List.of(sampleVehicle));

        List<Vehicle> available = vehicleService.getAvailableVehicles();
        assertEquals(1, available.size());
        assertEquals("AVAILABLE", available.get(0).getAvailabilityStatus());
    }

    @Test
    void testUpdateVehicle() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(sampleVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(sampleVehicle);

        Vehicle updateData = new Vehicle(null, "SUV", "BMW X5", "AVAILABLE", 120.0);
        Vehicle updated = vehicleService.updateVehicle(1L, updateData);

        assertNotNull(updated);
        assertEquals("SUV", sampleVehicle.getType());
        assertEquals("BMW X5", sampleVehicle.getModel());
        assertEquals(120.0, sampleVehicle.getRentalPrice());
    }

    @Test
    void testUpdateVehicleStatus() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(sampleVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(sampleVehicle);

        Vehicle updated = vehicleService.updateVehicleStatus(1L, "BOOKED");
        assertNotNull(updated);
        assertEquals("BOOKED", sampleVehicle.getAvailabilityStatus());
    }

    @Test
    void testDeleteVehicle() {
        when(vehicleRepository.existsById(1L)).thenReturn(true);
        doNothing().when(vehicleRepository).deleteById(1L);

        vehicleService.deleteVehicle(1L);
        verify(vehicleRepository, times(1)).deleteById(1L);
    }
}

package com.klu.service;

import com.klu.entity.Vehicle;

import java.util.List;

public interface VehicleService {

    Vehicle addVehicle(Vehicle vehicle);

    List<Vehicle> getAllVehicles();

    Vehicle getVehicleById(Long id);

    List<Vehicle> getAvailableVehicles();

    Vehicle updateVehicle(Long id, Vehicle vehicle);

    Vehicle updateVehicleStatus(Long id, String status);

    void deleteVehicle(Long id);
}

package com.klu.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vehicleId;

    @NotBlank(message = "Vehicle type is required")
    private String type;

    @NotBlank(message = "Vehicle model is required")
    private String model;

    @NotBlank(message = "Availability status is required")
    private String availabilityStatus;

    @NotNull(message = "Rental price is required")
    @Positive(message = "Rental price must be positive")
    private Double rentalPrice;

    public Vehicle() {
    }

    public Vehicle(Long vehicleId, String type, String model, String availabilityStatus, Double rentalPrice) {
        this.vehicleId = vehicleId;
        this.type = type;
        this.model = model;
        this.availabilityStatus = availabilityStatus;
        this.rentalPrice = rentalPrice;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public Double getRentalPrice() {
        return rentalPrice;
    }

    public void setRentalPrice(Double rentalPrice) {
        this.rentalPrice = rentalPrice;
    }
}

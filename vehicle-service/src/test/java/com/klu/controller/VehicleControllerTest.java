package com.klu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klu.entity.Vehicle;
import com.klu.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VehicleController.class)
@AutoConfigureMockMvc(addFilters = false)
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VehicleService vehicleService;

    @Autowired
    private ObjectMapper objectMapper;

    private Vehicle sampleVehicle;

    @BeforeEach
    void setUp() {
        sampleVehicle = new Vehicle(1L, "Sedan", "Tesla Model 3", "AVAILABLE", 85.0);
    }

    @Test
    void testAddVehicle() throws Exception {
        when(vehicleService.addVehicle(any(Vehicle.class))).thenReturn(sampleVehicle);

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleVehicle)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vehicleId").value(1))
                .andExpect(jsonPath("$.model").value("Tesla Model 3"));
    }

    @Test
    void testGetAllVehicles() throws Exception {
        when(vehicleService.getAllVehicles()).thenReturn(List.of(sampleVehicle));

        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].model").value("Tesla Model 3"));
    }

    @Test
    void testGetVehicleById() throws Exception {
        when(vehicleService.getVehicleById(1L)).thenReturn(sampleVehicle);

        mockMvc.perform(get("/api/vehicles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleId").value(1))
                .andExpect(jsonPath("$.availabilityStatus").value("AVAILABLE"));
    }

    @Test
    void testGetAvailableVehicles() throws Exception {
        when(vehicleService.getAvailableVehicles()).thenReturn(List.of(sampleVehicle));

        mockMvc.perform(get("/api/vehicles/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].availabilityStatus").value("AVAILABLE"));
    }

    @Test
    void testUpdateVehicle() throws Exception {
        when(vehicleService.updateVehicle(eq(1L), any(Vehicle.class))).thenReturn(sampleVehicle);

        mockMvc.perform(put("/api/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleVehicle)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleId").value(1));
    }

    @Test
    void testUpdateVehicleStatus() throws Exception {
        sampleVehicle.setAvailabilityStatus("BOOKED");
        when(vehicleService.updateVehicleStatus(1L, "BOOKED")).thenReturn(sampleVehicle);

        mockMvc.perform(put("/api/vehicles/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "BOOKED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availabilityStatus").value("BOOKED"));
    }

    @Test
    void testDeleteVehicle() throws Exception {
        doNothing().when(vehicleService).deleteVehicle(1L);

        mockMvc.perform(delete("/api/vehicles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Vehicle deleted successfully"));
    }
}

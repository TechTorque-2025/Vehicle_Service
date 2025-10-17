package com.techtorque.vehicle_service.service;

import com.techtorque.vehicle_service.dto.ServiceHistoryDto;

import java.util.List;

public interface ServiceHistoryService {

    /**
     * Retrieves service history for a specific vehicle.
     * This will eventually make inter-service calls to the Project/Service Management service.
     *
     * @param vehicleId The ID of the vehicle.
     * @param customerId The ID of the customer (for ownership verification).
     * @return List of service history records.
     */
    List<ServiceHistoryDto> getServiceHistory(String vehicleId, String customerId);
}


package com.techtorque.vehicle_service.service.impl;

import com.techtorque.vehicle_service.dto.ServiceHistoryDto;
import com.techtorque.vehicle_service.entity.Vehicle;
import com.techtorque.vehicle_service.exception.VehicleNotFoundException;
import com.techtorque.vehicle_service.repository.VehicleRepository;
import com.techtorque.vehicle_service.service.ServiceHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ServiceHistoryServiceImpl implements ServiceHistoryService {

    private final VehicleRepository vehicleRepository;
    // TODO: Inject RestTemplate or WebClient for inter-service communication
    // private final RestTemplate restTemplate;

    public ServiceHistoryServiceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public List<ServiceHistoryDto> getServiceHistory(String vehicleId, String customerId) {
        log.info("Fetching service history for vehicle: {} and customer: {}", vehicleId, customerId);

        // Verify vehicle exists and belongs to customer
        Vehicle vehicle = vehicleRepository.findByIdAndCustomerId(vehicleId, customerId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId, customerId));

        // TODO: Make REST call to Project/Service Management service
        // String serviceManagementUrl = "http://project-service/api/v1/services/vehicle/" + vehicleId;
        // ResponseEntity<List<ServiceHistoryDto>> response = restTemplate.exchange(
        //     serviceManagementUrl,
        //     HttpMethod.GET,
        //     null,
        //     new ParameterizedTypeReference<List<ServiceHistoryDto>>() {}
        // );
        // return response.getBody();

        // For now, return empty list as placeholder
        log.info("Returning empty service history (inter-service communication not yet implemented)");
        return new ArrayList<>();
    }
}

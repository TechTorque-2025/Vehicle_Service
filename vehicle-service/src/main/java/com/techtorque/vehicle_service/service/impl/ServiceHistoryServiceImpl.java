package com.techtorque.vehicle_service.service.impl;

import com.techtorque.vehicle_service.dto.ServiceHistoryDto;
import com.techtorque.vehicle_service.exception.VehicleNotFoundException;
import com.techtorque.vehicle_service.repository.VehicleRepository;
import com.techtorque.vehicle_service.service.ServiceHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
// import org.springframework.web.reactive.function.client.WebClient;
// import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation for fetching vehicle service history.
 * This service communicates with the Project/Service Management microservice
 * to retrieve service records for a specific vehicle.
 * 
 * Currently returns empty list as placeholder. To enable inter-service communication:
 * 1. Add WebClient dependency to pom.xml
 * 2. Uncomment WebClient configuration below
 * 3. Uncomment the makeServiceHistoryCall() method
 * 4. Update getServiceHistory() to call makeServiceHistoryCall()
 */
@Service
@Slf4j
public class ServiceHistoryServiceImpl implements ServiceHistoryService {

    private final VehicleRepository vehicleRepository;
    
    // WebClient for inter-service communication
    // Uncomment when Project Service is ready
    // private final WebClient webClient;
    
    @Value("${service.project-service.url:http://localhost:8084}")
    private String projectServiceUrl;

    public ServiceHistoryServiceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
        // this.webClient = WebClient.builder()
        //         .baseUrl(projectServiceUrl)
        //         .build();
    }

    @Override
    public List<ServiceHistoryDto> getServiceHistory(String vehicleId, String customerId) {
        log.info("Fetching service history for vehicle: {} and customer: {}", vehicleId, customerId);

        // Verify vehicle exists and belongs to customer
        vehicleRepository.findByIdAndCustomerId(vehicleId, customerId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId, customerId));

        // TODO: Enable when Project Service is ready
        // return makeServiceHistoryCall(vehicleId, customerId);

        // For now, return empty list as placeholder
        log.info("Returning empty service history (inter-service communication not yet implemented)");
        return new ArrayList<>();
    }

    /**
     * Makes an HTTP call to Project Service to fetch service history.
     * Uncomment this method when Project Service is ready for integration.
     * 
     * @param vehicleId The ID of the vehicle
     * @param customerId The ID of the customer
     * @return List of service history records
     */
    /*
    private List<ServiceHistoryDto> makeServiceHistoryCall(String vehicleId, String customerId) {
        try {
            String endpoint = String.format("/api/v1/services?vehicleId=%s", vehicleId);
            
            Mono<List<ServiceHistoryDto>> response = webClient.get()
                    .uri(endpoint)
                    .header("X-User-Subject", customerId)
                    .retrieve()
                    .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> {
                            log.error("Error fetching service history from Project Service: {}", 
                                    clientResponse.statusCode());
                            return Mono.error(new RuntimeException(
                                "Failed to fetch service history from Project Service"));
                        }
                    )
                    .bodyToFlux(ServiceHistoryDto.class)
                    .collectList();
            
            return response.block(); // Block to convert reactive to synchronous
            
        } catch (Exception e) {
            log.error("Error calling Project Service for service history", e);
            // Return empty list on error (graceful degradation)
            return new ArrayList<>();
        }
    }
    */
}


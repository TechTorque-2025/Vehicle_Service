package com.techtorque.vehicle_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vehicles")
@Data // Lombok: Generates getters, setters, toString, equals, and hashCode
@Builder // Lombok: Implements the builder pattern
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

  @Id
  private String id;

  @Column(nullable = false, updatable = false)
  private String customerId; // Foreign key linking to the user in the auth service

  @Column(nullable = false)
  private String make;

  @Column(nullable = false)
  private String model;

  @Column(nullable = false)
  private int year;

  @Column(unique = true, nullable = false)
  private String vin; // Vehicle Identification Number

  @Column(nullable = false)
  private String licensePlate;

  private String color;

  private int mileage;

  @CreationTimestamp // Automatically set the creation time
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp // Automatically set the update time on every modification
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  /**
   * Generates a vehicle ID before persisting if not already set
   * Format: VEH-YYYY-MAKE-MODEL-####
   * Example: VEH-2022-TOYOTA-CAMRY-0001
   */
  @PrePersist
  public void generateId() {
    if (this.id == null || this.id.isEmpty()) {
      // Generate a sequential number (using UUID last 4 chars as pseudo-random)
      String randomSuffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
      
      // Clean make and model (remove spaces, convert to uppercase)
      String cleanMake = this.make.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
      String cleanModel = this.model.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
      
      // Format: VEH-YYYY-MAKE-MODEL-####
      this.id = String.format("VEH-%d-%s-%s-%s", 
          this.year, 
          cleanMake, 
          cleanModel, 
          randomSuffix);
    }
  }
}
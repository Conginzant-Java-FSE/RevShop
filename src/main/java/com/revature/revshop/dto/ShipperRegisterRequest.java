package com.revature.revshop.dto;

/**
 * Request DTO for shipper registration.
 * Extends AuthBaseDTO for common auth fields.
 */
public class ShipperRegisterRequest extends AuthBaseDTO {

    private String vehicleNumber;

    public ShipperRegisterRequest() {
        // Required by Jackson for JSON deserialization
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }
}

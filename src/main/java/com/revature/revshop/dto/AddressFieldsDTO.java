package com.revature.revshop.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Shared base class for address-related DTOs.
 * Centralizes common address fields.
 */
public class AddressFieldsDTO {

    @NotBlank(message = "address line is required")
    private String addressLine;

    private String street;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    private String country;

    @NotBlank(message = "Zip code is required")
    private String zipCode;

    private String addressType;

    public AddressFieldsDTO() {
        /* Empty constructor for deserialization */
    }

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }
}

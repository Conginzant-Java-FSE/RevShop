package com.revature.revshop.dto;

public class AddressDTO extends AddressFieldsDTO {

    private Long addressId;
    private Long userId;
    private Boolean isDefault;

    public AddressDTO() {
        // Required by Jackson for JSON deserialization
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
}

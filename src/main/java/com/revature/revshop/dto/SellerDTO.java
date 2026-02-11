package com.revature.revshop.dto;

import java.util.List;

public class SellerDTO extends UserDTO {
    private String businessDescription;
    private String taxId;
    private String businessName;

    public SellerDTO() {
        super();
    }

    public SellerDTO(Long userId, String name, String email, String password, String phone, Integer age,
                     String businessName,
                     List<AddressDTO> addresses, String businessDescription, String taxId,
                     String securityQuestion, String securityAnswer) {
        super(userId, name, email, password, phone, age, "SELLER", securityQuestion, securityAnswer,
                addresses);
        this.businessName = businessName;
        this.businessDescription = businessDescription;
        this.taxId = taxId;
    }

    public String getBusinessDescription() {
        return businessDescription;
    }

    public void setBusinessDescription(String businessDescription) {
        this.businessDescription = businessDescription;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }
}

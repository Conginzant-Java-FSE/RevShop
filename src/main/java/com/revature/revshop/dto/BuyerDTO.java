package com.revature.revshop.dto;

import java.util.List;

public class BuyerDTO extends UserDTO {
    // currently no buyer specific fields, but architecture allows for them later (eg. loyalty points)

    public BuyerDTO() {
        super();
    }

    public BuyerDTO(Long userId, String name, String email, String password, String phone, Integer age,
                    List<AddressDTO> addresses, String securityQuestion, String securityAnswer) {
        super(userId, name, email, password, phone, age, "BUYER", securityQuestion, securityAnswer, addresses);
    }
}

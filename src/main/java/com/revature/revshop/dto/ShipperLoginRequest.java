package com.revature.revshop.dto;

public class ShipperLoginRequest {

    private String email;
    private String password;

    public ShipperLoginRequest() {
        // Required by Jackson for JSON deserialization
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

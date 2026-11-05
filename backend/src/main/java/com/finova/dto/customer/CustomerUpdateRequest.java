package com.finova.dto.customer;

import com.finova.entity.Gender;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating existing customer profile.
 * Protected attributes (customerCode, user, kycStatus, dateOfBirth) are strictly excluded.
 */
public class CustomerUpdateRequest {

    @Pattern(regexp = "^(\\+?[0-9\\s\\-\\(\\)]{7,25})?$", message = "Phone number must be valid with 7 to 25 digits/symbols")
    private String phoneNumber;

    @Pattern(regexp = "^(\\+?[0-9\\s\\-\\(\\)]{7,25})?$", message = "Alternate phone number must be valid")
    private String alternatePhoneNumber;

    private Gender gender;

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State cannot exceed 100 characters")
    private String state;

    @Size(min = 3, max = 20, message = "Postal code must be between 3 and 20 characters")
    private String postalCode;

    @Size(max = 100, message = "Country cannot exceed 100 characters")
    private String country;

    @Size(max = 100, message = "Occupation cannot exceed 100 characters")
    private String occupation;

    public CustomerUpdateRequest() {
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAlternatePhoneNumber() {
        return alternatePhoneNumber;
    }

    public void setAlternatePhoneNumber(String alternatePhoneNumber) {
        this.alternatePhoneNumber = alternatePhoneNumber;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }
}


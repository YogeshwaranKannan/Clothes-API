package Farme_rich.Seller.DTO.Request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSignupRequest {
    private String companyname;
    private String firstname;
    private String lastname;
    private String email;
    private String userid;
    public String created_at;
    private String mobileNum;
    private String deviceId;
    private String firstPiN;
    private String role;
    public String language_preferred;

    private String validationCode;

    // Getters and Setters


    public String getValidationCode() {
        return validationCode;
    }
    public void setValidationCode(String validationCode) {
        this.validationCode = validationCode;
    }

    public String getCompanyname() {
        return companyname;
    }
    public void setCompanyname(String companyname) {
        this.companyname = companyname;
    }

    public String getMobileNum() { return mobileNum; }
    public void setMobileNum(String mobileNum) { this.mobileNum = mobileNum; }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getFirstPiN() { return firstPiN; }
    public void setFirstPiN(String firstPiN) { this.firstPiN = firstPiN; }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}


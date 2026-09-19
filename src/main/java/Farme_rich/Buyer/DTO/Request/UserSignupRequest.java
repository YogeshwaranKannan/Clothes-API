package Farme_rich.Buyer.DTO.Request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserSignupRequest {
    private String firstname;
    private String lastname;
    private String email;
    private String userid;
    private String mobileNum;
    private String deviceId;
    private String firstPiN;
    @JsonProperty("ValidationCode")
    private String validationCode;

    // Getters and Setters
    public String getMobileNum() { return mobileNum; }
    public void setMobileNum(String mobileNum) { this.mobileNum = mobileNum; }

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

    public String getValidationCode() {
        return validationCode;
    }

    public void setValidationCode(String validationCode) {
        this.validationCode = validationCode;
    }
}

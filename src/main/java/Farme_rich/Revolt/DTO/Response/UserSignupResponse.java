package Farme_rich.Revolt.DTO.Response;


import Farme_rich.Seller.DTO.Response.MessageResponse;
import org.bson.types.ObjectId;

import java.util.List;

public class UserSignupResponse {
    public ObjectId id;
    public String token;
    public String companyid;
    public List<String> userid;
    public String email;
    public String user_id;
    public String firstName;
    public String lastName;
    public String mobileNum;
    public String deviceId;
    public String seller_type;
    public String language_preferred;
    public String companyname;
    public String firstPiN;
    public boolean isActive;
    public boolean paidUser;
    public boolean isgst;
    public boolean ispriceinclusive;
    public List<String> role;
    public String createAT;
    public String updatedAT;
    public String upi;
    public MessageResponse ResponseMessage;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCompanyname() {
        return companyname;
    }

    public void setCompanyname(String companyname) {
        this.companyname = companyname;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<String> getUserid() {
        return userid;
    }

    public void setUserid(List<String> userid) {
        this.userid = userid;
    }

    public String getSeller_type() {
        return seller_type;
    }

    public void setSeller_type(String seller_type) {
        this.seller_type = seller_type;
    }

    public String getLanguage_preferred() {
        return language_preferred;
    }

    public void setLanguage_preferred(String language_preferred) {
        this.language_preferred = language_preferred;
    }

    public String getUpi() {
        return upi;
    }

    public void setUpi(String upi) {
        this.upi = upi;
    }

    public boolean isIsgst() {
        return isgst;
    }

    public void setIsgst(boolean isgst) {
        this.isgst = isgst;
    }

    public boolean isIspriceinclusive() {
        return ispriceinclusive;
    }

    public void setIspriceinclusive(boolean ispriceinclusive) {
        this.ispriceinclusive = ispriceinclusive;
    }

    public boolean isPaidUser() {
        return paidUser;
    }

    public void setPaidUser(boolean paidUser) {
        this.paidUser = paidUser;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMobileNum() {
        return mobileNum;
    }

    public void setMobileNum(String mobileNum) {
        this.mobileNum = mobileNum;
    }

    public List<String> getRole() {
        return role;
    }

    public void setRole(List<String> role) {
        this.role = role;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getFirstPiN() {
        return firstPiN;
    }

    public void setFirstPiN(String firstPiN) {
        this.firstPiN = firstPiN;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getCreateAT() {
        return createAT;
    }

    public void setCreateAT(String createAT) {
        this.createAT = createAT;
    }

    public String getUpdatedAT() {
        return updatedAT;
    }

    public void setUpdatedAT(String updatedAT) {
        this.updatedAT = updatedAT;
    }

    public MessageResponse getResponseMessage() {
        return ResponseMessage;
    }

    public void setResponseMessage(MessageResponse responseMessage) {
        ResponseMessage = responseMessage;
    }
}

package Farme_rich.Buyer.DTO.Response;

public class UserSignupResponse {
    public String  id;
    public String userid;
    public String firstName;
    public String lastName;
    public String mobileNum;
    public String deviceId;
    public String firstPiN;
    public boolean isActive;
    public String createAT;
    public String updatedAT;
    public MessageResponse ResponseMessage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
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

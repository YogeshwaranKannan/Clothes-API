package Farme_rich.Buyer.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

// Annotation to specify this class as a MongoDB document
@Document(collection = "Buyer_User")
public class User {

    @Id
    private String id; // MongoDB's unique identifier (_id)
    private String createAT;
    private String updateAT;
    private boolean isActive;

    @Indexed(unique = true)
    private String mobileNum; // Mobile number, must be unique

    private String firstPiN; // PIN for user authentication
//    @Indexed(unique = true)
    private String deviceId;



    // Default no-argument constructor (required for deserialization)
    public User() {
    }

    // Parameterized constructor (optional, for convenience)
    public User(String mobileNum, String firstPiN , String devideId) {

        this.mobileNum = mobileNum;
        this.firstPiN = firstPiN;
        this.deviceId=devideId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMobileNum() {
        return mobileNum;
    }

    public void setMobileNum(String mobileNum) {
        this.mobileNum = mobileNum;
    }

    public String getFirstPiN() {
        return firstPiN;
    }

    public void setFirstPiN(String firstPiN) {
        this.firstPiN = firstPiN;
    }
    public String getDeviceId() {
        return deviceId;
    }

    public String getCreateAT() {
        return createAT;
    }

    public void setCreateAT(String createAT) {
        this.createAT = createAT;
    }

    public String getUpdateAT() {
        return updateAT;
    }

    public void setUpdateAT(String updateAT) {
        this.updateAT = updateAT;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    // Optional: Override toString for debugging purposes

}

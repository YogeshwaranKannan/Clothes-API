package Farme_rich.Seller.Model.BackEnd;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "customerinformation")
public class CustomerInformation {
    @Id
    private ObjectId _id;
    private String customerName;
    private String customerMobileNum;
    private String deliveryAddress;
    private String city;
    private String postalCode;
    private String state;
    private String email;

    public CustomerInformation() {
    }

    public CustomerInformation(String customerName, String customerMobileNum, String deliveryAddress, String city, String postalCode, String state, String email) {
        this.customerName = customerName;
        this.customerMobileNum = customerMobileNum;
        this.deliveryAddress = deliveryAddress;
        this.city = city;
        this.postalCode = postalCode;
        this.state = state;
        this.email = email;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerMobileNum() {
        return customerMobileNum;
    }

    public void setCustomerMobileNum(String customerMobileNum) {
        this.customerMobileNum = customerMobileNum;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}

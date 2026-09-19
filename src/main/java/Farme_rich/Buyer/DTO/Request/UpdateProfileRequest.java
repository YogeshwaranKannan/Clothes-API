package Farme_rich.Buyer.DTO.Request;

import org.bson.types.ObjectId;


public class UpdateProfileRequest {
    public ObjectId buyerid;
    public String firstName;
    public String lastName;
    public String email;
    public String phone;
    public String city;
    public String state;
    public String streetAddress;
    public String existStreetAddress;
    public String zipCode;
}

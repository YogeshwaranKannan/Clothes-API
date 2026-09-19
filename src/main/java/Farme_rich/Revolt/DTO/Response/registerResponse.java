package Farme_rich.Revolt.DTO.Response;

import org.bson.types.ObjectId;

public class registerResponse {
    public ObjectId id;
    public String token;
    public String firstName;
    public String lastName;
    public String mobileNum;
    public String deviceId;
    public String pin;
    public boolean status;
    public MessageResponse ResponseMessage;
}

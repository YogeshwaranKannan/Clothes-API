package Farme_rich.Buyer.DTO.Request;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SignupRequest {
    public String firstName;
    public String lastName;
    public String email;
    public String mobileNum;
    public String password;
    public ObjectId sellerid;
    public String companyname;
}

package Farme_rich.Buyer.DTO.Request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GetOTPRequest {
    public String otp;
    public String email;
    public ObjectId sellerid;
    public String mobileNum;
    public String password;
    public int attempt;
}

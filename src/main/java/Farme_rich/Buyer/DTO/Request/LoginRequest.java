package Farme_rich.Buyer.DTO.Request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginRequest {
    public String identifier;
    public ObjectId sellerid;
    public String password;
}

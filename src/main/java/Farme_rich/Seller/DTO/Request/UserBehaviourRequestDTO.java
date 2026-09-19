package Farme_rich.Seller.DTO.Request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserBehaviourRequestDTO {
    public ObjectId companyid;
    public ObjectId userid;
    public String input_text;
    public String selected_text;
}

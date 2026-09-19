package Farme_rich.Buyer.DTO.Request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ViewOrderDetailsRequest {
    public String refno;
    public ObjectId companyid;

}

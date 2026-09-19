package Farme_rich.Seller.DTO.Request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QICustomerRequestDTO {
    public ObjectId _id;
    public String mobile_num;
    public String name;
    public String address;


}

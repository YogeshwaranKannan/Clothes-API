package Farme_rich.Seller.DTO.Request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;


import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchOrdersRequest {

    public String _id;
    public String value;
    public List<String> order_status;

}

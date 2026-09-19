package Farme_rich.Seller.DTO.Response;

import Farme_rich.Seller.DTO.Request.PropertyAttributesDTO;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

public class InitialServiceInventoryServiceInventoryResponse {
    public ObjectId id;
    public  ObjectId sellerid;//user_id

    public String servicename;  //Mandatory

    public String servicecost; //Mandatory

    public List<PropertyAttributesDTO> propertyAttributesDTO;
    public String created_at;
    public boolean isActive;
}

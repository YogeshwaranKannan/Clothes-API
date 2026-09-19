package Farme_rich.Seller.DTO.Request;


import Farme_rich.Seller.DTO.Response.ProductCategoryDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)

public class ServiceInventoryRequest {
    public ObjectId id;
    public ObjectId sellerid;//user_id

    public String servicename;  //Mandatory

    public String servicecost; //Mandatory
    public String serviceDesc;
    public String serviceNotes;
    public boolean isActive;
    public ProductCategoryDTO category;
    public List<PropertyAttributesDTO> propertyAttributesDTO;
    public List<ObjectId> addOnIds;
    public String msg;
    public boolean isDelete;
}

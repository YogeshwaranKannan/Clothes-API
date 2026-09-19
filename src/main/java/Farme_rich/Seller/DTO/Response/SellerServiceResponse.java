package Farme_rich.Seller.DTO.Response;


import Farme_rich.Seller.DTO.Request.PropertyAttributesDTO;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class SellerServiceResponse {
    public String servicename;
    public String servicecost;
    public List<PropertyAttributesDTO> propertyAttributes;
    public boolean isActive;
    public String created_at;
    public String defaultImage;
    public ObjectId id;
    public ObjectId sellerid;
}

package Farme_rich.Seller.Model.BackEnd;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Document(collection = "ServiceInventory")
public class ServiceInventory {
    @Id
    public ObjectId id;

    public ObjectId sellerid; //user_id

    public String servicename;  //Mandatory
    public String serviceDesc;  //Mandatory
    public String serviceNotes;  //Mandatory

    public String defaultImage;

    public String servicecost; //Mandatory

    public ProductCategory category;

    public List<PropertyAttributes> propertyAttributes;
    public boolean isActive;
    public String created_at;
    public List<ObjectId> addOnIds;
}

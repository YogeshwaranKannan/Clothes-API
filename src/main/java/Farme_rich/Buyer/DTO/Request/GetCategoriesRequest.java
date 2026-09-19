package Farme_rich.Buyer.DTO.Request;

import org.bson.types.ObjectId;

import java.util.List;

public class GetCategoriesRequest {
    public ObjectId sellerId;
    public List<String> categories_ID;
}

package Farme_rich.Seller.DTO.Response;

import org.bson.types.ObjectId;

import javax.xml.crypto.Data;
import java.util.Date;

public class GetPromoMessageResponse {

    public ObjectId _id;
    public ObjectId sellerId;
    public String promo_Msg;
    public boolean isActive;
    public String created_By;
    public String updated_By;
    public Date created_At;
    public Date updated_At;

}

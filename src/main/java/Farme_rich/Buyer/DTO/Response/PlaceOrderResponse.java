package Farme_rich.Buyer.DTO.Response;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class PlaceOrderResponse {
    public String refno;
    public ObjectId saveid;
    public String razorStatus;
    public String razorPayid;
    public String customerName;
    public String customerMobile;
    public int amount;
    public String currency;
    public String email;
    public String reason;
    public String successCode;
    public String errorCode;
    public String message;
    public List<String> errorMessages = new ArrayList<>();
}

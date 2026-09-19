package Farme_rich.Seller.DTO.Request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RejectReviewOrdersRequest {
    public ObjectId _id;
    public String reason;
    public double refund_amount;
    public String refund_status;
    public String refund_payment_mode;

}

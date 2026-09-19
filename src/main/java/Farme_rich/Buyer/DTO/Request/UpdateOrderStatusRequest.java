package Farme_rich.Buyer.DTO.Request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateOrderStatusRequest {
    public ObjectId id;
    public RazorPaymentDetailsDTO data;
    public boolean isError;
    public String error_code;
    public String error_description;
    public String error_reason;
    public String order_id;
    public int amount;
    public String payment_id;


    public UpdateOrderStatusRequest() {
    }

    public UpdateOrderStatusRequest(ObjectId id, RazorPaymentDetailsDTO data, boolean isError, String error_code, String error_description, String error_reason, String order_id, int amount, String payment_id) {
        this.id = id;
        this.data = data;
        this.isError = isError;
        this.error_code = error_code;
        this.error_description = error_description;
        this.error_reason = error_reason;
        this.order_id = order_id;
        this.amount = amount;
        this.payment_id = payment_id;
    }


}

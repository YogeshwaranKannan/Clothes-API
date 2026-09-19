package Farme_rich.Buyer.DTO.Request;

import Farme_rich.Seller.Model.BackEnd.AcquirerData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RazorPaymentDetailsDTO {
        public String id;
        public String paymentId;
        public String receipt;
        public int amount;
        public int amount_paid;
        public int amount_due;
        public int created_at;
        public String currency;
        public String status;
        public int attempts;
        public String entity;

        public AcquirerData acquirer_data;
        public int amount_refunded;
        public String bank;
        public String contact;
        public String description;
        public String email;

        public String error_code;
        public String error_description;
        public String error_reason;

        public int fee;
        public boolean international;
        public ObjectId invoice_id;
        public String method;
        public String order_id;
        public String refund_status;
        public String captured;
        public int tax;

}

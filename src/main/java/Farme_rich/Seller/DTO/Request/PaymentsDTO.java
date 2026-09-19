package Farme_rich.Seller.DTO.Request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentsDTO {

    public double total_amount;
    public double paid_amount;
    public double payment_amount;
    public String payment_mode;
    public double current_amount;
    public double discount;
    public int current_installments;
    public boolean isEMI;
    public boolean isAdvance;
    public double advanceAmt;
    public double amountDue;
    public double outsanding_amount;
    public String payment_date;
    public int no_of_installments;
    public String paymentMode;
    public String next_payment_due;
}

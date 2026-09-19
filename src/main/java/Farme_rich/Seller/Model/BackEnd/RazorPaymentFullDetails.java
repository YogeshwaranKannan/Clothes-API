package Farme_rich.Seller.Model.BackEnd;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "RazorPaymentFullDetails")
public class RazorPaymentFullDetails {
    @Id
    private ObjectId _id;
    private String order_id; // save._ID
    private List<RazorPaymentDetails> razorPaymentDetails = new ArrayList<RazorPaymentDetails>();

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public List<RazorPaymentDetails> getRazorPaymentDetails() {
        return razorPaymentDetails;
    }

    public void setRazorPaymentDetails(List<RazorPaymentDetails> razorPaymentDetails) {
        this.razorPaymentDetails = razorPaymentDetails;
    }
}

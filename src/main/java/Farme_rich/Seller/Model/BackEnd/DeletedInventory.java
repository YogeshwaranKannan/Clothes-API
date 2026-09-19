package Farme_rich.Seller.Model.BackEnd;

import org.bson.types.ObjectId;

public class DeletedInventory {
    private ObjectId _id;
    private String product_Name;
    private String reason_deleted;
    private double total_amount;
    private int ordered;

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getProduct_Name() {
        return product_Name;
    }

    public void setProduct_Name(String product_Name) {
        this.product_Name = product_Name;
    }

    public String getReason_deleted() {
        return reason_deleted;
    }

    public void setReason_deleted(String reason_deleted) {
        this.reason_deleted = reason_deleted;
    }

    public double getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(double total_amount) {
        this.total_amount = total_amount;
    }

    public int getOrdered() {
        return ordered;
    }

    public void setOrdered(int ordered) {
        this.ordered = ordered;
    }
}

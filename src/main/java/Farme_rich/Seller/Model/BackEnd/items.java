package Farme_rich.Seller.Model.BackEnd;

import Farme_rich.Seller.DTO.Response.BatchesDTO;
import org.bson.types.ObjectId;

public class items {
    private String _id;
    private String companyid;
    private ObjectId productid;

    private String discount_percentage;
    private String created_at;
    private String updated_at;
    private BatchesDTO batchid;
    private int version;
    private int selling_value;
    private int Order_quantity;


    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public ObjectId getProductid() {
        return productid;
    }

    public void setProductid(ObjectId productid) {
        this.productid = productid;
    }

    public String getCompanyid() {
        return companyid;
    }

    public void setCompanyid(String companyid) {
        this.companyid = companyid;
    }


    public String getDiscount_percentage() {
        return discount_percentage;
    }

    public void setDiscount_percentage(String discount_percentage) {
        this.discount_percentage = discount_percentage;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public BatchesDTO getBatchid() {
        return batchid;
    }

    public void setBatchid(BatchesDTO batchid) {
        this.batchid = batchid;
    }

    public int getSelling_value() {
        return selling_value;
    }

    public void setSelling_value(int selling_value) {
        this.selling_value = selling_value;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getOrder_quantity() {
        return Order_quantity;
    }

    public void setOrder_quantity(int order_quantity) {
        Order_quantity = order_quantity;
    }
}

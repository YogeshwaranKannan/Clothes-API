package Farme_rich.Seller.DTO.Response;

import Farme_rich.Seller.DTO.Request.PropertyAttributesDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InventoryDTO {
    public String _id;
    public String uniqueID;
    public String companyid;
    public String flag;
    public ObjectId productid;
    public String defaultImage;
    public String productname;
    public String variantName;
    public boolean deals;
    public boolean newArrivals;

    public ObjectId categoryId;
    public String brand;
    public boolean quickadd;
    public String unit;
    public String expiry;
    public int minimum_order;
    public String productDescription;

    public int CGST;
    public int SGST;
    public String gst;
    public String discount_percentage;
    public String created_at;
    public String updated_at;
    public String batch_StringID;
    public String batch_id;
    public double offerPrice;
    public BatchesDTO batchid;
    public List<BatchesDTO> variantBatches;
    public Map<String, String> propertyAttributes;
    public List<PropertyAttributesDTO> available_properties;
    public String servicename;
    public double servicecost;
    public int version;
    public double sellerprice;
    public double procurement_price;
    public int Order_quantity;
    public double sellerstock;
    public int remaining_quantity;
    public String message;
    public String quantityChangeReason;


    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getCompanyid() {
        return companyid;
    }

    public void setCompanyid(String companyid) {
        this.companyid = companyid;
    }

    public ObjectId getProductid() {
        return productid;
    }

    public void setProductid(ObjectId productid) {
        this.productid = productid;
    }

    public int getCGST() {
        return CGST;
    }

    public void setCGST(int CGST) {
        this.CGST = CGST;
    }

    public Map<String, String> getPropertyAttributes() {
        return propertyAttributes;
    }

    public void setPropertyAttributes(Map<String, String> propertyAttributes) {
        this.propertyAttributes = propertyAttributes;
    }

    public int getSGST() {
        return SGST;
    }

    public void setSGST(int SGST) {
        this.SGST = SGST;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public String getServicename() {
        return servicename;
    }

    public void setServicename(String servicename) {
        this.servicename = servicename;
    }

    public int getRemaining_quantity() {
        return remaining_quantity;
    }

    public void setRemaining_quantity(int remaining_quantity) {
        this.remaining_quantity = remaining_quantity;
    }

    public double getSellerprice() {
        return sellerprice;
    }

    public void setSellerprice(double sellerprice) {
        this.sellerprice = sellerprice;
    }
}

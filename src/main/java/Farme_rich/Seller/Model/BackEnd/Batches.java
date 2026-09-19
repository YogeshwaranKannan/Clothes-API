package Farme_rich.Seller.Model.BackEnd;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "batches")
public class Batches {
    @Id
    private String _id;
    private String Batch_Id;
    private int stock_availability;
    private double procurement_price;
    private double margin_percentage;
    private double seller_price;
    private boolean active;
    private String to_be_deleted;
    private String maufactured_date;
    private String expiry_date;
    private int minimum_order;
    private String shelf_life;

    @Field("created_at")
    private String createdAt;

    @Field("updated_at")
    private String updatedAt;
    private ObjectId productid;
    private String companyid;
    private String productname;
    private String variantName;
    private double offerPrice;
    private double discount;
    private String unit;
    private String batch_unit;
    private String batch_expiryDate;
    @Version
    private Integer version;


    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getBatch_Id() {
        return Batch_Id;
    }

    public void setBatch_Id(String batch_Id) {
        Batch_Id = batch_Id;
    }

    public double getProcurement_price() {
        return procurement_price;
    }

    public void setProcurement_price(double procurement_price) {
        this.procurement_price = procurement_price;
    }

    public double getMargin_percentage() {
        return margin_percentage;
    }

    public void setMargin_percentage(double margin_percentage) {
        this.margin_percentage = margin_percentage;
    }

    public double getSeller_price() {
        return seller_price;
    }

    public void setSeller_price(double seller_price) {
        this.seller_price = seller_price;
    }

    public boolean getActive() {
        return active;
    }

    public String getTo_be_deleted() {
        return to_be_deleted;
    }

    public void setTo_be_deleted(String to_be_deleted) {
        this.to_be_deleted = to_be_deleted;
    }

    public String getMaufactured_date() {
        return maufactured_date;
    }

    public void setMaufactured_date(String maufactured_date) {
        this.maufactured_date = maufactured_date;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
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

    public String getShelf_life() {
        return shelf_life;
    }

    public void setShelf_life(String shelf_life) {
        this.shelf_life = shelf_life;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getBatch_unit() {
        return batch_unit;
    }

    public void setBatch_unit(String batch_unit) {
        this.batch_unit = batch_unit;
    }

    public String getBatch_expiryDate() {
        return batch_expiryDate;
    }

    public void setBatch_expiryDate(String batch_expiryDate) {
        this.batch_expiryDate = batch_expiryDate;
    }

    public int getStock_availability() {
        return stock_availability;
    }

    public void setStock_availability(int stock_availability) {
        this.stock_availability = stock_availability;
    }

    public String getExpiry_date() {
        return expiry_date;
    }

    public void setExpiry_date(String expiry_date) {
        this.expiry_date = expiry_date;
    }

    public int getMinimum_order() {
        return minimum_order;
    }

    public void setMinimum_order(int minimum_order) {
        this.minimum_order = minimum_order;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getProductname() {
        return productname;
    }

    public void setProductname(String productname) {
        this.productname = productname;
    }

    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }

    public double getOfferPrice() {
        return offerPrice;
    }

    public void setOfferPrice(double offerPrice) {
        this.offerPrice = offerPrice;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }
}

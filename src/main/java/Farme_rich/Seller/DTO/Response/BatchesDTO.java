package Farme_rich.Seller.DTO.Response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BatchesDTO {
    public String _id;
    public int stock_availability;
    public double procurement_price;
    public double margin_percentage;
    public double seller_price;
    public boolean active;
    public String to_be_deleted;
    public String maufactured_date;
    public String expiry_date;
    public String shelf_life;
    public String created_at;
    public String updated_at;
    public String productid;
    public String companyid;
    public String productname;
    public String variantName;
    public double offerPrice;
    public double discount;
    public String unit;
    public String batch_unit;
    public String batch_expiryDate;
    public int minimum_order;
    public int version;


    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public int getStock_availability() {
        return stock_availability;
    }

    public void setStock_availability(int stock_availability) {
        this.stock_availability = stock_availability;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public String getExpiry_date() {
        return expiry_date;
    }

    public void setExpiry_date(String expiry_date) {
        this.expiry_date = expiry_date;
    }

    public String getShelf_life() {
        return shelf_life;
    }

    public void setShelf_life(String shelf_life) {
        this.shelf_life = shelf_life;
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

    public String getProductid() {
        return productid;
    }

    public void setProductid(String productid) {
        this.productid = productid;
    }

    public String getCompanyid() {
        return companyid;
    }

    public void setCompanyid(String companyid) {
        this.companyid = companyid;
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

    public int getMinimum_order() {
        return minimum_order;
    }

    public void setMinimum_order(int minimum_order) {
        this.minimum_order = minimum_order;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getProductname() {
        return productname;
    }

    public void setProductname(String productname) {
        this.productname = productname;
    }
}

package Farme_rich.Seller.DTO.Request;

public class SellerUpdateRequest {
    public String _id;
    public String gst;
    public int quantity;
    public int minimumorder;
    public double procumentprice;
    public String unit;

    public double seller_price;
    public double margin;
    public String expiry;
    public String batch_unit;
    public String batch_expiryDate;
    public String status;
    public String variantName;
    public double offerPrice;
    public double discount;
    public boolean soldout;
    public int batch_version;
    public int inventory_version;
    public String productid;
    public String companyid;


    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getGst() {
        return gst;
    }

    public void setGst(String gst) {
        this.gst = gst;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBatch_unit() {
        return batch_unit;
    }

    public void setBatch_unit(String batch_unit) {
        this.batch_unit = batch_unit;
    }

    public String getExpiry() {
        return expiry;
    }


    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }


    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }


    public int getMinimumorder() {
        return minimumorder;
    }

    public void setMinimumorder(int minimumorder) {
        this.minimumorder = minimumorder;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getBatch_expiryDate() {
        return batch_expiryDate;
    }

    public void setBatch_expiryDate(String batch_expiryDate) {
        this.batch_expiryDate = batch_expiryDate;
    }

    public boolean getSoldout() {
        return soldout;
    }

    public boolean isSoldout() {
        return soldout;
    }

    public void setSoldout(boolean soldout) {
        this.soldout = soldout;
    }

    public int getBatch_version() {
        return batch_version;
    }

    public void setBatch_version(int batch_version) {
        this.batch_version = batch_version;
    }

    public int getInventory_version() {
        return inventory_version;
    }

    public void setInventory_version(int inventory_version) {
        this.inventory_version = inventory_version;
    }

    public String getCompanyid() {
        return companyid;
    }

    public void setCompanyid(String companyid) {
        this.companyid = companyid;
    }

    public String getProductid() {
        return productid;
    }

    public void setProductid(String productid) {
        this.productid = productid;
    }

    public double getProcumentprice() {
        return procumentprice;
    }

    public void setProcumentprice(double procumentprice) {
        this.procumentprice = procumentprice;
    }

    public double getSeller_price() {
        return seller_price;
    }

    public void setSeller_price(double seller_price) {
        this.seller_price = seller_price;
    }

    public double getMargin() {
        return margin;
    }

    public void setMargin(double margin) {
        this.margin = margin;
    }
}



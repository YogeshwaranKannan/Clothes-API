package Farme_rich.Seller.DTO.Request;


import Farme_rich.Seller.DTO.Response.ProductCategoryDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SellerBatchesRequest {

    //batches datas
    public ObjectId categoryId;
    public ProductCategoryDTO productCategoryDTO;
    public boolean deals;
    public boolean newArrivals;
    public double seller_price;
    public ProductCategoryDTO productCategory;
    public double offerPrice;
    public String variantName;
    public double discount;
    //inventory datas
    private String gst;
    private int stock_availability;
    private double procurement_price;
    private String productname;
    private String productid;
    private String companyid;
    private double margin;
    private String unit;
    private int minimumorder;
    private String expiry;
    private String batch_unit;
    private String batch_expiryDate;

    public SellerBatchesRequest() {
        // Default constructor is required for deserialization
    }

    public SellerBatchesRequest(int stock_availability,
                                double procurement_price,
                                double seller_price,
                                String productid, String companyid,
                                double margin, String unit, int minimumorder,
                                String expiry, String batch_unit,
                                String batch_expiryDate, String gst, String productname, ObjectId categoryId) {
        this.gst = gst;
        this.stock_availability = stock_availability;
        this.procurement_price = procurement_price;
        this.seller_price = seller_price;
        this.productid = productid;
        this.companyid = companyid;
        this.margin = margin;
        this.unit = unit;
        this.minimumorder = minimumorder;
        this.expiry = expiry;
        this.batch_unit = batch_unit;
        this.batch_expiryDate = batch_expiryDate;
        this.productname = productname;
        this.categoryId = categoryId;
    }

    public String getGst() {
        return gst;
    }

    public void setGst(String gst) {
        this.gst = gst;
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

    public String getProductname() {
        return productname;
    }

    public void setProductname(String productname) {
        this.productname = productname;
    }

    public double getProcurement_price() {
        return procurement_price;
    }

    public void setProcurement_price(double procurement_price) {
        this.procurement_price = procurement_price;
    }

    public double getMargin() {
        return margin;
    }

    public void setMargin(double margin) {
        this.margin = margin;
    }

    public double getSeller_price() {
        return seller_price;
    }

    public void setSeller_price(double seller_price) {
        this.seller_price = seller_price;
    }

    public int getStock_availability() {
        return stock_availability;
    }

    public void setStock_availability(int stock_availability) {
        this.stock_availability = stock_availability;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public int getMinimumorder() {
        return minimumorder;
    }

    public void setMinimumorder(int minimumorder) {
        this.minimumorder = minimumorder;
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
}

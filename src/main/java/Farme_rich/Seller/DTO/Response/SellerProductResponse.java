package Farme_rich.Seller.DTO.Response;


import org.bson.types.ObjectId;

public class SellerProductResponse {
    public String productName;
    public ObjectId productid;
    public String productDescription;
    public ProductCategoryDTO productCategory;
    public InventoryDTO inventoryData;

    public String unit;
    public String product_type;
    public boolean Quickadd;
    public String imgurl;
    public String brand;
    public double procumentPrice;
    public double sellingPrice;
    public double margin;
    public double offerPrice;
    public double discount;
    public String storeid;
    public String batch_unit;
    public String batch_expiryDate;

    public MessageResponse ResponseMessage;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public ObjectId getProductid() {
        return productid;
    }

    public void setProductid(ObjectId productid) {
        this.productid = productid;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public ProductCategoryDTO getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(ProductCategoryDTO productCategory) {
        this.productCategory = productCategory;
    }

    public InventoryDTO getInventoryData() {
        return inventoryData;
    }

    public void setInventoryData(InventoryDTO inventoryData) {
        this.inventoryData = inventoryData;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getProduct_type() {
        return product_type;
    }

    public void setProduct_type(String product_type) {
        this.product_type = product_type;
    }


    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }


    public MessageResponse getResponseMessage() {
        return ResponseMessage;
    }

    public void setResponseMessage(MessageResponse responseMessage) {
        ResponseMessage = responseMessage;
    }

    public String getStoreid() {
        return storeid;
    }

    public void setStoreid(String storeid) {
        this.storeid = storeid;
    }

    public boolean isQuickadd() {
        return Quickadd;
    }

    public void setQuickadd(boolean quickadd) {
        Quickadd = quickadd;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getBatch_expiryDate() {
        return batch_expiryDate;
    }

    public void setBatch_expiryDate(String batch_expiryDate) {
        this.batch_expiryDate = batch_expiryDate;
    }

    public String getBatch_unit() {
        return batch_unit;
    }

    public void setBatch_unit(String batch_unit) {
        this.batch_unit = batch_unit;
    }
}

package Farme_rich.Seller.Model.BackEnd;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "products")
public class Products {
    @Id
    @JsonProperty("_id")
    private ObjectId _id;
    private String productName;
    private String productDescription;
//    @DBRef
    private ProductCategory productCategory;
    private boolean Quickadd;
    private String unit;
    private String product_type;
    private String brand;
    private String image_url;
    private String defaultImage;
    private String productfeatureid;

    private List<SellerList> sellerLists;

    private String createdAt;
    private String updatedAt;


    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }



    public String getProductName() {
        return productName;
    }



    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public ProductCategory getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(ProductCategory productCategory) {
        this.productCategory = productCategory;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getDefaultImage() {
        return defaultImage;
    }

    public void setDefaultImage(String defaultImage) {
        this.defaultImage = defaultImage;
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

    public String getProductfeatureid() {
        return productfeatureid;
    }

    public void setProductfeatureid(String productfeatureid) {
        this.productfeatureid = productfeatureid;
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

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isQuickadd() {
        return Quickadd;
    }

    public void setQuickadd(boolean quickadd) {
        Quickadd = quickadd;
    }

    public List<SellerList> getSellerLists() {
        return sellerLists;
    }

    public void setSellerLists(List<SellerList> sellerLists) {
        this.sellerLists = sellerLists;
    }


}


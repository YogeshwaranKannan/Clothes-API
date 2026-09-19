package Farme_rich.Seller.DTO.Request;

import Farme_rich.Seller.DTO.Response.ProductCategoryDTO;
import org.bson.types.ObjectId;

public class ProductsDTO {
    public ObjectId _id;
    public String productName;
    public ObjectId seller_id;
    public ProductCategoryDTO  productCategory ;
    public String defaultImage;
    public double sellingPrice;

    public double offerPrice;
    public String productDescription;
    public boolean Quickadd;
    public String unit;
    public String product_type;
    public String brand;
    public String image_url;
    public boolean isService;
    public String productfeatureid;
    public double procumentPrice;
    public double margin;
    public double discount;
    public String createdAt;
    public String updatedAt;
}

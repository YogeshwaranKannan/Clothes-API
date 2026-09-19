package Farme_rich.Seller.DTO.Request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)


public class InsertProductsRequest {
    public ObjectId _id;
    public ObjectId productId;
    public ObjectId companyId;
    public String companyName;
    public String productName;
    public String productDescription;
    public ObjectId categoryId;
    public boolean quickadd;
    public boolean deals;
    public boolean newArrivals;
    public String unit;
    public String product_type;
    public String brand;
    public List<ProductImageDTO> defaultImage;
    public String productfeatureid;
    public List<ProductVariantDTO> variants;
    public int minimumOrder;
    public String expiry;
    public String batchUnit;
    public String batchExpiryDate;
    public String gst;
    public double offerPrice;
    public double discount;
    public String createdAt;
    public String updatedAt;

}

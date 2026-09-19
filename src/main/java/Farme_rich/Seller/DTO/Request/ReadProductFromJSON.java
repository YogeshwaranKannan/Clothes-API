package Farme_rich.Seller.DTO.Request;

import Farme_rich.ObjectIdDeserializer;
import Farme_rich.Seller.DTO.Response.ProductCategoryDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.bson.types.ObjectId;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReadProductFromJSON {

    public String _id;

    public String inventory_id;
    public String productId;

    @JsonDeserialize(using = ObjectIdDeserializer.class)
    public ObjectId seller_id;

    public String productName;

    @JsonProperty("productCategory")   // was "prodcutCategory" (typo) — now matches the JSON key
    public ProductCategoryDTO productCategory;

    public String defaultImage;

    @JsonProperty("sellerprice")       // JSON key is "sellerprice", field is sellingPrice
    public double sellingPrice;

    @JsonProperty("offerprice")        // JSON key is "offerprice", field is offerPrice
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
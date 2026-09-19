package Farme_rich.Seller.DTO.Request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductVariantDTO {
    public ObjectId _id;

    public double procumentPrice;

    public String variantName;

    public double sellingPrice;

    public double margin;

    public double offerPrice;

    public double discount;

    public int quantity;

}

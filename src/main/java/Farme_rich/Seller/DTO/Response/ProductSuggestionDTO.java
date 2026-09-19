package Farme_rich.Seller.DTO.Response;

public class ProductSuggestionDTO {
    public String id; // inventoryId
    public String productId;
    public String productName;
    public double price; // mrp
    public double offerPrice;

    public ProductSuggestionDTO(String id, String productName, double price) {
        this.id = id;
        this.productName = productName;
        this.price = price;
    }

    public ProductSuggestionDTO(String id, String productId, String productName, double price, double offerPrice) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.offerPrice = offerPrice;
    }


}



package Farme_rich.Seller.DTO.Response;

public class ProductSuggestDTO {
    private String id;
    private String productId;
    private String productName;
    private Double price;
    private Double offerPrice;

    public ProductSuggestDTO(String id, String productId, String productName, Double price, Double offerPrice) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.offerPrice = offerPrice;
    }

    // getters
    public String getId() {
        return id;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Double getPrice() {
        return price;
    }

    public Double getOfferPrice() {
        return offerPrice;
    }
}

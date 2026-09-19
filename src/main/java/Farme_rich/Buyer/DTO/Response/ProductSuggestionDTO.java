package Farme_rich.Buyer.DTO.Response;

public class ProductSuggestionDTO {
    public String ProductId;
    public String inventoryId;
    public String ProductName;
    public boolean isService;
    public String Image;
    public String price;


    public ProductSuggestionDTO(String productId, String inventoryId, String productName, boolean isService, String image, String price) {
        this.ProductId = productId;
        this.inventoryId = inventoryId;
        this.ProductName = productName;
        this.isService = isService;
        this.Image = image;
        this.price = price;
    }
}
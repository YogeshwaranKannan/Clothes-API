package Farme_rich.Seller.DTO.Request;

public class StockProductDTO {
    private String productName;
    private String variantName;      // null if no variant
    private int stockAvailable;
    private String lastUpdatedAt;    // only set when stockAvailable == 0

    public StockProductDTO() {
    }

    public StockProductDTO(String productName, String variantName, int stockAvailable, String lastUpdatedAt) {
        this.productName = productName;
        this.variantName = variantName;
        this.stockAvailable = stockAvailable;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }

    public int getStockAvailable() {
        return stockAvailable;
    }

    public void setStockAvailable(int stockAvailable) {
        this.stockAvailable = stockAvailable;
    }

    public String getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(String lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
}

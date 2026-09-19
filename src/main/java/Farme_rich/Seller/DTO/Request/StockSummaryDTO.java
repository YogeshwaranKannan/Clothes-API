package Farme_rich.Seller.DTO.Request;


public class StockSummaryDTO {
    private int outOfStockCount;   // stock == 0  -> red
    private int lowStockCount;     // 0 < stock < 10 -> yellow
    private int inStockCount;      // stock >= 10 -> green

    public StockSummaryDTO() {
    }

    public StockSummaryDTO(int outOfStockCount, int lowStockCount, int inStockCount) {
        this.outOfStockCount = outOfStockCount;
        this.lowStockCount = lowStockCount;
        this.inStockCount = inStockCount;
    }

    public int getOutOfStockCount() {
        return outOfStockCount;
    }

    public void setOutOfStockCount(int outOfStockCount) {
        this.outOfStockCount = outOfStockCount;
    }

    public int getLowStockCount() {
        return lowStockCount;
    }

    public void setLowStockCount(int lowStockCount) {
        this.lowStockCount = lowStockCount;
    }

    public int getInStockCount() {
        return inStockCount;
    }

    public void setInStockCount(int inStockCount) {
        this.inStockCount = inStockCount;
    }
}

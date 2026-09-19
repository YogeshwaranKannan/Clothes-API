package Farme_rich.Seller.Model.ReportsModel;
 

public class ProfitRow {
    private String productName;
    private double sellingOrOfferPrice; // A
    private double procurementPrice;    // B
    private double profit;              // A - B
    private double marginPercent;       // profit / A * 100

    public ProfitRow() {
    }

    public ProfitRow(String productName, double sellingOrOfferPrice, double procurementPrice) {
        this.productName = productName;
        this.sellingOrOfferPrice = sellingOrOfferPrice;
        this.procurementPrice = procurementPrice;
        this.profit = sellingOrOfferPrice - procurementPrice;
        this.marginPercent = sellingOrOfferPrice == 0 ? 0 : (this.profit / sellingOrOfferPrice) * 100.0;
    }

    public String getProductName() {
        return productName;
    }

    public double getSellingOrOfferPrice() {
        return sellingOrOfferPrice;
    }

    public double getProcurementPrice() {
        return procurementPrice;
    }

    public double getProfit() {
        return profit;
    }

    public double getMarginPercent() {
        return marginPercent;
    }
}

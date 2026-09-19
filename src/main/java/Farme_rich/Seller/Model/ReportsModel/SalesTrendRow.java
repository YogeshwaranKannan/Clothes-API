package Farme_rich.Seller.Model.ReportsModel;

public class SalesTrendRow {

    private String productName;
    private int noOfOrders;
    private double totalValue;

    private double totalQty;

    public SalesTrendRow(String productName, int noOfOrders, double totalValue) {
        this.productName = productName;
        this.noOfOrders = noOfOrders;
        this.totalValue = totalValue;
        this.totalQty = 0;
    }

    public void addOrder(double lineValue) {
        addOrder(0, lineValue);
    }
 
    public void addOrder(int quantity, double lineValue) {
        this.noOfOrders += 1;
        this.totalQty += quantity;
        this.totalValue += lineValue;
    }

    public String getProductName() {
        return productName;
    }

    public int getNoOfOrders() {
        return noOfOrders;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public double getTotalQty() {
        return totalQty;
    }
}
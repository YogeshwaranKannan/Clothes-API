package Farme_rich.Seller.Model.ReportsModel;
 

public class OrderTypeRow {
    private String orderType;
    private int noOfOrders;
    private double totalValue;

    public OrderTypeRow() {
    }

    public OrderTypeRow(String orderType, int noOfOrders, double totalValue) {
        this.orderType = orderType;
        this.noOfOrders = noOfOrders;
        this.totalValue = totalValue;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public int getNoOfOrders() {
        return noOfOrders;
    }

    public void setNoOfOrders(int noOfOrders) {
        this.noOfOrders = noOfOrders;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }

    public void addOrder(double value) {
        this.noOfOrders += 1;
        this.totalValue += value;
    }
}

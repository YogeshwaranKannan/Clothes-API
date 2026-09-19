package Farme_rich.Seller.Model.ReportsModel;


public class OrderSourceRow {
    private String source;
    private int noOfOrders;
    private double totalValue;

    public OrderSourceRow() {
    }

    public OrderSourceRow(String source, int noOfOrders, double totalValue) {
        this.source = source;
        this.noOfOrders = noOfOrders;
        this.totalValue = totalValue;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
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
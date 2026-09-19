package Farme_rich.Seller.Model.ReportsModel;


public class LoyaltyRow {
    private String customerName;
    private String mobileNo;
    private int noOfOrders;
    private double totalValue;

    public LoyaltyRow() {
    }

    public LoyaltyRow(String customerName, String mobileNo, int noOfOrders, double totalValue) {
        this.customerName = customerName;
        this.mobileNo = mobileNo;
        this.noOfOrders = noOfOrders;
        this.totalValue = totalValue;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
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
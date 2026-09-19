package Farme_rich.Seller.Model.ReportsModel;


import java.util.List;

public class ReportData {
    private String companyName;
    private String generatedOn;
    private String fromDate;
    private String toDate;

    private List<SalesTrendRow> salesTrend;
    private List<OrderSourceRow> orderSource;
    private List<OrderTypeRow> orderType;
    private List<LoyaltyRow> loyalty;
    private List<ProfitRow> profitStats;

    // Summary KPIs shown at the top of the report
    private int totalOrders;
    private double totalRevenue;
    private double balanceDue;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getGeneratedOn() {
        return generatedOn;
    }

    public void setGeneratedOn(String generatedOn) {
        this.generatedOn = generatedOn;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public List<SalesTrendRow> getSalesTrend() {
        return salesTrend;
    }

    public void setSalesTrend(List<SalesTrendRow> salesTrend) {
        this.salesTrend = salesTrend;
    }

    public List<OrderSourceRow> getOrderSource() {
        return orderSource;
    }

    public void setOrderSource(List<OrderSourceRow> orderSource) {
        this.orderSource = orderSource;
    }

    public List<OrderTypeRow> getOrderType() {
        return orderType;
    }

    public void setOrderType(List<OrderTypeRow> orderType) {
        this.orderType = orderType;
    }

    public List<LoyaltyRow> getLoyalty() {
        return loyalty;
    }

    public void setLoyalty(List<LoyaltyRow> loyalty) {
        this.loyalty = loyalty;
    }

    public List<ProfitRow> getProfitStats() {
        return profitStats;
    }

    public void setProfitStats(List<ProfitRow> profitStats) {
        this.profitStats = profitStats;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getBalanceDue() {
        return balanceDue;
    }

    public void setBalanceDue(double balanceDue) {
        this.balanceDue = balanceDue;
    }
}
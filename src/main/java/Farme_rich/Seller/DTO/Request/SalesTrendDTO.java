package Farme_rich.Seller.DTO.Request;

/**
 * Response item for GET /api/dashboard/sales-trend
 * One entry per date for the last 5 days.
 */
public class SalesTrendDTO {

    private String date;       // x-axis
    private double orderValue; // y-axis - sum of total_Amount for Completed orders on that date

    public SalesTrendDTO() {
    }

    public SalesTrendDTO(String date, double orderValue) {
        this.date = date;
        this.orderValue = orderValue;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getOrderValue() {
        return orderValue;
    }

    public void setOrderValue(double orderValue) {
        this.orderValue = orderValue;
    }
}

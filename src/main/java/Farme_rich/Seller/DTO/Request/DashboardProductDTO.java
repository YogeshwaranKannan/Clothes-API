package Farme_rich.Seller.DTO.Request;

import java.util.List;

/**
 * Response payload for GET /api/dashboard/products
 * <p>
 * dates        -> x-axis labels, e.g. ["2026-08-17","2026-08-18","2026-08-19"]
 * topProducts  -> the 3 best-selling products overall across the date range
 * (these become the fixed bar-series / legend, same as
 * Prod1..Prod4 in the sample sheet)
 * data         -> one entry per date, each holding the value for every
 * top product on that date (0 if the product had no sales
 * that day)
 */
public class DashboardProductDTO {

    private List<String> dates;
    private List<String> topProducts;
    private List<ProductDayData> data;
    public DashboardProductDTO() {
    }
    public DashboardProductDTO(List<String> dates, List<String> topProducts, List<ProductDayData> data) {
        this.dates = dates;
        this.topProducts = topProducts;
        this.data = data;
    }

    public List<String> getDates() {
        return dates;
    }

    public void setDates(List<String> dates) {
        this.dates = dates;
    }

    public List<String> getTopProducts() {
        return topProducts;
    }

    public void setTopProducts(List<String> topProducts) {
        this.topProducts = topProducts;
    }

    public List<ProductDayData> getData() {
        return data;
    }

    public void setData(List<ProductDayData> data) {
        this.data = data;
    }

    public static class ProductValue {
        private String productName;
        private double value; // Order_quantity * sellerprice

        public ProductValue() {
        }

        public ProductValue(String productName, double value) {
            this.productName = productName;
            this.value = value;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }
    }

    public static class ProductDayData {
        private String date;
        private List<ProductValue> values;

        public ProductDayData() {
        }

        public ProductDayData(String date, List<ProductValue> values) {
            this.date = date;
            this.values = values;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public List<ProductValue> getValues() {
            return values;
        }

        public void setValues(List<ProductValue> values) {
            this.values = values;
        }
    }
}

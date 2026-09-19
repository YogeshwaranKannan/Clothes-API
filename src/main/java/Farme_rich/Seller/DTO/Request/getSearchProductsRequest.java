package Farme_rich.Seller.DTO.Request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class getSearchProductsRequest {

    public String companyId;
    public String searchValue;
    public Sort sort;
    public Filters filters;
    public List<String> categories;
    public Integer page;
    public Integer pageSize;

    public String stockSummary;

    public int seeInv;
    public int seeServ;
    public int seeProd;

    public int invTaken;
    public int servTaken;
    public int prodTaken;

    // ---------- Inner Classes ----------

    public static class Sort {
        private String field;
        private String order; // ASC / DESC

        // getters & setters
        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getOrder() {
            return order;
        }

        public void setOrder(String order) {
            this.order = order;
        }
    }

//    public static class

    public static class Filters {
        private PriceRange priceRange;
        private Map<String, Boolean> offerPercentage;
        private List<String> brand;

        // getters & setters
        public PriceRange getPriceRange() {
            return priceRange;
        }

        public void setPriceRange(PriceRange priceRange) {
            this.priceRange = priceRange;
        }

        public Map<String, Boolean> getOfferPercentage() {
            return offerPercentage;
        }

        public void setOfferPercentage(Map<String, Boolean> offerPercentage) {
            this.offerPercentage = offerPercentage;
        }

        public List<String> getBrand() {
            return brand;
        }

        public void setBrand(List<String> brand) {
            this.brand = brand;
        }
    }

    public static class PriceRange {
        private Double min;
        private Double max;

        // getters & setters
        public Double getMin() {
            return min;
        }

        public void setMin(Double min) {
            this.min = min;
        }

        public Double getMax() {
            return max;
        }

        public void setMax(Double max) {
            this.max = max;
        }
    }


    // ---------- Getters & Setters ----------


}


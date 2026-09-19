package Farme_rich.Seller.DTO.Request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SellerProductRequest {
    public String searchvalue;
    public List<String> categories;
    public String sellerType;
    public String companynameid;
    public boolean isQI;

    public String getSearchvalue() {
        return searchvalue;
    }

    public void setSearchvalue(String searchvalue) {
        this.searchvalue = searchvalue;
    }

    public String getCompanynameid() {
        return companynameid;
    }

    public void setCompanynameid(String companynameid) {
        this.companynameid = companynameid;
    }



}

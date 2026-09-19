package Farme_rich.Seller.DTO.Request;

public class SellerGetBatchesRequest {
    public String companyid;
    public String productid;
    public String variantName;

    public String getCompanyid() {
        return companyid;
    }

    public void setCompanyid(String companyid) {
        this.companyid = companyid;
    }

    public String getProductid() {
        return productid;
    }

    public void setProductid(String productid) {
        this.productid = productid;
    }
}

package Farme_rich.Seller.Model.BackEnd;

import org.bson.types.ObjectId;

public class SellerList {
    private String companyname;
    private ObjectId companyid;

    public String getCompanyname() {
        return companyname;
    }

    public void setCompanyname(String companyname) {
        this.companyname = companyname;
    }

    public ObjectId getCompanyid() {
        return companyid;
    }

    public void setCompanyid(ObjectId companyid) {
        this.companyid = companyid;
    }
}

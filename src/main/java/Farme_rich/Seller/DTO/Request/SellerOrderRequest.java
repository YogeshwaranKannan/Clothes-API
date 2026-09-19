package Farme_rich.Seller.DTO.Request;

import Farme_rich.Seller.DTO.Response.InventoryDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)

public class SellerOrderRequest {
    public ObjectId companyid;
    public String userFolder;
    public String refno;
    public String save_id;
    public List<InventoryDTO> inventory;
    public CustomerInformationDTO customerInformationDTO;
    public String delivery_details;
    public String delivery_mode;
    public String isgst;
    public String ispriceInclusive;


    public ObjectId getCompanyid() {
        return companyid;
    }

    public void setCompanyid(ObjectId companyid) {
        this.companyid = companyid;
    }

    public String getRefno() {
        return refno;
    }

    public void setRefno(String refno) {
        this.refno = refno;
    }

    public List<InventoryDTO> getInventory() {
        return inventory;
    }

    public void setInventory(List<InventoryDTO> inventory) {
        this.inventory = inventory;
    }


}

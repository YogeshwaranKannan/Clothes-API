package Farme_rich.Seller.DTO.Response;

import Farme_rich.Seller.DTO.Request.TopSellingProductDTO;
import Farme_rich.Seller.DTO.Request.TopSellingServiceDTO;
import Farme_rich.Seller.Model.BackEnd.Save;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class SaveOrderResponse {
    public String refno;
    public List<TopSellingServiceDTO> Serviceinventory;
    public List<TopSellingProductDTO> Productinventory;
    public ObjectId save_id;
    public String isgst;
    public String ispriceInclusive;
    public String order_date;
    public List<String> messages = new ArrayList<>();
    public Save save;
    private ObjectId companyid;
    ;
    private List<InventoryDTO> inventory;

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

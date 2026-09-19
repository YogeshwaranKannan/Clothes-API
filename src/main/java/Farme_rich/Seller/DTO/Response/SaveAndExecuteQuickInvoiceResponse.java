package Farme_rich.Seller.DTO.Response;

import Farme_rich.Seller.DTO.Request.TopSellingProductDTO;
import Farme_rich.Seller.DTO.Request.TopSellingServiceDTO;
import org.bson.types.ObjectId;

import java.util.List;

public class SaveAndExecuteQuickInvoiceResponse {

    private ObjectId companyid;
    public String refno;
    public List<InventoryDTO> inventory;
//    public List<TopSellingServiceDTO> Serviceinventory;
//    public List<TopSellingProductDTO> Productinventory;

    public ObjectId save_id;
    public String isgst;
    public String ispriceInclusive;
    public String order_date;
    public List<String> messages;
}

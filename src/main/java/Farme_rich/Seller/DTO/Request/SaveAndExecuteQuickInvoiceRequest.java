package Farme_rich.Seller.DTO.Request;

import Farme_rich.Seller.DTO.Response.InventoryDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SaveAndExecuteQuickInvoiceRequest {
    public ObjectId companyid;
    public String refno;
    public ExecuteOrderRequest.Rewards rewards;
    public List<InventoryDTO> inventory;
    public String isgst;
    public String ispriceInclusive;


    public String payment_mode;
    public PaymentsDTO paymentsDTO;
    public CustomerInformationDTO customerInformationDTO;
    public String address;

    public boolean draft_active;


    public static class Rewards {
        public int RewardsEarned;
        public int rewardsRedeemed;

        public Rewards() {
        }
    }


}

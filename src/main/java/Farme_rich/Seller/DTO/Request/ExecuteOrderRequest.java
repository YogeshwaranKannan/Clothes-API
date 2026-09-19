package Farme_rich.Seller.DTO.Request;

import Farme_rich.Seller.DTO.Response.InventoryDTO;
import Farme_rich.Seller.Model.BackEnd.DeletedInventory;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecuteOrderRequest {

    public String companyName;
    public String refno;
    public Rewards rewards;
    public String save_id;
    public String payment_mode;
    public String deliveryDueDate;
    public boolean isDelivered;
    public boolean isMarkAsPaid;
    public PaymentsDTO paymentsDTO;
    public double ordervalue;
    public CustomerInformationDTO customerInformationDTO;
    public String address;
    public String delivery_mode;
    public ObjectId companyid;
    public List<DeletedInventory> deletedInventories;
    public List<InventoryDTO> inventory;
    public double need_to_pay;
    public double refund_amount;
    public boolean draft_active;

    public static class Rewards {
        public int RewardsEarned;
        public int rewardsRedeemed;

        public Rewards() {
        }
    }


}

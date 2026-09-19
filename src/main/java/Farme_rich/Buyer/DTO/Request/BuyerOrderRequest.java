package Farme_rich.Buyer.DTO.Request;

import Farme_rich.Buyer.DTO.Response.GetSellerDetailsResponse;
import Farme_rich.Seller.DTO.Request.CustomerInformationDTO;
import Farme_rich.Seller.DTO.Request.PaymentsDTO;
import Farme_rich.Seller.DTO.Response.InventoryDTO;
import Farme_rich.Seller.Model.FrontEnd.Seller;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BuyerOrderRequest {
    public ObjectId companyid;
    public String userFolder;
    public String refno;
    public ObjectId buyerid;
    public String orderStatus;
    public String save_id;
    public List<InventoryDTO> inventory;
    public String isgst;
    public String ispriceInclusive;

    public int deliveryFee;
    public int orderThreshold;


    public GetSellerDetailsResponse.RewardsDTO sellerConfigRewards;
    public String payment_mode;
    public String deliveryDueDate;
    public boolean isDelivered;
    public PaymentsDTO paymentsDTO;
    public CustomerInformationDTO customerInformationDTO;
    public Rewards rewards;
    public Seller.Rewards sellerRewards;
    public String address;
    public String delivery_mode;

    public static class Rewards {
        public int RewardsEarned;
        public int rewardsRedeemed;

        public Rewards() {
        }
    }

}

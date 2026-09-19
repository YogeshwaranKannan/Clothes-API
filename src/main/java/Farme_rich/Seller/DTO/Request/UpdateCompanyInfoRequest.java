package Farme_rich.Seller.DTO.Request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateCompanyInfoRequest {
    public String user_id;
    public String companyId;
    //company info
    public String companyname;
    public String companyMobileNum;
    public String userMobileNum;
    public String firstname;
    public List<String> paymentModes;
    public String lastname;
    public RewardsDTO rewardsDTO;
    public List<String> product_type;
    public String language_preferred;
    public boolean isLangChange;
    public AddressDTO addressDTO;
    public List<String> deliverymode;
    public List<PickupPointsDTO> pickupPointsDTO;
    public List<DeliveryAreasDTO> deliveryAreasDTO;
    public String gst_enabled;
    public String price_inclusive_gst;
    public int deliveryFee;
    public int orderThreshold;

    public static class RewardsDTO {
        public int RewardAmount;
        public int RewardPoints;
        public int RedeeemAmount;
        public int RedeemPoints;

        public RewardsDTO() {
        }

        public RewardsDTO(int rewardAmount, int rewardPoints, int redeeemAmount, int redeemPoints) {
            RewardAmount = rewardAmount;
            RewardPoints = rewardPoints;
            RedeeemAmount = redeeemAmount;
            RedeemPoints = redeemPoints;
        }
    }

}

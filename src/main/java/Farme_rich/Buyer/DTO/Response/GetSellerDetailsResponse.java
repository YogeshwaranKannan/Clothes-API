package Farme_rich.Buyer.DTO.Response;

import Farme_rich.Seller.Model.FrontEnd.PickupPoints;
import Farme_rich.Seller.Model.FrontEnd.Seller;

import java.util.List;

public class GetSellerDetailsResponse {
    public String sellerid;
    public String companyname;
    public String companyMobile;
    public SellerPaymentDetails sellerPaymentDetails;
    public String serviceImageFolder;

    public String profileImgPath;
    public List<String> paymentModes;

    public List<PickupPoints> pickupPoints;
    public List<Seller.DeliveryAreas> deliveryAreas;

    public RewardsDTO rewardsDTO;

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

    public static class SellerPaymentDetails {
        public String upi_Id;
        public String secret_Key;
        public String key;
        public String pay_Online_Link;

        public SellerPaymentDetails() {
        }
    }
}

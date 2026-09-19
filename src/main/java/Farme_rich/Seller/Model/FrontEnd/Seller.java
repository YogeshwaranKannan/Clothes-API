package Farme_rich.Seller.Model.FrontEnd;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "Seller") // company
public class Seller {
    @Id
    private String id;
    private List<String> userid;  //company  id join
    private String companyname;
    private String companyMobile;
    private List<String> paymentModes;
    private String email;
    private List<String> product_type;
    private String seller_type;
    private boolean gst_enabled;

    private boolean price_inclusive_gst;
    private Rewards rewards;
    private String createAT;
    private String updateAT;
    private List<String> cropOptions;
    private Address address;
    private SellerPaymentDetails seller_Payment_Details;
    private List<String> deliverymode;
    private List<PickupPoints> pickupPoints;
    private List<DeliveryAreas> deliveryAreas;

    private DeliveryFeeDetails deliveryFeeDetails;

    public DeliveryFeeDetails getDeliveryFeeDetails() {
        return deliveryFeeDetails;
    }

    public void setDeliveryFeeDetails(DeliveryFeeDetails deliveryFeeDetails) {
        this.deliveryFeeDetails = deliveryFeeDetails;
    }

    public String getCompanyname() {
        return companyname;
    }

    public void setCompanyname(String companyname) {
        this.companyname = companyname;
    }

    public List<String> getPaymentModes() {
        return paymentModes;
    }

    public void setPaymentModes(List<String> paymentModes) {
        this.paymentModes = paymentModes;
    }

    public String getCompanyMobile() {
        return companyMobile;
    }

    public void setCompanyMobile(String companyMobile) {
        this.companyMobile = companyMobile;
    }

    public Rewards getRewards() {
        return rewards;
    }

    public void setRewards(Rewards rewards) {
        this.rewards = rewards;
    }

    public List<String> getUserid() {
        return userid;
    }

    public void setUserid(List<String> userid) {
        this.userid = userid;
    }

    public String getUpdateAT() {
        return updateAT;
    }

    public void setUpdateAT(String updateAT) {
        this.updateAT = updateAT;
    }

    public String getCreateAT() {
        return createAT;
    }

    public void setCreateAT(String createAT) {
        this.createAT = createAT;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getProduct_type() {
        return product_type;
    }

    public void setProduct_type(List<String> product_type) {
        this.product_type = product_type;
    }

    public boolean isPrice_inclusive_gst() {
        return price_inclusive_gst;
    }

    public void setPrice_inclusive_gst(boolean price_inclusive_gst) {
        this.price_inclusive_gst = price_inclusive_gst;
    }

    public boolean isGst_enabled() {
        return gst_enabled;
    }

    public void setGst_enabled(boolean gst_enabled) {
        this.gst_enabled = gst_enabled;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public List<String> getDeliverymode() {
        return deliverymode;
    }

    public void setDeliverymode(List<String> deliverymode) {
        this.deliverymode = deliverymode;
    }

    public List<PickupPoints> getPickupPoints() {
        return pickupPoints;
    }

    public void setPickupPoints(List<PickupPoints> pickupPoints) {
        this.pickupPoints = pickupPoints;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getSeller_type() {
        return seller_type;
    }

    public void setSeller_type(String seller_type) {
        this.seller_type = seller_type;
    }

    public List<String> getCropOptions() {
        return cropOptions;
    }

    public void setCropOptions(List<String> cropOptions) {
        this.cropOptions = cropOptions;
    }


    public List<DeliveryAreas> getDeliveryAreas() {
        return deliveryAreas;
    }

    public void setDeliveryAreas(List<DeliveryAreas> deliveryAreas) {
        this.deliveryAreas = deliveryAreas;
    }

    public SellerPaymentDetails getSeller_Payment_Details() {
        return seller_Payment_Details;
    }

    public void setSeller_Payment_Details(SellerPaymentDetails seller_Payment_Details) {
        this.seller_Payment_Details = seller_Payment_Details;
    }

    public static class DeliveryAreas {
        public String city;
        public String pincode;

        public DeliveryAreas() {
        }

        public DeliveryAreas(String city, String pincode) {
            this.city = city;
            this.pincode = pincode;
        }
    }

    public static class Rewards {
        private int RewardAmount;
        private int RewardPoints;
        private int RedeeemAmount;
        private int RedeemPoints;

        public Rewards() {
        }

        public Rewards(int rewardAmount, int rewardPoints, int redeeemAmount, int redeemPoints) {
            RewardAmount = rewardAmount;
            RewardPoints = rewardPoints;
            RedeeemAmount = redeeemAmount;
            RedeemPoints = redeemPoints;
        }

        public int getRewardAmount() {
            return RewardAmount;
        }

        public void setRewardAmount(int rewardAmount) {
            RewardAmount = rewardAmount;
        }

        public int getRewardPoints() {
            return RewardPoints;
        }

        public void setRewardPoints(int rewardPoints) {
            RewardPoints = rewardPoints;
        }

        public int getRedeeemAmount() {
            return RedeeemAmount;
        }

        public void setRedeeemAmount(int redeeemAmount) {
            RedeeemAmount = redeeemAmount;
        }

        public int getRedeemPoints() {
            return RedeemPoints;
        }

        public void setRedeemPoints(int redeemPoints) {
            RedeemPoints = redeemPoints;
        }
    }

    public static class SellerPaymentDetails {
        private String upi_Id;
        private String secret_Key;
        private String key;
        private String pay_Online_Link;

        public SellerPaymentDetails() {
        }

        public String getUpi_Id() {
            return upi_Id;
        }

        public void setUpi_Id(String upi_Id) {
            this.upi_Id = upi_Id;
        }

        public String getSecret_Key() {
            return secret_Key;
        }

        public void setSecret_Key(String secret_Key) {
            this.secret_Key = secret_Key;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getPay_Online_Link() {
            return pay_Online_Link;
        }

        public void setPay_Online_Link(String pay_Online_Link) {
            this.pay_Online_Link = pay_Online_Link;
        }
    }

    public static class DeliveryFeeDetails {
        private int deliveryFee;
        private int orderThreshold;

        public DeliveryFeeDetails() {
        }

        public DeliveryFeeDetails(int deliveryFee, int orderThreshold) {
            this.deliveryFee = deliveryFee;
            this.orderThreshold = orderThreshold;
        }

        public int getDeliveryFee() {
            return deliveryFee;
        }

        public void setDeliveryFee(int deliveryFee) {
            this.deliveryFee = deliveryFee;
        }

        public int getOrderThreshold() {
            return orderThreshold;
        }

        public void setOrderThreshold(int orderThreshold) {
            this.orderThreshold = orderThreshold;
        }
    }
}

package Farme_rich.Seller.Model.BackEnd;

import Farme_rich.Buyer.Model.Buyer;
import Farme_rich.Seller.Model.FrontEnd.Seller;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

@Document(collection = "sellerorder")
public class Save {
    @Id
    private ObjectId _id;
    private String refno;
    private Rewards rewards;
    private Buyer buyerid;
    private String orderSource;
    private ObjectId companyid;
    @Field("order_status")
    private String orderStatus;
    private String payment_status;
    private Seller.DeliveryFeeDetails deliveryFeeDetails;
    private String isgst;
    private String ispriceInclusive;
    private List<Inventory> seller_items;
    private String payment_mode;

    private double total_Discount;
    private double total_Advance;
    private double total_Refund_amount;
    private double total_Paid;
    private double total_AmountDue;
    private double total_Amount;

    private boolean isDelivered;
    private String deliveryDueDate;
    private String deliveryDate;

    private Payments paymentDetails;
    private CustomerInformation customerInformation;
    private String delivery_details;
    private String delivery_mode;
    private String tracking_details;

    private RazorPaymentDetails razorpaymentdetails;

    private List<DeletedInventory> deletedInventories;

    private AcceptReviewDetails acceptReviewDetails;

    private String reason_FullyRejectedOrders;

    @Indexed
    private Date orderDate;
    private String updatedAt;

    public String getPayment_status() {
        return payment_status;
    }

    public void setPayment_status(String payment_status) {
        this.payment_status = payment_status;
    }

    public Seller.DeliveryFeeDetails getDeliveryFeeDetails() {
        return deliveryFeeDetails;
    }

    public void setDeliveryFeeDetails(Seller.DeliveryFeeDetails deliveryFeeDetails) {
        this.deliveryFeeDetails = deliveryFeeDetails;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public Rewards getRewards() {
        return rewards;
    }

    public void setRewards(Rewards rewards) {
        this.rewards = rewards;
    }

    public String getIsgst() {
        return isgst;
    }

    public void setIsgst(String isgst) {
        this.isgst = isgst;
    }

    public String getIspriceInclusive() {

        return ispriceInclusive;
    }

    public void setIspriceInclusive(String ispriceInclusive) {
        this.ispriceInclusive = ispriceInclusive;
    }

    public String getRefno() {
        return refno;
    }

    public void setRefno(String refno) {
        this.refno = refno;
    }

    public String getDelivery_mode() {
        return delivery_mode;
    }

    public void setDelivery_mode(String delivery_mode) {
        this.delivery_mode = delivery_mode;
    }

    public Buyer getBuyerid() {
        return buyerid;
    }

    public void setBuyerid(Buyer buyerid) {
        this.buyerid = buyerid;
    }

    public ObjectId getCompanyid() {
        return companyid;
    }

    public void setCompanyid(ObjectId companyid) {
        this.companyid = companyid;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public List<Inventory> getSeller_items() {
        return seller_items;
    }

    public void setSeller_items(List<Inventory> seller_items) {
        this.seller_items = seller_items;
    }

    public String getPayment_mode() {
        return payment_mode;
    }

    public void setPayment_mode(String payment_mode) {
        this.payment_mode = payment_mode;
    }

    public String getDelivery_details() {
        return delivery_details;
    }

    public void setDelivery_details(String delivery_details) {
        this.delivery_details = delivery_details;
    }

    public String getTracking_details() {
        return tracking_details;
    }

    public void setTracking_details(String tracking_details) {
        this.tracking_details = tracking_details;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Payments getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(Payments paymentDetails) {
        this.paymentDetails = paymentDetails;
    }

    public CustomerInformation getCustomerInformation() {
        return customerInformation;
    }

    public void setCustomerInformation(CustomerInformation customerInformation) {
        this.customerInformation = customerInformation;
    }

    public double getTotal_Discount() {
        return total_Discount;
    }

    public void setTotal_Discount(double total_Discount) {
        this.total_Discount = total_Discount;
    }

    public double getTotal_Advance() {
        return total_Advance;
    }

    public void setTotal_Advance(double total_Advance) {
        this.total_Advance = total_Advance;
    }

    public double getTotal_Paid() {
        return total_Paid;
    }

    public void setTotal_Paid(double total_Paid) {
        this.total_Paid = total_Paid;
    }

    public double getTotal_AmountDue() {
        return total_AmountDue;
    }

    public void setTotal_AmountDue(double total_AmountDue) {
        this.total_AmountDue = total_AmountDue;
    }

    public boolean isDelivered() {
        return isDelivered;
    }

    public void setDelivered(boolean delivered) {
        isDelivered = delivered;
    }

    public String getDeliveryDueDate() {
        return deliveryDueDate;
    }

    public void setDeliveryDueDate(String deliveryDueDate) {
        this.deliveryDueDate = deliveryDueDate;
    }

    public double getTotal_Amount() {
        return total_Amount;
    }

    public void setTotal_Amount(double total_Amount) {
        this.total_Amount = total_Amount;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public RazorPaymentDetails getRazorpaymentdetails() {
        return razorpaymentdetails;
    }

    public void setRazorpaymentdetails(RazorPaymentDetails razorpaymentdetails) {
        this.razorpaymentdetails = razorpaymentdetails;
    }

    public String getOrderSource() {
        return orderSource;
    }

    public void setOrderSource(String orderSource) {
        this.orderSource = orderSource;
    }

    public List<DeletedInventory> getDeletedInventories() {
        return deletedInventories;
    }

    public void setDeletedInventories(List<DeletedInventory> deletedInventories) {
        this.deletedInventories = deletedInventories;
    }

    public AcceptReviewDetails getAcceptReviewDetails() {
        return acceptReviewDetails;
    }

    public void setAcceptReviewDetails(AcceptReviewDetails acceptReviewDetails) {
        this.acceptReviewDetails = acceptReviewDetails;
    }

    public String getReason_FullyRejectedOrders() {
        return reason_FullyRejectedOrders;
    }

    public void setReason_FullyRejectedOrders(String reason_FullyRejectedOrders) {
        this.reason_FullyRejectedOrders = reason_FullyRejectedOrders;
    }

    public double getTotal_Refund_amount() {
        return total_Refund_amount;
    }

    public void setTotal_Refund_amount(double total_Refund_amount) {
        this.total_Refund_amount = total_Refund_amount;
    }

    public static class Rewards {
        private int RewardsEarned;
        private int rewardsRedeemed;
        private boolean isApplied;

        public Rewards() {
        }

        public Rewards(int rewardsEarned, int rewardsRedeemed, boolean isApplied) {
            RewardsEarned = rewardsEarned;
            this.rewardsRedeemed = rewardsRedeemed;
            this.isApplied = isApplied;
        }

        public boolean isApplied() {
            return isApplied;
        }

        public void setApplied(boolean applied) {
            isApplied = applied;
        }

        public int getRewardsEarned() {
            return RewardsEarned;
        }

        public void setRewardsEarned(int rewardsEarned) {
            RewardsEarned = rewardsEarned;
        }

        public int getRewardsRedeemed() {
            return rewardsRedeemed;
        }

        public void setRewardsRedeemed(int rewardsRedeemed) {
            this.rewardsRedeemed = rewardsRedeemed;
        }
    }
}

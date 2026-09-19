package Farme_rich.Buyer.DTO.Response;

import java.util.List;

public class ViewOrderDetailsResponse {

    public String order_id;

    public String order_status;
    public List<Stages> stages;
    public GetSellerDetailsResponse.RewardsDTO rewardsDTO;

    public List<ItemsDTO> items;
    public String tot_order_value;
    public PaymentDTO payment;
    public CustomerInfoDTO customer;
    public DeliveryDTO delivery;

//    public ObjectId save_orderid;
//    public ObjectId companyid;
//    public String buyerid;
//    public String payment_mode;
//    public String delivery_details;
//    public String delivery_mode;
//    public String tracking_details;
//    public double total_Discount;
//    public double total_Advance;
//    public double total_Paid;
//    public double total_AmountDue;
//    public String payment_status;
//    public boolean isDelivered;
//    public String deliveryDueDate;
//    public String deliveredDate;
//    public String order_date;
//    public String updatedAt;
//    public String isgst;
//    public String ispriceInclusive;
}

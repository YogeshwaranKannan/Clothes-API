package Farme_rich.Seller.DTO.Response;

import Farme_rich.Buyer.DTO.Request.RazorPaymentDetailsDTO;
import Farme_rich.Buyer.Model.Buyer;
import Farme_rich.Seller.DTO.Request.CustomerInformationDTO;
import Farme_rich.Seller.DTO.Request.PaymentsDTO;
import Farme_rich.Seller.Model.BackEnd.AcceptReviewDetails;
import org.bson.types.ObjectId;

import java.util.List;

public class ViewOrderListResponse {
    public ObjectId companyid;
    public String refno;
    public Buyer buyerid;
    public String orderSource;
    public ObjectId save_orderid;
    public String order_status;
    public String payment_status;
    public List<InventoryDTO> inventory;
    public String payment_mode;
    public String delivery_details;
    public String delivery_mode;
    public String tracking_details;

    public RazorPaymentDetailsDTO razorPaymentDetailsDTO;

    public AcceptReviewDetails acceptReviewDetails;

    public double total_Discount;
    public double total_Advance;
    public double total_Paid;
    public double total_AmountDue;
    public double total_Amount;

    public boolean isDelivered;
    public String deliveryDueDate;
    public String deliveredDate;

    public PaymentsDTO paymentsDTO;
    public CustomerInformationDTO customerInformationDTO;
    public boolean draft_active;
    public String order_date;
    public String updatedAt;
    public String isgst;
    public String ispriceInclusive;

}

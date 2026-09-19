package Farme_rich.Seller.DTO.Response;

import Farme_rich.Buyer.Model.Buyer;
import Farme_rich.Seller.DTO.Request.CustomerInformationDTO;
import Farme_rich.Seller.DTO.Request.PaymentsDTO;
import org.bson.types.ObjectId;

import java.util.List;

public class InvoiceResponse {
    public ObjectId companyid;
    public String companyName;
    public String companyFolder;
    public String baseUrl;
    public String companyLogo;
    public String signatureLogo;
    public String refno;
    public Buyer buyerid;
    public String orderSource;
    public ObjectId save_orderid;
    public String order_status;
    public List<InventoryDTO> inventory;
    public String isgst;
    public String ispriceInclusive;
    public String payment_details;
    public String delivery_details;
    public String delivery_mode;
    public String tracking_details;

    public double total_Discount;
    public double total_Amount;
    public double total_Advance;
    public double total_Paid;
    public double total_AmountDue;

    public CustomerInformationDTO customerInformationDTO;

    public String payment_status;
    public String payment_mode;
    public boolean isDelivered;
    public String deliveryDueDate;
    public String deliveredDate;
    public PaymentsDTO paymentsDTO;


    public boolean draft_active;
    public String order_date;
    public String updatedAt;
    public String invoiceHtml;


}

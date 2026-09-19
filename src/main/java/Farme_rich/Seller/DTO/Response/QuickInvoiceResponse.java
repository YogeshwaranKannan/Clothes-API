package Farme_rich.Seller.DTO.Response;

import Farme_rich.Seller.DTO.Request.QuickProductsDTO;
import org.bson.types.ObjectId;

import java.util.List;

public class QuickInvoiceResponse {
    public ObjectId _id;
    public ObjectId qiCustomer_id;
    public ObjectId companyid;
    public  ObjectId QuickInvoiceID;
    public String refno;
    public String quickinvoicereference;
    public List<QuickProductsDTO> quickProductsDTO;
    public int totalitem;
    public int ordervalue;
    public String order_date;
    public String updatedAt;
    public int AmountDue;
    public int AmountPaid;
    public int Discount;
    public int Balance;
    public String paymentmode;
    public String invoiceHtml;
    public String delivery_mobileNum;
    public String note;
    public String deletedproductsresponse;
    public ObjectId QICustomerID;

}

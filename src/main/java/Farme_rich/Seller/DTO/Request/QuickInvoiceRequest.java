package Farme_rich.Seller.DTO.Request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
 public class QuickInvoiceRequest {
    public ObjectId _id;
    public ObjectId qiCustomer_id;
    public ObjectId companyid;
    public List<QuickProductsDTO> quickProductsDTO;
    public int totalitem;
    public int ordervalue;
    public String template;
    public String note;
    public String paymentmode;
    public int AmountDue;
    public int AmountPaid;

    public String user_id;  //Seller_ID
    public List<QuickProductsDeleteDTO> quickProductsDeleteDTO;
    public String delivery_mobileNum;
    public String customerName;
    public String mobile_num;
    public String name;
    public String address;

}


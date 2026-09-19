package Farme_rich.Buyer.DTO.Response;

import java.util.List;

public class GetAllOrdersRes {

    public String order_id;
    public String refno;

    public String customerSiteName;
    public String siteLocation;

    public String work;
    public String flooring;
    public String totalLength;

    public String contactName;
    public String type;

    public String deliveryDate;

    public String jobValue;
    public String amountReceived;
    public String siteExpenses;
    public String balanceAmount;

    public String duration;

    public List<ItemsDTO> items;

    public String tot_order_value;

    public String order_Status;
    public String order_date;
    public String payment_status;
}

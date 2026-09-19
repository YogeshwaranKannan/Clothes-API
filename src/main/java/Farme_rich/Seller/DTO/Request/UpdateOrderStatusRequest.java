package Farme_rich.Seller.DTO.Request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateOrderStatusRequest {
    public String _id;
    public String oldStatus;
    public boolean isDelayclicked;
    public String newStatus;
    public String deliveryDueDate;
}

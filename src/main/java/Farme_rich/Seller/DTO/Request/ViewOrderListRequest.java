package Farme_rich.Seller.DTO.Request;

import jdk.dynalink.linker.LinkerServices;
import org.bson.types.ObjectId;

import java.util.List;

public class ViewOrderListRequest {
    public String user_type;
    public ObjectId companyid;
    public List<String> order_status;

}

package Farme_rich.Seller.DTO.Request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReadQuickInvoiceRequest {
    public ObjectId id;
    public String template;
    public String userfolder;
}

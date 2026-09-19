package Farme_rich.Seller.DTO.Request;





import java.util.List;

public class UpdateCompanyDeliveryInfoRequest {

    //unique id
    public String id;

    //deliver options

    public List<String> deliverymode;
    public List<PickupPointsDTO> pickupPointsDTO;

}

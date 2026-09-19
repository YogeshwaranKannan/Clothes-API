package Farme_rich.Seller.DTO.Response;

import Farme_rich.Seller.DTO.Request.AddressDTO;
import Farme_rich.Seller.DTO.Request.DeliveryAreasDTO;
import Farme_rich.Seller.DTO.Request.PickupPointsDTO;
import Farme_rich.Seller.DTO.Request.UpdateCompanyInfoRequest;

import java.util.List;

public class InitialGetUpdateCompanyInfoResponse {
    public String firstname;
    public String lastname;
    public String email;
    public List<String> product_type;
    public List<String> paymentModes;
    public String companyname;
    public String companyMobile;
    public UpdateCompanyInfoRequest.RewardsDTO rewardsDTO;
    public String userMobileNum;
    public List<PickupPointsDTO> pickupPointsDTO;
    public List<DeliveryAreasDTO> deliveryAreasDTOS;
    public String language_preferred;
    public AddressDTO addressDTO;
    public boolean gst_enabled;
    public boolean price_inclusive_gst;
    public int deliveryFee;
    public int orderThreshold;
}

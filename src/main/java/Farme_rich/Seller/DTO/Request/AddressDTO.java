package Farme_rich.Seller.DTO.Request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressDTO {
    //{address line1, addressline2, suburb,state,postcode}
    public String PickupPointname;
    public String address_line1;
    public String address_line2;
    public String city;
    public String pincode;
}

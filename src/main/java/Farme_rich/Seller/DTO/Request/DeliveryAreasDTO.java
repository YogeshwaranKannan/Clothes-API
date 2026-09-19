package Farme_rich.Seller.DTO.Request;

public class DeliveryAreasDTO {
    public String city;
    public String pincode;
    public DeliveryAreasDTO() {
    }
    public DeliveryAreasDTO(String city, String pincode) {
        this.city = city;
        this.pincode = pincode;
    }
}

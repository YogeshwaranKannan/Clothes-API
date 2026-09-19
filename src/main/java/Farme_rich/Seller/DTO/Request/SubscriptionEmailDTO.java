package Farme_rich.Seller.DTO.Request;

public class SubscriptionEmailDTO {
    public String companyName;
    public String firstName;
    public String mobileNumber;
    public String weekday;
    public String email;
    public String bestTimeToConnect;
    public String comment;

    public SubscriptionEmailDTO() {
    }

    public SubscriptionEmailDTO(String email){
        this.email=email;
    }
}


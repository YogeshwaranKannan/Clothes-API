package Farme_rich.Buyer.DTO.Response;

import Farme_rich.Buyer.Model.Buyer;

import java.util.List;

public class SignupOrLoginResponse {

    public boolean success;

    public String message;

    public String errorCode;

    public String errorMsg;

    public UserDTO user;

    public String accessToken;

    public String refreshToken;

    public Long expiresAt;


    public static class UserDTO {

        public String id;

        public String firstName;

        public String lastName;

        public String email;

        public String mobileNum;

        public String type; // buyer / seller

        public String role; // USER / ADMIN

        public List<Buyer.LastDeliveryAddess> lastDeliveryAddesses;

        public int totalRewards;
        public int lastEarned;
        public int lastRedeemed;
        public int totalRedeemed;
        public int availableRewards;

        public UserDTO() {
        }
    }
}

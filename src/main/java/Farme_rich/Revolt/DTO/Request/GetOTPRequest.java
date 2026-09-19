package Farme_rich.Revolt.DTO.Request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GetOTPRequest {
    public String otp;
    public String email;
    public String mobileNum;
    public int attempt;
}

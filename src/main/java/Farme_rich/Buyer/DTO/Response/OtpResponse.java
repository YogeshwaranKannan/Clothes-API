package Farme_rich.Buyer.DTO.Response;

public class OtpResponse {
    public String msg;
    public int otp;

    public OtpResponse() {
    }

    public OtpResponse(String msg, int otp) {
        this.msg = msg;
        this.otp = otp;
    }
}

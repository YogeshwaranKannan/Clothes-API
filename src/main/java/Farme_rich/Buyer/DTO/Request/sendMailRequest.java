package Farme_rich.Buyer.DTO.Request;

public class sendMailRequest {
    public String mailId;
    public String save_id;
    public sendMailRequest() {
    }

    public sendMailRequest(String mailId, String save_id) {
        this.mailId = mailId;
        this.save_id = save_id;
    }
}

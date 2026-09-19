package Farme_rich.Seller.DTO.Request;

import org.springframework.web.multipart.MultipartFile;

public class UploadImageRequest {
    private String companyid;
    private MultipartFile img;

    // getters & setters
    public String getCompanyid() {
        return companyid;
    }
    public void setCompanyid(String companyid) {
        this.companyid = companyid;
    }
    public MultipartFile getImg() {
        return img;
    }
    public void setImg(MultipartFile img) {
        this.img = img;
    }
}


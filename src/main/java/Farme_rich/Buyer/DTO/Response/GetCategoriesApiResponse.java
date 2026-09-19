package Farme_rich.Buyer.DTO.Response;

import java.util.List;

public class GetCategoriesApiResponse {
    public ResponseHeader responseHeader;
    public List<GetCategoriesResponse> categories;

    public static class ResponseHeader {
        public String action;
        public int seller_id;
        public String status;
    }
}

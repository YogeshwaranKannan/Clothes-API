package Farme_rich.Buyer.DTO.Response;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class GetCategoriesResponse {
        public String id;
        public String parent;

        public String coverImage;
        public String category;
        public boolean isDisplayOnHome;

        public String Description;

        public List<GetCategoriesResponse> children = new ArrayList<>(); // rename
}

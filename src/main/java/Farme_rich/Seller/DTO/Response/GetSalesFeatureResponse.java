package Farme_rich.Seller.DTO.Response;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GetSalesFeatureResponse {
    public List<features> features=new ArrayList<features>();

    public static class features{
       public ObjectId features_id;
       public  String features_name;

        public features(ObjectId features_id, String features_name) {
            this.features_id = features_id;
            this.features_name = features_name;
        }
    }
}

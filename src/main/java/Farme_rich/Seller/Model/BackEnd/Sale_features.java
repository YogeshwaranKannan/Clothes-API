package Farme_rich.Seller.Model.BackEnd;


import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Sale_features")
public class Sale_features {

    @Id
    private ObjectId _id;
    private String feature_name;
    private ObjectId sellerid;

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getFeature_name() {
        return feature_name;
    }

    public void setFeature_name(String feature_name) {
        this.feature_name = feature_name;
    }

    public ObjectId getSellerid() {
        return sellerid;
    }

    public void setSellerid(ObjectId sellerid) {
        this.sellerid = sellerid;
    }
}

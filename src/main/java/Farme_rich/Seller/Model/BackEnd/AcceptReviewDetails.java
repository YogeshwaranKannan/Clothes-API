package Farme_rich.Seller.Model.BackEnd;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "AcceptReviewDetails")
public class AcceptReviewDetails {
    @Id
    private ObjectId _id;
    private List<AcceptReviewInfo> acceptReviewInfos;

    public AcceptReviewDetails(List<AcceptReviewInfo> acceptReviewInfos) {
        this.acceptReviewInfos = acceptReviewInfos;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public List<AcceptReviewInfo> getAcceptReviewInfos() {
        return acceptReviewInfos;
    }

    public void setAcceptReviewInfos(List<AcceptReviewInfo> acceptReviewInfos) {
        this.acceptReviewInfos = acceptReviewInfos;
    }
}

package Farme_rich.Seller.DTO.Request;

import org.bson.types.ObjectId;

public class SecondTimeQIRequest {
    public String id;
    public int limit;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

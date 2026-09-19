package Farme_rich.Seller.Model.BackEnd;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "Promo_messages")
public class Promo_messages {
    @Id
    private ObjectId _id;
    private ObjectId sellerid;
    private String promo_msg;
    private boolean is_active;
    private String created_by;
    private String updated_by;
    private Date created_At;
    private Date updated_At;

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public ObjectId getSellerid() {
        return sellerid;
    }

    public void setSellerid(ObjectId sellerid) {
        this.sellerid = sellerid;
    }

    public String getPromo_msg() {
        return promo_msg;
    }

    public void setPromo_msg(String promo_msg) {
        this.promo_msg = promo_msg;
    }

    public boolean isIs_active() {
        return is_active;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public String getUpdated_by() {
        return updated_by;
    }

    public void setUpdated_by(String updated_by) {
        this.updated_by = updated_by;
    }

    public Date getCreated_At() {
        return created_At;
    }

    public void setCreated_At(Date created_At) {
        this.created_At = created_At;
    }

    public Date getUpdated_At() {
        return updated_At;
    }

    public void setUpdated_At(Date updated_At) {
        this.updated_At = updated_At;
    }
}

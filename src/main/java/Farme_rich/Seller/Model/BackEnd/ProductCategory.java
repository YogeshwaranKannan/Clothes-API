package Farme_rich.Seller.Model.BackEnd;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "product_category")
public class ProductCategory {
    @Id
    private ObjectId _id;
    private String category_name;
    private ObjectId parent_id;
    private String description;
    private List<ObjectId> sellerids;
    private String createdAt;
    private String coverImagePath;
    private boolean displayOnHomePage;
    private String updatedAt;

    public ProductCategory() {

    }

    public ProductCategory(String category_name, ObjectId parent) {
        this.category_name = category_name;
        this.parent_id = parent;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public ObjectId getParent_id() {
        return parent_id;
    }

    public void setParent_id(ObjectId parent_id) {
        this.parent_id = parent_id;
    }

    public List<ObjectId> getSellerids() {
        return sellerids;
    }

    public void setSellerids(List<ObjectId> sellerids) {
        this.sellerids = sellerids;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isDisplayOnHomePage() {
        return displayOnHomePage;
    }

    public void setDisplayOnHomePage(boolean displayOnHomePage) {
        this.displayOnHomePage = displayOnHomePage;
    }

    public String getCoverImagePath() {
        return coverImagePath;
    }

    public void setCoverImagePath(String coverImagePath) {
        this.coverImagePath = coverImagePath;
    }
}

package Farme_rich.Seller.DTO.Response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductCategoryDTO {
    public String categoryId;
    public String categoryName;

    public String _id;
    public String category_name;
    public ObjectId sellerid;
    public String coverImagePath;
    public boolean displayOnHomePage;
    private String parent;
    private String description;
    private String createdAt;
    private String updatedAt;

//    public ProductCategoryDTO(String category_name, String parent) {
//        this.category_name = category_name;
//        this.parent = parent;
//    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
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
}

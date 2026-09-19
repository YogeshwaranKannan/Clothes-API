package Farme_rich.Seller.DTO.Request;
 

import java.util.ArrayList;
import java.util.List;

public class CategoryTreeDTO {

    private String id;
    private String category_name;
    private String description;
    private String coverImagePath;
    private boolean displayOnHomePage;
    private String parentId;
    private List<CategoryTreeDTO> children = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverImagePath() {
        return coverImagePath;
    }

    public void setCoverImagePath(String coverImagePath) {
        this.coverImagePath = coverImagePath;
    }

    public boolean isDisplayOnHomePage() {
        return displayOnHomePage;
    }

    public void setDisplayOnHomePage(boolean displayOnHomePage) {
        this.displayOnHomePage = displayOnHomePage;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public List<CategoryTreeDTO> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryTreeDTO> children) {
        this.children = children;
    }
}

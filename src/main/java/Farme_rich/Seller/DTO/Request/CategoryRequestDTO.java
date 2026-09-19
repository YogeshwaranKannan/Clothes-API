package Farme_rich.Seller.DTO.Request;


import java.util.List;

public class CategoryRequestDTO {

    private String category_name;
    private String companyName;
    private String parent_id;          // null / empty = top-level category
    private String description;
    private String coverImagePath;
    private boolean displayOnHomePage;
    private List<String> sellerids;    // optional

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getParent_id() {
        return parent_id;
    }

    public void setParent_id(String parent_id) {
        this.parent_id = parent_id;
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

    public List<String> getSellerids() {
        return sellerids;
    }

    public void setSellerids(List<String> sellerids) {
        this.sellerids = sellerids;
    }
}

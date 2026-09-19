package Farme_rich.Seller.DTO.Response;

import Farme_rich.Seller.DTO.Request.ProductVariantDTO;
import Farme_rich.Seller.Model.BackEnd.Batches;
import Farme_rich.Seller.Model.BackEnd.ProductCategory;
import Farme_rich.Seller.Model.BackEnd.PropertyAttributes;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class InitialGetAllProductsResponse {

    public List<ProductItem> products = new ArrayList<ProductItem>();

    public int seeInv;
    public int seeServ;
    public int seeProd;

    public int invTaken;
    public int servTaken;
    public int prodTaken;

    public static class ProductItem {

        public String _id;
        private String ProductId;
        private String ProductName;
        public String brand;
        private String variantName;
        private String SellingPrice;
        public String serviceDesc;
        public String serviceNotes;
        private ProductCategory serviceCategory;
        public String  CreatedAt;
        public String batchid;
        public ProductCategory productCategoryDTO;
        public String uniquebatchid;
        public String stock_availability;
        private String OfferPrice;
        public String unit;
        public boolean isActive;
        public boolean deals;
        public boolean newArrivals;

        public List<Batches> productVariantDTOS;
        public boolean Quickadd;
        public List<PropertyAttributes> propertyAttributes;
        public List<ObjectId> addOnIds;
        private String DiscountPercent;
        private String defaultImage;
        private Category Category; // NULL for services

        // getters & setters
        public String getProductId() { return ProductId; }
        public void setProductId(String productId) { ProductId = productId; }


        public boolean isActive() {
            return isActive;
        }

        public ProductCategory getServiceCategory() {
            return serviceCategory;
        }

        public void setServiceCategory(ProductCategory serviceCategory) {
            this.serviceCategory = serviceCategory;
        }

        public void setActive(boolean active) {
            this.isActive = active;
        }
        public String getProductName() { return ProductName; }
        public void setProductName(String productName) { ProductName = productName; }

        public String getSellingPrice() { return SellingPrice; }
        public void setSellingPrice(String sellingPrice) { SellingPrice = sellingPrice; }

        public String getOfferPrice() { return OfferPrice; }
        public void setOfferPrice(String offerPrice) { OfferPrice = offerPrice; }

        public String getDiscountPercent() { return DiscountPercent; }
        public void setDiscountPercent(String discountPercent) {
            DiscountPercent = discountPercent;
        }

        public String getDefaultImage() { return defaultImage; }
        public void setDefaultImage(String defaultImage) {
            this.defaultImage = defaultImage;
        }

        public Category getCategory() { return Category; }
        public void setCategory(Category category) { Category = category; }

        public String getVariantName() {
            return variantName;
        }

        public void setVariantName(String variantName) {
            this.variantName = variantName;
        }
    }

    public static class Category {
        private String CategoryId;
        private String CategoryName;

        // getters & setters
        public String getCategoryId() { return CategoryId; }
        public void setCategoryId(String categoryId) {
            CategoryId = categoryId;
        }

        public String getCategoryName() { return CategoryName; }
        public void setCategoryName(String categoryName) {
            CategoryName = categoryName;
        }
    }




    // ---------- Getters & Setters ----------

    public List<ProductItem> getProducts() {
        return products;
    }

    public void setProducts(List<ProductItem> products) {
        this.products = products;
    }
}


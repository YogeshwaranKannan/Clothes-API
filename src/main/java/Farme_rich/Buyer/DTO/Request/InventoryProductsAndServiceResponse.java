package Farme_rich.Buyer.DTO.Request;

import Farme_rich.Seller.Model.BackEnd.PropertyAttributes;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class InventoryProductsAndServiceResponse {

    public List<ProductItem> products = new ArrayList<>();

    public int seeInv;
    public int seeServ;
    public int seeProd;

    public int invTaken;
    public int servTaken;
    public int prodTaken;

    public static class ProductItem {
        public String inventoryId;
        public String sellerid;
        public String ProductId;
        public String description;

        public String ProductName;
        public double SellingPrice;
        public int AvilableQty;
        public String Brand;
        public String Size;
        public String Gender;
        public String Speed;

        public String StockStatus = "In Stock";
        public String OfferPrice;
        public String DiscountPercent;

        public ProductCategory Category;

        public String Batch_Id;
        public String batch_unit;

        public String Image;

        public boolean isService;
        public List<VariantItem> variants;

        public List<ObjectId> addons;
        public boolean isAttributePresent;
        public List<PropertyAttributes> propertyAttributes;

        public String CreatedAt;
    }

    public static class ProductCategory {
        public ObjectId CategoryId;
        public String CategoryName;
    }

    public static class VariantItem {
        public String variantsid;
        public String variantsName;

        public String SellingPrice;
        public Double OfferPrice;
        public String DiscountPercent;

        public Integer AvilableQty;
        public String StockStatus;

        public String offerText;
    }
}
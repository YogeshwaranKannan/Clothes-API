package Farme_rich.Buyer.DTO.Response;


import Farme_rich.Seller.DTO.Response.ProductCategoryDTO;
import Farme_rich.Seller.Model.BackEnd.Inventory;
import Farme_rich.Seller.Model.BackEnd.ProductCategory;

import java.util.List;

public class BuyerProductResponse {
    public String productName;
    public String productDescription;
    public ProductCategoryDTO productCategory; //
    public String imgurl;
    public Inventory inventoryData;

    public MessageResponse ResponseMessage;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public ProductCategoryDTO getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(ProductCategoryDTO productCategory) {
        this.productCategory = productCategory;
    }

    public Inventory getInventoryData() {
        return inventoryData;
    }

    public void setInventoryData(Inventory inventoryData) {
        this.inventoryData = inventoryData;
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    public MessageResponse getResponseMessage() {
        return ResponseMessage;
    }

    public void setResponseMessage(MessageResponse responseMessage) {
        ResponseMessage = responseMessage;
    }
}

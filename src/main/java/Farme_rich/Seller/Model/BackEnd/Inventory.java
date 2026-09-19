package Farme_rich.Seller.Model.BackEnd;

import Farme_rich.Seller.DTO.Request.PropertyAttributesDTO;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Document(collection = "inventory")
public class Inventory {
    public int Order_quantity;
    public double selling_value;
    @Id
    private String _id;
    private String uniqueID;
    private boolean isActive;
    private String companyid;
    private ObjectId productid;
    private String flag;
    private int CGST;
    private String productName;
    private double sellerprice;
    private double offerprice;
    private ProductCategory productCategory;
    private int SGST;
    private String gst;
    private ObjectId categorID;
    private boolean deals;
    private boolean newArrivals;
    private String defaultImage;
    private String price_inclusive;
    private String discount_percentage;
    private String created_at;
    private String updated_at;
    private String unique_batch_ID;
    private String batch_Id;
    private Batches batchid;
    private List<Batches> variantBatches;
    private Map<String, String> propertyAttributes;
    private Map<String, String> sale_features;
    private ObjectId categoryId;
    private List<PropertyAttributesDTO> Available_properties;
    private String servicename;
    private double servicecost;
    private int sellerstock;
    @Version
    private Integer version;
    private String reason_for_QtyChanges;


    public String getGst() {
        return gst;
    }

    public void setGst(String gst) {
        this.gst = gst;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public String getBatch_Id() {
        return batch_Id;
    }

    public void setBatch_Id(String batch_Id) {
        this.batch_Id = batch_Id;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getPrice_inclusive() {
        return price_inclusive;
    }

    public void setPrice_inclusive(String price_inclusive) {
        this.price_inclusive = price_inclusive;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCompanyid() {
        return companyid;
    }

    public void setCompanyid(String companyid) {
        this.companyid = companyid;
    }

    public int getSellerstock() {
        return sellerstock;
    }

    public void setSellerstock(int sellerstock) {
        this.sellerstock = sellerstock;
    }

    public ProductCategory getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(ProductCategory productCategory) {
        this.productCategory = productCategory;
    }

    public String getUnique_batch_ID() {
        return unique_batch_ID;
    }

    public void setUnique_batch_ID(String unique_batch_ID) {
        this.unique_batch_ID = unique_batch_ID;
    }

    public ObjectId getCategorID() {
        return categorID;
    }

    public void setCategorID(ObjectId categorID) {
        this.categorID = categorID;
    }

    public ObjectId getProductid() {
        return productid;
    }

    public void setProductid(String productid) {
        this.productid = new ObjectId(productid);
    }

    public void setProductid(ObjectId productid) {
        this.productid = productid;
    }

    public int getCGST() {
        return CGST;
    }

    public void setCGST(int CGST) {
        this.CGST = CGST;
    }

    public int getSGST() {
        return SGST;
    }

    public void setSGST(int SGST) {
        this.SGST = SGST;
    }

    public String getDefaultImage() {
        return defaultImage;
    }

    public void setDefaultImage(String defaultImage) {
        this.defaultImage = defaultImage;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public int getOrder_quantity() {
        return Order_quantity;
    }

    public void setOrder_quantity(int order_quantity) {
        Order_quantity = order_quantity;
    }

    public String getDiscount_percentage() {
        return discount_percentage;
    }

    public void setDiscount_percentage(String discount_percentage) {
        this.discount_percentage = discount_percentage;
    }

    public Map<String, String> getPropertyAttributes() {
        return propertyAttributes;
    }

    public void setPropertyAttributes(Map<String, String> propertyAttributes) {
        this.propertyAttributes = propertyAttributes;
    }

    public List<PropertyAttributesDTO> getAvailable_properties() {
        return Available_properties;
    }

    public void setAvailable_properties(List<PropertyAttributesDTO> available_properties) {
        Available_properties = available_properties;
    }

    public double getSellerprice() {
        return sellerprice;
    }

    public void setSellerprice(double sellerprice) {
        this.sellerprice = sellerprice;
    }

    public double getServicecost() {
        return servicecost;
    }

    public void setServicecost(double servicecost) {
        this.servicecost = servicecost;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getServicename() {
        return servicename;
    }

    public void setServicename(String servicename) {
        this.servicename = servicename;
    }

    public Batches getBatchid() {
        return batchid;
    }

    public void setBatchid(Batches batchid) {
        this.batchid = batchid;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public double getSelling_value() {
        return selling_value;
    }

    public void setSelling_value(double selling_value) {
        this.selling_value = selling_value;
    }

    public List<Batches> getVariantBatches() {
        return variantBatches;
    }

    public void setVariantBatches(List<Batches> variantBatches) {
        this.variantBatches = variantBatches;
    }

    public boolean isDeals() {
        return deals;
    }

    public void setDeals(boolean deals) {
        this.deals = deals;
    }

    public boolean isNewArrivals() {
        return newArrivals;
    }

    public void setNewArrivals(boolean newArrivals) {
        this.newArrivals = newArrivals;
    }

    public double getOfferprice() {
        return offerprice;
    }

    public void setOfferprice(double offerprice) {
        this.offerprice = offerprice;
    }

    public String getReason_for_QtyChanges() {
        return reason_for_QtyChanges;
    }

    public void setReason_for_QtyChanges(String reason_for_QtyChanges) {
        this.reason_for_QtyChanges = reason_for_QtyChanges;
    }
}

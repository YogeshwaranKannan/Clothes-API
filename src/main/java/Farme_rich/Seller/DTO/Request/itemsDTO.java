package Farme_rich.Seller.DTO.Request;

import Farme_rich.Seller.DTO.Response.InventoryDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;

@JsonIgnoreProperties(ignoreUnknown = true)
public class itemsDTO {

    private InventoryDTO inventory;

    private int version;
    private int selling_value;



    public InventoryDTO getInventory() {
        return inventory;
    }

    public void setInventory(InventoryDTO inventory) {
        this.inventory = inventory;
    }

    public int getSelling_value() {
        return selling_value;
    }

    public void setSelling_value(int selling_value) {
        this.selling_value = selling_value;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}

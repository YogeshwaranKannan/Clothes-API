package Farme_rich.Seller.DTO.Response;

import Farme_rich.Seller.Model.BackEnd.Inventory;

public class InventoryBatchResponse {


    private String status;
    private Inventory inventory;

    public InventoryBatchResponse() {
    }

    public InventoryBatchResponse(String status, Inventory inventory) {
        this.status = status;
        this.inventory = inventory;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}

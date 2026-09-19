package Farme_rich.Seller.DTO.Response;

import Farme_rich.Seller.Model.BackEnd.Batches;

import java.util.List;

public class SellerGetBatchesResponse {
    List<Batches> batch;
    public int inventory_version;

    public SellerGetBatchesResponse(List<Batches> batch, int inventory_version) {
        this.batch = batch;
        this.inventory_version = inventory_version;
    }

    public List<Batches> getBatch() {
        return batch;
    }

    public void setBatch(List<Batches> batch) {
        this.batch = batch;
    }

    public int getInventory_version() {
        return inventory_version;
    }

    public void setInventory_version(int inventory_version) {
        this.inventory_version = inventory_version;
    }
}

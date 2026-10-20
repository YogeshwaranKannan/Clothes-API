package Farme_rich.Buyer.BL;


import Farme_rich.Seller.DTO.Request.GetCategoriesRequest;
import Farme_rich.Seller.DTO.Response.AddProductResponse;
import Farme_rich.Seller.DTO.Response.GetCategoriesResponse;
import Farme_rich.Seller.DTO.Response.GetProductCategoryResponseDTO;
import Farme_rich.Seller.Model.BackEnd.Batches;
import Farme_rich.Seller.Model.BackEnd.Inventory;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// ← adjust these imports to where the entities live in the buyer project

@Service
public class InventoryService {

    private final MongoTemplate mongoTemplate;

    public InventoryService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * MUST be implemented before going live.
     * The caller must be a logged-in buyer with isSeller() == true AND must own
     * `companyId`. Without this, any logged-in user can read or delete any
     * seller's inventory by sending a different companyId.
     */
    private void requireSellerOf(String companyId) {
        // TODO: read the authenticated buyer from your JWT / SecurityContext,
        //       check buyer.isSeller(), and check buyer's company == companyId.
        //       Throw an exception (→ 403) if either check fails.
    }

    /**
     * All active inventory rows for one seller company, newest first.
     */
    public List<Inventory> getInventory(String companyId) {
        requireSellerOf(companyId);

        Query query = Query.query(
                        Criteria.where("companyid").is(companyId)
                                .and("isActive").is(true))
                .with(Sort.by(Sort.Direction.DESC, "updated_at"));

        return mongoTemplate.find(query, Inventory.class);
    }

    /**
     * Soft delete: the inventory row and its batches are marked inactive, so
     * orders that reference them keep working. Re-adding the same product later
     * reactivates the row (AddBatchandInventory already calls setActive(true)).
     */
    @Transactional(rollbackFor = Exception.class)
    public AddProductResponse deleteInventory(String inventoryId, String companyId) {
        requireSellerOf(companyId);

        AddProductResponse response = new AddProductResponse();

        Inventory inventory = mongoTemplate.findOne(
                Query.query(Criteria.where("_id").is(inventoryId)
                        .and("companyid").is(companyId)),
                Inventory.class);

        if (inventory == null) {
            response.success = false;
            response.message = "Product not found";
            return response;
        }

        String now = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        inventory.setActive(false);
        inventory.setUpdated_at(now);
        mongoTemplate.save(inventory);

        // keep the batches collection in step with the inventory row
        mongoTemplate.updateMulti(
                Query.query(Criteria.where("productid").is(inventory.getProductid())
                        .and("companyid").is(companyId)),
                new Update().set("active", false),
                Batches.class);

        response.success = true;
        response.message = "Product deleted successfully";
        response.productId = inventory.getProductid() != null
                ? inventory.getProductid().toString() : null;
        return response;
    }


}

package Farme_rich.Seller.Repo.BackEnd;

import Farme_rich.Seller.Model.BackEnd.Inventory;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepo extends MongoRepository<Inventory, String> {
    List<Inventory> findByproductid(ObjectId productid);

    List<Inventory> findBycompanyid(String companyid);

    Inventory findByProductidAndCompanyid(ObjectId productid, String companyid);

    @Query("{ 'companyid': ?0, '_id': { $in: ?1 }}")
    List<Inventory> findByIdInAndCompanyId(String companyid, List<ObjectId> ids);

    @Query("{ 'productName': { $eq: ?0 }, 'companyid': { $eq: ?1 } }")
    Inventory findByProductName(String productName, String companyid);

    @Query("""
            {
              'companyid': ?0,
              'isActive': true,
              '$or': [
                { 'batchid.active': true },
                { 'variantBatches.active': true }
              ]
            }
            """)
    List<Inventory> findByCompanyIdAndBatchActive(String companyid);

    @Aggregation(pipeline = {
            "{ '$match': { 'companyid': ?0 } }",
            "{ '$match': { 'batchid': { '$ne': null } } }",
            "{ '$match': { 'batchid.stock_availability': { '$eq': 0 } } }",
            "{ '$sort': { 'updated_at': -1 } }",
            "{ '$limit': ?#{ [1] > 0 ? [1] : 999999999 } }"
    })
    List<Inventory> findBycompanyidAndOutOfStock(String companyid, int limit);

    //    @Aggregation(pipeline = {
//            "{ '$match': { 'companyid': ?0 } }",
//            "{ '$match': { 'productid': { '$in': ?1 } } }",
//            "{ '$match': { 'batchid': { '$ne': null } } }",
//            "{ '$match': { 'batchid.stock_availability': { '$gte': 0 } } }"
//    })
//    List<Inventory> findByCompanyIdAndProductidAndStockAvailability(String companyid,List<ObjectId> productIds);
    @Aggregation(pipeline = {
            "{ '$match': { 'companyid': ?0 } }",

            "{ '$match': { 'productid': { '$in': ?1 } } }",

            "{ '$match': { '$or': [ " +
                    "{ 'batchid.stock_availability': { '$gte': 0 } }, " +
                    "{ 'variantBatches.stock_availability': { '$gte': 0 } } " +
                    "] } }"
    })
    List<Inventory> findByCompanyIdAndProductidAndVariantStockAvailability(
            String companyid,
            List<ObjectId> productIds
    );


}

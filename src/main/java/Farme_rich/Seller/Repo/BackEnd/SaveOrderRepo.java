package Farme_rich.Seller.Repo.BackEnd;

import Farme_rich.Seller.DTO.Request.TopSellingProductDTO;
import Farme_rich.Seller.DTO.Request.TopSellingServiceDTO;
import Farme_rich.Seller.Model.BackEnd.Save;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface SaveOrderRepo extends MongoRepository<Save, String> {
    @Aggregation(pipeline = {
            "{ '$sort': { 'updatedAt': -1 } }",
    })
    List<Save> findByCompanyid(ObjectId companyid);

    @Aggregation(pipeline = {
            "{ '$match': { 'companyid': ?0,} }",
            "{ '$sort': { 'orderDate': -1 } }",
            "{ $limit: ?#{ [1] > 0 ? [1] : 999999999 } }"
    })
    List<Save> findTop5ByCompanyidOrderByOrderDateDesc(ObjectId companyid, int limit);

    @Aggregation(pipeline = {
            "{ '$match': { " +
                    "'companyid': ?0, " +
                    "'$or': [" +
                    "{ 'customerInformation.customerName': { $regex: ?1, $options: 'i' } }, " +
                    "{ 'customerInformation.customerMobileNum': { $regex: ?1, $options: 'i' } }, " +
                    "{ 'refno': { $regex: ?1, $options: 'i' } } " +
                    "] " +
                    "} }",
            "{ '$sort': { 'updatedAt': -1 } }",
            "{ '$limit': ?#{ [2] > 0 ? [2] : 999999999 } }"
    })
    List<Save> findByCompanyAndNameOrMobile(ObjectId companyid, String value, int limit);

    @Query("{ 'companyid': ?0, '_id': ?1 }")
    List<Save> findByCompanyidAndId(
            ObjectId companyid,
            ObjectId id
    );

    @Query("{ 'companyid': ?0, 'refno': { $regex: ?1, $options: 'i' } }")
    List<Save> findByCompanyidAndRefnoRegex(
            ObjectId companyid,
            String refno
    );

    @Aggregation(pipeline = {
            "{ '$match': { 'companyid': ?0, 'orderDate': { '$gte': ?1, '$lte': ?2 } } }",
            "{ '$unwind': '$seller_items' }",

            "{ '$match': { '$and': [ " +
                    "{ '$or': [ { 'seller_items.flag': null }, { 'seller_items.flag': '' } ] }, " +
                    "{ 'order_status': 'Completed' } " +
                    "] } }",

            "{ '$addFields': { 'seller_items': [ '$seller_items' ] } }",

            "{ '$sort': { 'orderDate': -1 } }",
            "{ $limit: ?#{ [3] > 0 ? [3] : 999999999 } }"
    })
    List<Save> getTop5ProductsBetweenDate(ObjectId companyid, Date from, Date to, int limit);


    @Aggregation(pipeline = {
            "{ '$match': { 'companyid': ?0, 'orderDate': { '$gte': ?1, '$lte': ?2 } } }",
            "{ '$match': { 'order_status': 'Completed' } }",
            "{ '$sort': { 'orderDate': -1 } }",
            "{ '$limit': ?#{ [3] > 0 ? [3] : 999999999 } }"
    })
    List<Save> getProductsAndServicesBetweenDate(ObjectId companyid, Date from, Date to, int limit);


    @Aggregation(pipeline = {
            "{ '$match': { 'companyid': ?0, 'orderDate': { '$gte': ?1, '$lte': ?2 } } }",
            "{ '$unwind': '$seller_items' }",

            "{ '$match': { '$and': [ " +
                    "{ 'seller_items.flag': 'isService' }, " +
                    "{ 'order_status': 'Completed' } " +
                    "] } }",

            "{ '$addFields': { 'seller_items': [ '$seller_items' ] } }",

            "{ '$sort': { 'orderDate': -1 } }",
            "{ $limit: ?#{ [3] > 0 ? [3] : 999999999 } }"
    })
    List<Save> getTop5ServicesBetweenDate(
            ObjectId companyid,
            Date from,
            Date to,
            int limit
    );

    @Aggregation(pipeline = {
            "{ $match: { companyid: ?0, orderDate: { $gte: ?1, $lte: ?2 } } }",
            "{ $unwind: '$seller_items' }",
            "{ $match: { 'seller_items.batchid': { $ne: null } } }",
            "{ $match: { order_status: 'Completed' } }",
            "{ $group: { " +
                    "_id: '$seller_items.batchid.productname', " +
                    "totalQuantity: { $sum: '$seller_items.Order_quantity' }, " +
                    "totalSales: { $sum: { $multiply: [ '$seller_items.Order_quantity', { $toDouble: '$seller_items.batchid.seller_price' } ] } } " +
                    "} }",
            "{ $project: { " +
                    "productName: '$_id', " +
                    "totalQuantity: 1, " +
                    "totalSales: 1, " +
                    "_id: 0 } }",
            "{ $sort: { totalSales: -1 } }",
            "{ $limit: ?#{ [3] > 0 ? [3] : 999999999 } }"
    })
    List<TopSellingProductDTO> getTopSellingProducts(ObjectId companyid, Date from, Date to, int limit);

    @Aggregation(
            pipeline = {
                    "{ $match: { companyid: ?0, orderDate: { $gte: ?1, $lte: ?2 } } }",
                    "{ $unwind: '$seller_items' }",

                    "{ $match: { " +
                            "'seller_items.flag': 'isService', " +
                            "'order_status': 'Completed' " +
                            "} }",

                    "{ $group: { " +
                            "_id: '$seller_items.servicename', " +
                            "totalQuantity: { $sum: '$seller_items.Order_quantity' }, " +
                            "totalSales: { $sum: { $multiply: [ " +
                            "'$seller_items.Order_quantity', " +
                            "{ $toDouble: '$seller_items.servicecost' } " +
                            "] } } " +
                            "} }",

                    "{ $project: { " +
                            "serviceName: '$_id', " +
                            "totalQuantity: 1, " +
                            "totalSales: 1, " +
                            "_id: 0 } " +
                            "}",

                    "{ $sort: { totalSales: -1 } }",
                    "{ $limit: ?#{ [3] > 0 ? [3] : 999999999 } }"
            }
    )
    List<TopSellingServiceDTO> getTopSellingService(ObjectId companyid, Date from, Date to, int limit);

    List<Save> findByOrderStatus(String orderStatus);

    @Aggregation(pipeline = {
            "{ '$match': { 'buyerid._id': ?0 } }",
            "{ '$sort': { 'updatedAt': -1 } }"
    })
    List<Save> findByBuyerId(ObjectId buyerid);

    @Query("{ 'companyid': ?0, 'orderSource': 'INOCK' }")
    List<Save> findAppOrdersByCompanyid(ObjectId companyid);

    @Query("{ 'companyid': ?0, 'orderSource': { $regex: '^WEB', $options: 'i' } }")
    List<Save> findWebOrdersByCompanyid(ObjectId companyid);
}

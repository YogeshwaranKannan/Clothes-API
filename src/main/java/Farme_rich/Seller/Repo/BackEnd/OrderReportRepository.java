package Farme_rich.Seller.Repo.BackEnd;


import Farme_rich.Seller.Model.BackEnd.Save;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.List;

public interface OrderReportRepository extends MongoRepository<Save, ObjectId> {

    @Query("""
            {
                'companyid': ?0,
                'orderDate': { '$gte': ?1 },
                'orderStatus': {
                    '$nin': ['Draft', 'Cancelled']
                }
            }
            """)
    List<Save> findByCompanyidAndOrderDateGreaterThanEqualOrderByOrderDateDesc(
            ObjectId companyid,
            Date lastDate
    );


    @Query("""
            {
                'companyid': ?0,
                'orderDate': {
                    '$gte': ?1,
                    '$lte': ?2
                },
                'orderStatus': {
                   '$nin': ['Draft', 'Cancelled']
                }
            }
            """)
    List<Save> findByCompanyidAndOrderDateBetweenOrderByOrderDateDesc(
            ObjectId companyId,
            Date fromDate,
            Date toDate
    );
}
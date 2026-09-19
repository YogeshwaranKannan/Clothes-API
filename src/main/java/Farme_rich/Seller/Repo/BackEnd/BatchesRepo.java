package Farme_rich.Seller.Repo.BackEnd;


import Farme_rich.Seller.Model.BackEnd.Batches;


import Farme_rich.Seller.Model.BackEnd.Inventory;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatchesRepo extends MongoRepository<Batches, String> {

    Optional<Batches> findById(String _id);

    List<Batches> findByProductidAndCompanyid(ObjectId productid, String companyid, Sort sort);

    @Query("{ '_id': { $in: ?0 } }")
    List<Batches> findBatchesByIds(List<ObjectId> ids);

    @Query("{ 'productid': ?0, 'companyid': ?1, 'variantName': ?2  , 'active': true}")
    Batches findByProductidAndCompanyidAndVariantName(
            ObjectId productid,
            String companyid,
            String variantName
    );
    @Query("{ 'productid': ?0, 'companyid': ?1, 'variantName': ?2  }")
    List<Batches> findByListProductidAndCompanyidAndVariantName(
            ObjectId productid,
            String companyid,
            String variantName
    );


    Optional<Batches> findTopByCompanyidOrderByCreatedAtDesc(String companyid);
    void deleteBy_idIn(List<ObjectId> ids);




}

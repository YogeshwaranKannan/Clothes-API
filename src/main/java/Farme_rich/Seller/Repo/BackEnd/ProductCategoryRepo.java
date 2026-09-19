package Farme_rich.Seller.Repo.BackEnd;

import Farme_rich.Seller.Model.BackEnd.ProductCategory;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCategoryRepo extends MongoRepository<ProductCategory, String> {

    @Query("{ 'sellerids': ?0 }")
    List<ProductCategory> findBySellerids(ObjectId sellerid);

    @Query("{ '_id': ?0 }")
    ProductCategory findByCategoryID(ObjectId _id);

    @Query("{ 'parent_id' : null }")
    List<ProductCategory> findTopLevelCategories();

    @Query("{ 'parent_id' : ?0 }")
    List<ProductCategory> findByParentId(ObjectId parentId);
}

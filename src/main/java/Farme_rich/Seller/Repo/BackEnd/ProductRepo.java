package Farme_rich.Seller.Repo.BackEnd;

import Farme_rich.Seller.Model.BackEnd.Products;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepo extends MongoRepository<Products, String> {

    @Query("{ 'productName': { $regex: ?0, $options: 'i' } }")
    List<Products> searchByProductName(String productName);

    List<Products> findByProductNameContainingIgnoreCase(String productName);

    List<Products> findByBrandContainingIgnoreCase(String brand);

    @Query("{'_id': ?0}")
    public Products searchById(ObjectId id);

    @Query("{ 'sellerLists': { $elemMatch: { 'companyid': ?0 } } }")
    List<Products> findByCompanyIdInSellerLists(ObjectId companyid);

    @Query("{ 'productName': { $regex: ?0, $options: 'i' }, " +
            "'brand': { $regex: ?1, $options: 'i' }, " +
            "'productCategory._id': ?2 }")
    Products findByProductNameAndBrandAndCategory(
            String productName,
            String brand,
            ObjectId _id
    );

    @Query("{ 'productName': ?0, 'brand': ?1 }")
    Products findByProductNameAndBrand(
            String productName,
            String brand
    );

    @Query("{ 'productName': ?0}")
    Products findByProductName(
            String productName
    );
}

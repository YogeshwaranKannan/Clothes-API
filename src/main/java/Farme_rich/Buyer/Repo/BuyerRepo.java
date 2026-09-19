package Farme_rich.Buyer.Repo;

import Farme_rich.Buyer.Model.Buyer;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface BuyerRepo extends MongoRepository<Buyer, ObjectId> {
    Buyer findByEmail(String email);

    Buyer findByMobileNum(String mobileNum);

    @Query("{ 'mobileNum': ?0 }")
    List<Buyer> findByMobile(String mobileNum);

    @Query("{ 'mobileNum': ?0, 'buyerUseSites.sellerid': ?1 }")
    Buyer findByMobileNumAndBuyerSiteid(String mobileNum, ObjectId id);

    @Query("{ 'mobileNum': { $regex: ?0, $options: 'i' }, 'buyerUseSites.sellerid': ?1 }")
    List<Buyer> findByMobileNumContainingAndBuyerSiteid(String mobileNum, ObjectId id);

    @Query("{ 'firstName': { $regex: ?0, $options: 'i' }, 'buyerUseSites.sellerid': ?1 }")
    List<Buyer> findByCustomerNameContainingAndBuyerSiteid(String name, ObjectId id);

}

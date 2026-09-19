package Farme_rich.Seller.Repo.BackEnd;


import Farme_rich.Seller.Model.BackEnd.QICustomers;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface QICustomersRepo extends MongoRepository<QICustomers, ObjectId> {
    @Query("{ 'mobileNum': { $regex: '^?0', $options: 'i' } }")
    List<QICustomers> findByMobileNumStartingWith(String mobileNum);
    QICustomers findByMobileNum(String mobileNum);
}

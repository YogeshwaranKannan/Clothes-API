package Farme_rich.Seller.Repo.FrontEnd;


import Farme_rich.Seller.Model.FrontEnd.Seller;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface SellerRepo extends MongoRepository<Seller, String> {
    @Query(value = "{ 'userid': ?0 }", fields = "{ 'pickupPoints': 1 }")
    Seller findByUserid(String userid);

    Seller findByUseridContaining(String userid);
    Seller findByCompanyname(String companyname);


}

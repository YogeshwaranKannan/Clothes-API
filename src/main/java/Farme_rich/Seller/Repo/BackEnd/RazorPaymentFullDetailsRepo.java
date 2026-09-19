package Farme_rich.Seller.Repo.BackEnd;

import Farme_rich.Seller.Model.BackEnd.RazorPaymentFullDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RazorPaymentFullDetailsRepo extends MongoRepository<RazorPaymentFullDetails,String> {
    @Query("{ 'order_id': ?0}")
    public RazorPaymentFullDetails findByOrderID(String receipt);
}

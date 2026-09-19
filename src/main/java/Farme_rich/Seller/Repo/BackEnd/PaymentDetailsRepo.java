package Farme_rich.Seller.Repo.BackEnd;


import Farme_rich.Seller.Model.BackEnd.PaymentDetails;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentDetailsRepo extends MongoRepository<PaymentDetails, String> {

    PaymentDetails findByOrderId(ObjectId orderId);
}


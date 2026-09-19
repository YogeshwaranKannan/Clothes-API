package Farme_rich.Seller.Repo.BackEnd;


import Farme_rich.Seller.Model.BackEnd.CustomerInformation;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CustomerInfoRepo extends MongoRepository<CustomerInformation, ObjectId> {

    CustomerInformation findByCustomerNameAndCustomerMobileNum(
            String customerName,
            String customerMobileNum
    );
}

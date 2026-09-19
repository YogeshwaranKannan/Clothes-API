package Farme_rich.Seller.Repo.FrontEnd;

import Farme_rich.Seller.Model.FrontEnd.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerUserRepo extends MongoRepository<User, String> {
    User findBymobileNum(String mobileNum);
    User findBydeviceId(String deviceId);
}

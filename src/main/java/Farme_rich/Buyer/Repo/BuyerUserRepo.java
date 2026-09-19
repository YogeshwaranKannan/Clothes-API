package Farme_rich.Buyer.Repo;

import Farme_rich.Buyer.Model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuyerUserRepo extends MongoRepository<User, String> {
    // Query method to find user by mobile number
    User findBymobileNum(String mobileNum);
    User findBydeviceId(String deviceId);
    User findByid(String id);

//    // Correct update methods using MongoTemplate (recommended approach)
//
//    @Transactional
//    @Query("{ 'id' : ?0 }")
//    void updateMobileNoById(@Param("id") String id, @Param("newMobileNo") String newMobileNo);
//
//
//    @Transactional
//    @Query("{ 'id' : ?0 }")
//    void updatePinById(@Param("id") String id, @Param("newPin") String newPin);
//
//
//    @Transactional
//    @Query("{ 'id' : ?0 }")
//    void updateDeviceById(@Param("id") String id, @Param("newDeviceId") String newDeviceId);


}


package Farme_rich.Revolt.Repo;

import Farme_rich.Revolt.Model.RevoltUser;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RevoltUserRepo extends MongoRepository<RevoltUser, ObjectId> {
    RevoltUser findBydeviceId(String deviceId);

    RevoltUser findBymobileNum(String mobileNum);
}

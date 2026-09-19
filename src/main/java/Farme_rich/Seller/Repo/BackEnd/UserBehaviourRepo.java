package Farme_rich.Seller.Repo.BackEnd;

import Farme_rich.Seller.Model.FrontEnd.UserBehaviour;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserBehaviourRepo extends MongoRepository<UserBehaviour, ObjectId> {

}

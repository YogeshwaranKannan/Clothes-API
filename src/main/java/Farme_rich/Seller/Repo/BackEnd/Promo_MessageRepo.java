package Farme_rich.Seller.Repo.BackEnd;

import Farme_rich.Seller.Model.BackEnd.Promo_messages;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Promo_MessageRepo extends MongoRepository<Promo_messages, ObjectId> {
    public Promo_messages  findBysellerid(ObjectId sellerid);
}

package Farme_rich.Seller.Repo.BackEnd;

import Farme_rich.Seller.Model.BackEnd.Sale_features;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalefeaturesRepo extends MongoRepository<Sale_features, ObjectId> {

     public List<Sale_features> findBysellerid(ObjectId sellerid);

}

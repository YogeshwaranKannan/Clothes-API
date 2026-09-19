package Farme_rich.Seller.Repo.BackEnd;

import Farme_rich.Seller.Model.BackEnd.AcceptReviewDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AcceptReviewDetailsRepo extends MongoRepository<AcceptReviewDetails, String> {
}

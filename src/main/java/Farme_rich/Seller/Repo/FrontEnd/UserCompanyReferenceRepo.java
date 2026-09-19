package Farme_rich.Seller.Repo.FrontEnd;

 
import Farme_rich.Seller.Model.FrontEnd.UserCompanyReference;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserCompanyReferenceRepo extends MongoRepository<UserCompanyReference, String> {
}

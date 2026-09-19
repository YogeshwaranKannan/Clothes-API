package Farme_rich.Seller.Repo.BackEnd;

import Farme_rich.Seller.Model.BackEnd.ServiceInventory;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ServiceInventoryRepo extends MongoRepository<ServiceInventory, ObjectId> {
    List<ServiceInventory> findBySellerid(ObjectId sellerid);

    List<ServiceInventory> findBySelleridAndIsActive(ObjectId sellerid, boolean isActive);

    ServiceInventory findByServicename(String name);

    @Query("{ 'servicename': { $eq: ?0 }, 'sellerid': { $eq: ?1 } }")
    ServiceInventory findByServicenameAndSellerid(String name, ObjectId sellerid);


}

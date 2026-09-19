package Farme_rich.Revolt.Repo;

import Farme_rich.Revolt.Model.Charger;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChargerRepo extends MongoRepository<Charger, ObjectId> {

    List<Charger> findByStationId(ObjectId stationId);

}

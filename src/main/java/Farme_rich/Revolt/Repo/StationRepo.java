package Farme_rich.Revolt.Repo;

import Farme_rich.Revolt.Model.Stations;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StationRepo extends MongoRepository<Stations, ObjectId> {
    @Query("{ 'station_Address' : { $regex: ?0, $options: 'i' } }")
    List<Stations> findByStationAddress(String address);
}

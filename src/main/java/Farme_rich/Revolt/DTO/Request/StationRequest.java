package Farme_rich.Revolt.DTO.Request;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StationRequest {
    public ObjectId stationId;
    public String searchSimilarityByAddress;
}

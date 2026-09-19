package Farme_rich.Revolt.DTO.Request;

import Farme_rich.ObjectIdDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.bson.types.ObjectId;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReadStationsFromJSON {
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    public ObjectId _id;
    public String station_Name;
    public String station_Address;

}

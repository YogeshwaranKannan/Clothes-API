package Farme_rich.Buyer.DTO.Request;

import Farme_rich.ObjectIdDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.bson.types.ObjectId;
@JsonIgnoreProperties(ignoreUnknown = true)
public class SuggestProductCategoryDTO {
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    public ObjectId _id;
    @JsonProperty("category_name")
    public String categoryName;
}



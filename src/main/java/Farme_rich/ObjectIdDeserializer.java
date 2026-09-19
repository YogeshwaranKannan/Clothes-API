package Farme_rich;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.bson.types.ObjectId;

import java.io.IOException;

public class ObjectIdDeserializer extends JsonDeserializer<ObjectId> {

    @Override
    public ObjectId deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {

        JsonNode node = p.getCodec().readTree(p);

        if (node == null || node.isNull()) {
            return null;
        }

        // Extended JSON form: {"seller_id": {"$oid": "..."}}
        if (node.has("$oid")) {
            String oid = node.get("$oid").asText();
            return isValidObjectId(oid) ? new ObjectId(oid) : null;
        }

        // Plain string form: {"seller_id": "6954d2dfa8cca61b24a2ea12"}
        if (node.isTextual()) {
            String text = node.asText();
            return isValidObjectId(text) ? new ObjectId(text) : null;
        }

        return null;
    }

    private boolean isValidObjectId(String value) {
        return value != null && ObjectId.isValid(value);
    }
}
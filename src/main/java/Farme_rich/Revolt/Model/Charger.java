package Farme_rich.Revolt.Model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Charger")
public class Charger {
    @Id
    private ObjectId _id;
    private ObjectId stationId;
    private ChargerDetails chargerDetails;

    public Charger() {
    }

    public Charger(ObjectId _id, ObjectId stationId, ChargerDetails chargerDetails) {
        this._id = _id;
        this.stationId = stationId;
        this.chargerDetails = chargerDetails;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public ObjectId getStationId() {
        return stationId;
    }

    public void setStationId(ObjectId stationId) {
        this.stationId = stationId;
    }

    public ChargerDetails getChargerDetails() {
        return chargerDetails;
    }

    public void setChargerDetails(ChargerDetails chargerDetails) {
        this.chargerDetails = chargerDetails;
    }

    public static class ChargerDetails {
        private String charger_type;
        private int max_power;
        private int max_voltage;
        private int max_current;
        private String status;

        public ChargerDetails() {
        }

        public ChargerDetails(String charger_type, int max_power, int max_voltage, int max_current, String status) {
            this.charger_type = charger_type;
            this.max_power = max_power;
            this.max_voltage = max_voltage;
            this.max_current = max_current;
            this.status = status;
        }

        public String getCharger_type() {
            return charger_type;
        }

        public void setCharger_type(String charger_type) {
            this.charger_type = charger_type;
        }

        public int getMax_power() {
            return max_power;
        }

        public void setMax_power(int max_power) {
            this.max_power = max_power;
        }

        public int getMax_voltage() {
            return max_voltage;
        }

        public void setMax_voltage(int max_voltage) {
            this.max_voltage = max_voltage;
        }

        public int getMax_current() {
            return max_current;
        }

        public void setMax_current(int max_current) {
            this.max_current = max_current;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}

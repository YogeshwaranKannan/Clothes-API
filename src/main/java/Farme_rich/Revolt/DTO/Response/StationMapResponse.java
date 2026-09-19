package Farme_rich.Revolt.DTO.Response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class StationMapResponse {

    public String stationId;
    public String stationName;
    public String stationAddress;

    public double latitude;
    public double longitude;

    public List<ChargerDetailsDTO> chargers;

    public static class ChargerDetailsDTO {

        public String charger_type;
        public int max_power;
        public int max_voltage;
        public int max_current;
        public String status;

        public ChargerDetailsDTO() {
        }

        public ChargerDetailsDTO(String charger_type, int max_power, int max_voltage, int max_current, String status) {
            this.charger_type = charger_type;
            this.max_power = max_power;
            this.max_voltage = max_voltage;
            this.max_current = max_current;
            this.status = status;
        }
    }
}

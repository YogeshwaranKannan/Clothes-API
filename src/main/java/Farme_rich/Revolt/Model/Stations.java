package Farme_rich.Revolt.Model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "Stations")
public class Stations {
    @Id
    private ObjectId _id;
    private String station_Name;
    private String station_Address;
    private LocationPoints locationPoints;
    private LocalDateTime created_At;
    private LocalDateTime updated_At;

    public Stations() {
    }

    public Stations(ObjectId _id, String station_Name, String station_Address, LocationPoints locationPoints, LocalDateTime created_At, LocalDateTime updated_At) {
        this._id = _id;
        this.station_Name = station_Name;
        this.station_Address = station_Address;
        this.locationPoints = locationPoints;
        this.created_At = created_At;
        this.updated_At = updated_At;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getStation_Name() {
        return station_Name;
    }

    public void setStation_Name(String station_Name) {
        this.station_Name = station_Name;
    }

    public String getStation_Address() {
        return station_Address;
    }

    public void setStation_Address(String station_Address) {
        this.station_Address = station_Address;
    }

    public LocationPoints getLocationPoints() {
        return locationPoints;
    }

    public void setLocationPoints(LocationPoints locationPoints) {
        this.locationPoints = locationPoints;
    }

    public LocalDateTime getCreated_At() {
        return created_At;
    }

    public void setCreated_At(LocalDateTime created_At) {
        this.created_At = created_At;
    }

    public LocalDateTime getUpdated_At() {
        return updated_At;
    }

    public void setUpdated_At(LocalDateTime updated_At) {
        this.updated_At = updated_At;
    }

    public static class LocationPoints {

        private double latitude;
        private double longitude;

        public LocationPoints() {
        }

        public LocationPoints(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }


}

package Farme_rich.Seller.Model.FrontEnd;

import java.util.List;

public class PickupPoints {
    public Address address;
    public List<String> time_slot;
    public boolean active;



    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<String> getTime_slot() {
        return time_slot;
    }

    public void setTime_slot(List<String> time_slot) {
        this.time_slot = time_slot;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

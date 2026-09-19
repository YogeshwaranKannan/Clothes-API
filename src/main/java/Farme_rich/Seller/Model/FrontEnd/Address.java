package Farme_rich.Seller.Model.FrontEnd;

public class Address {
    //{address line1, addressline2, suburb,state,postcode}
    public String PickupPointname;
    public String address_line1;
    public String address_line2;
    public String city;
    public String pincode;


    public String getPickupPointname() {
        return PickupPointname;
    }

    public void setPickupPointname(String pickupPointname) {
        this.PickupPointname = pickupPointname;
    }

    public String getAddress_line1() {
        return address_line1;
    }

    public void setAddress_line1(String address_line1) {
        this.address_line1 = address_line1;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress_line2() {
        return address_line2;
    }

    public void setAddress_line2(String address_line2) {
        this.address_line2 = address_line2;
    }



    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }
}

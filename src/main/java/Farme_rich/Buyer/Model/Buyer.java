package Farme_rich.Buyer.Model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "Buyer")
public class Buyer {
    @Id
    private String id;

    private String firstName; // First name of the user
    private String lastName;
    private String email;
    private String mobileNum;
    private String password;
    private List<LastDeliveryAddess> lastDeliveryAddesses;
    private List<String> roles;
    private BuyerUseSites buyerUseSites;
    private Rewards rewards;
    private boolean isUser;
    private Date createAT;
    private Date updateAT;


    public Buyer(String firstName, String lastName, String email, String mobileNum, String password, Date createAT, Date updateAT) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.mobileNum = mobileNum;
        this.password = password;
        this.createAT = createAT;
        this.updateAT = updateAT;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNum() {
        return mobileNum;
    }

    public void setMobileNum(String mobileNum) {
        this.mobileNum = mobileNum;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<LastDeliveryAddess> getLastDeliveryAddesses() {
        return lastDeliveryAddesses;
    }

    public void setLastDeliveryAddesses(List<LastDeliveryAddess> lastDeliveryAddesses) {
        this.lastDeliveryAddesses = lastDeliveryAddesses;
    }

    public Rewards getRewards() {
        return rewards;
    }

    public void setRewards(Rewards rewards) {
        this.rewards = rewards;
    }

    public Date getCreateAT() {
        return createAT;
    }

    public void setCreateAT(Date createAT) {
        this.createAT = createAT;
    }

    public Date getUpdateAT() {
        return updateAT;
    }

    public void setUpdateAT(Date updateAT) {
        this.updateAT = updateAT;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public BuyerUseSites getBuyerUseSites() {
        return buyerUseSites;
    }

    public void setBuyerUseSites(BuyerUseSites buyerUseSites) {
        this.buyerUseSites = buyerUseSites;
    }

    @Override
    public String toString() {
        return "Buyer{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", mobileNum='" + mobileNum + '\'' +
                ", password='" + password + '\'' +
                ", createAT=" + createAT +
                ", updateAT=" + updateAT +
                '}';
    }

    public static class BuyerUseSites {
        public ObjectId sellerid;
        public String companyname;

        public BuyerUseSites() {
        }

        public BuyerUseSites(ObjectId sellerid, String companyname) {
            this.sellerid = sellerid;
            this.companyname = companyname;
        }
    }

    public static class Rewards {
        private int totalRewards;
        private int lastEarned;
        private int lastRedeemed;
        private int currentRewards;
        private int totalRedeemed;

        public Rewards() {
        }

        public Rewards(int totalRewards, int lastEarned, int lastRedeemed, int currentRewards, int totalRedeemed) {
            this.totalRewards = totalRewards;
            this.lastEarned = lastEarned;
            this.lastRedeemed = lastRedeemed;
            this.currentRewards = currentRewards;
            this.totalRedeemed = totalRedeemed;
        }

        public int getTotalRewards() {
            return totalRewards;
        }

        public void setTotalRewards(int totalRewards) {
            this.totalRewards = totalRewards;
        }

        public int getLastEarned() {
            return lastEarned;
        }

        public void setLastEarned(int lastEarned) {
            this.lastEarned = lastEarned;
        }

        public int getLastRedeemed() {
            return lastRedeemed;
        }

        public void setLastRedeemed(int lastRedeemed) {
            this.lastRedeemed = lastRedeemed;
        }

        public int getTotalRedeemed() {
            return totalRedeemed;
        }

        public void setTotalRedeemed(int totalRedeemed) {
            this.totalRedeemed = totalRedeemed;
        }

        public int getCurrentRewards() {
            return currentRewards;
        }

        public void setCurrentRewards(int currentRewards) {
            this.currentRewards = currentRewards;
        }
    }

    public static class LastDeliveryAddess {

        public String Address;
        public String city;
        public String postalCode;
        public String state;

        public LastDeliveryAddess() {
        }

        public LastDeliveryAddess(String address, String city, String postalCode, String state) {

            Address = address;
            this.city = city;
            this.postalCode = postalCode;
            this.state = state;
        }
    }
}

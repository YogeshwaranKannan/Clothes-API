package Farme_rich.Seller.Model.FrontEnd;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "Seller_User")  // employess
public class User {
        @Id
        private String id; // MongoDB's unique identifier (_id)
        private String firstname; // First name of the user
        private String lastname;
        private String companyid;
        private String createAT;
        private String updateAT;
        private boolean isActive;
        private String language_preferred;
        private String email;
        @Indexed(unique = true)
        private String mobileNum; // Mobile number, must be unique
        private List<String> role;
        private String firstPiN; // PIN for user authentication
        //    @Indexed(unique = true)
        private String deviceId;
        private boolean paidUser ;

        // Getters and Setters
        public String getId() {
                return id;
        }

        public void setId(String id) {
                this.id = id;
        }

        public String getFirstname() {
                return firstname;
        }

        public void setFirstname(String firstname) {
                this.firstname = firstname;
        }

        public String getLastname() {
                return lastname;
        }

        public void setLastname(String lastname) {
                this.lastname = lastname;
        }

        public String getCompanyid() {
                return companyid;
        }

        public void setCompanyid(String companyid) {
                this.companyid = companyid;
        }

        public String getMobileNum() {
                return mobileNum;
        }

        public void setMobileNum(String mobileNum) {
                this.mobileNum = mobileNum;
        }

        public String getFirstPiN() {
                return firstPiN;
        }

        public void setFirstPiN(String firstPiN) {
                this.firstPiN = firstPiN;
        }
        public String getDeviceId() {
                return deviceId;
        }

        public String getCreateAT() {
                return createAT;
        }

        public void setCreateAT(String createAT) {
                this.createAT = createAT;
        }

        public String getUpdateAT() {
                return updateAT;
        }

        public void setUpdateAT(String updateAT) {
                this.updateAT = updateAT;
        }

        public boolean isActive() {
                return isActive;
        }

        public void setActive(boolean active) {
                isActive = active;
        }

        public String getLanguage_preferred() {
                return language_preferred;
        }

        public void setLanguage_preferred(String language_preferred) {
                this.language_preferred = language_preferred;
        }

        public void setDeviceId(String deviceId) {
                this.deviceId = deviceId;
        }

        public boolean isPaidUser() {
                return paidUser;
        }

        public void setPaidUser(boolean paidUser) {
                this.paidUser = paidUser;
        }

        public List<String> getRole() {
                return role;
        }

        public void setRole(List<String> role) {
                this.role = role;
        }

        public String getEmail() {
                return email;
        }

        public void setEmail(String email) {
                this.email = email;
        }
}

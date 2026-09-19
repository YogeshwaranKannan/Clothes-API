package Farme_rich.Security;

public class MobileUtils {
    public String maskMobileNumber(String mobileNum) {
        if (mobileNum.length() >= 10) {
            return "XXXXXX" + mobileNum.substring(6);
        }
        return mobileNum;
    }

    public String unmaskMobileNumber(String maskedNum, String originalNum) {
        if (maskedNum.startsWith("XXXXXX") && originalNum.length() >= 10) {
            return originalNum;
        }
        return maskedNum;
    }

}

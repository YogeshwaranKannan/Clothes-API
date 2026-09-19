package Farme_rich.Seller.Model.BackEnd;

import java.util.Date;

public class AcceptReviewInfo {
    private String reason;
    private Date action_date;
    private double refund_amount;
    private double customer_need_to_pay;
    private String refund_payment_mode;
    private String refund_status;


    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Date getAction_date() {
        return action_date;
    }

    public void setAction_date(Date action_date) {
        this.action_date = action_date;
    }

    public double getRefund_amount() {
        return refund_amount;
    }

    public void setRefund_amount(double refund_amount) {
        this.refund_amount = refund_amount;
    }

    public String getRefund_status() {
        return refund_status;
    }

    public void setRefund_status(String refund_status) {
        this.refund_status = refund_status;
    }

    public String getRefund_payment_mode() {
        return refund_payment_mode;
    }

    public void setRefund_payment_mode(String refund_payment_mode) {
        this.refund_payment_mode = refund_payment_mode;
    }

    public double getCustomer_need_to_pay() {
        return customer_need_to_pay;
    }

    public void setCustomer_need_to_pay(double customer_need_to_pay) {
        this.customer_need_to_pay = customer_need_to_pay;
    }
}

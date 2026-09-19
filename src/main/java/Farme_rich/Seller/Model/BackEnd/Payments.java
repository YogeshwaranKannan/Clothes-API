package Farme_rich.Seller.Model.BackEnd;

public class Payments {

    private double total_amount;
    private double paid_amount;
    private String payment_mode;
    private double Current_payment_amount;
    private String payment_date;
    private double discount;
    private int ongoing_installment;
    private boolean isEMI;
    private boolean isAdvance;
    private double amountDue;
    private double advanceAmt;
    private double outstanding_amount;
    private String payment_status;

    private String next_payment_due;
    private int no_of_installments;
    private String reference_number;
    private String Order_created_At;

    public double getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(double total_amount) {
        this.total_amount = total_amount;
    }

    public double getCurrent_payment_amount() {
        return Current_payment_amount;
    }

    public void setCurrent_payment_amount(double current_payment_amount) {
        Current_payment_amount = current_payment_amount;
    }

    public double getPaid_amount() {
        return paid_amount;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getAmountDue() {
        return amountDue;
    }

    public void setAmountDue(double amountDue) {
        this.amountDue = amountDue;
    }

    public void setPaid_amount(double paid_amount) {
        this.paid_amount = paid_amount;
    }

    public String getPayment_date() {
        return payment_date;
    }

    public void setPayment_date(String payment_date) {
        this.payment_date = payment_date;
    }

    public int getOngoing_installment() {
        return ongoing_installment;
    }

    public void setOngoing_installment(int ongoing_installment) {
        this.ongoing_installment = ongoing_installment;
    }

    public boolean isEMI() {
        return isEMI;
    }

    public void setEMI(boolean EMI) {
        isEMI = EMI;
    }

    public double getOutstanding_amount() {
        return outstanding_amount;
    }

    public void setOutstanding_amount(double outstanding_amount) {
        this.outstanding_amount = outstanding_amount;
    }

    public String getPayment_status() {
        return payment_status;
    }

    public void setPayment_status(String payment_status) {
        this.payment_status = payment_status;
    }



    public String getNext_payment_due() {
        return next_payment_due;
    }

    public void setNext_payment_due(String next_payment_due) {
        this.next_payment_due = next_payment_due;
    }

    public int getNo_of_installments() {
        return no_of_installments;
    }

    public void setNo_of_installments(int no_of_installments) {
        this.no_of_installments = no_of_installments;
    }

    public String getReference_number() {
        return reference_number;
    }

    public void setReference_number(String reference_number) {
        this.reference_number = reference_number;
    }

    public String getOrder_created_At() {
        return Order_created_At;
    }

    public void setOrder_created_At(String order_created_At) {
        Order_created_At = order_created_At;
    }

    public boolean isAdvance() {
        return isAdvance;
    }

    public void setAdvance(boolean advance) {
        isAdvance = advance;
    }

    public double getAdvanceAmt() {
        return advanceAmt;
    }

    public void setAdvanceAmt(double advanceAmt) {
        this.advanceAmt = advanceAmt;
    }

    public String getPayment_mode() {
        return payment_mode;
    }

    public void setPayment_mode(String payment_mode) {
        this.payment_mode = payment_mode;
    }
}

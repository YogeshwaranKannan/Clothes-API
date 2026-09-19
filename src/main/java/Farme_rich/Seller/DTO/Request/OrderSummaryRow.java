package Farme_rich.Seller.DTO.Request;


import org.bson.types.ObjectId;

public class OrderSummaryRow {

    private String customerName;
    private String customerMobile;
    private ObjectId _id;
    private String refNo;
    private double orderValue;
    private double amountPaid;
    private double amountDue;
    private String orderDate;
    private String orderStatus;
    private String paymentMode;
    private String paymentStatus;
    private String deliveryDate;

    public OrderSummaryRow() {
    }

    public OrderSummaryRow(String customerName, String customerMobile, ObjectId _id, String refNo,
                           double orderValue, double amountPaid, double amountDue,
                           String orderDate, String orderStatus, String paymentStatus,
                           String deliveryDate, String paymentMode) {
        this.customerName = customerName;
        this.customerMobile = customerMobile;
        this._id = _id;
        this.refNo = refNo;
        this.orderValue = orderValue;
        this.amountPaid = amountPaid;
        this.amountDue = amountDue;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.paymentStatus = paymentStatus;
        this.deliveryDate = deliveryDate;
        this.paymentMode = paymentMode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerMobile() {
        return customerMobile;
    }

    public void setCustomerMobile(String customerMobile) {
        this.customerMobile = customerMobile;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getRefNo() {
        return refNo;
    }

    public void setRefNo(String refNo) {
        this.refNo = refNo;
    }

    public double getOrderValue() {
        return orderValue;
    }

    public void setOrderValue(double orderValue) {
        this.orderValue = orderValue;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public double getAmountDue() {
        return amountDue;
    }

    public void setAmountDue(double amountDue) {
        this.amountDue = amountDue;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }
}
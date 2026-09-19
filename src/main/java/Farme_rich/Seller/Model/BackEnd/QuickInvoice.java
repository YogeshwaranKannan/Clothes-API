package Farme_rich.Seller.Model.BackEnd;


import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "quickinvoice")
public class QuickInvoice {
    @Id
    private ObjectId _id;
    private ObjectId qiCustomer_id;
    private ObjectId companyid;
    private List<QuickProducts> quickProducts;
    private int totalitems;
    private int ordervalue;
    private String quickinvoice_reference;

    private String orderDate;
    private String updatedAt;

    private String note;
    private  String paymentmode;
    private int amountdue;
    private int amountpaid;
    private int discount;
    private int balance;

    //option add pannikittean
    private String cgst;
    private String sgst;
    private String gstTotal;
    private String payment;
    private String delivery;
    private String delivery_mobileNum;
    private String customer_name;

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public ObjectId getQiCustomer_id() {
        return qiCustomer_id;
    }

    public void setQiCustomer_id(ObjectId qiCustomer_id) {
        this.qiCustomer_id = qiCustomer_id;
    }

    public ObjectId getCompanyid() {
        return companyid;
    }

    public void setCompanyid(ObjectId companyid) {
        this.companyid = companyid;
    }

    public List<QuickProducts> getQuickProducts() {
        return quickProducts;
    }

    public void setQuickProducts(List<QuickProducts> quickProducts) {
        this.quickProducts = quickProducts;
    }

    public int getTotalitems() {
        return totalitems;
    }

    public String getQuickinvoice_reference() {
        return quickinvoice_reference;
    }

    public void setQuickinvoice_reference(String quickinvoice_reference) {
        this.quickinvoice_reference = quickinvoice_reference;
    }

    public void setTotalitems(int totalitems) {
        this.totalitems = totalitems;
    }

    public int getOrdervalue() {
        return ordervalue;
    }

    public void setOrdervalue(int ordervalue) {
        this.ordervalue = ordervalue;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    //optional


    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPaymentmode() {
        return paymentmode;
    }

    public void setPaymentmode(String paymentmode) {
        this.paymentmode = paymentmode;
    }

    public int getAmountdue() {
        return amountdue;
    }

    public void setAmountdue(int amountdue) {
        this.amountdue = amountdue;
    }

    public int getAmountpaid() {
        return amountpaid;
    }

    public void setAmountpaid(int amountpaid) {
        this.amountpaid = amountpaid;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public String getSgst() {
        return sgst;
    }

    public void setSgst(String sgst) {
        this.sgst = sgst;
    }

    public String getCgst() {
        return cgst;
    }

    public void setCgst(String cgst) {
        this.cgst = cgst;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public String getGstTotal() {
        return gstTotal;
    }

    public void setGstTotal(String gstTotal) {
        this.gstTotal = gstTotal;
    }

    public String getDelivery() {
        return delivery;
    }

    public void setDelivery(String delivery) {
        this.delivery = delivery;
    }

    public String getDelivery_mobileNum() {
        return delivery_mobileNum;
    }

    public void setDelivery_mobileNum(String delivery_mobileNum) {
        this.delivery_mobileNum = delivery_mobileNum;
    }

    public String getCustomer_name() {
        return customer_name;
    }

    public void setCustomer_name(String customer_name) {
        this.customer_name = customer_name;
    }
}

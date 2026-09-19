package Farme_rich.Seller.DTO.Response;


import Farme_rich.Seller.DTO.Request.OrderSummaryRow;

import java.util.List;

public class OrdersSummaryResponse {

    private int totalOrders;
    private double totalSales;
    private double balanceDue;
    private List<OrderSummaryRow> orders;

    public OrdersSummaryResponse() {
    }

    public OrdersSummaryResponse(int totalOrders, double totalSales, double balanceDue, List<OrderSummaryRow> orders) {
        this.totalOrders = totalOrders;
        this.totalSales = totalSales;
        this.balanceDue = balanceDue;
        this.orders = orders;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public double getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(double totalSales) {
        this.totalSales = totalSales;
    }

    public double getBalanceDue() {
        return balanceDue;
    }

    public void setBalanceDue(double balanceDue) {
        this.balanceDue = balanceDue;
    }

    public List<OrderSummaryRow> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderSummaryRow> orders) {
        this.orders = orders;
    }
}
package Farme_rich.Seller.BL;

import Farme_rich.Seller.DTO.Request.OrderSummaryRow;
import Farme_rich.Seller.DTO.Response.OrdersSummaryResponse;
import Farme_rich.Seller.Model.BackEnd.*;
import Farme_rich.Seller.Model.ReportsModel.*;
import Farme_rich.Seller.Repo.BackEnd.OrderReportRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ReportService {

    private static final SimpleDateFormat DISPLAY_FMT = new SimpleDateFormat("dd MMM yyyy");

    @Autowired
    private OrderReportRepository orderReportRepository;

    public List<Save> getAllOrders(ObjectId companyId, Date lastDate) {
        return orderReportRepository.findByCompanyidAndOrderDateGreaterThanEqualOrderByOrderDateDesc(companyId, lastDate);
    }


    public ReportData buildReport(ObjectId companyId, Date fromDate, Date toDate, String companyName) {

        List<Save> orders = orderReportRepository
                .findByCompanyidAndOrderDateBetweenOrderByOrderDateDesc(companyId, fromDate, toDate);

        ReportData data = new ReportData();

        data.setCompanyName(companyName);
        data.setGeneratedOn(DISPLAY_FMT.format(new Date()));
        data.setFromDate(DISPLAY_FMT.format(fromDate));
        data.setToDate(DISPLAY_FMT.format(toDate));

        data.setTotalOrders(orders.size());

        Map<String, ProcurementAgg> procurementAgg = new LinkedHashMap<>();

        data.setSalesTrend(buildSalesTrend(orders, procurementAgg));

        // Only valid payment modes will be included
        data.setOrderSource(buildOrderSource(orders));

        data.setOrderType(buildOrderType(orders));

        // Only customers having BOTH name and mobile will be included
        data.setLoyalty(buildLoyalty(orders));

        data.setProfitStats(
                buildProfitStats(
                        data.getSalesTrend(),
                        procurementAgg
                )
        );

        // ============================
        // TOTAL SALES
        // ============================
        double totalSales = orders.stream()
                .mapToDouble(Save::getTotal_Amount)
                .sum();

        data.setTotalRevenue(totalSales);

        // ============================
        // BALANCE DUE
        // ============================
        double balanceDue = orders.stream()
                .filter(order -> order.getPaymentDetails() != null)
                .mapToDouble(order -> {

                    Payments payment = order.getPaymentDetails();

                    // Prefer amountDue
                    if (payment.getAmountDue() > 0) {
                        return payment.getAmountDue();
                    }

                    // Fallback to outstanding amount
                    if (payment.getOutstanding_amount() > 0) {
                        return payment.getOutstanding_amount();
                    }

                    return 0;
                })
                .sum();

        data.setBalanceDue(balanceDue);

        return data;
    }


    private String resolveSalesTrendName(Inventory item, Batches batch) {
        String flag = item.getFlag();
        String productName = item.getProductName();
        String variantName = batch != null ? batch.getVariantName() : null;

        boolean hasProductName = productName != null && !productName.trim().isEmpty();
        boolean hasVariantName = variantName != null && !variantName.trim().isEmpty();

        if (flag == null && hasProductName && hasVariantName) {
            return productName.trim() + "-" + variantName.trim();
        }
        if (flag == null && hasProductName) {
            return productName.trim();
        }
        if ("isService".equals(flag)) {
            String service = item.getServicename();
            return (service != null && !service.trim().isEmpty()) ? service.trim() : "NA";
        }
        return "NA";
    }

    private List<SalesTrendRow> buildSalesTrend(List<Save> orders, Map<String, ProcurementAgg> procurementAgg) {
        LinkedHashMap<String, SalesTrendRow> map = new LinkedHashMap<>();

        for (Save order : orders) {
            if (order.getSeller_items() == null) continue;

            for (Inventory item : order.getSeller_items()) {
                Batches batch = resolveBatch(item);

                String name = resolveSalesTrendName(item, batch);

                int quantity = item.getOrder_quantity();
                double price = resolvePrice(item, batch);
                double lineValue = quantity * price;

                map.computeIfAbsent(name, k -> new SalesTrendRow(k, 0, 0)).addOrder(quantity, lineValue);

                if (batch != null && batch.getProcurement_price() > 0 && quantity > 0) {
                    ProcurementAgg agg = procurementAgg.computeIfAbsent(name, k -> new ProcurementAgg());
                    agg.totalProcurementValue += batch.getProcurement_price() * quantity;
                    agg.totalQty += quantity;
                }
            }
        }

        return sortByValueDesc(map.values());
    }

    private Batches resolveBatch(Inventory item) {
        if (item.getBatchid() != null) {
            return item.getBatchid();
        }

        List<Batches> variants = item.getVariantBatches();
        if (variants == null || variants.isEmpty()) {
            return null;
        }

        String batchId = item.getBatch_Id();
        if (batchId != null && !batchId.trim().isEmpty()) {
            for (Batches b : variants) {
                if (batchId.equals(b.getBatch_Id())) {
                    return b;
                }
            }
        }

        return variants.get(0);
    }

    private double resolvePrice(Inventory item, Batches batch) {
        if (batch != null) {
            if (batch.getSeller_price() > 0) return batch.getSeller_price();
            if (batch.getOfferPrice() > 0) return batch.getOfferPrice();
        }
        if (item.getSellerprice() > 0) return item.getSellerprice();
        return item.getOfferprice();
    }

    public OrdersSummaryResponse buildOrdersSummary(ObjectId companyId, Date fromDate, Date toDate) {

        List<Save> orders = orderReportRepository
                .findByCompanyidAndOrderDateBetweenOrderByOrderDateDesc(companyId, fromDate, toDate);

        List<OrderSummaryRow> rows = new ArrayList<>();
        double totalSales = 0;
        double balanceDue = 0;

        for (Save order : orders) {

            CustomerInformation ci = order.getCustomerInformation();
            String customerName = (ci != null && ci.getCustomerName() != null) ? ci.getCustomerName() : "N/A";
            String customerMobile = (ci != null && ci.getCustomerMobileNum() != null) ? ci.getCustomerMobileNum() : "N/A";

            Payments payments = order.getPaymentDetails();
            String paymentMode = (payments != null && payments.getPayment_mode() != null) ? payments.getPayment_mode() : "N/A";

            double orderValue = order.getTotal_Amount();
            double amountPaid = resolveAmountPaid(order);
            double amountDue = resolveAmountDue(order);

            totalSales += orderValue;
            balanceDue += amountDue;

            rows.add(new OrderSummaryRow(
                    customerName,
                    customerMobile,
                    order.get_id(),
                    order.getRefno(),
                    round2(orderValue),
                    round2(amountPaid),
                    round2(amountDue),
                    order.getOrderDate() != null ? DISPLAY_FMT.format(order.getOrderDate()) : "-",
                    order.getOrderStatus(),
                    order.getPayment_status(),
                    order.getDeliveryDate(),
                    paymentMode
            ));
        }

        return new OrdersSummaryResponse(orders.size(), round2(totalSales), round2(balanceDue), rows);
    }

    private double resolveAmountPaid(Save order) {
        Payments payment = order.getPaymentDetails();
        if (payment != null && payment.getPaid_amount() > 0) {
            return payment.getPaid_amount();
        }
        return order.getTotal_Paid();
    }

    private double resolveAmountDue(Save order) {
        double due = order.getTotal_AmountDue();
        return due > 0.0 ? due : 0.0;
    }


    private List<OrderSourceRow> buildOrderSource(List<Save> orders) {

        LinkedHashMap<String, OrderSourceRow> map = new LinkedHashMap<>();

        for (Save order : orders) {

            // No payment details -> skip
            if (order.getPaymentDetails() == null) {
                continue;
            }

            String paymentMode = order.getPaymentDetails().getPayment_mode();

            // No payment mode -> skip
            if (paymentMode == null || paymentMode.trim().isEmpty()) {
                continue;
            }

            String source = paymentMode.trim().toUpperCase();

            // Explicitly ignore UNKNOWN
            if ("UNKNOWN".equals(source)) {
                continue;
            }

            map.computeIfAbsent(
                    source,
                    k -> new OrderSourceRow(k, 0, 0)
            ).addOrder(order.getTotal_Amount());
        }

        return sortByValueDesc(map.values());
    }

    private List<OrderTypeRow> buildOrderType(List<Save> orders) {
        LinkedHashMap<String, OrderTypeRow> map = new LinkedHashMap<>();

        for (Save order : orders) {
            String raw = order.getOrderSource() == null ? "" : order.getOrderSource().toUpperCase();
            String type;
            if (raw.contains("WEB")) {
                type = "Online";
            } else if (raw.contains("INOCK")) {
                type = "Store";
            } else {
                type = "Other";
            }
            map.computeIfAbsent(type, k -> new OrderTypeRow(k, 0, 0)).addOrder(order.getTotal_Amount());
        }

        return sortByValueDesc(map.values());
    }

    private List<LoyaltyRow> buildLoyalty(List<Save> orders) {

        LinkedHashMap<String, LoyaltyRow> map = new LinkedHashMap<>();

        for (Save order : orders) {

            CustomerInformation ci = order.getCustomerInformation();

            // No customer information -> skip
            if (ci == null) {
                continue;
            }

            String name = ci.getCustomerName();
            String mobile = ci.getCustomerMobileNum();

            // Both fields are mandatory
            if (name == null || name.trim().isEmpty()) {
                continue;
            }

            if (mobile == null || mobile.trim().isEmpty()) {
                continue;
            }

            String final_name = name.trim();
            String final_mobile = mobile.trim();

            String key = name + "|" + mobile;

            map.computeIfAbsent(
                    key,
                    k -> new LoyaltyRow(
                            name,
                            mobile,
                            0,
                            0
                    )
            ).addOrder(order.getTotal_Amount());
        }

        return sortByValueDesc(map.values());
    }


    // ---------------------------------------------------------------
    // 5. Profit statistics - uses REAL procurement price from Batches
    // (accumulated in procurementAgg while building Sales Trend) when available.
    // Falls back to a stable dummy estimate (55%-75% of selling price) only for
    // products where no batch procurement price could be found.
    // ---------------------------------------------------------------
    private List<ProfitRow> buildProfitStats(List<SalesTrendRow> salesTrend, Map<String, ProcurementAgg> procurementAgg) {
        List<ProfitRow> rows = new ArrayList<>();
        Random rnd = new Random(42); // fixed seed so dummy fallback numbers stay stable between calls

        int count = 0;
        for (SalesTrendRow row : salesTrend) {
            if (count++ >= 6) break; // top 6 products only

            double avgSellingPrice = row.getNoOfOrders() > 0
                    ? row.getTotalValue() / Math.max(1, row.getNoOfOrders())
                    : 0;
            if (avgSellingPrice <= 0) avgSellingPrice = 50 + rnd.nextInt(150);

            double procurement;
            ProcurementAgg agg = procurementAgg.get(row.getProductName());
            if (agg != null && agg.totalQty > 0) {
                // real, quantity-weighted procurement price from the batches actually sold
                procurement = agg.totalProcurementValue / agg.totalQty;
            } else {
                // no procurement price recorded on the batch yet - dummy 55%-75% of selling price
                procurement = avgSellingPrice * (0.55 + rnd.nextDouble() * 0.20);
            }

            rows.add(new ProfitRow(row.getProductName(), round2(avgSellingPrice), round2(procurement)));
        }

        if (rows.isEmpty()) {
            // fallback sample data when there is no order history yet
            rows.add(new ProfitRow("Tomato (1kg)", 40, 26));
            rows.add(new ProfitRow("Banana (dozen)", 60, 38));
            rows.add(new ProfitRow("Rice (5kg)", 320, 250));
        }

        return rows;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private <T> List<T> sortByValueDesc(Collection<T> values) {
        List<T> list = new ArrayList<>(values);
        list.sort((a, b) -> Double.compare(extractValue(b), extractValue(a)));
        return list;
    }

    private double extractValue(Object row) {
        if (row instanceof SalesTrendRow) return ((SalesTrendRow) row).getTotalValue();
        if (row instanceof OrderSourceRow) return ((OrderSourceRow) row).getTotalValue();
        if (row instanceof OrderTypeRow) return ((OrderTypeRow) row).getTotalValue();
        if (row instanceof LoyaltyRow) return ((LoyaltyRow) row).getTotalValue();
        return 0;
    }

    /**
     * Quantity-weighted procurement accumulator, keyed by product/variant name. Local to one report build - not shared state.
     */
    private static class ProcurementAgg {
        double totalProcurementValue;
        double totalQty;
    }
}
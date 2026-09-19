package Farme_rich.Seller.BL;

import Farme_rich.Seller.DTO.Request.*;
import Farme_rich.Seller.Model.BackEnd.Batches;
import Farme_rich.Seller.Model.BackEnd.Inventory;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final String COLLECTION = "sellerorder";
    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String INVENTORY_COLLECTION = "inventory";
    @Autowired
    private MongoTemplate mongoTemplate;

    private List<String> lastNDays(int n) {
        List<String> days = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = n - 1; i >= 0; i--) {
            days.add(today.minusDays(i).format(DAY_FMT));
        }
        return days;
    }

    private Date startOfDaysAgo(int n) {
        LocalDateTime start = LocalDate.now().minusDays(n - 1L).atStartOfDay();
        return Date.from(start.atZone(ZoneId.systemDefault()).toInstant());
    }

    public DashboardProductDTO getTopProductsLast3Days(ObjectId companyId) {
        Date from = startOfDaysAgo(3);
        List<String> dates = lastNDays(3);

        // ---- Step 1: find the overall top 3 products across the 3-day window ----
        MatchOperation matchRange = Aggregation.match(
                Criteria.where("orderDate").gte(from).and("companyid").is(companyId)
        );
        UnwindOperation unwindItems = Aggregation.unwind("seller_items");

        AggregationExpression saleValueExpr = ArithmeticOperators.valueOf("seller_items.Order_quantity")
                .multiplyBy(
                        ConditionalOperators.when(Criteria.where("seller_items.offerprice").gt(0))
                                .thenValueOf("seller_items.offerprice")
                                .otherwiseValueOf("seller_items.sellerprice")
                );

        ProjectionOperation projectSale = Aggregation.project()
                .and("seller_items.productName").as("productName")
                .and(saleValueExpr).as("saleValue");

        GroupOperation groupByProduct = Aggregation.group("productName")
                .sum("saleValue").as("totalValue");

        SortOperation sortDesc = Aggregation.sort(Sort.Direction.DESC, "totalValue");
        LimitOperation top3 = Aggregation.limit(3);

        Aggregation topProductsAgg = Aggregation.newAggregation(
                matchRange, unwindItems, projectSale, groupByProduct, sortDesc, top3
        );

        List<Document> topProductDocs = mongoTemplate.aggregate(topProductsAgg, COLLECTION, Document.class)
                .getMappedResults();

        List<String> topProductNames = topProductDocs.stream()
                .map(d -> d.getString("_id"))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (topProductNames.isEmpty()) {
            return new DashboardProductDTO(dates, Collections.emptyList(), buildEmptyProductDays(dates));
        }

        // ---- Step 2: per-day totals for just those top 3 products ----
        MatchOperation matchProducts = Aggregation.match(
                Criteria.where("seller_items.productName").in(topProductNames)
        );

        ProjectionOperation projectDayProduct = Aggregation.project()
                .and(DateOperators.DateToString.dateOf("orderDate").toString("%Y-%m-%d")).as("date")
                .and("seller_items.productName").as("productName")
                .and(saleValueExpr).as("saleValue");

        GroupOperation groupByDayProduct = Aggregation.group("date", "productName")
                .sum("saleValue").as("totalValue");

        Aggregation dayProductAgg = Aggregation.newAggregation(
                matchRange, unwindItems, matchProducts, projectDayProduct, groupByDayProduct
        );

        List<Document> dayProductDocs = mongoTemplate.aggregate(dayProductAgg, COLLECTION, Document.class)
                .getMappedResults();

        // map: date -> (productName -> value)
        Map<String, Map<String, Double>> byDate = new HashMap<>();
        for (Document doc : dayProductDocs) {
            Document id = (Document) doc.get("_id");
            String date = id.getString("date");
            String product = id.getString("productName");
            double value = doc.get("totalValue") instanceof Number
                    ? ((Number) doc.get("totalValue")).doubleValue() : 0d;
            byDate.computeIfAbsent(date, k -> new HashMap<>()).put(product, value);
        }

        List<DashboardProductDTO.ProductDayData> data = new ArrayList<>();
        for (String date : dates) {
            Map<String, Double> productMap = byDate.getOrDefault(date, Collections.emptyMap());
            List<DashboardProductDTO.ProductValue> values = topProductNames.stream()
                    .map(p -> new DashboardProductDTO.ProductValue(p, productMap.getOrDefault(p, 0d)))
                    .collect(Collectors.toList());
            data.add(new DashboardProductDTO.ProductDayData(date, values));
        }

        return new DashboardProductDTO(dates, topProductNames, data);
    }

    private List<DashboardProductDTO.ProductDayData> buildEmptyProductDays(List<String> dates) {
        return dates.stream()
                .map(d -> new DashboardProductDTO.ProductDayData(d, Collections.emptyList()))
                .collect(Collectors.toList());
    }

    public List<SalesTrendDTO> getSalesTrendLast5Days(ObjectId companyId) {
        Date from = startOfDaysAgo(5);
        List<String> dates = lastNDays(5);

        MatchOperation match = Aggregation.match(
                Criteria.where("orderDate").gte(from)
                        .and("companyid").is(companyId)
                        .and("order_status").is("Completed")
        );

        ProjectionOperation project = Aggregation.project()
                .and(DateOperators.DateToString.dateOf("orderDate").toString("%Y-%m-%d")).as("date")
                .and("total_Amount").as("total_Amount");

        GroupOperation group = Aggregation.group("date")
                .sum("total_Amount").as("orderValue");

        Aggregation agg = Aggregation.newAggregation(match, project, group);

        List<Document> results = mongoTemplate.aggregate(agg, COLLECTION, Document.class).getMappedResults();

        Map<String, Double> valueByDate = new HashMap<>();
        for (Document doc : results) {
            String date = doc.getString("_id");
            double value = doc.get("orderValue") instanceof Number
                    ? ((Number) doc.get("orderValue")).doubleValue() : 0d;
            valueByDate.put(date, value);
        }

        List<SalesTrendDTO> trend = new ArrayList<>();
        for (String date : dates) {
            trend.add(new SalesTrendDTO(date, valueByDate.getOrDefault(date, 0d)));
        }
        return trend;
    }

    public List<InvoiceDTO> getInvoicesLast3Days(ObjectId companyId) {
        Date from = startOfDaysAgo(3);
        List<String> dates = lastNDays(3);

        MatchOperation match = Aggregation.match(
                Criteria.where("orderDate").gte(from).and("companyid").is(companyId)
        );

        ProjectionOperation project = Aggregation.project()
                .and(DateOperators.DateToString.dateOf("orderDate").toString("%Y-%m-%d")).as("date")
                .and("total_AmountDue").as("total_AmountDue");

        GroupOperation group = Aggregation.group("date")
                .sum(ConditionalOperators.when(Criteria.where("total_AmountDue").gt(0))
                        .then(1).otherwise(0)).as("unpaidCount")
                .sum(ConditionalOperators.when(Criteria.where("total_AmountDue").is(0))
                        .then(1).otherwise(0)).as("paidCount");

        Aggregation agg = Aggregation.newAggregation(match, project, group);

        List<Document> results = mongoTemplate.aggregate(agg, COLLECTION, Document.class).getMappedResults();

        Map<String, long[]> byDate = new HashMap<>(); // [paid, unpaid]
        for (Document doc : results) {
            String date = doc.getString("_id");
            long paid = doc.get("paidCount") instanceof Number ? ((Number) doc.get("paidCount")).longValue() : 0;
            long unpaid = doc.get("unpaidCount") instanceof Number ? ((Number) doc.get("unpaidCount")).longValue() : 0;
            byDate.put(date, new long[]{paid, unpaid});
        }

        List<InvoiceDTO> invoices = new ArrayList<>();
        for (String date : dates) {
            long[] counts = byDate.getOrDefault(date, new long[]{0, 0});
            invoices.add(new InvoiceDTO(date, counts[0], counts[1]));
        }
        return invoices;
    }

    /**
     * Flattens one Inventory doc into its batch-level stock rows.
     */
    private List<StockProductDTO> extractStockEntries(Inventory inv) {
        List<StockProductDTO> entries = new ArrayList<>();

        if (inv.getVariantBatches() != null && !inv.getVariantBatches().isEmpty()) {
            for (Batches b : inv.getVariantBatches()) {
                entries.add(toStockProductDTO(inv.getProductName(), b));
            }
        } else if (inv.getBatchid() != null) {
            entries.add(toStockProductDTO(inv.getProductName(), inv.getBatchid()));
        } else {
            // no batch info at all -> treat as out of stock so it isn't silently dropped
            entries.add(new StockProductDTO(inv.getProductName(), null, 0, inv.getUpdated_at()));
        }
        return entries;
    }

    private StockProductDTO toStockProductDTO(String productName, Batches b) {
        int stock = b.getStock_availability();
        String lastUpdated = stock == 0 ? b.getUpdatedAt() : null;
        return new StockProductDTO(productName, b.getVariantName(), stock, lastUpdated);
    }

    private String stockCategory(int stock) {
        if (stock == 0) return "ZERO";
        if (stock < 10) return "LOW";
        return "HIGH";
    }

    public StockSummaryDTO getStockSummary(String companyId) {
        List<Inventory> inventories = mongoTemplate.find(
                Query.query(
                        Criteria.where("companyid").is(companyId)
                                .and("isActive").is(true)
                ),
                Inventory.class,
                INVENTORY_COLLECTION
        );

        int zero = 0, low = 0, high = 0;
        for (Inventory inv : inventories) {
            for (StockProductDTO entry : extractStockEntries(inv)) {
                switch (stockCategory(entry.getStockAvailable())) {
                    case "ZERO" -> zero++;
                    case "LOW" -> low++;
                    default -> high++;
                }
            }
        }
        return new StockSummaryDTO(zero, low, high);
    }

    /**
     * category: ZERO | LOW | HIGH
     */
    public List<StockProductDTO> getStockProductsByCategory(String companyId, String category) {
        List<Inventory> inventories = mongoTemplate.find(
                Query.query(
                        Criteria.where("companyid").is(companyId)
                                .and("isActive").is(true)
                ),
                Inventory.class,
                INVENTORY_COLLECTION
        );

        String target = category == null ? "" : category.toUpperCase();
        List<StockProductDTO> result = new ArrayList<>();
        for (Inventory inv : inventories) {
            for (StockProductDTO entry : extractStockEntries(inv)) {
                if (stockCategory(entry.getStockAvailable()).equals(target)) {
                    result.add(entry);
                }
            }
        }
        return result;
    }
}

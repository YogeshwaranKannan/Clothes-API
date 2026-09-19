package Farme_rich.Buyer.BL;

import Farme_rich.Buyer.Adaptor.BuyerAdaptor;
import Farme_rich.Buyer.DTO.Request.*;
import Farme_rich.Buyer.DTO.Response.*;
import Farme_rich.Buyer.Model.Buyer;
import Farme_rich.Buyer.Repo.BuyerRepo;
import Farme_rich.Buyer.Repo.BuyerUserRepo;
import Farme_rich.Security.JwtService;
import Farme_rich.Seller.Adaptor.SellerAdaptor;
import Farme_rich.Seller.DTO.Request.SubscriptionEmailDTO;
import Farme_rich.Seller.DTO.Response.InventoryDTO;
import Farme_rich.Seller.DTO.Response.InvoiceResponse;
import Farme_rich.Seller.Model.BackEnd.*;
import Farme_rich.Seller.Model.FrontEnd.Seller;
import Farme_rich.Seller.Repo.BackEnd.*;
import Farme_rich.Seller.Repo.FrontEnd.SellerRepo;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.mongodb.DuplicateKeyException;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.razorpay.*;
import jakarta.mail.internet.MimeMessage;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static Farme_rich.Security.PasswordUtils.encrypt;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;


@Service
public class BuyerService {
    private static final Logger log = LoggerFactory.getLogger(BuyerService.class);
    private static final Logger logger = LoggerFactory.getLogger(BuyerService.class);
    private static final String Product_BASE_PATH = "uploads/uploadsProductImages/";
    private static final String Service_BASE_PATH = "uploads/uploadsServiceImages/";
    private final BuyerRepo buyerRepo;
    private final InventoryRepo inventoryRepo;
    private final SaveOrderRepo saveOrderRepo;
    private final ServiceInventoryRepo serviceInventoryRepo;
    private final ProductCategoryRepo productCategoryRepo;
    private final PaymentDetailsRepo paymentDetailsRepo;
    private final RazorPaymentFullDetailsRepo razorPaymentFullDetailsRepo;
    private final SellerRepo sellerRepo;
    private final BatchesRepo batchesRepo;
    private final ProductRepo productRepo;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${app.baseUrl}")
    private String hostUrl;
    @Value("${app.Ec2BaseUrl}")
    private String ShareUrl;
    private MongoTemplate mongoTemplate;
    private MongoClient mongoClient;
    @Value("${spring.mail.username}")
    private String fromEmail;
    @Value("${app.SuggestUrl}")
    private String uploadHost;

    @Autowired
    public BuyerService(BuyerRepo buyerRepo, BuyerUserRepo buyerUserRepo,
                        ServiceInventoryRepo serviceInventoryRepo,
                        InventoryRepo inventoryRepo,
                        SaveOrderRepo saveOrderRepo,
                        ProductCategoryRepo productCategoryRepo, PaymentDetailsRepo paymentDetailsRepo,
                        RazorPaymentFullDetailsRepo razorPaymentFullDetailsRepo,
                        SellerRepo sellerRepo, BatchesRepo batchesRepo, ProductRepo productRepo) {
        this.buyerRepo = buyerRepo;

        this.inventoryRepo = inventoryRepo;
        this.serviceInventoryRepo = serviceInventoryRepo;
        this.productCategoryRepo = productCategoryRepo;
        this.saveOrderRepo = saveOrderRepo;
        this.paymentDetailsRepo = paymentDetailsRepo;
        this.razorPaymentFullDetailsRepo = razorPaymentFullDetailsRepo;
        this.sellerRepo = sellerRepo;
        this.batchesRepo = batchesRepo;
        this.productRepo = productRepo;

    }

    @Autowired
    public void SetMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Autowired
    public void SetMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public List<GetCategoriesResponse> GetCategoriesBySellerID(GetCategoriesRequest request) {
        List<GetCategoriesResponse> ans = new ArrayList<GetCategoriesResponse>();
        List<ProductCategory> productCategories = productCategoryRepo.findBySellerids(request.sellerId);
//      for (ProductCategory i : productCategories){
//          log.info("id : "+ i.get_id());
//          log.info("categoryname: "+i.getCategory_name());
//      }
        if (!productCategories.isEmpty()) {
            ans.addAll(BuyerAdaptor.FromProductCategoriesToGetCategories(productCategories));
        }
        return ans;
    }

    public List<GetCategoriesResponse> buildTree(List<GetCategoriesResponse> list) {

        Map<String, GetCategoriesResponse> map = new HashMap<>();
        List<GetCategoriesResponse> roots = new ArrayList<>();

        // Map all
        for (GetCategoriesResponse item : list) {
            map.put(item.id, item);
        }

        // Build hierarchy
        for (GetCategoriesResponse item : list) {

            if (item.parent == null) {
                roots.add(item);
            } else {
                GetCategoriesResponse parent = map.get(item.parent);
                if (parent != null) {
                    parent.children.add(item);
                } else {
                    roots.add(item);
                }
            }
        }

        return roots;
    }

//    public InventoryProductsAndServiceResponse searchProductsManageOrderSeller(InventoryProductsAndServiceRequest request) {
//        log.info("Page No : " + request.page);
//        log.info("IsToSeeInventory : " + request.seeInv);
//        log.info("IsToSeeProduct : " + request.seeProd);
//        log.info("IsToSeeService : " + request.seeServ);
//        log.info("Taken -Inventory : " + request.invTaken);
//        log.info("Taken -Service : " + request.servTaken);
//        log.info("Taken -Product : " + request.prodTaken);
//
//        InventoryProductsAndServiceResponse response = new InventoryProductsAndServiceResponse();
//
//        if (request.companyId == null || request.companyId.isBlank()) {
//            throw new IllegalArgumentException("companyId is required");
//        }
//
//        int pageSize = request.pageSize != null && request.pageSize > 0
//                ? request.pageSize
//                : 1000;
//
//        int AlreadyTaken_Inv = request.invTaken;
//        int AlreadyTaken_Serv = request.servTaken;
//
//        boolean hasSearch = request.searchValue != null && !request.searchValue.trim().isEmpty();
//        boolean hasCategories = request.categories != null && !request.categories.isEmpty();
//
//        // ── Base match criteria shared between the count query and the aggregation ──
//        Criteria invBaseCriteria = new Criteria().andOperator(
//                Criteria.where("companyid").is(request.companyId),
//                Criteria.where("isActive").is(true),
//                new Criteria().orOperator(
//                        Criteria.where("batchid.active").is(true),
//                        Criteria.where("variantBatches.active").is(true)
//                )
//        );
//
//        Criteria servBaseCriteria = Criteria.where("sellerid")
//                .is(new ObjectId(request.companyId))
//                .and("isActive").is(true);
//        if (hasSearch) {
//            servBaseCriteria = servBaseCriteria.and("servicename")
//                    .regex(".*" + Pattern.quote(request.searchValue.trim()) + ".*", "i");
//        }
//
//        // ── Totals for THIS filter set (search + categories applied) — drives seeInv/seeServ ──
//        int Initial_Inv = 0;
//        if (request.seeInv == 1) {
//            List<AggregationOperation> countPipeline = new ArrayList<>();
//            countPipeline.add(match(invBaseCriteria));
//            countPipeline.add(lookup("products", "productid", "_id", "product"));
//            countPipeline.add(unwind("product", true));
//            if (hasCategories) {
//                countPipeline.add(match(
//                        Criteria.where("product.productCategory._id")
//                                .in(request.categories.stream().map(ObjectId::new).toList())
//                ));
//            }
//            if (hasSearch) {
//                String searchRegex = ".*" + Pattern.quote(request.searchValue.trim()) + ".*";
//                countPipeline.add(match(
//                        new Criteria().orOperator(
//                                Criteria.where("productName").regex(searchRegex, "i"),
//                                Criteria.where("variantBatches.productname").regex(searchRegex, "i"),
//                                Criteria.where("variantBatches.variantName").regex(searchRegex, "i")
//                        )
//                ));
//            }
//            countPipeline.add(Aggregation.count().as("total"));
//
//            var countResult = mongoTemplate.aggregate(newAggregation(countPipeline), "inventory", Document.class)
//                    .getUniqueMappedResult();
//            Initial_Inv = countResult != null ? countResult.getInteger("total", 0) : 0;
//        }
//
//        int Initial_Serv = request.seeServ == 1
//                ? (int) mongoTemplate.count(Query.query(servBaseCriteria), "ServiceInventory")
//                : 0;
//
//        log.info("Initial -Inventory for this seller (filtered) : " + Initial_Inv);
//        log.info("Initial -ServiceInventory for this seller (filtered) : " + Initial_Serv);
//
//        boolean inventoryExists = false;
//        boolean ServiceInventoryExists = false;
//
//        if (hasSearch) {
//            inventoryExists = mongoTemplate.exists(
//                    Query.query(
//                            new Criteria().andOperator(
//                                    Criteria.where("companyid").is(request.companyId)
//                                            .orOperator(
//                                                    Criteria.where("batchid.active").is(true),
//                                                    Criteria.where("variantBatches.active").is(true)
//                                            ),
//                                    new Criteria().orOperator(
//                                            Criteria.where("batchid.productname")
//                                                    .regex(".*" + Pattern.quote(request.searchValue) + ".*", "i"),
//                                            Criteria.where("variantBatches.productname")
//                                                    .regex(".*" + Pattern.quote(request.searchValue) + ".*", "i"),
//                                            Criteria.where("variantBatches.variantName")
//                                                    .regex(".*" + Pattern.quote(request.searchValue) + ".*", "i")
//                                    )
//                            )
//                    ),
//                    "inventory"
//            );
//
//            ServiceInventoryExists = mongoTemplate.exists(
//                    Query.query(
//                            Criteria.where("sellerid")
//                                    .is(new ObjectId(request.companyId))
//                                    .and("isActive").is(true)
//                                    .and("servicename").regex(
//                                            ".*" + Pattern.quote(request.searchValue) + ".*", "i"
//                                    )
//                    ),
//                    "ServiceInventory"
//            );
//        }
//
//        log.info("inventoryExists : " + inventoryExists);
//        log.info("SearchValue : " + request.searchValue);
//        log.info("ServiceInventoryExists : " + ServiceInventoryExists);
//
//        List<InventoryProductsAndServiceResponse.ProductItem> resultList = new ArrayList<>();
//
//        if (inventoryExists || ServiceInventoryExists) {
//
//            // ================= INVENTORY =================
//            List<AggregationOperation> pipeline = new ArrayList<>();
//            if (request.seeInv == 1) {
//
//                pipeline.add(match(invBaseCriteria));
//                pipeline.add(lookup("products", "productid", "_id", "product"));
//                pipeline.add(unwind("product", true));
//
//                if (hasCategories) {
//                    pipeline.add(match(
//                            Criteria.where("product.productCategory._id")
//                                    .in(request.categories.stream().map(ObjectId::new).toList())
//                    ));
//                }
//                if (hasSearch) {
//                    String searchRegex = ".*" + Pattern.quote(request.searchValue.trim()) + ".*";
//                    pipeline.add(match(
//                            new Criteria().orOperator(
//                                    Criteria.where("productName").regex(searchRegex, "i"),
//                                    Criteria.where("variantBatches.productname").regex(searchRegex, "i"),
//                                    Criteria.where("variantBatches.variantName").regex(searchRegex, "i")
//                            )
//                    ));
//                }
//
//                pipeline.add(project()
//                        .and("_id").as("inventoryId")
//                        .and("productid").as("ProductId")
//                        .and("companyid").as("sellerid")
//                        .and("productCategory").as("productCategoryDTO")
//                        .and(
//                                ConditionalOperators.ifNull(
//                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.productname").elementAt(0)
//                                ).thenValueOf("batchid.productname")
//                        ).as("ProductName")
//                        .and(
//                                ConditionalOperators.ifNull(
//                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.created_at").elementAt(0)
//                                ).thenValueOf("batchid.created_at")
//                        ).as("CreatedAt")
//                        .and(
//                                ConditionalOperators.ifNull(
//                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.stock_availability").elementAt(0)
//                                ).thenValueOf("batchid.stock_availability")
//                        ).as("AvilableQty")
//                        .and(
//                                ConditionalOperators.ifNull(
//                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches._id").elementAt(0)
//                                ).thenValueOf("batchid._id")
//                        ).as("Batch_Id")
//                        .and(
//                                context -> new Document("$map",
//                                        new Document("input", "$variantBatches")
//                                                .append("as", "v")
//                                                .append("in",
//                                                        new Document("variantsid", "$$v._id")
//                                                                .append("variantsName", "$$v.variantName")
//                                                                .append("SellingPrice", "$$v.seller_price")
//                                                                .append("OfferPrice", "$$v.offerPrice")
//                                                                .append("DiscountPercent", "$$v.discount")
//                                                                .append("AvilableQty", "$$v.stock_availability")
//                                                                .append("StockStatus",
//                                                                        new Document("$cond", Arrays.asList(
//                                                                                new Document("$gt", Arrays.asList("$$v.stock_availability", 0)),
//                                                                                "IN STOCK", "OUT OF STOCK"
//                                                                        )))
//                                                                .append("offerText",
//                                                                        new Document("$concat", Arrays.asList(
//                                                                                "₹", new Document("$toString", "$$v.discount"), " OFF"
//                                                                        )))
//                                                )
//                                )
//                        ).as("variants")
//                        .and("Quickadd").as("Quickadd")
//                        .and(
//                                ConditionalOperators.ifNull(
//                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.Batch_Id").elementAt(0)
//                                ).thenValueOf("batchid.Batch_Id")
//                        ).as("uniquebatchid")
//                        .and(
//                                ConditionalOperators.ifNull(
//                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.seller_price").elementAt(0)
//                                ).thenValueOf("batchid.seller_price")
//                        ).as("SellingPrice")
//                        .and(
//                                ConditionalOperators.ifNull(
//                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.offerPrice").elementAt(0)
//                                ).thenValueOf("batchid.offerPrice")
//                        ).as("OfferPrice")
//                        .and(
//                                ConditionalOperators.ifNull(
//                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.discount").elementAt(0)
//                                ).thenValueOf("batchid.discount")
//                        ).as("DiscountPercent")
//                        .and("product.defaultImage").as("Image")
//                        .and("product.brand").as("Brand")
//                        .and(String.valueOf(false)).as("isAttributePresent")
//                        .and(String.valueOf(false)).as("isService")
//                        .and("product.productCategory._id").as("Category.CategoryId")
//                        .and("product.productCategory.category_name").as("Category.CategoryName")
//                        .and("product.unit").as("batch_unit")
//                );
//
//                pipeline.add(sort(Sort.by(Sort.Direction.ASC, "ProductName")));
//                pipeline.add(skip((long) request.invTaken));
//                pipeline.add(limit(pageSize));
//
//                List<InventoryProductsAndServiceResponse.ProductItem> inventoryResults =
//                        mongoTemplate.aggregate(
//                                newAggregation(pipeline),
//                                "inventory",
//                                InventoryProductsAndServiceResponse.ProductItem.class
//                        ).getMappedResults();
//
//                resultList.addAll(inventoryResults);
//                AlreadyTaken_Inv += inventoryResults.size();
//            }
//
//            int remaining = pageSize - resultList.size();
//            log.info("Remaining size after Inventory found: " + remaining);
//
//            response.products = resultList;
//            response.invTaken = AlreadyTaken_Inv;
//            response.servTaken = AlreadyTaken_Serv;
//            response.seeInv = AlreadyTaken_Inv >= Initial_Inv ? 0 : 1;
//            response.seeServ = AlreadyTaken_Serv >= Initial_Serv ? 0 : 1;
//
//            if (remaining <= 0) {
//                return response;
//            }
//
//            // ================= SERVICE INVENTORY =================
//            if (request.seeServ == 1) {
//                List<InventoryProductsAndServiceResponse.ProductItem> serviceResults =
//                        mongoTemplate.aggregate(
//                                newAggregation(
//                                        match(servBaseCriteria),
//                                        project()
//                                                .and("_id").as("_id")
//                                                .and("servicename").as("ProductName")
//                                                .and("servicecost").as("SellingPrice")
//                                                .and("category").as("serviceCategory")
//                                                .and("created_at").as("CreatedAt")
//                                                .and("propertyAttributes").as("propertyAttributes")
//                                                .and("defaultImage").as("defaultImage"),
//                                        sort(Sort.by(Sort.Direction.ASC, "ProductName")),
//                                        skip((long) request.servTaken),
//                                        limit(remaining)
//                                ),
//                                "ServiceInventory",
//                                InventoryProductsAndServiceResponse.ProductItem.class
//                        ).getMappedResults();
//
//                resultList.addAll(serviceResults);
//                AlreadyTaken_Serv += serviceResults.size();
//            }
//
//            response.products = resultList;
//            response.invTaken = AlreadyTaken_Inv;
//            response.servTaken = AlreadyTaken_Serv;
//            response.seeInv = AlreadyTaken_Inv >= Initial_Inv ? 0 : 1;
//            response.seeServ = AlreadyTaken_Serv >= Initial_Serv ? 0 : 1;
//            return response;
//
//        } else if (!hasSearch) {
//
//            log.info("No search match, returning inventory + services (priority)");
//
//            List<InventoryProductsAndServiceResponse.ProductItem> finalResult = new ArrayList<>();
//
//            List<AggregationOperation> inventoryPipeline = new ArrayList<>();
//            if (request.seeInv == 1) {
//                inventoryPipeline.add(match(invBaseCriteria));
//                inventoryPipeline.add(lookup("products", "productid", "_id", "product"));
//                inventoryPipeline.add(unwind("product", true));
//
//                if (request.deals) {
//                    inventoryPipeline.add(match(Criteria.where("deals").is(true)));
//                }
//                if (request.newArrivals) {
//                    inventoryPipeline.add(match(Criteria.where("newArrivals").is(true)));
//                }
//                if (hasCategories) {
//                    inventoryPipeline.add(match(
//                            Criteria.where("productCategory._id")
//                                    .in(request.categories.stream().map(ObjectId::new).toList())
//                    ));
//                }
//
//                inventoryPipeline.add(project()
//                        .and("_id").as("inventoryId")
//                        .and("productid").as("ProductId")
//                        .and("companyid").as("sellerid")
//                        .and("productCategory").as("productCategoryDTO")
//                        .and(
//                                ConditionalOperators.ifNull(
//                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.productname").elementAt(0)
//                                ).thenValueOf("batchid.productname")
//                        ).as("ProductName")
//                        .and(
//                                ConditionalOperators.ifNull(
//                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.created_at").elementAt(0)
//                                ).thenValueOf("batchid.created_at")
//                        ).as("CreatedAt")
//                        .and(
//                                ConditionalOperators.ifNull(
//                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.stock_availability").elementAt(0)
//                                ).thenValueOf("batchid.stock_availability")
//                        ).as("AvilableQty")
//                        .and(
//                                ConditionalOperators.ifNull(
//                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches._id").elementAt(0)
//                                ).thenValueOf("batchid._id")
//                        ).as("Batch_Id")
//                        .and(
//                                context -> new Document("$map",
//                                        new Document("input", "$variantBatches")
//                                                .append("as", "v")
//                                                .append("in",
//                                                        new Document("variantsid", "$$v._id")
//                                                                .append("variantsName", "$$v.variantName")
//                                                                .append("SellingPrice", "$$v.seller_price")
//                                                                .append("OfferPrice", "$$v.offerPrice")
//                                                                .append("DiscountPercent", "$$v.discount")
//                                                                .append("AvilableQty", "$$v.stock_availability")
//                                                                .append("StockStatus",
//                                                                        new Document("$cond", Arrays.asList(
//                                                                                new Document("$gt", Arrays.asList("$$v.stock_availability", 0)),
//                                                                                "IN STOCK", "OUT OF STOCK"
//                                                                        )))
//                                                                .append("offerText",
//                                                                        new Document("$concat", Arrays.asList(
//                                                                                "₹", new Document("$toString", "$$v.discount"), " OFF"
//                                                                        )))
//                                                )
//                                )
//                        ).as("variants")
//                        .and("Quickadd").as("Quickadd")
//                        .and(
//                                ConditionalOperators.ifNull(
//                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.Batch_Id").elementAt(0)
//                                ).thenValueOf("batchid.Batch_Id")
//                        ).as("uniquebatchid")
//                        .and(
//                                ConditionalOperators.ifNull(
//                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.seller_price").elementAt(0)
//                                ).thenValueOf("batchid.seller_price")
//                        ).as("SellingPrice")
//                        .and(
//                                ConditionalOperators.ifNull(
//                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.offerPrice").elementAt(0)
//                                ).thenValueOf("batchid.offerPrice")
//                        ).as("OfferPrice")
//                        .and(
//                                ConditionalOperators.ifNull(
//                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.discount").elementAt(0)
//                                ).thenValueOf("batchid.discount")
//                        ).as("DiscountPercent")
//                        .and("product.brand").as("Brand")
//                        .and("product.defaultImage").as("Image")
//                        .and(String.valueOf(false)).as("isAttributePresent")
//                        .and(String.valueOf(false)).as("isService")
//                        .and("product.productCategory._id").as("Category.CategoryId")
//                        .and("product.productCategory.category_name").as("Category.CategoryName")
//                        .and("product.unit").as("batch_unit")
//                );
//
//                inventoryPipeline.add(sort(Sort.by(
//                        Sort.Order.desc("CreatedAt"),
//                        Sort.Order.asc("ProductName")
//                )));
//                inventoryPipeline.add(skip((long) request.invTaken));
//                inventoryPipeline.add(limit(pageSize));
//
//                List<InventoryProductsAndServiceResponse.ProductItem> inventoryList =
//                        mongoTemplate.aggregate(
//                                newAggregation(inventoryPipeline),
//                                "inventory",
//                                InventoryProductsAndServiceResponse.ProductItem.class
//                        ).getMappedResults();
//
//                finalResult.addAll(inventoryList);
//                AlreadyTaken_Inv += inventoryList.size();
//            }
//
//            int remaining = pageSize - finalResult.size();
//            log.info("Remaining size after Inventory found: " + remaining);
//
//            if (remaining > 0 && request.seeServ == 1) {
//                List<AggregationOperation> servicePipeline = new ArrayList<>();
//
//                servicePipeline.add(match(
//                        Criteria.where("sellerid").is(new ObjectId(request.companyId)).and("isActive").is(true)
//                ));
//                if (hasCategories) {
//                    servicePipeline.add(match(
//                            Criteria.where("category._id")
//                                    .in(request.categories.stream().map(ObjectId::new).toList())
//                    ));
//                }
//                servicePipeline.add(project()
//                        .and("_id").as("inventoryId")
//                        .and("_id").as("ProductId")
//                        .and("sellerid").as("sellerid")
//                        .and("category._id").as("Category.CategoryId")
//                        .and("category.category_name").as("Category.CategoryName")
//                        .and("servicename").as("ProductName")
//                        .and("servicecost").as("SellingPrice")
//                        .and("defaultImage").as("Image")
//                        .and(context -> new Document("$literal", true)).as("isAttributePresent")
//                        .and(context -> new Document("$literal", true)).as("isService")
//                        .and("propertyAttributes").as("propertyAttributes")
//                        .and("addOnIds").as("addons")
//                        .and("created_at").as("CreatedAt")
//                );
//                servicePipeline.add(sort(Sort.by("ProductName")));
//                servicePipeline.add(skip((long) request.servTaken));
//                servicePipeline.add(limit(remaining));
//
//                List<InventoryProductsAndServiceResponse.ProductItem> serviceList =
//                        mongoTemplate.aggregate(
//                                newAggregation(servicePipeline),
//                                "ServiceInventory",
//                                InventoryProductsAndServiceResponse.ProductItem.class
//                        ).getMappedResults();
//
//                finalResult.addAll(serviceList);
//                AlreadyTaken_Serv += serviceList.size();
//            }
//
//            response.products = finalResult;
//            response.invTaken = AlreadyTaken_Inv;
//            response.servTaken = AlreadyTaken_Serv;
//            response.seeInv = AlreadyTaken_Inv >= Initial_Inv ? 0 : 1;
//            response.seeServ = AlreadyTaken_Serv >= Initial_Serv ? 0 : 1;
//            return response;
//
//        } else {
//            response.products = Collections.emptyList();
//            response.invTaken = AlreadyTaken_Inv;
//            response.servTaken = AlreadyTaken_Serv;
//            response.seeInv = 0;
//            response.seeServ = 0;
//            return response;
//        }
//    }

    public InventoryProductsAndServiceResponse searchProductsManageOrderSeller(InventoryProductsAndServiceRequest request) {
        log.info("Page No : " + request.page);
        log.info("IsToSeeInventory : " + request.seeInv);
        log.info("IsToSeeProduct : " + request.seeProd);
        log.info("IsToSeeService : " + request.seeServ);
        log.info("Taken -Inventory : " + request.invTaken);
        log.info("Taken -Service : " + request.servTaken);
        log.info("Taken -Product : " + request.prodTaken);

        InventoryProductsAndServiceResponse response = new InventoryProductsAndServiceResponse();

        if (request.companyId == null || request.companyId.isBlank()) {
            throw new IllegalArgumentException("companyId is required");
        }

        // ── pageSize: if not provided (null or <= 0), don't limit at all — take everything.
        boolean hasPageSize = request.pageSize != null && request.pageSize > 0;
        int pageSize = hasPageSize ? request.pageSize : Integer.MAX_VALUE;

        int AlreadyTaken_Inv = request.invTaken;
        int AlreadyTaken_Serv = request.servTaken;

        boolean hasSearch = request.searchValue != null && !request.searchValue.trim().isEmpty();
        boolean hasCategories = request.categories != null && !request.categories.isEmpty();

        // ── Base match criteria shared between the count query and the aggregation ──
        Criteria invBaseCriteria = new Criteria().andOperator(
                Criteria.where("companyid").is(request.companyId),
                Criteria.where("isActive").is(true),
                new Criteria().orOperator(
                        Criteria.where("batchid.active").is(true),
                        Criteria.where("variantBatches.active").is(true)
                )
        );

        Criteria servBaseCriteria = Criteria.where("sellerid")
                .is(new ObjectId(request.companyId))
                .and("isActive").is(true);
        if (hasSearch) {
            servBaseCriteria = servBaseCriteria.and("servicename")
                    .regex(".*" + Pattern.quote(request.searchValue.trim()) + ".*", "i");
        }

        // ── Totals for THIS filter set (search + categories applied) — drives seeInv/seeServ ──
        int Initial_Inv = 0;
        if (request.seeInv == 1) {
            List<AggregationOperation> countPipeline = new ArrayList<>();
            countPipeline.add(match(invBaseCriteria));
            countPipeline.add(lookup("products", "productid", "_id", "product"));
            countPipeline.add(unwind("product", true));
            if (hasCategories) {
                countPipeline.add(match(
                        Criteria.where("product.productCategory._id")
                                .in(request.categories.stream().map(ObjectId::new).toList())
                ));
            }
            if (hasSearch) {
                String searchRegex = ".*" + Pattern.quote(request.searchValue.trim()) + ".*";
                countPipeline.add(match(
                        new Criteria().orOperator(
                                Criteria.where("productName").regex(searchRegex, "i"),
                                Criteria.where("variantBatches.productname").regex(searchRegex, "i"),
                                Criteria.where("variantBatches.variantName").regex(searchRegex, "i")
                        )
                ));
            }
            countPipeline.add(Aggregation.count().as("total"));

            var countResult = mongoTemplate.aggregate(newAggregation(countPipeline), "inventory", Document.class)
                    .getUniqueMappedResult();
            Initial_Inv = countResult != null ? countResult.getInteger("total", 0) : 0;
        }

        int Initial_Serv = request.seeServ == 1
                ? (int) mongoTemplate.count(Query.query(servBaseCriteria), "ServiceInventory")
                : 0;

        log.info("Initial -Inventory for this seller (filtered) : " + Initial_Inv);
        log.info("Initial -ServiceInventory for this seller (filtered) : " + Initial_Serv);

        boolean inventoryExists = false;
        boolean ServiceInventoryExists = false;

        if (hasSearch) {
            inventoryExists = mongoTemplate.exists(
                    Query.query(
                            new Criteria().andOperator(
                                    Criteria.where("companyid").is(request.companyId)
                                            .orOperator(
                                                    Criteria.where("batchid.active").is(true),
                                                    Criteria.where("variantBatches.active").is(true)
                                            ),
                                    new Criteria().orOperator(
                                            Criteria.where("batchid.productname")
                                                    .regex(".*" + Pattern.quote(request.searchValue) + ".*", "i"),
                                            Criteria.where("variantBatches.productname")
                                                    .regex(".*" + Pattern.quote(request.searchValue) + ".*", "i"),
                                            Criteria.where("variantBatches.variantName")
                                                    .regex(".*" + Pattern.quote(request.searchValue) + ".*", "i")
                                    )
                            )
                    ),
                    "inventory"
            );

            ServiceInventoryExists = mongoTemplate.exists(
                    Query.query(
                            Criteria.where("sellerid")
                                    .is(new ObjectId(request.companyId))
                                    .and("isActive").is(true)
                                    .and("servicename").regex(
                                            ".*" + Pattern.quote(request.searchValue) + ".*", "i"
                                    )
                    ),
                    "ServiceInventory"
            );
        }

        log.info("inventoryExists : " + inventoryExists);
        log.info("SearchValue : " + request.searchValue);
        log.info("ServiceInventoryExists : " + ServiceInventoryExists);

        List<InventoryProductsAndServiceResponse.ProductItem> resultList = new ArrayList<>();

        if (inventoryExists || ServiceInventoryExists) {

            // ================= INVENTORY =================
            List<AggregationOperation> pipeline = new ArrayList<>();
            if (request.seeInv == 1) {

                pipeline.add(match(invBaseCriteria));
                pipeline.add(lookup("products", "productid", "_id", "product"));
                pipeline.add(unwind("product", true));

                if (hasCategories) {
                    pipeline.add(match(
                            Criteria.where("product.productCategory._id")
                                    .in(request.categories.stream().map(ObjectId::new).toList())
                    ));
                }
                if (hasSearch) {
                    String searchRegex = ".*" + Pattern.quote(request.searchValue.trim()) + ".*";
                    pipeline.add(match(
                            new Criteria().orOperator(
                                    Criteria.where("productName").regex(searchRegex, "i"),
                                    Criteria.where("variantBatches.productname").regex(searchRegex, "i"),
                                    Criteria.where("variantBatches.variantName").regex(searchRegex, "i")
                            )
                    ));
                }

                pipeline.add(project()
                        .and("_id").as("inventoryId")
                        .and("productid").as("ProductId")
                        .and("companyid").as("sellerid")
                        .and("productCategory").as("productCategoryDTO")
                        .and(
                                ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.productname").elementAt(0)
                                ).thenValueOf("batchid.productname")
                        ).as("ProductName")
                        .and(
                                ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.created_at").elementAt(0)
                                ).thenValueOf("batchid.created_at")
                        ).as("CreatedAt")
                        .and(
                                ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.stock_availability").elementAt(0)
                                ).thenValueOf("batchid.stock_availability")
                        ).as("AvilableQty")
                        .and(
                                ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches._id").elementAt(0)
                                ).thenValueOf("batchid._id")
                        ).as("Batch_Id")
                        .and(
                                context -> new Document("$map",
                                        new Document("input", "$variantBatches")
                                                .append("as", "v")
                                                .append("in",
                                                        new Document("variantsid", "$$v._id")
                                                                .append("variantsName", "$$v.variantName")
                                                                .append("SellingPrice", "$$v.seller_price")
                                                                .append("OfferPrice", "$$v.offerPrice")
                                                                .append("DiscountPercent", "$$v.discount")
                                                                .append("AvilableQty", "$$v.stock_availability")
                                                                .append("StockStatus",
                                                                        new Document("$cond", Arrays.asList(
                                                                                new Document("$gt", Arrays.asList("$$v.stock_availability", 0)),
                                                                                "IN STOCK", "OUT OF STOCK"
                                                                        )))
                                                                .append("offerText",
                                                                        new Document("$concat", Arrays.asList(
                                                                                "₹", new Document("$toString", "$$v.discount"), " OFF"
                                                                        )))
                                                )
                                )
                        ).as("variants")
                        .and("Quickadd").as("Quickadd")
                        .and(
                                ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.Batch_Id").elementAt(0)
                                ).thenValueOf("batchid.Batch_Id")
                        ).as("uniquebatchid")
                        .and(
                                ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.seller_price").elementAt(0)
                                ).thenValueOf("batchid.seller_price")
                        ).as("SellingPrice")
                        .and(
                                ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.offerPrice").elementAt(0)
                                ).thenValueOf("batchid.offerPrice")
                        ).as("OfferPrice")
                        .and(
                                ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.discount").elementAt(0)
                                ).thenValueOf("batchid.discount")
                        ).as("DiscountPercent")
                        .and("product.defaultImage").as("Image")
                        .and("product.brand").as("Brand")
                        .and(String.valueOf(false)).as("isAttributePresent")
                        .and(String.valueOf(false)).as("isService")
                        .and("product.productCategory._id").as("Category.CategoryId")
                        .and("product.productCategory.category_name").as("Category.CategoryName")
                        .and("product.unit").as("batch_unit")
                );

                pipeline.add(sort(Sort.by(Sort.Direction.ASC, "ProductName")));
                pipeline.add(skip((long) request.invTaken));
                // Only limit when a real pageSize was requested — otherwise take everything.
                if (hasPageSize) {
                    pipeline.add(limit(pageSize));
                }

                List<InventoryProductsAndServiceResponse.ProductItem> inventoryResults =
                        mongoTemplate.aggregate(
                                newAggregation(pipeline),
                                "inventory",
                                InventoryProductsAndServiceResponse.ProductItem.class
                        ).getMappedResults();

                resultList.addAll(inventoryResults);
                AlreadyTaken_Inv += inventoryResults.size();
            }

            int remaining = hasPageSize ? pageSize - resultList.size() : Integer.MAX_VALUE;
            log.info("Remaining size after Inventory found: " + remaining);

            response.products = resultList;
            response.invTaken = AlreadyTaken_Inv;
            response.servTaken = AlreadyTaken_Serv;
            response.seeInv = AlreadyTaken_Inv >= Initial_Inv ? 0 : 1;
            response.seeServ = AlreadyTaken_Serv >= Initial_Serv ? 0 : 1;

            if (hasPageSize && remaining <= 0) {
                return response;
            }

            // ================= SERVICE INVENTORY =================
            if (request.seeServ == 1) {
                List<AggregationOperation> servicePipeline = new ArrayList<>();
                servicePipeline.add(match(servBaseCriteria));
                servicePipeline.add(project()
                        .and("_id").as("_id")
                        .and("servicename").as("ProductName")
                        .and("servicecost").as("SellingPrice")
                        .and("category").as("serviceCategory")
                        .and("created_at").as("CreatedAt")
                        .and("propertyAttributes").as("propertyAttributes")
                        .and("defaultImage").as("defaultImage"));
                servicePipeline.add(sort(Sort.by(Sort.Direction.ASC, "ProductName")));
                servicePipeline.add(skip((long) request.servTaken));
                // Only limit when bounded — "remaining" is only a real cap if pageSize was set.
                if (hasPageSize) {
                    servicePipeline.add(limit(remaining));
                }

                List<InventoryProductsAndServiceResponse.ProductItem> serviceResults =
                        mongoTemplate.aggregate(
                                newAggregation(servicePipeline),
                                "ServiceInventory",
                                InventoryProductsAndServiceResponse.ProductItem.class
                        ).getMappedResults();

                resultList.addAll(serviceResults);
                AlreadyTaken_Serv += serviceResults.size();
            }

            response.products = resultList;
            response.invTaken = AlreadyTaken_Inv;
            response.servTaken = AlreadyTaken_Serv;
            response.seeInv = AlreadyTaken_Inv >= Initial_Inv ? 0 : 1;
            response.seeServ = AlreadyTaken_Serv >= Initial_Serv ? 0 : 1;
            return response;

        } else if (!hasSearch) {

            log.info("No search match, returning inventory + services (priority)");

            List<InventoryProductsAndServiceResponse.ProductItem> finalResult = new ArrayList<>();

            List<AggregationOperation> inventoryPipeline = new ArrayList<>();
            if (request.seeInv == 1) {
                inventoryPipeline.add(match(invBaseCriteria));
                inventoryPipeline.add(lookup("products", "productid", "_id", "product"));
                inventoryPipeline.add(unwind("product", true));

                if (request.deals) {
                    inventoryPipeline.add(match(Criteria.where("deals").is(true)));
                }
                if (request.newArrivals) {
                    inventoryPipeline.add(match(Criteria.where("newArrivals").is(true)));
                }
                if (hasCategories) {
                    inventoryPipeline.add(match(
                            Criteria.where("productCategory._id")
                                    .in(request.categories.stream().map(ObjectId::new).toList())
                    ));
                }

                inventoryPipeline.add(project()
                        .and("_id").as("inventoryId")
                        .and("productid").as("ProductId")
                        .and("companyid").as("sellerid")
                        .and("productCategory").as("productCategoryDTO")
                        .and(
                                ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.productname").elementAt(0)
                                ).thenValueOf("batchid.productname")
                        ).as("ProductName")
                        .and(
                                ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.created_at").elementAt(0)
                                ).thenValueOf("batchid.created_at")
                        ).as("CreatedAt")
                        .and(
                                ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.stock_availability").elementAt(0)
                                ).thenValueOf("batchid.stock_availability")
                        ).as("AvilableQty")
                        .and(
                                ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches._id").elementAt(0)
                                ).thenValueOf("batchid._id")
                        ).as("Batch_Id")
                        .and(
                                context -> new Document("$map",
                                        new Document("input", "$variantBatches")
                                                .append("as", "v")
                                                .append("in",
                                                        new Document("variantsid", "$$v._id")
                                                                .append("variantsName", "$$v.variantName")
                                                                .append("SellingPrice", "$$v.seller_price")
                                                                .append("OfferPrice", "$$v.offerPrice")
                                                                .append("DiscountPercent", "$$v.discount")
                                                                .append("AvilableQty", "$$v.stock_availability")
                                                                .append("StockStatus",
                                                                        new Document("$cond", Arrays.asList(
                                                                                new Document("$gt", Arrays.asList("$$v.stock_availability", 0)),
                                                                                "IN STOCK", "OUT OF STOCK"
                                                                        )))
                                                                .append("offerText",
                                                                        new Document("$concat", Arrays.asList(
                                                                                "₹", new Document("$toString", "$$v.discount"), " OFF"
                                                                        )))
                                                )
                                )
                        ).as("variants")
                        .and("Quickadd").as("Quickadd")
                        .and(
                                ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.Batch_Id").elementAt(0)
                                ).thenValueOf("batchid.Batch_Id")
                        ).as("uniquebatchid")
                        .and(
                                ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.seller_price").elementAt(0)
                                ).thenValueOf("batchid.seller_price")
                        ).as("SellingPrice")
                        .and(
                                ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.offerPrice").elementAt(0)
                                ).thenValueOf("batchid.offerPrice")
                        ).as("OfferPrice")
                        .and(
                                ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.discount").elementAt(0)
                                ).thenValueOf("batchid.discount")
                        ).as("DiscountPercent")
                        .and("product.brand").as("Brand")
                        .and("product.defaultImage").as("Image")
                        .and(String.valueOf(false)).as("isAttributePresent")
                        .and(String.valueOf(false)).as("isService")
                        .and("product.productCategory._id").as("Category.CategoryId")
                        .and("product.productCategory.category_name").as("Category.CategoryName")
                        .and("product.unit").as("batch_unit")
                );

                inventoryPipeline.add(sort(Sort.by(
                        Sort.Order.desc("CreatedAt"),
                        Sort.Order.asc("ProductName")
                )));
                inventoryPipeline.add(skip((long) request.invTaken));
                if (hasPageSize) {
                    inventoryPipeline.add(limit(pageSize));
                }

                List<InventoryProductsAndServiceResponse.ProductItem> inventoryList =
                        mongoTemplate.aggregate(
                                newAggregation(inventoryPipeline),
                                "inventory",
                                InventoryProductsAndServiceResponse.ProductItem.class
                        ).getMappedResults();

                finalResult.addAll(inventoryList);
                AlreadyTaken_Inv += inventoryList.size();
            }

            int remaining = hasPageSize ? pageSize - finalResult.size() : Integer.MAX_VALUE;
            log.info("Remaining size after Inventory found: " + remaining);

            if ((!hasPageSize || remaining > 0) && request.seeServ == 1) {
                List<AggregationOperation> servicePipeline = new ArrayList<>();

                servicePipeline.add(match(
                        Criteria.where("sellerid").is(new ObjectId(request.companyId)).and("isActive").is(true)
                ));
                if (hasCategories) {
                    servicePipeline.add(match(
                            Criteria.where("category._id")
                                    .in(request.categories.stream().map(ObjectId::new).toList())
                    ));
                }
                servicePipeline.add(project()
                        .and("_id").as("inventoryId")
                        .and("_id").as("ProductId")
                        .and("sellerid").as("sellerid")
                        .and("category._id").as("Category.CategoryId")
                        .and("category.category_name").as("Category.CategoryName")
                        .and("servicename").as("ProductName")
                        .and("servicecost").as("SellingPrice")
                        .and("defaultImage").as("Image")
                        .and(context -> new Document("$literal", true)).as("isAttributePresent")
                        .and(context -> new Document("$literal", true)).as("isService")
                        .and("propertyAttributes").as("propertyAttributes")
                        .and("addOnIds").as("addons")
                        .and("created_at").as("CreatedAt")
                );
                servicePipeline.add(sort(Sort.by("ProductName")));
                servicePipeline.add(skip((long) request.servTaken));
                if (hasPageSize) {
                    servicePipeline.add(limit(remaining));
                }

                List<InventoryProductsAndServiceResponse.ProductItem> serviceList =
                        mongoTemplate.aggregate(
                                newAggregation(servicePipeline),
                                "ServiceInventory",
                                InventoryProductsAndServiceResponse.ProductItem.class
                        ).getMappedResults();

                finalResult.addAll(serviceList);
                AlreadyTaken_Serv += serviceList.size();
            }

            response.products = finalResult;
            response.invTaken = AlreadyTaken_Inv;
            response.servTaken = AlreadyTaken_Serv;
            response.seeInv = AlreadyTaken_Inv >= Initial_Inv ? 0 : 1;
            response.seeServ = AlreadyTaken_Serv >= Initial_Serv ? 0 : 1;
            return response;

        } else {
            response.products = Collections.emptyList();
            response.invTaken = AlreadyTaken_Inv;
            response.servTaken = AlreadyTaken_Serv;
            response.seeInv = 0;
            response.seeServ = 0;
            return response;
        }
    }

    public InventoryProductsAndServiceResponse searchProductById(searchProductByIdRequest request) {

        InventoryProductsAndServiceResponse response = new InventoryProductsAndServiceResponse();
        List<InventoryProductsAndServiceResponse.ProductItem> finalResult = new ArrayList<>();

        List<AggregationOperation> inventoryPipeline = new ArrayList<>();

        // Match company + active batches
        inventoryPipeline.add(
                match(
                        Criteria.where("companyid").is(request.companyId)
                                .orOperator(
                                        Criteria.where("batchid.active").is(true),
                                        Criteria.where("variantBatches.active").is(true)
                                )
                )
        );

        // Lookup product
        inventoryPipeline.add(
                lookup("products", "productid", "_id", "product")
        );

        inventoryPipeline.add(unwind("product", true));

        // Product filter
        if (request.productId != null) {
            inventoryPipeline.add(
                    match(Criteria.where("productid").is(request.productId))
            );
        }

        // Projection
        inventoryPipeline.add(
                project()

                        .and("_id").as("inventoryId")
                        .and("productid").as("ProductId")
                        .and("companyid").as("sellerid")
                        .and("product.productDescription").as("description")
                        .and("productCategory").as("productCategoryDTO")

                        // Product Name
                        .and(
                                ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt
                                                .arrayOf("variantBatches.productname")
                                                .elementAt(0)
                                ).thenValueOf("batchid.productname")
                        ).as("ProductName")

                        // Created At
                        .and(
                                ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt
                                                .arrayOf("variantBatches.created_at")
                                                .elementAt(0)
                                ).thenValueOf("batchid.created_at")
                        ).as("CreatedAt")

                        // Available Qty
                        .and(
                                ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt
                                                .arrayOf("variantBatches.stock_availability")
                                                .elementAt(0)
                                ).thenValueOf("batchid.stock_availability")
                        ).as("AvilableQty")

                        // Batch Id
                        .and(
                                ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt
                                                .arrayOf("variantBatches._id")
                                                .elementAt(0)
                                ).thenValueOf("batchid._id")
                        ).as("Batch_Id")

                        // Variants Mapping
                        .and(
                                context -> new Document(
                                        "$map",
                                        new Document("input", "$variantBatches")
                                                .append("as", "v")
                                                .append(
                                                        "in",
                                                        new Document("variantsid", "$$v._id")
                                                                .append("variantsName", "$$v.variantName")
                                                                .append("SellingPrice", "$$v.seller_price")
                                                                .append("OfferPrice", "$$v.offerPrice")
                                                                .append("DiscountPercent", "$$v.discount")
                                                                .append("AvilableQty", "$$v.stock_availability")
                                                                .append(
                                                                        "StockStatus",
                                                                        new Document(
                                                                                "$cond",
                                                                                Arrays.asList(
                                                                                        new Document(
                                                                                                "$gt",
                                                                                                Arrays.asList(
                                                                                                        "$$v.stock_availability",
                                                                                                        0
                                                                                                )
                                                                                        ),
                                                                                        "IN STOCK",
                                                                                        "OUT OF STOCK"
                                                                                )
                                                                        )
                                                                )
                                                                .append(
                                                                        "offerText",
                                                                        new Document(
                                                                                "$concat",
                                                                                Arrays.asList(
                                                                                        "₹",
                                                                                        new Document(
                                                                                                "$toString",
                                                                                                "$$v.discount"
                                                                                        ),
                                                                                        " OFF"
                                                                                )
                                                                        )
                                                                )
                                                )
                                )
                        ).as("variants")

                        .and("Quickadd").as("Quickadd")

                        // Unique Batch Id
                        .and(
                                ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt
                                                .arrayOf("variantBatches.Batch_Id")
                                                .elementAt(0)
                                ).thenValueOf("batchid.Batch_Id")
                        ).as("uniquebatchid")

                        // Selling Price
                        .and(
                                ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt
                                                .arrayOf("variantBatches.seller_price")
                                                .elementAt(0)
                                ).thenValueOf("batchid.seller_price")
                        ).as("SellingPrice")

                        // Offer Price
                        .and(
                                ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt
                                                .arrayOf("variantBatches.offerPrice")
                                                .elementAt(0)
                                ).thenValueOf("batchid.offerPrice")
                        ).as("OfferPrice")

                        // Discount Percent
                        .and(
                                ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt
                                                .arrayOf("variantBatches.discount")
                                                .elementAt(0)
                                ).thenValueOf("batchid.discount")
                        ).as("DiscountPercent")

                        .and("product.defaultImage").as("Image")

                        .andExclude("_id")

//                        .andExpression("false").as("isAttributePresent")
//                        .andExpression("false").as("isService")

                        .and("product.productCategory._id").as("Category.CategoryId")
                        .and("product.productCategory.category_name").as("Category.CategoryName")

                        .and("product.unit").as("batch_unit")
        );

        // Sorting
        inventoryPipeline.add(
                sort(
                        Sort.by(
                                Sort.Order.desc("CreatedAt"),
                                Sort.Order.asc("ProductName")
                        )
                )
        );

        // Limit
        inventoryPipeline.add(limit(1));

        // Execute aggregation
        List<InventoryProductsAndServiceResponse.ProductItem> inventoryList =
                mongoTemplate.aggregate(
                        newAggregation(inventoryPipeline),
                        "inventory",
                        InventoryProductsAndServiceResponse.ProductItem.class
                ).getMappedResults();

        if (inventoryList != null) {
            finalResult.addAll(inventoryList);
        }


        Criteria baseCriteria = Criteria.where("_id")
                .is(new ObjectId(String.valueOf(request.productId)))
                .and("sellerid").is(new ObjectId(request.companyId))
                .and("isActive").is(true);

        List<InventoryProductsAndServiceResponse.ProductItem> serviceResults =
                mongoTemplate.aggregate(
                        newAggregation(
                                match(baseCriteria),

                                project()
                                        .and("_id").as("inventoryId")
                                        .and("servicename").as("ProductName")
                                        .and("servicecost").as("SellingPrice")
                                        .and("category").as("serviceCategory")
                                        .and("created_at").as("CreatedAt")
                                        .and("propertyAttributes").as("propertyAttributes")
                                        .and("defaultImage").as("Image")
                                        .andExpression("true").as("isService"),

                                sort(Sort.by(Sort.Direction.ASC, "ProductName"))
                        ),
                        "ServiceInventory",
                        InventoryProductsAndServiceResponse.ProductItem.class
                ).getMappedResults();

        if (serviceResults != null) {
            finalResult.addAll(serviceResults);
        }
        response.products = finalResult;
        return response;
    }

    private void logPriceDetails(InventoryDTO item, double requestPrice, double requestOffer, double dbPrice, double dbOffer) {
        log.info("Checking Product: {}", item.getProductid());
        log.info("Request Seller Price: {}", requestPrice);
        log.info("Request Offer Price : {}", requestOffer);
        log.info("DB Seller Price     : {}", dbPrice);
        log.info("DB Offer Price      : {}", dbOffer);
    }

    private void performStockAndPriceValidation(InventoryDTO item, Inventory inventory, Batches batch) {

        int availableQty = batch.getStock_availability();
        int orderedQty = item.getOrder_quantity();

        double requestedSellingPrice = item.getSellerprice();

        double requestedOfferPrice = (item.variantBatches != null &&
                !item.variantBatches.isEmpty())
                ? item.variantBatches.get(0).offerPrice
                : item.offerPrice;

        double dbSellingPrice = batch.getSeller_price();
        double dbOfferPrice = batch.getOfferPrice();

        logPriceDetails(item, requestedSellingPrice, requestedOfferPrice,
                dbSellingPrice, dbOfferPrice);

        boolean priceMatched = Double.compare(requestedSellingPrice, dbSellingPrice) == 0;

        boolean offerMatched = requestedOfferPrice > 0 &&
                dbOfferPrice > 0 &&
                Double.compare(requestedOfferPrice, dbOfferPrice) == 0;

        boolean validPrice = priceMatched || offerMatched;

        if (orderedQty <= availableQty && validPrice) {
            item.setMessage("");
            item.setRemaining_quantity(availableQty - orderedQty);
            return;
        }

        if (orderedQty <= availableQty) {
            item.setMessage(item.productname +
                    " - Qty is available but price changed. Please reload the page to complete order.");
            return;
        }

        if (!validPrice) {
            item.setMessage(item.productname +
                    " - Quantity and price updated for some items. Please reload the page to complete order.");
            return;
        }

        checkNextBatchAvailability(item, inventory, orderedQty, requestedSellingPrice);

        if (item.getMessage() == null || item.getMessage().isEmpty()) {
            item.setMessage(item.productname +
                    " - Less items in stock. Review order quantity to complete order.");
        }
    }

    private void checkNextBatchAvailability(InventoryDTO item, Inventory inventory, int orderedQty, double sellingPrice) {

        List<Batches> batchList = batchesRepo.findByProductidAndCompanyid(
                item.getProductid(),
                item.getCompanyid(),
                Sort.by(Sort.Direction.ASC, "created_at"));

        int currentIndex = batchList.indexOf(inventory.getBatchid());

        if (currentIndex >= 0 && currentIndex + 1 < batchList.size()) {
            Batches nextBatch = batchList.get(currentIndex + 1);
            if (nextBatch.getStock_availability() >= orderedQty &&
                    Double.compare(nextBatch.getProcurement_price(), sellingPrice) == 0) {

                item.setMessage(item.productname + " - Lesser items in current batch. Review order quantity/inventory to complete order.");
            }
        }
    }

    private void validateInventory(InventoryDTO item, List<Inventory> inventoryList) {

        Inventory inventory = findMatchingInventory(item, inventoryList);

        if (inventory == null) {
            item.setMessage("Inventory missing. Please refresh and try again.");
            return;
        }

        Batches batch = findMatchingBatch(item, inventory);

        if (batch == null) {
            item.setMessage("Inventory details changed. Please reload the page to complete the order.");
            return;
        }
        performStockAndPriceValidation(item, inventory, batch);
    }

    private Inventory findMatchingInventory(InventoryDTO item, List<Inventory> inventoryList) {

        return inventoryList.stream()
                .filter(inv ->
                        Objects.equals(inv.getProductid(), item.getProductid()) &&
                                Objects.equals(inv.getCompanyid(), item.getCompanyid()))
                .findFirst()
                .orElse(null);
    }

    private Batches findMatchingBatch(InventoryDTO item, Inventory inventory) {

        if (inventory.getBatchid() != null &&
                Objects.equals(inventory.getBatchid().get_id(), item.batch_id)) {
            return inventory.getBatchid();
        }

        if (inventory.getVariantBatches() != null) {
            return inventory.getVariantBatches()
                    .stream()
                    .filter(batch ->
                            Objects.equals(batch.get_id(), item.batch_id))
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }

    public List<InventoryDTO> precheck(List<Inventory> inventoryList, List<InventoryDTO> requestedItems) {
        log.info("Inside precheck");
//        requestedItems.forEach(item -> validateInventory(item, inventoryList));
        requestedItems.forEach(item -> {
            log.info("BEFORE validation - product: {}, message: {}",
                    item.productname, item.getMessage());

            validateInventory(item, inventoryList);

            log.info("AFTER validation - product: {}, message: {}",
                    item.productname, item.getMessage());
        });
        return requestedItems;
    }

    public PlaceOrderResponse placeOrderForBuyer(BuyerOrderRequest request) {
        PlaceOrderResponse placeOrderResponse = new PlaceOrderResponse();
        ClientSession session = null;
        try {
            session = mongoClient.startSession();
            session.startTransaction();
            List<ObjectId> ProductinventoryIds = new ArrayList<>();

            List<InventoryDTO> Serviceinventory = new ArrayList<>();
            List<InventoryDTO> Productinventory = new ArrayList<>();


            for (InventoryDTO i : request.inventory) {
                log.info("productname: " + i.productname);
                log.info("that product Ordered stock : " + i.sellerstock);
                String flag = i.flag != null ? i.flag : "";
                if ("isService".equals(flag)) {
                    Serviceinventory.add(i);
                } else {
                    Productinventory.add(i);
                    ProductinventoryIds.add(new ObjectId(i._id));
                }
            }
            log.info("Productinventory size : " + Productinventory.size());
            log.info("Serviceinventory size : " + Serviceinventory.size());
//
            List<Inventory> Productinventories = inventoryRepo.findByIdInAndCompanyId(String.valueOf(request.companyid), ProductinventoryIds);
//            log.debug("ProductInventories from Repo: " , Productinventories.size());

            List<InventoryDTO> checkedItems = new ArrayList<>();
            List<InventoryDTO> validItems = new ArrayList<>();

            Optional<Seller> seller = sellerRepo.findById(String.valueOf(request.companyid));

            if (!seller.isPresent()) {
                if (session != null && session.hasActiveTransaction()) {
                    session.abortTransaction();
                }

                placeOrderResponse.message = "seller not found";
                placeOrderResponse.successCode = "400";
                return placeOrderResponse;
            }

            if (request.sellerConfigRewards != null) {
                Seller.Rewards sellerRewards = seller.get().getRewards();


                if (sellerRewards == null) {

                    if (session != null && session.hasActiveTransaction()) {
                        session.abortTransaction();
                    }

                    placeOrderResponse.message = "seller reward config not found";
                    placeOrderResponse.successCode = "400";
                    return placeOrderResponse;
                }


                boolean rewardMismatch =
                        sellerRewards.getRewardAmount() != request.sellerConfigRewards.RewardAmount
                                ||
                                sellerRewards.getRewardPoints() != request.sellerConfigRewards.RewardPoints
                                ||
                                sellerRewards.getRedeeemAmount() != request.sellerConfigRewards.RedeeemAmount
                                ||
                                sellerRewards.getRedeemPoints() != request.sellerConfigRewards.RedeemPoints;


                if (rewardMismatch) {
                    if (session != null && session.hasActiveTransaction()) {
                        session.abortTransaction();
                    }

                    placeOrderResponse.message = "reward configuration changed, please refresh";
                    placeOrderResponse.successCode = "400";
                    return placeOrderResponse;
                }
            }
            if (request.deliveryFee != 0 && request.orderThreshold != 0) {
                Seller.DeliveryFeeDetails a = seller.get().getDeliveryFeeDetails();

                if (a == null) {
                    if (session != null && session.hasActiveTransaction()) {
                        session.abortTransaction();
                    }
                    placeOrderResponse.message = "seller DeliveryFeeDetails config not found";
                    placeOrderResponse.successCode = "400";
                    return placeOrderResponse;
                }
                boolean rewardMismatch = a.getDeliveryFee() != request.deliveryFee || a.getOrderThreshold() != request.orderThreshold;
                if (rewardMismatch) {
                    if (session != null && session.hasActiveTransaction()) {
                        session.abortTransaction();
                    }

                    placeOrderResponse.message = "reward configuration changed, please refresh";
                    placeOrderResponse.successCode = "400";
                    return placeOrderResponse;
                }
            }

            if (!Productinventories.isEmpty()) {
                checkedItems = precheck(Productinventories, Productinventory);
                log.info("CheckedItems: " + checkedItems.size());

                validItems = checkedItems.stream()
                        .filter(item -> item.getMessage() == null || item.getMessage().isEmpty())
                        .collect(Collectors.toList());
                log.info("validItems: " + validItems.size());

                List<String> msg = checkedItems.stream()
                        .map(item -> item.getMessage())
                        .filter(message -> message != null && !message.isEmpty())
                        .distinct()
                        .collect(Collectors.toList());

                for (String message : msg) {
                    log.info("msg: {}", message);
                }

                if (validItems.size() != Productinventory.size()) {
                    placeOrderResponse.errorMessages.addAll(msg);
                    return placeOrderResponse;
                }
            }

            // Add services to validItems regardless
            validItems.addAll(Serviceinventory);
            log.info("validItems: " + validItems.size());

            List<Inventory> finalItems = SellerAdaptor.FromInventoryDTOtoInventory(validItems);
            log.info("FinalItems: " + finalItems.size());

            String refno = request.refno;
            log.info("can i get refno from UI:" + refno);
            log.info("can i get refno from Save_id:" + request.save_id);


            if (request.save_id != null && !request.save_id.trim().isEmpty()) {
                Save save = mongoTemplate.withSession(session).findById(request.save_id, Save.class);
                if (save != null && save.getOrderStatus().equals("Draft")) {
                    refno = save.getRefno();
                }
            }

            LocalDateTime now = LocalDateTime.now();

            List<Save> all = saveOrderRepo.findWebOrdersByCompanyid(request.companyid);

            all.sort(Comparator.comparing(Save::getOrderDate).reversed());

            if (all.size() > 0) {
                log.info("Last refno : " + all.get(0).getRefno());
            }

            Save latest = all.isEmpty() ? null : all.get(0);

            Save newSave = BuyerAdaptor.TosaveModel(request, finalItems, now.toString(), refno, latest, request.customerInformationDTO, request.paymentsDTO);

            Payments Current_payments = SellerAdaptor.fromPaymentsDTOtoPayments(request.paymentsDTO);
            log.info("Current_payments : " + Current_payments);

            Current_payments.setPayment_status("UnPaid");
            newSave.setPayment_status("UnPaid");
            newSave.setOrderStatus("Draft");
            Current_payments.setPayment_mode(request.payment_mode);
            newSave.setTotal_Discount(Current_payments.getDiscount());
            if (!Current_payments.getPayment_mode().equals("RAZORPAY")) {
                newSave.setTotal_AmountDue(Current_payments.getTotal_amount());
            }
            newSave.setTotal_Paid(Current_payments.getCurrent_payment_amount());


            newSave.setPaymentDetails(Current_payments);

            //CutomerInfo
            String name = request.customerInformationDTO.customerName;
            String mobileNum = request.customerInformationDTO.customerMobileNum;
            String add = request.customerInformationDTO.deliveryAddress;
            String email = request.customerInformationDTO.email;
            String state = request.customerInformationDTO.state;
            String city = request.customerInformationDTO.city;
            String postalCode = request.customerInformationDTO.postalCode;


            CustomerInformation a = new CustomerInformation();
            if (name != null) a.setCustomerName(name);
            if (mobileNum != null) a.setCustomerMobileNum(mobileNum);
            if (add != null) {
                a.setDeliveryAddress(add);

            }
            if (email != null) {
                a.setEmail(email);
            }
            if (state != null) a.setState(state);
            if (city != null) a.setCity(city);
            if (postalCode != null) a.setPostalCode(postalCode);
            if (request.delivery_mode != null && request.delivery_mode.equals("Home Delivery")) {
                newSave.setDelivery_details(add + "," + city + "," + state + "," + postalCode);
            } else {
                newSave.setDelivery_details(request.address);
            }
            newSave.setCustomerInformation(a);
            if (request.delivery_mode != null) {
                if (request.delivery_mode.equals("Home Delivery")) {
                    newSave.setDelivery_mode("DELIVERY");
                } else {
                    newSave.setDelivery_mode("PICKUP");
                }
            }


            Save inserted = newSave;
            if (request.buyerid != null) {

                Buyer buyer = mongoTemplate.withSession(session).findById(request.buyerid, Buyer.class);
                if (buyer != null && request.delivery_mode.toLowerCase().equals("delivery") || request.delivery_mode.toLowerCase().equals("home delivery")) {
                    Buyer existingBuyer = buyer;
                    newSave.setBuyerid(existingBuyer);
                    List<Buyer.LastDeliveryAddess> lastDeliveryAddesses =
                            existingBuyer.getLastDeliveryAddesses() != null
                                    ? new ArrayList<>(existingBuyer.getLastDeliveryAddesses())
                                    : new ArrayList<>();

                    Buyer.LastDeliveryAddess newAddress = new Buyer.LastDeliveryAddess(add, city, postalCode, state);

                    String finalAdd = add;
                    boolean alreadyExists = lastDeliveryAddesses.stream().anyMatch(addr ->
                            Objects.equals(addr.Address, finalAdd) &&
                                    Objects.equals(addr.city, city) &&
                                    Objects.equals(addr.postalCode, postalCode) &&
                                    Objects.equals(addr.state, state)
                    );
                    if (!alreadyExists) {
                        if (lastDeliveryAddesses.size() >= 3) {
                            lastDeliveryAddesses.remove(0);
                        }
                        lastDeliveryAddesses.add(newAddress);

                        existingBuyer.setLastDeliveryAddesses(lastDeliveryAddesses);

                        mongoTemplate
                                .withSession(session)
                                .save(existingBuyer);
                    }

                    Save.Rewards rewards = new Save.Rewards(request.rewards.RewardsEarned, request.rewards.rewardsRedeemed, false);
                    newSave.setRewards(rewards);
                }
            }


            if (request.save_id != null && !request.save_id.trim().isEmpty() && !request.save_id.equals("null")) {
                log.info("Inside already have save property");
                if (!ObjectId.isValid(request.save_id)) {
                    throw new IllegalArgumentException("Invalid save_id");
                }
                ObjectId saveId = new ObjectId(request.save_id);
                Save existing = mongoTemplate.withSession(session).findById(saveId, Save.class);
                if (existing == null) throw new RuntimeException("Order not found with id: " + saveId);
                if (request.payment_mode != null) {
                    if (!request.payment_mode.equals("RAZORPAY")) newSave.setOrderStatus("Review");
                    if (request.payment_mode.equals("ONLINEPAYMENT")) {
                        //Payment-mode: Pay online --> Payment status: Pending Payment ; Order Status: Review
                        Current_payments.setPayment_status("Pending Payment Verification");
                        newSave.setOrderStatus("Review");
                        newSave.setPayment_status("Pending Payment Verification");

                    }
                }

                existing.setOrderDate(newSave.getOrderDate());

                existing.setOrderStatus(newSave.getOrderStatus());
                existing.setPayment_mode(newSave.getPayment_mode());
                existing.setCompanyid(newSave.getCompanyid());
                existing.setTotal_Amount(newSave.getTotal_Amount());
                existing.setTotal_Paid(newSave.getTotal_Paid());
                existing.setUpdatedAt(newSave.getUpdatedAt());
                existing.setSeller_items(newSave.getSeller_items());
                existing.setPaymentDetails(Current_payments);
                existing.setCustomerInformation(newSave.getCustomerInformation());
                existing.setDelivery_details(newSave.getDelivery_details());
                existing.setDelivery_mode(newSave.getDelivery_mode());
                if (request.buyerid != null) {
                    Save.Rewards rewards = new Save.Rewards(request.rewards.RewardsEarned, request.rewards.rewardsRedeemed, false);
                    existing.setRewards(rewards);
                }

                inserted = existing;

                PaymentDetails paymentDetails1 = paymentDetailsRepo.findByOrderId(existing.get_id());

                if (paymentDetails1 != null) {
                    if (paymentDetails1.getPayments() == null) {
                        paymentDetails1.setPayments(new ArrayList<>());
                    }
                    List<Payments> payments = paymentDetails1.getPayments();
                    payments.add(Current_payments);
                    paymentDetails1.setPayments(payments);

                    mongoTemplate.withSession(session).save(paymentDetails1);
                }

                placeOrderResponse.message = "success";
                placeOrderResponse.successCode = "202";
                placeOrderResponse.refno = newSave.getRefno();


//                session.commitTransaction();
//                return placeOrderResponse;
            } else {
                log.info("Order saving... ");

                newSave.setOrderSource(request.orderStatus);
                if (request.payment_mode != null) {
                    if (!request.payment_mode.equals("RAZORPAY")) newSave.setOrderStatus("Review");
                    if (request.payment_mode.equals("ONLINEPAYMENT")) {
                        //Payment-mode: Pay online --> Payment status: Pending Payment ; Order Status: Review
                        Current_payments.setPayment_status("Pending Payment Verification");
                        newSave.setOrderStatus("Review");
                        newSave.setPayment_status("Pending Payment Verification");

                    }
                }
                inserted = mongoTemplate
                        .withSession(session)
                        .insert(newSave);


                PaymentDetails paymentDetails1 = new PaymentDetails();
                paymentDetails1.setOrderId(inserted.get_id());
                paymentDetails1.setPayments(new ArrayList<>(Collections.singletonList(Current_payments)));

                mongoTemplate.withSession(session).save(paymentDetails1);


            }
            if (Current_payments.getPayment_mode().equals("RAZORPAY")) {
                String receipt_id = inserted.getCompanyid() + "-" + inserted.getRefno();

                JSONObject razorResponse = createRazorPay(receipt_id, inserted.getPaymentDetails().getCurrent_payment_amount(), "INR");
                try {
                    log.info("razorResponse : {}",
                            objectMapper.writerWithDefaultPrettyPrinter()
                                    .writeValueAsString(razorResponse));
                } catch (Exception e) {
                    log.error("Error logging request", e);
                }
                if (razorResponse != null) {
                    RazorPaymentDetails converted = BuyerAdaptor.ToRazorPaymentModel(razorResponse);
                    if (converted != null) {


                        RazorPaymentFullDetails razorPaymentFullDetails = null;

                        if (inserted.getRazorpaymentdetails() != null && inserted.getRazorpaymentdetails().getId() != null) {
                            log.info("Already have razorPay created status " + inserted.getRazorpaymentdetails().getId());

                            razorPaymentFullDetails = razorPaymentFullDetailsRepo.findByOrderID(inserted.getRazorpaymentdetails().getId());
                        }

                        if (razorPaymentFullDetails == null) {
                            log.info("RazorPaymentFullDetails  is null");
                            razorPaymentFullDetails = new RazorPaymentFullDetails();
                            razorPaymentFullDetails.setRazorPaymentDetails(new ArrayList<>());
                        }

                        razorPaymentFullDetails.setOrder_id(razorResponse.getString("id"));
                        List<RazorPaymentDetails> merge = razorPaymentFullDetails.getRazorPaymentDetails();

                        if (merge == null) {
                            merge = new ArrayList<>();
                        }

                        merge.add(converted);
                        inserted.setRazorpaymentdetails(converted);
                        razorPaymentFullDetails.setRazorPaymentDetails(merge);
                        mongoTemplate.withSession(session).save(razorPaymentFullDetails);
                    }
                    if (razorResponse.getBoolean("success")) {
                        placeOrderResponse.razorStatus = razorResponse.getString("status") == null ? "Created" : razorResponse.getString("status");
                        placeOrderResponse.amount = razorResponse.getInt("amount");
                        placeOrderResponse.currency = razorResponse.getString("currency");
                        placeOrderResponse.customerName = name;
                        placeOrderResponse.customerMobile = mobileNum;
                        placeOrderResponse.email = email;
                        placeOrderResponse.saveid = inserted.get_id();
                        log.info("Razor pay generated id : " + razorResponse.getString("id"));
                        placeOrderResponse.razorPayid = razorResponse.getString("id");
                    } else {
                        placeOrderResponse.razorStatus = razorResponse.getString("error");
                        placeOrderResponse.reason = razorResponse.getString("description");
                        placeOrderResponse.successCode = "400";
                    }
                    inserted = mongoTemplate
                            .withSession(session)
                            .save(inserted);
                    log.info("Razorpay details saved");
                }
            } else {
                inserted = mongoTemplate
                        .withSession(session)
                        .save(inserted);
                log.info("Razorpay details saved");
            }

            placeOrderResponse.saveid = inserted.get_id();
            placeOrderResponse.message = "success";
            placeOrderResponse.successCode = "200";
            placeOrderResponse.refno = newSave.getRefno();
            session.commitTransaction();

            if (request.payment_mode != null && !request.payment_mode.equals("RAZORPAY")) {
                if (request.customerInformationDTO.email != null && !request.customerInformationDTO.email.equals("")) {
                    ResponseEntity<Map<String, String>> mailRes = sendMail(
                            new sendMailRequest(request.customerInformationDTO.email, String.valueOf(inserted.get_id())));
                    log.info("Mail Response Status: {}", mailRes.getStatusCode());
                    log.info("Mail Response Body: {}", mailRes.getBody());
                    log.info("Mail Response Headers: {}", mailRes.getHeaders());
                }
            }
            return placeOrderResponse;
        } catch (DuplicateKeyException e) {

            if (session != null && session.hasActiveTransaction()) {
                session.abortTransaction();
            }

            placeOrderResponse.message = e.getMessage();
            placeOrderResponse.successCode = "400";
            return placeOrderResponse;


        } catch (Exception e) {

            if (session != null && session.hasActiveTransaction()) {
                session.abortTransaction();
            }

            e.printStackTrace();

            placeOrderResponse.successCode = "400";
            placeOrderResponse.message = e.getMessage();

            return placeOrderResponse;
        }
    }

    public ViewOrderDetailsResponse ViewOrderDetails(ViewOrderDetailsRequest request, boolean flag, Save getSave) {
//        List<Save> saves = flag
//                ? Collections.singletonList(getSave)
//                : saveOrderRepo.findByCompanyAndRefnoOrOrderID(request.companyid, request.refno,request.refno.contains("BA")? null:new ObjectId(request.refno));

        List<Save> saves = flag
                ? Collections.singletonList(getSave) : new ArrayList<>();

        if (ObjectId.isValid(request.refno)) {

            saves = saveOrderRepo.findByCompanyidAndId(
                    request.companyid,
                    new ObjectId(request.refno)
            );

        } else {

            saves = saveOrderRepo.findByCompanyidAndRefnoRegex(
                    request.companyid,
                    request.refno
            );
        }
        if (saves == null || saves.isEmpty()) {
            log.info("No saves ");
            return new ViewOrderDetailsResponse();
        }
        log.info("Size : " + saves.size());


        Save save = saves.get(0);
        System.out.println("Refno : " + save.getRefno());

        List<ObjectId> ids = new ArrayList<>();

        if (save != null) {
            for (Inventory i : save.getSeller_items()) {
                ids.add(new ObjectId(i.get_id()));
            }
        }

        HashMap<String, Integer> map = new HashMap<>();
        if (ids.size() > 0) {
            List<Inventory> ans = inventoryRepo.findByIdInAndCompanyId(String.valueOf(request.companyid), ids);
            for (Inventory i : ans) {
                if (i.getBatchid() != null) {
                    map.put(String.valueOf(i.getProductid()), i.getBatchid().getStock_availability());
                } else {
                    map.put(String.valueOf(i.getProductid()), i.getVariantBatches().get(0).getStock_availability());
                }
            }
        }
        ViewOrderDetailsResponse res = BuyerAdaptor.ToViewOrderDetailsResponse(save, map);

        return res;
    }

    public JSONObject createRazorPay(String receiptId, double amount, String currency) {
        JSONObject response = new JSONObject();

        try {
            RazorpayClient razorpay = new RazorpayClient("rzp_test_TGUE3JFlVfq7pu", "PxYsznflBonTm7j2lfbjMHJc");

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", (int) (amount * 100));
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", receiptId);

            Order order = razorpay.orders.create(orderRequest);

            response.put("success", true);
            response.put("amount", (int) (amount * 100));
            response.put("id", order.toJson().getString("id"));
            response.put("status", order.toJson().getString("status"));
            response.put("receipt", receiptId);
            response.put("currency", currency);
            response.put("attempts", order.toJson().getInt("attempts"));
            response.put("created_at", order.toJson().getInt("created_at"));

            response.put("data", new JSONObject(order.toString()));

        } catch (RazorpayException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("description", e.toString());
        }

        return response;
    }

    public ResponseEntity<?> verifyPayment(Map<String, String> data) {
        Map<String, Object> response = new HashMap<>();

        try {
            String orderId = data.get("razorpay_order_id");
            String paymentId = data.get("razorpay_payment_id");
            String signature = data.get("razorpay_signature");
            String save_id = data.get("save_id");


            String error_code = data.get("error_code");
            String error_description = data.get("error_description");
            String error_reason = data.get("error_reason");

            String mailid = null;
            int amount = 0;

            UpdateOrderStatusRequest updateOrderStatusRequest = new UpdateOrderStatusRequest(
                    new ObjectId(save_id),
                    null,
                    false,
                    error_code,
                    error_description,
                    error_reason,
                    orderId,
                    amount,
                    paymentId
            );

            Optional<Save> saveOpt = saveOrderRepo.findById(save_id);
            RazorPaymentFullDetails razorDetails = razorPaymentFullDetailsRepo.findByOrderID(orderId);

            if (saveOpt.isPresent() && razorDetails != null) {
                Save save = saveOpt.get();

                List<RazorPaymentDetails> list = razorDetails.getRazorPaymentDetails();
                RazorPaymentDetails last = list.get(list.size() - 1);

                list.remove(list.size() - 1);

                last.setPayment_id(paymentId);
                last.setOrder_id(orderId);

                mailid = save.getCustomerInformation().getEmail();
                amount = last.getAmount();

                list.add(last);
                razorDetails.setRazorPaymentDetails(list);
                razorPaymentFullDetailsRepo.save(razorDetails);

                save.setRazorpaymentdetails(last);
                saveOrderRepo.save(save);
            }


            if (signature == null || paymentId == null) {
                log.info("Payment failed - skipping signature verification");
                updateOrderStatusRequest =
                        new UpdateOrderStatusRequest(
                                new ObjectId(save_id),
                                null,
                                true,
                                error_code,
                                error_description,
                                error_reason,
                                orderId,
                                amount,
                                paymentId
                        );
                updateOrderStatus(updateOrderStatusRequest);
                response.put("success", false);
                response.put("message", "Payment failed");
                return ResponseEntity.badRequest().body(response);
            }

            String payload = orderId + "|" + paymentId;
            boolean isValid = Utils.verifySignature(payload, signature, "PxYsznflBonTm7j2lfbjMHJc");

            log.info("Signature valid: " + isValid);

            if (isValid) {
                RazorpayClient client = new RazorpayClient("rzp_test_TGUE3JFlVfq7pu", "PxYsznflBonTm7j2lfbjMHJc");
                // razorPay will give this
                Payment payment = client.payments.fetch(paymentId);

                updateOrderStatusRequest.data = BuyerAdaptor.ToRazorPaymentDetailsDTO(payment);

                updateOrderStatus(updateOrderStatusRequest);
                if (mailid != null && !mailid.equals("")) {
                    ResponseEntity<Map<String, String>> mailRes = sendMail(new sendMailRequest(mailid, save_id));

                    log.info("Mail Response Status: {}", mailRes.getStatusCode());
                    log.info("Mail Response Body: {}", mailRes.getBody());
                    log.info("Mail Response Headers: {}", mailRes.getHeaders());

                    response.put("success", true);
                    response.put("message", "Payment verified successfully");
                    response.put("Email", mailid);
                }
                return ResponseEntity.ok(response);
            } else {
                updateOrderStatusRequest.isError = true;
                updateOrderStatus(updateOrderStatusRequest);
                response.put("success", false);
                response.put("message", "Invalid payment signature");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("Payment verification error", e);

            response.put("success", false);
            response.put("message", "Something went wrong");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public boolean updateOrderStatus(UpdateOrderStatusRequest req) {
        if (req.id == null) return false;

        log.info("Update Request:" + req.id);

        Optional<Save> saveOpt = saveOrderRepo.findById(String.valueOf(req.id));
        String orderId = (req.data != null) ? req.data.order_id : req.order_id;
        if (orderId == null) {
            log.error("Order ID is null");
            return false;
        }
        log.info("orderId => " + orderId);

        RazorPaymentFullDetails razorDetails = razorPaymentFullDetailsRepo.findByOrderID(orderId);
        if (!saveOpt.isPresent() || razorDetails == null) return false;


        List<RazorPaymentDetails> list = razorDetails.getRazorPaymentDetails();
        if (list == null) list = new ArrayList<>();

        Save save = saveOpt.get();

        if (req.isError) {
            log.info("error in payment");
            RazorPaymentDetails latest = new RazorPaymentDetails();
            latest.setError_code(req.error_code);
            latest.setError_reason(req.error_reason);
            latest.setError_description(req.error_description);
            latest.setId(req.order_id);
            latest.setOrder_id(req.order_id);
            latest.setPayment_id(req.payment_id);
            latest.setAmount(req.amount);
            list.add(latest);
            razorDetails.setRazorPaymentDetails(list);
            log.info("razorPaymentFullDetails saved");
            razorPaymentFullDetailsRepo.save(razorDetails);

            save.setPayment_status("Failed");
            save.getPaymentDetails().setPayment_status("Failed");
            save.setRazorpaymentdetails(latest);

            saveOrderRepo.save(save);

            return false;
        } else {
//            if (save.getBuyerid() != null) {
//                Save.Rewards rewards = new Save.Rewards(save.getRewards().getRewardsEarned(), save.getRewards().getRewardsRedeemed(), true);
//                save.setRewards(rewards);
//
//                // update buyer collection pannanum
//
//                Optional<Buyer> buyerDB = buyerRepo.findById(new ObjectId(save.getBuyerid().getId()));
//                Optional<Seller> seller = sellerRepo.findById(String.valueOf(save.getCompanyid()));
//                if (buyerDB.isPresent() && seller.isPresent()) {
//                    Buyer buyer = buyerDB.get();
//                    if (buyer.getRewards() == null) {
//                        buyer.setRewards(new Buyer.Rewards());
//                    }
//
//                    if (save.getRewards().getRewardsRedeemed() > 0) {
//
//
//                        int seller_redeemAmount = seller.get().getRewards().getRedeeemAmount();
//                        int seller_redeemPoints = seller.get().getRewards().getRedeemPoints();
//
//                        int div = save.getRewards().getRewardsRedeemed() / seller_redeemPoints;
//                        int cost = div * seller_redeemAmount;
//                        log.info("Cost earned :" + cost);
//
//                        if (cost > save.getTotal_Amount()) {
//                            return false;
//                        }
//                        buyer.getRewards().setLastRedeemed(save.getRewards().getRewardsRedeemed());
//                        buyer.getRewards().setCurrentRewards(Math.max(buyer.getRewards().getCurrentRewards() - save.getRewards().getRewardsRedeemed(), 0));
//                        buyer.getRewards().setTotalRedeemed(buyer.getRewards().getTotalRedeemed() + save.getRewards().getRewardsRedeemed());
//                    }
//                    buyer.getRewards().setLastEarned(save.getRewards().getRewardsEarned()); // from ui
//
//                    buyer.getRewards().setTotalRewards(buyer.getRewards().getTotalRewards() + save.getRewards().getRewardsEarned());
//                    buyer.getRewards().setCurrentRewards(buyer.getRewards().getCurrentRewards() + save.getRewards().getRewardsEarned());
//                    buyerRepo.save(buyer);
//
//                }
//            }

            RazorPaymentDetails previous = list.isEmpty() ? null : list.get(list.size() - 1);
            RazorPaymentDetails latest = BuyerAdaptor.ToRazorPaymentDetailsDTOtoRazorPaymentDetails(previous, req.data);

            list.add(latest);

            razorDetails.setRazorPaymentDetails(list);
            razorPaymentFullDetailsRepo.save(razorDetails);

            String razorStatus = latest.getStatus() != null
                    ? latest.getStatus().toLowerCase()
                    : "";

            String status = razorStatus.equals("captured")
                    ? "PAID"
                    : razorStatus.equals("authorized")
                      ? "Pending"
                      : "Failed";

            save.setPayment_status(status);
            if (razorStatus.equals("captured")) save.getPaymentDetails().setPaid_amount(req.amount);
            save.getPaymentDetails().setPayment_status(status);
            save.setRazorpaymentdetails(latest);


            save.setOrderStatus("Review");

            saveOrderRepo.save(save);

            return true;
        }
    }

    public ResponseEntity<Map<String, String>> sendMail(sendMailRequest request) {

        Map<String, String> response = new HashMap<>();

        if (request.mailId == null || request.mailId.equals("")
                || request.save_id == null || request.save_id.equals("")) {

            response.put("message", "Failed to send email: Mail id missing");

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }

        try {

            InvoiceResponse data = InvoiceData(request.save_id);

            Optional<Save> save = saveOrderRepo.findById(request.save_id);

            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(request.mailId);
            helper.setFrom(fromEmail);
//            helper.setBcc("yogeshgimmy07@gmail.com");

            StringBuilder itemsHtml = new StringBuilder();
            long total_value = 0;
            String companyName = "";
            if (save.isPresent()) {
                Optional<Seller> seller = sellerRepo.findById(String.valueOf(save.get().getCompanyid()));
                if (!seller.isPresent()) {
                    response.put("message",
                            "Failed to send email , Company not found for this : " + save.get().getCompanyid());

                    return ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(response);
                }
                companyName = seller.get().getCompanyname();
                for (Inventory item : save.get().getSeller_items()) {
                    try {
                        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(item);
                        log.info("Incoming Request Payload:\n{}", json);
                    } catch (Exception e) {
                        log.error("Error while logging request payload", e);
                    }
                    int qty = item.getOrder_quantity();

                    String name;

                    if ("isService".equals(item.getFlag())) {

                        name = (item.getServicename() != null && !item.getServicename().isEmpty())
                                ? item.getServicename()
                                : item.getProductName();

                    } else {

                        name = item.getProductName();
                    }


                    double price;

                    if ("isService".equals(item.getFlag())) {

                        price = item.getServicecost() > 0
                                ? item.getServicecost()
                                : item.getSellerprice();

                    } else {

                        price = item.getOfferprice() != 0 ? item.getOfferprice() : item.getSellerprice();
                    }

                    if (item.getVariantBatches() != null && item.getVariantBatches().size() > 0) {
                        name += "-" +
                                item.getVariantBatches()
                                        .get(0)
                                        .getVariantName();
                    }
                    total_value += qty * price;
                    itemsHtml.append("<tr>")
                            .append("<td>").append(name).append("</td>")
                            .append("<td>").append(qty).append("</td>")
                            .append("<td>₹").append(price).append("</td>")
                            .append("</tr>");
                }
            }
            log.info("Per rows : " + itemsHtml.toString());

            String host = ShareUrl.substring(0, hostUrl.length()) + "/";

            String invoiceUrl = host + "CustomerTemplate.html?orderid=" + request.save_id;

            String trackUrl = host + "track-order/" + companyName + "/" +
                    (save.isPresent() ? save.get().getRefno() : "");

            String date = data.order_date != null ? data.order_date : "";

            String html = """
                    <html>
                    <body style="font-family:Arial;padding:10px;">
                    
                      <h2>Invoice</h2>
                    
                      <p><b>Company:</b> %s</p>
                      <p><b>Order ID:</b> %s</p>
                      <p><b>Date:</b> %s</p>
                    
                      <table border="1" cellpadding="8" cellspacing="0" width="100%%">
                        <tr>
                          <th>Item</th>
                          <th>Qty</th>
                          <th>Price</th>
                        </tr>
                    
                        %s
                    
                      </table>
                    
                      <h3>Total: ₹%s</h3>
                    
                      <br/>
                    
                      <a href="%s"
                         style="display:inline-block;
                                padding:10px 15px;
                                background:#1a5fe0;
                                color:#ffffff;
                                text-decoration:none;
                                border-radius:5px;
                                font-weight:bold;
                                margin-right:10px;">
                    
                         View Full Invoice
                    
                      </a>
                    
                      <a href="%s"
                         style="display:inline-block;
                                padding:10px 15px;
                                background:#1a5fe0;
                                color:#ffffff;
                                text-decoration:none;
                                border-radius:5px;
                                font-weight:bold;">
                    
                         Track Order
                    
                      </a>
                    
                      <p style="margin-top:10px;font-size:12px;color:#777;">
                        If the button doesn't work, copy this link:<br/>
                        <a href="%s">%s</a>
                      </p>
                    
                    </body>
                    </html>
                    """.formatted(
                    data.companyName,
                    data.refno,
                    date,
                    itemsHtml.toString(),
                    String.format("%.2f", (double) total_value),
                    invoiceUrl,
                    trackUrl,
                    invoiceUrl,
                    invoiceUrl
            );

            helper.setSubject("Invoice - " + data.refno);

            helper.setText(html, true);

            mailSender.send(mimeMessage);

            response.put("message", "Email sent successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            log.error("Error: " + e);

            e.printStackTrace();

            response.put("message",
                    "Failed to send email: " + e.getMessage());

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    public InvoiceResponse InvoiceData(String save_id) throws Exception {
        log.info("Invoice Id: " + save_id);

        Optional<Save> optionalSave = saveOrderRepo.findById(save_id);
        log.info("isSavePresent :" + optionalSave.isPresent());
        if (optionalSave.isEmpty()) {
            return new InvoiceResponse();
        }
        Optional<Seller> seller = sellerRepo.findById(String.valueOf(optionalSave.get().getCompanyid()));
        if (!seller.isPresent()) {
            seller = Optional.of(new Seller());
        }
        Save save = optionalSave.get();

        return SellerAdaptor.ToInvoiceDataResponse(save, null, seller.get(), null);
    }

    public String formatServiceImgFolderPath(String companyName, String createdAt) {
        LocalDateTime dateTime = LocalDateTime.parse(createdAt);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

        return companyName + "-" + dateTime.format(formatter);
    }

    public GetSellerDetailsResponse GetSellerDetails(GetSellerDetailsRequest request) {
        GetSellerDetailsResponse response = new GetSellerDetailsResponse();
        Seller seller = sellerRepo.findByCompanyname(request.companyname);
        if (seller != null) {
            response.companyname = seller.getCompanyname();
            response.sellerid = seller.getId();
            response.companyMobile = seller.getCompanyMobile();
            if (seller.getSeller_Payment_Details() != null) {
                response.sellerPaymentDetails = new GetSellerDetailsResponse.SellerPaymentDetails();
                response.sellerPaymentDetails.key = seller.getSeller_Payment_Details().getKey();
                response.sellerPaymentDetails.pay_Online_Link = seller.getSeller_Payment_Details().getPay_Online_Link();
                response.sellerPaymentDetails.upi_Id = seller.getSeller_Payment_Details().getUpi_Id();
                response.sellerPaymentDetails.secret_Key = seller.getSeller_Payment_Details().getSecret_Key();
            }
            response.serviceImageFolder = formatServiceImgFolderPath(seller.getCompanyname(), seller.getCreateAT());
            response.pickupPoints = seller.getPickupPoints();
            response.deliveryAreas = seller.getDeliveryAreas();
            response.profileImgPath = "uploads/" + seller.getId() + "_profile.jpg";
            response.paymentModes = seller.getPaymentModes();
            response.rewardsDTO = new GetSellerDetailsResponse.RewardsDTO();
            response.deliveryFee = seller.getDeliveryFeeDetails().getDeliveryFee();
            response.orderThreshold = seller.getDeliveryFeeDetails().getOrderThreshold();
            if (seller.getRewards() != null) {
                response.rewardsDTO.RedeemPoints = seller.getRewards().getRedeemPoints();
                response.rewardsDTO.RedeeemAmount = seller.getRewards().getRedeeemAmount();
                response.rewardsDTO.RewardAmount = seller.getRewards().getRewardAmount();
                response.rewardsDTO.RewardPoints = seller.getRewards().getRewardPoints();
            }
        }
        return response;
    }

    public List<ProductSuggestionDTO> SuggestProductsByName(SellerProductRequest request) {

        final String rawSearch = request.getSearchvalue();
        if (rawSearch == null || rawSearch.isBlank()) {
            log.warn("SuggestProductsByName: empty/blank search value, returning empty list");
            return Collections.emptyList();
        }
        final String searchValue = rawSearch.toLowerCase(Locale.ROOT);

        final ObjectId companyId;
        final String companyNameId = request.getCompanynameid();

        log.info("SuggestProductsByName: search='{}', companyNameId={}, sellerType={}, categories={}",
                searchValue, companyNameId, request.sellerType, request.categories);

        if (companyNameId == null || companyNameId.isBlank()) {
            log.warn("SuggestProductsByName: companyNameId missing/blank, returning empty list");
            return Collections.emptyList();
        }

        try {
            companyId = new ObjectId(companyNameId);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid company ID: {}", companyNameId);
            return Collections.emptyList();
        }
        final Set<String> requiredCategories =
                request.categories != null && !request.categories.isEmpty()
                        ? new HashSet<>(request.categories)
                        : Collections.emptySet();


        final JsonFactory factory = new JsonFactory();


        final List<ProductSuggestionDTO> result = new ArrayList<>(10);
        factory.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        final ObjectMapper mapper = new ObjectMapper(factory);
        final ObjectReader reader = mapper.readerFor(SuggestProductDTO.class);

        final String inventoryUrl =
                uploadHost + "/uploads/FilterProducts/InventoryData.json";

        log.info("Reading Inventory JSON from: {}", inventoryUrl);

        int totalRecordsRead = 0;
        int nameMatchCount = 0;
        int sellerMatchCount = 0;

        // Stream parse JSON efficiently
        try (InputStream inputStream = URI.create(inventoryUrl).toURL().openStream();
             BufferedInputStream bis = new BufferedInputStream(inputStream);
             MappingIterator<SuggestProductDTO> iterator = reader.readValues(bis)) {

            log.info("SuggestProductsByName: stream opened successfully, starting iteration");

            while (iterator.hasNext() && result.size() < 10) {

                final SuggestProductDTO p;
                try {
                    p = iterator.next();
                } catch (Exception e) {
                    log.warn("Skipping malformed record while reading InventoryData.json after {} records read",
                            totalRecordsRead, e);
                    break;
                }

                totalRecordsRead++;

                if (p == null) {
                    log.debug("Record #{} is null, skipping", totalRecordsRead);
                    continue;
                }

                final String productName = p.productName;

                if (productName == null || productName.isBlank()) {
                    log.debug("Record #{} has null/blank productName, skipping", totalRecordsRead);
                    continue;
                }

                if (!productName.toLowerCase(Locale.ROOT).contains(searchValue)) {
                    continue; // too noisy to log every non-match
                }

                nameMatchCount++;
                log.info("Record #{} name MATCH: productName='{}', seller_id={}, category={}",
                        totalRecordsRead, productName, p.seller_id,
                        p.productCategory != null ? p.productCategory._id : null);

                final ObjectId sellerId = p.seller_id;

                if (sellerId == null || !sellerId.equals(companyId)) {
                    log.info("Record #{} '{}' REJECTED at seller filter: record sellerId={}, expected companyId={}",
                            totalRecordsRead, productName, sellerId, companyId);
                    continue;
                }

                sellerMatchCount++;


                if (!requiredCategories.isEmpty()) {
                    if (p.productCategory == null || p.productCategory._id == null) {
                        log.info("Record #{} '{}' REJECTED at category filter: productCategory or its _id is null",
                                totalRecordsRead, productName);
                        continue;
                    }

                    final String categoryId = p.productCategory._id.toString();

                    if (!requiredCategories.contains(categoryId)) {
                        log.info("Record #{} '{}' REJECTED at category filter: record categoryId={}, required={}",
                                totalRecordsRead, productName, categoryId, requiredCategories);
                        continue;
                    }
                }

                log.info("Record #{} '{}' ADDED to results (product_id={},inventory_id={}, price={})", totalRecordsRead, productName, p.productId, p.inventory_id, p.sellingPrice);
                double price = p.offerPrice > 0.0 ? p.offerPrice : p.sellingPrice;
                result.add(
                        //String productId, String productName, boolean isService, String image, String price
                        new ProductSuggestionDTO(p.productId, p.inventory_id, p.productName, false, p.defaultImage, String.valueOf(price))
                );
            }

            log.info("SuggestProductsByName: finished iteration. totalRecordsRead={}, nameMatches={}, sellerMatches={}, finalResultSize={}",
                    totalRecordsRead, nameMatchCount, sellerMatchCount, result.size());


        } catch (IOException e) {
            log.error("Error reading remote InventoryData.json from: {} (after {} records read, {} matched so far)",
                    inventoryUrl, totalRecordsRead, result.size(), e);
            return result;

        } catch (Exception e) {
            log.error("Unexpected error while suggesting products (after {} records read, {} matched so far)",
                    totalRecordsRead, result.size(), e);
            return result;
        }

        return result;
    }


    public SignupOrLoginResponse signup(SignupRequest signupRequest) throws Exception {

        SignupOrLoginResponse response = new SignupOrLoginResponse();

        Buyer buyer = buyerRepo.findByMobileNumAndBuyerSiteid(signupRequest.mobileNum, signupRequest.sellerid);

        if (buyer != null && buyer.isUser()) {

            response.success = false;
            response.message = "Your existing user, Please Login";

            return response;
        }

        // =========================
        // Encrypt Password
        // =========================

        String secretKey = signupRequest.mobileNum + "intellesydetech";

        String encryptedInputPin = encrypt(signupRequest.password, secretKey);

        // =========================
        // Save Buyer
        // =========================

        Buyer newBuyer = new Buyer(
                signupRequest.firstName,
                signupRequest.lastName,
                signupRequest.email,
                signupRequest.mobileNum,
                encryptedInputPin,
                new Date(),
                new Date()
        );
        if (buyer != null && !buyer.isUser()) newBuyer.setId(buyer.getId());
        newBuyer.setRoles(Arrays.asList("WEB-LOGGED"));
        newBuyer.setUser(true);
        newBuyer.setBuyerUseSites(new Buyer.BuyerUseSites(signupRequest.sellerid, signupRequest.companyname));
        if (buyer != null && !buyer.isUser() && buyer.getRewards() != null) {
            newBuyer.setRewards(buyer.getRewards());
        } else {
            newBuyer.setRewards(new Buyer.Rewards(0, 0, 0, 0, 0));
        }
        buyerRepo.save(newBuyer);

        // =========================
        // JWT Claims
        // =========================

        Map<String, Object> claims = new HashMap<>();

        claims.put("role", "WEB-LOGGED");

        claims.put("type", "buyer");

//        claims.put("email", newBuyer.getEmail());

        claims.put("platform", "WEB");
//        claims.put("pin",decrypt(newBuyer.getPassword(),secretKey));

        // =========================
        // Generate JWT
        // =========================

        String accessToken = JwtService.generateToken(
                claims,
                encryptedInputPin,
                "",
                signupRequest.mobileNum
        );

        String refreshToken = UUID.randomUUID().toString();

        SignupOrLoginResponse.UserDTO userDTO = new SignupOrLoginResponse.UserDTO();
        userDTO.id = String.valueOf(newBuyer.getId());
        userDTO.firstName = newBuyer.getFirstName();
        userDTO.lastName = newBuyer.getLastName();
        userDTO.email = newBuyer.getEmail();
        userDTO.mobileNum = newBuyer.getMobileNum();
        userDTO.type = "buyer";
        userDTO.role = "WEB-LOGGED";

        response.success = true;
        response.message = "Registered Successfully";
        response.user = userDTO;
        response.accessToken = accessToken;
        response.refreshToken = refreshToken;
        response.expiresAt =
                System.currentTimeMillis() + (60 * 60 * 1000);

        return response;
    }

    public SignupOrLoginResponse login(LoginRequest loginRequest) throws Exception {
        SignupOrLoginResponse response = new SignupOrLoginResponse();
        log.info("Sellerid :" + loginRequest.sellerid);
        Buyer buyer = buyerRepo.findByEmail(loginRequest.identifier);
        if (buyer == null) {
            log.info("Buyer not found ");
            response.success = false;
            response.message = "You are not an existing user, Please Signup";
            return response;
        }
        if (buyer != null && !buyer.isUser()) {
            response.success = false;
            response.message = "You are not an existing user, Please Signup";
            return response;
        }

        String secretKey = buyer.getMobileNum() + "intellesydetech";

        String encryptedInputPassword = encrypt(loginRequest.password, secretKey);

        if (!buyer.getPassword().equals(encryptedInputPassword)) {
            response.success = false;
            response.message = "Password incorrect";
            return response;
        }
        String role = buyer.getRoles() != null ? buyer.getRoles().get(0) : "WEB-GUEST";
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("type", "buyer");
//        claims.put("email", buyer.getEmail());
        claims.put("platform", "WEB");
//        claims.put("pin",decrypt(buyer.getPassword(),secretKey));
        String accessToken = JwtService.generateToken(
                claims,
                encryptedInputPassword,
                "",
                buyer.getMobileNum()
        );

        String refreshToken = UUID.randomUUID().toString();
        SignupOrLoginResponse.UserDTO userDTO = new SignupOrLoginResponse.UserDTO();

        userDTO.id = String.valueOf(buyer.getId());
        userDTO.firstName = buyer.getFirstName();
        userDTO.lastName = buyer.getLastName();
        userDTO.email = buyer.getEmail();
        userDTO.mobileNum = buyer.getMobileNum();
        userDTO.type = "buyer";
        userDTO.role = role;
        userDTO.lastDeliveryAddesses = buyer.getLastDeliveryAddesses();
        if (buyer.getRewards() != null) {
            userDTO.lastEarned = buyer.getRewards().getLastEarned();
            userDTO.lastRedeemed = buyer.getRewards().getLastRedeemed();
            userDTO.totalRewards = buyer.getRewards().getTotalRewards();
            userDTO.totalRedeemed = buyer.getRewards().getTotalRedeemed();
            userDTO.availableRewards = buyer.getRewards().getCurrentRewards();
        }
        response.success = true;
        response.message = "Login Successful";
        response.user = userDTO;
        response.accessToken = accessToken;
        response.refreshToken = refreshToken;
        response.expiresAt = System.currentTimeMillis() + (60 * 60 * 1000);

        return response;
    }

    public boolean updateProfile(UpdateProfileRequest request) {
        Optional<Buyer> buyerOpt = buyerRepo.findById(request.buyerid);
        if (buyerOpt.isEmpty()) {
            return false;
        }
        Buyer buyer = buyerOpt.get();
        buyer.setFirstName(request.firstName);
        buyer.setLastName(request.lastName);
        buyer.setMobileNum(request.phone);
        buyer.setEmail(request.email);

        List<Buyer.LastDeliveryAddess> exist = buyer.getLastDeliveryAddesses();
        boolean flag = false;
        if (exist != null && !exist.isEmpty()) {

            for (Buyer.LastDeliveryAddess addr : exist) {

                if (addr.Address != null && addr.Address.equals(request.existStreetAddress)) {

                    addr.Address = (request.streetAddress);
                    addr.city = (request.city);
                    addr.state = (request.state);
                    addr.postalCode = (request.zipCode);
                    flag = true;
                    break;
                }
            }
        }

        if (!flag) {
            Buyer.LastDeliveryAddess newAdd = new Buyer.LastDeliveryAddess(
                    request.streetAddress,
                    request.city, request.zipCode, request.state);
            buyer.setLastDeliveryAddesses(Arrays.asList(newAdd));
        }


        buyerRepo.save(buyer);
        return true;
    }

    public List<GetAllOrdersRes> getAllOrders(@RequestBody GetAllOrdersRequest request) {
        log.info("Request buyer id : " + request.buyerid);
        List<GetAllOrdersRes> ans = new ArrayList<GetAllOrdersRes>();
        List<Save> allOrders = saveOrderRepo.findByBuyerId(request.buyerid);
        if (allOrders != null) {
            for (Save i : allOrders) {
                GetAllOrdersRes res = new GetAllOrdersRes();
                res.order_id = String.valueOf(i.get_id());
                res.refno = i.getRefno();
                res.items = BuyerAdaptor.FromInventorytoItemsDTO(i.getSeller_items());
                res.tot_order_value = String.valueOf(i.getTotal_Amount());
                res.order_Status = i.getOrderStatus();
                res.payment_status = i.getPayment_status();

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                res.order_date = sdf.format(i.getOrderDate());

                ans.add(res);
            }
        }
        return ans;
    }

    public OtpResponse GenerateOtp(GetOTPRequest request, String directory) {
        try {

            int otp = 100000 + new Random().nextInt(900000);

            File folder = new File(directory);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File[] files = folder.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().startsWith(request.email + "_")) {
                        f.delete();
                    }
                }
            }

            ResponseEntity<String> res = sendSubscriptionEmail(
                    new SubscriptionEmailDTO(request.email),
                    String.valueOf(otp),
                    request.email
            );

            if (res == null || res.getBody() == null ||
                    !res.getBody().equals("Email sent successfully")) {
                return new OtpResponse("FAILED TO SEND OTP", 0);
            }


            String fileName = request.email + "_" + otp + ".txt";
            File file = new File(folder, fileName);

            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Your OTP is: " + otp);
            }
            log.info("OTP SENT");
            return new OtpResponse("OTP SENT", otp);
        } catch (Exception e) {
            e.printStackTrace();
            return new OtpResponse("FAILED TO SEND OTP", 0);
        }
    }

    public boolean ValidateOtp(GetOTPRequest getOTPRequest, String Directory, int attempt) {
        try {

            String fileName = getOTPRequest.email + "_" + getOTPRequest.otp + ".txt";

            File file = new File(Directory + File.separator + fileName);

            if (file.exists()) {
                log.info("File is Exist");
                return true;
            } else {
                if (attempt == 3) {
                    if (DeleteOtpFile(getOTPRequest, Directory)) return false;
                }
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean DeleteOtpFile(GetOTPRequest getOTPRequest, String directory) {
        try {
            File folder = new File(directory);

            if (!folder.exists() || !folder.isDirectory()) {
                return false;
            }

            String mobileNum = getOTPRequest.mobileNum;

            File[] files = folder.listFiles();

            if (files == null) return false;

            for (File file : files) {
                String fileName = file.getName();

                if (fileName.contains(mobileNum)) {
                    boolean deleted = file.delete();
                    return deleted;
                }
            }

            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean ResetPassword(@RequestBody GetOTPRequest getOTPRequest) throws Exception {
        Buyer buyer = buyerRepo.findByEmail(getOTPRequest.email);
        if (buyer != null) {
            String secretKey = buyer.getMobileNum() + "intellesydetech";
            String encryptedInputPin = encrypt(getOTPRequest.password, secretKey);
            buyer.setPassword(encryptedInputPin);
            buyerRepo.save(buyer);
            return true;
        }
        return false;
    }

    public ResponseEntity<String> sendSubscriptionEmail(SubscriptionEmailDTO dto, String message, String mobileNum) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
//            String [] cc = {"yogeshgimmy07@gmail.com"};

            helper.setTo(dto.email);
            helper.setFrom(fromEmail);
//            helper.setBcc("yogeshgimmy07@gmail.com");
//          helper.setCc(cc);

            if (message == null) {
                helper.setSubject("New Subscription Inquiry");
                String htmlContent = "<html><body>" +
                        "<h3>New Subscription Inquiry Details</h3>" +
                        "<table border='1' cellpadding='8' cellspacing='0' style='border-collapse: collapse; font-family: Arial;'>" +
                        "<tr><th>Field</th><th>Value</th></tr>" +
                        "<tr><td>Company Name</td><td>" + dto.companyName + "</td></tr>" +
                        "<tr><td>First Name</td><td>" + dto.firstName + "</td></tr>" +
                        "<tr><td>Mobile Number</td><td>" + dto.mobileNumber + "</td></tr>" +
                        "<tr><td>Email</td><td>" + dto.email + "</td></tr>" +
                        "<tr><td>Available Days</td><td>" + dto.weekday + "</td></tr>" +
                        "<tr><td>Best Time to Connect</td><td>" + dto.bestTimeToConnect + "</td></tr>" +
                        "<tr><td>Comments</td><td>" + (dto.comment != null ? dto.comment : "N/A") + "</td></tr>" +
                        "</table></body></html>";

                helper.setText(htmlContent, true); // true = isHtml

                mailSender.send(mimeMessage);
            } else {
                helper.setSubject("Your OTP Code ");
                helper.setText(
                        "<html><body>" +
                                "<p>Your OTP for forget Password is:</p>" +
                                "<h2 style='color:#1B4BA4;'>" + message + "</h2>" +
                                "</body></html>",
                        true
                );
                mailSender.send(mimeMessage);
            }

            return ResponseEntity.ok("Email sent successfully");
        } catch (Exception e) {
            log.error("Error: " + e);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send email: " + e.getMessage());
        }
    }

    public SignupOrLoginResponse.UserDTO GetBuyerProfile(GetBuyerProfileRequest getBuyerProfileRequest) {
        SignupOrLoginResponse.UserDTO userDTO = new SignupOrLoginResponse.UserDTO();
        Optional<Buyer> buyer = buyerRepo.findById(getBuyerProfileRequest.buyerid);
        if (buyer.isPresent()) {
            userDTO.id = String.valueOf(buyer.get().getId());
            userDTO.firstName = buyer.get().getFirstName();
            userDTO.lastName = buyer.get().getLastName();
            userDTO.email = buyer.get().getEmail();
            userDTO.mobileNum = buyer.get().getMobileNum();
            userDTO.type = "buyer";
            userDTO.role = buyer.get().getRoles() != null ? buyer.get().getRoles().get(0) : "";
            userDTO.lastDeliveryAddesses = buyer.get().getLastDeliveryAddesses();
            userDTO.lastEarned = buyer.get().getRewards().getLastEarned();
            userDTO.lastRedeemed = buyer.get().getRewards().getLastRedeemed();
            userDTO.totalRewards = buyer.get().getRewards().getTotalRewards();
            userDTO.totalRedeemed = buyer.get().getRewards().getTotalRedeemed();
            userDTO.availableRewards = buyer.get().getRewards().getCurrentRewards();
        }
        return userDTO;
    }

    public ResponseEntity<List<String>> getImages(String userfolder, String type, String inventoryId, String companyName) {
        log.info("In GetImages :" + inventoryId + "  " + userfolder);
        String base = type.equals("uploadsProductImages") ? Product_BASE_PATH : Service_BASE_PATH;
        Path Product_imageDir = Paths.get(base, companyName, inventoryId);

        //$host/uploads/uploadsServiceImages/$<sellername-createdat>/$<ServiceName>/
        Path Service_imageDir = Paths.get(base, userfolder, inventoryId);
        Path imageDir = type.equals("uploadsProductImages") ? Product_imageDir : Service_imageDir;
        if (imageDir != null) {
            System.out.println("Path is : " + imageDir.toString());
        }

        if (!Files.exists(imageDir)) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        try (
                Stream<Path> paths = Files.list(imageDir)) {
            List<String> imageUrls = paths
                    .filter(Files::isRegularFile)
                    .map(path -> base + (
                            type.equals("uploadsServiceImages") ? userfolder : companyName) + "/" + inventoryId + "/"
                            + path.getFileName().toString())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(imageUrls);

        } catch (IOException e) {
            log.error("Error in getImages : ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    public PlaceOrderResponse createDraft(BuyerOrderRequest request) {
        PlaceOrderResponse placeOrderResponse = new PlaceOrderResponse();
        ClientSession session = null;
        try {
            session = mongoClient.startSession();
            session.startTransaction();
            List<ObjectId> ProductinventoryIds = new ArrayList<>();

            List<InventoryDTO> Serviceinventory = new ArrayList<>();
            List<InventoryDTO> Productinventory = new ArrayList<>();


            for (InventoryDTO i : request.inventory) {
                log.info("productname: " + i.productname);
                log.info("that product Ordered stock : " + i.sellerstock);
                String flag = i.flag != null ? i.flag : "";
                if ("isService".equals(flag)) {
                    Serviceinventory.add(i);
                } else {
                    Productinventory.add(i);
                    ProductinventoryIds.add(new ObjectId(i._id));
                }
            }
            log.info("Productinventory size : " + Productinventory.size());
            log.info("Serviceinventory size : " + Serviceinventory.size());
//
            List<Inventory> Productinventories = inventoryRepo.findByIdInAndCompanyId(String.valueOf(request.companyid), ProductinventoryIds);
//            log.debug("ProductInventories from Repo: " , Productinventories.size());

            List<InventoryDTO> checkedItems = new ArrayList<>();
            List<InventoryDTO> validItems = new ArrayList<>();

            if (!Productinventories.isEmpty()) {
                checkedItems = precheck(Productinventories, Productinventory);
//                checkedItems.forEach(item ->
//                        log.info("Checked Item -> Product: {}, Message: {}, Qty: {}",
//                                item.productname,
//                                item.getMessage(),
//                                item.getOrder_quantity())
//                );

                validItems = checkedItems.stream()
                        .filter(item -> item.getMessage() == null || item.getMessage().isEmpty())
                        .collect(Collectors.toList());
                log.info("validItems: " + validItems.size());


                List<String> msg = checkedItems.stream()
                        .map(InventoryDTO::getMessage)
                        .filter(message -> message != null && !message.trim().isEmpty())
                        .collect(Collectors.toList());

                for (String i : msg) {
                    log.info("msg: " + i);
                }
                // Only return error if we had products and some of them failed
                if (validItems.size() != Productinventory.size()) {
//
                    if (!msg.isEmpty() && msg != null) placeOrderResponse.errorMessages.addAll(msg);
                    return placeOrderResponse;
                }
            }

            // Add services to validItems regardless
            validItems.addAll(Serviceinventory);
            log.info("validItems: " + validItems.size());

            List<Inventory> finalItems = SellerAdaptor.FromInventoryDTOtoInventory(validItems);
            log.info("FinalItems: " + finalItems.size());

            String refno = request.refno;
            log.info("can i get refno from UI:" + refno);
            log.info("can i get refno from Save_id:" + request.save_id);

//            if (request.save_id != null && !request.save_id.trim().isEmpty()   ) {
//                Optional<Save> save = saveOrderRepo.findById(request.save_id);
//                if (save.isPresent() && save.get().getOrderStatus().equals("Draft")){
//                    refno=save.get().getRefno();
//                }
//            }

            LocalDateTime now = LocalDateTime.now();

            List<Save> all = saveOrderRepo.findWebOrdersByCompanyid(request.companyid);

            all.sort(Comparator.comparing(Save::getOrderDate).reversed());

            if (all.size() > 0) {
                log.info("Last refno : " + all.get(0).getRefno());
            }

            Save latest = all.isEmpty() ? null : all.get(0);

            Save newSave = BuyerAdaptor.TosaveModel(request, finalItems, now.toString(), refno, latest, request.customerInformationDTO, request.paymentsDTO);

            Payments Current_payments = SellerAdaptor.fromPaymentsDTOtoPayments(request.paymentsDTO);
            log.info("Current_payments : " + Current_payments);
            Current_payments.setPayment_status("UnPaid");
            newSave.setPayment_status("UnPaid");
            newSave.setOrderStatus("Draft");
            Current_payments.setPayment_mode(request.payment_mode);
            newSave.setTotal_Discount(Current_payments.getDiscount());
            newSave.setTotal_Paid(Current_payments.getCurrent_payment_amount());

            newSave.setPaymentDetails(Current_payments);

            //CutomerInfo
            String name = request.customerInformationDTO.customerName;
            String mobileNum = request.customerInformationDTO.customerMobileNum;
            String add = request.customerInformationDTO.deliveryAddress;
            String email = request.customerInformationDTO.email;
            String state = request.customerInformationDTO.state;
            String city = request.customerInformationDTO.city;
            String postalCode = request.customerInformationDTO.postalCode;
            ;

            CustomerInformation a = new CustomerInformation();
            if (name != null) a.setCustomerName(name);
            if (mobileNum != null) a.setCustomerMobileNum(mobileNum);
            if (add != null) {
                a.setDeliveryAddress(add);
            }
            if (email != null) {
                a.setEmail(email);
            }
            if (state != null) a.setState(state);
            if (city != null) a.setCity(city);
            if (postalCode != null) a.setPostalCode(postalCode);

            newSave.setCustomerInformation(a);


            Save inserted = newSave;
            if (request.save_id != null && !request.save_id.trim().isEmpty() && !request.save_id.equals("null")) {
                log.info("Inside already have save property");
                if (!ObjectId.isValid(request.save_id)) {
                    throw new IllegalArgumentException("Invalid save_id");
                }
                ObjectId saveId = new ObjectId(request.save_id);
                Save existing = mongoTemplate.findById(saveId, Save.class);
                if (existing == null) throw new RuntimeException("Order not found with id: " + saveId);


                existing.setOrderDate(newSave.getOrderDate());
                existing.setOrderStatus(newSave.getOrderStatus());
                existing.setCompanyid(newSave.getCompanyid());
                existing.setTotal_Amount(newSave.getTotal_Amount());
                existing.setTotal_Paid(newSave.getTotal_Paid());
                existing.setUpdatedAt(newSave.getUpdatedAt());
                existing.setSeller_items(newSave.getSeller_items());
                existing.setPaymentDetails(Current_payments);


                inserted = mongoTemplate
                        .withSession(session)
                        .save(existing);

                Query query = Query.query(Criteria.where("orderId").is(existing.get_id()));

                PaymentDetails paymentDetails1 = mongoTemplate.withSession(session).findOne(query, PaymentDetails.class);

                if (paymentDetails1.getPayments() == null) {
                    paymentDetails1.setPayments(new ArrayList<>(Collections.singletonList(Current_payments)));
                } else {
                    List<Payments> payments = paymentDetails1.getPayments();
                    payments.add(Current_payments);
                    paymentDetails1.setPayments(payments);
                }

                mongoTemplate.withSession(session).save(paymentDetails1);

                placeOrderResponse.message = "success";
                placeOrderResponse.successCode = "202";
                placeOrderResponse.refno = newSave.getRefno();


            } else {
                log.info("Order saving... ");
                if (request.buyerid != null) {
                    Optional<Buyer> buyer = buyerRepo.findById(request.buyerid);
                    if (buyer.isPresent()) {
                        Buyer existingBuyer = buyer.get();
                        newSave.setBuyerid(existingBuyer);


                    }
                }
                newSave.setOrderSource(request.orderStatus);
                inserted = mongoTemplate
                        .withSession(session)
                        .insert(newSave);

                PaymentDetails paymentDetails1 = new PaymentDetails();
                paymentDetails1.setOrderId(inserted.get_id());
                paymentDetails1.setPayments(new ArrayList<>(Collections.singletonList(Current_payments)));

                mongoTemplate.withSession(session).save(paymentDetails1);
            }


            placeOrderResponse.message = "success";
            placeOrderResponse.saveid = inserted.get_id();
            placeOrderResponse.successCode = "200";
            placeOrderResponse.refno = newSave.getRefno();
            session.commitTransaction();
            return placeOrderResponse;
        } catch (DuplicateKeyException e) {

            if (session != null && session.hasActiveTransaction()) {
                session.abortTransaction();
            }

            placeOrderResponse.message = e.getMessage();
            placeOrderResponse.successCode = "400";
            return placeOrderResponse;


        } catch (Exception e) {

            if (session != null && session.hasActiveTransaction()) {
                session.abortTransaction();
            }

            e.printStackTrace();

            placeOrderResponse.successCode = "400";
            placeOrderResponse.message = e.getMessage();

            return placeOrderResponse;
        }
    }

    public String DeleteDraft(String request) {
        if (request == null) {
            return "Something went wrong";
        }
        Optional<Save> save = saveOrderRepo.findById(request);
        if (save.isPresent()) {
            saveOrderRepo.delete(save.get());
            return "Successfully deleted";
        }
        return "Order not found";
    }

    public String SearchBuyerExist(String mobileNum, ObjectId sellerid) {
        Buyer buyer = buyerRepo.findByMobileNumAndBuyerSiteid(mobileNum, sellerid);
        if (buyer != null && buyer.isUser()) {
            return "Login with your password to accumulate points";
        } else if (buyer != null && !buyer.isUser()) {
            return "Sign up to accumulate points";
        }
        return null;
    }
}

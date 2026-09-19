package Farme_rich.Seller.BL;


import Farme_rich.Buyer.Model.Buyer;
import Farme_rich.Buyer.Repo.BuyerRepo;
import Farme_rich.Security.JwtService;
import Farme_rich.Seller.Adaptor.SellerAdaptor;
import Farme_rich.Seller.DTO.Request.*;
import Farme_rich.Seller.DTO.Response.*;
import Farme_rich.Seller.Model.BackEnd.*;
import Farme_rich.Seller.Model.FrontEnd.PickupPoints;
import Farme_rich.Seller.Model.FrontEnd.Seller;
import Farme_rich.Seller.Model.FrontEnd.User;
import Farme_rich.Seller.Model.FrontEnd.UserBehaviour;
import Farme_rich.Seller.Repo.BackEnd.*;
import Farme_rich.Seller.Repo.FrontEnd.SellerRepo;
import Farme_rich.Seller.Repo.FrontEnd.SellerUserRepo;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.mongodb.DuplicateKeyException;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.result.UpdateResult;
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
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static Farme_rich.Security.PasswordUtils.decrypt;
import static Farme_rich.Security.PasswordUtils.encrypt;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class SellerService {

    public static final String Product_BASE_PATH = "uploads/uploadsProductImages/";
    public static final String Service_BASE_PATH = "uploads/uploadsServiceImages/";


    private static final Logger log = LoggerFactory.getLogger(SellerService.class);
    private static final Logger logger = LoggerFactory.getLogger(SellerService.class);
    private static final long MAX_IMAGE_SIZE = 2_500_000; // 2.5 MB
    private static final int STOCK_THRESHOLD = 10;
    private final SellerRepo sellerRepo;
    private final SellerUserRepo sellerUserRepo;
    private final Promo_MessageRepo promoMessageRepo;
    private final ProductRepo productRepo;
    private final SalefeaturesRepo salefeaturesRepo;
    private final ProductCategoryRepo productCategoryRepo;
    private final InventoryRepo inventoryRepo;
    private final BatchesRepo batchesRepo;
    private final SaveOrderRepo saveOrderRepo;
    private final QuickInvoiceRepo quickInvoiceRepo;
    private final UserBehaviourRepo userBehaviourRepo;
    private final ServiceInventoryRepo serviceInventoryRepo;
    private final PaymentDetailsRepo paymentDetailsRepo;
    private final QICustomersRepo qiCustomersRepo;
    private final CustomerInfoRepo customerInfoRepo;
    private final JwtService jwtService;
    private final AcceptReviewDetailsRepo acceptReviewDetailsRepo;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private BuyerRepo buyerRepo;
    @Autowired
    private ImageSecurityService imageSecurityService;
    private MongoTemplate mongoTemplate;
    private MongoClient mongoClient;
    @Value("${app.SuggestUrl}")
    private String uploadHost;
    @Value("${app.upload-dir}")
    private String uploadDir;
    @Value("${app.ImgUrl}")
    private String imageBaseUrl;
    @Value("${app.image-storage-path}")
    private String imageStoragePath;

    @Autowired
    public SellerService(UserBehaviourRepo userBehaviourRepo,
                         SaveOrderRepo saveOrderRepo, SellerRepo sellerRepo,
                         SellerUserRepo sellerUserRepo, Promo_MessageRepo promoMessageRepo,
                         ProductRepo productRepo, SalefeaturesRepo salefeaturesRepo,
                         ProductCategoryRepo productCategoryRepo,
                         InventoryRepo inventoryRepo, BatchesRepo batchesRepo,
                         QuickInvoiceRepo quickInvoiceRepo, ServiceInventoryRepo serviceInventoryRepo,
                         PaymentDetailsRepo paymentDetailsRepo,
                         QICustomersRepo qiCustomersRepo, CustomerInfoRepo customerInfoRepo,
                         JwtService jwtService, AcceptReviewDetailsRepo acceptReviewDetailsRepo) {
        this.sellerRepo = sellerRepo;
        this.sellerUserRepo = sellerUserRepo;
        this.promoMessageRepo = promoMessageRepo;
        this.productRepo = productRepo;
        this.salefeaturesRepo = salefeaturesRepo;
        this.productCategoryRepo = productCategoryRepo;
        this.inventoryRepo = inventoryRepo;
        this.batchesRepo = batchesRepo;
        this.saveOrderRepo = saveOrderRepo;
        this.quickInvoiceRepo = quickInvoiceRepo;
        this.userBehaviourRepo = userBehaviourRepo;
        this.serviceInventoryRepo = serviceInventoryRepo;
        this.paymentDetailsRepo = paymentDetailsRepo;
        this.qiCustomersRepo = qiCustomersRepo;
        this.customerInfoRepo = customerInfoRepo;
        this.jwtService = jwtService;
        this.acceptReviewDetailsRepo = acceptReviewDetailsRepo;
    }

    @Autowired
    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Autowired
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public List<Seller> GetAllLogins() {
        return sellerRepo.findAll();
    }

    public List<Save> GetAllSave() {
        return saveOrderRepo.findAll();
    }

    public List<ProductCategory> GetProductCat() {
        return productCategoryRepo.findAll();
    }

    public List<QuickInvoice> GetAllQuickInvoice() {
        return quickInvoiceRepo.findAll();
    }

    public List<Products> GetProducts() {
        return productRepo.findAll();
    }

    public List<PaymentDetails> GetAllPaymentDetails() {
        return paymentDetailsRepo.findAll();
    }

    public List<Inventory> GetAllInventory() {
        return inventoryRepo.findAll();
    }

    public List<User> GetAllUsers() {
        return sellerUserRepo.findAll();
    }

    public List<Batches> GetAllBatches() {
        return batchesRepo.findAll();
    }

    public List<UserBehaviour> GetUserBehaviour() {
        return userBehaviourRepo.findAll();
    }

    public List<ServiceInventory> GetServiceInventory() {
        return serviceInventoryRepo.findAll();
    }

    public UserSignupResponse HandleDeviceId(User user) throws Exception {
        log.info("Device Id: " + user.getDeviceId());

        MessageResponse msgResponse = new MessageResponse();
        User existingUser = sellerUserRepo.findBydeviceId(user.getDeviceId());

        if (existingUser == null) {
            log.info("No existing user found for deviceId: " + user.getDeviceId());
            msgResponse.ValidationMessage = "User not found";
            UserSignupResponse res = SellerAdaptor.ToSighUpResponseDTO(null, user, msgResponse);
            res.role = new ArrayList<String>(List.of("GUEST"));
            return res;
        }

        log.info("Found existingUser with ID: " + existingUser.getId());

        Seller existingSeller = sellerRepo.findByUseridContaining(existingUser.getId());

        if (existingSeller == null) {
            log.info("No seller record found for userId: " + existingUser.getId());
            msgResponse.ValidationMessage = "Seller not found";
            return SellerAdaptor.ToSighUpResponseDTO(existingUser, user, msgResponse);
        }

        msgResponse.pin = String.valueOf(existingUser.getFirstPiN());


        msgResponse.setFirstname(existingUser.getFirstname());
        msgResponse.companyname = existingSeller.getCompanyname();
        msgResponse.setLastname(existingUser.getLastname());
        msgResponse.userid = existingUser.getId();

        msgResponse.ValidationCode = "UpdatePin";
        msgResponse.ValidationMessage = "success";

        // JWT Token Generation
        HashMap<String, Object> map = new HashMap<>();
        List<String> roles = existingUser.getRole();
        if (roles == null || roles.isEmpty()) {
            log.info("User has no roles assigned, setting default role.");
            roles = new ArrayList<>();
            roles.add("USER"); // or "PAID", "FREE", whatever your default is
        }
        map.put("role", roles.get(0)); // Assuming role is a list
//        map.put("deviceId", existingUser.getDeviceId());
        map.put("platform", "MOBILE");

        String encryptedPin = existingUser.getFirstPiN();
        String deviceKey = existingUser.getDeviceId() + "intellesydetech";
        String decryptedPin = "";

        if (encryptedPin == null || encryptedPin.trim().isEmpty()) {
            log.info("Encrypted PIN is null or empty, cannot decrypt.");
        } else {
            decryptedPin = decrypt(encryptedPin, deviceKey);
            log.info("Decrypted PIN: " + decryptedPin);
        }

        String token = jwtService.generateToken(
                map,
                encryptedPin,
                existingUser.getDeviceId(),
                ""
        );

        UserSignupResponse response = SellerAdaptor.ToSighUpResponseDTO(existingUser, user, msgResponse);
        if (existingSeller.getCompanyMobile() != null) {
            response.companyMobileNum = existingSeller.getCompanyMobile();
        }
        response.token = (token);
        Seller.Rewards rewards = existingSeller.getRewards();
        if (rewards != null) {
            response.rewardsDTO = new UpdateCompanyInfoRequest.RewardsDTO(
                    rewards.getRewardAmount(),
                    rewards.getRewardPoints(),
                    rewards.getRedeeemAmount(),
                    rewards.getRedeemPoints());
        } else {
            log.info("No rewards record found for sellerId: " + existingSeller.getId());
            response.rewardsDTO = new UpdateCompanyInfoRequest.RewardsDTO(0, 0, 0, 0); // or null, depending on what your DTO/frontend expects
        }

        response.seller_type = existingSeller.getSeller_type();
        response.companyname = existingSeller.getCompanyname();
        response.userMobileNum = existingUser.getMobileNum();
        response.role = (roles);
        response.companyid = existingSeller.getId();
        if (existingSeller.getSeller_Payment_Details() != null) {
            response.upi = existingSeller.getSeller_Payment_Details().getUpi_Id();
        }
        response.RegdUser = (existingUser.isPaidUser());
        response.language_preferred = existingUser.getLanguage_preferred();
        response.createAT = existingSeller.getCreateAT();

        return response;
    }


//    public HashMap<String,List<String>> SuggestProductsByName(SellerProductRequest sellerProductRequest) {
//        if (sellerProductRequest.getSearchvalue() == null || sellerProductRequest.getSearchvalue().isEmpty()) return new HashMap<>();
//
//        List<Products> allProducts = getProductsFromJsonFile(sellerProductRequest.isQI);

    @Transactional
    public UserSignupResponse RegisterProfile(User user, Seller seller, String validationCode) throws Exception {
        MessageResponse msgResponse = new MessageResponse();

        String password = user.getFirstPiN();
        log.info("Password: " + password);

        String encrypted = null;
        if (password != null && !password.trim().isEmpty() && user.getDeviceId() != null) {
            String Secret_key = user.getDeviceId() + "intellesydetech";
            encrypted = encrypt(password, Secret_key);
            log.info(Secret_key);
            log.info("encrypted: " + encrypted);
        }

        log.info("Userid: " + (seller != null ? seller.getUserid() : "nulllllll"));
        log.info("validationCode: " + validationCode);


//        if (buyer == null || user == null ||
//                buyer.getFirstname() == null || buyer.getFirstname().isEmpty() ||
//                buyer.getLastname() == null || buyer.getLastname().isEmpty() ||
//                user.getDeviceId() == null || user.getDeviceId().isEmpty() ||
//                user.getFirstPiN() == null || user.getFirstPiN().isEmpty()) {
//
//            msgResponse.ValidationMessage = "Invalid input data";
//            return BuyerAdaptor.ToUserRegisterResponseDTO(null, null, msgResponse);
//        }

        if (validationCode.equals("NewUser")) {
            try (ClientSession session = mongoClient.startSession()) {
                session.startTransaction();
                try {
                    // Set timestamps and status
                    LocalDateTime now = LocalDateTime.now();
                    user.setCreateAT(now.toString());
                    user.setUpdateAT(now.toString());
                    user.setFirstPiN(encrypted);
                    user.setLanguage_preferred("en");
                    user.setActive(true);
                    user.setRole(List.of("GUEST"));
                    // Insert user with session
                    User Saved_User = mongoTemplate.withSession(session).insert(user);   // ****************************
                    log.info("Set user_id: " + user.getId());
                    Seller saved_Seller = new Seller();
                    Seller companyNameisSame = sellerRepo.findByCompanyname(seller.getCompanyname());

                    if (companyNameisSame != null) {
                        log.info("Company Name : " + companyNameisSame.getCompanyname());
                        log.info("Company ID :" + companyNameisSame.getId());
                        List<String> existingIds = companyNameisSame.getUserid();

                        if (existingIds == null) {
                            existingIds = new ArrayList<>();
                        }

                        String newUserId = Saved_User.getId();

                        if (newUserId != null && !existingIds.contains(newUserId)) {
                            existingIds.add(newUserId);
                        }

                        companyNameisSame.setUserid(existingIds);
                        seller.setId(companyNameisSame.getId());

                        companyNameisSame.setUpdateAT(now.toString());
                        companyNameisSame.setUserid(existingIds);
                        user.setCompanyid(companyNameisSame.getId());
                        mongoTemplate.withSession(session).save(user);
                        mongoTemplate.withSession(session).save(companyNameisSame);
                    } else {
                        seller.setCreateAT(now.toString());
                        seller.setUpdateAT(now.toString());
                        seller.setUserid(Arrays.asList(user.getId()));
                        seller.setGst_enabled(false);
                        seller.setPrice_inclusive_gst(false);
                        seller.setCompanyMobile(user.getMobileNum());
                        user.setMobileNum(user.getMobileNum());

                        // Insert buyer with session
                        saved_Seller = mongoTemplate.withSession(session).insert(seller);
                        user.setCompanyid(saved_Seller.getId());
                        mongoTemplate.withSession(session).save(user);
                    }

                    session.commitTransaction();
                    msgResponse.ValidationMessage = "Successfully Registered";
                    msgResponse.userid = seller.getId();
                    return SellerAdaptor.ToUserRegisterResponseDTO(user, seller, msgResponse);

                } catch (DuplicateKeyException e) {
                    log.error("error : " + e);
                    session.abortTransaction();
                    msgResponse.ErrorMsg = "Duplicate Key Error: " + e.getMessage();
                } catch (Exception e) {
                    log.error("error : " + e);
                    session.abortTransaction();
                    msgResponse.ErrorMsg = "Transaction failed: " + e.getMessage();
                }
            } catch (Exception e) {
                log.error("error : " + e);
                msgResponse.ErrorMsg = "Session creation failed: " + e.getMessage();
            }

            return SellerAdaptor.ToUserRegisterResponseDTO(null, null, msgResponse);
        }

        // If user exists, update details
        else if (seller.getUserid() != null) {
            Query query = new Query(Criteria.where("id").is(seller.getUserid()));
            ;
            Update update = new Update();

            switch (validationCode) {
                case "UpdatePin":
                    log.info("pin inside");
                    update.set("firstPiN", encrypted);
                    break;
                case "UpdateMobile":
                    update.set("mobileNum", user.getMobileNum());
                    update.set("firstPiN", encrypted);
                    break;
                case "UpdateDevice":
                    log.info("device inside");
                    update.set("deviceId", user.getDeviceId());
                    update.set("firstPiN", encrypted);
                    break;
                default:
                    msgResponse.ErrorMsg = "Invalid validation code";
                    return SellerAdaptor.ToUserRegisterResponseDTO(null, null, msgResponse);
            }

            mongoTemplate.updateFirst(query, update, User.class);
            msgResponse.ValidationMessage = "Successfully Registered";

            // JWT Token Generation
            HashMap<String, Object> map = new HashMap<>();
            List<String> roles = user.getRole();
            if (roles == null || roles.isEmpty()) {
                log.info("User has no roles assigned, setting default role.");
                roles = new ArrayList<>();
                roles.add("GUEST");
            }
            log.info("get role [0] : " + roles.get(0));
            map.put("role", roles.get(0)); // Assuming role is a list
//            map.put("deviceId", user.getDeviceId());
            map.put("platform", "MOBILE");

            String encryptedPin = encrypted;
            String deviceKey = user.getDeviceId() + "intellesydetech";
            String decryptedPin = "";

            if (encryptedPin == null || encryptedPin.trim().isEmpty()) {
                log.info("Encrypted PIN is null or empty, cannot decrypt.");
            } else {
                decryptedPin = decrypt(encryptedPin, deviceKey);
                log.info("Decrypted PIN: " + decryptedPin);
            }

            String token = jwtService.generateToken(
                    map,
                    encryptedPin,
                    user.getDeviceId(),
                    ""
            );

            msgResponse.setUserid(user.getId() != null ? user.getId() : null);
            msgResponse.setFirstname(user.getFirstname() != null ? user.getFirstname() : null);
            UserSignupResponse ans = SellerAdaptor.ToUserRegisterResponseDTO(user, seller, msgResponse);

            ans.role = roles;
            ans.token = token;
            return ans;
        }

        // If no valid operation was performed, return an error
        msgResponse.ErrorMsg = "Invalid operation";
        return SellerAdaptor.ToUserRegisterResponseDTO(null, null, msgResponse);
    }

    public UserSignupResponse SignupValidate(User user) {
        MessageResponse msgResponse = new MessageResponse();
        User existingUser = null;
        log.info(user.getDeviceId() + "/////" + user.getMobileNum());
        try {
            existingUser = sellerUserRepo.findBydeviceId(user.getDeviceId());
            if (existingUser != null) {
                if (existingUser.getMobileNum().equals(user.getMobileNum())) {
                    msgResponse.ValidationMessage = "Duplicate User";
                    msgResponse.userid = String.valueOf(user.getId());
                } else {
                    msgResponse.ValidationMessage = "deviceID is exist";
                    msgResponse.pin = String.valueOf(existingUser.getFirstPiN());
                    msgResponse.mobileNum = user.getMobileNum();
                    msgResponse.userid = existingUser.getId();
                    msgResponse.ValidationCode = "UpdateMobile";
                    return SellerAdaptor.ToSighUpResponseDTO(user, existingUser, msgResponse);
                }
            } else {
                existingUser = sellerUserRepo.findBymobileNum(user.getMobileNum());
//                log.info("Exist mobile: "+existingUser!=null);
                if (existingUser != null) {
                    msgResponse.ValidationMessage = "Mobile exist but not found deviceID";
                    msgResponse.userid = existingUser.getId();
                    msgResponse.ValidationCode = "UpdateDevice";

                    return SellerAdaptor.ToSighUpResponseDTO(user, existingUser, msgResponse);

                } else {
                    msgResponse.ValidationMessage = "Successfully Registered";
                    msgResponse.ValidationCode = "NewUser";
                    msgResponse.userid = String.valueOf(user.getId());
                }

            }
        } catch (Exception e) {
            log.error("error : " + e);
            msgResponse.ErrorMsg = e.getMessage();
        }

        return SellerAdaptor.ToSighUpResponseDTO(user, existingUser, msgResponse);
    }

    public UserSignupResponse CheckPasscode(User user) throws Exception {
        MessageResponse msgResponse = new MessageResponse();
        log.info("Inside checkpasscode deviceid: " + user.getDeviceId() + " ");

        String secretKey = user.getDeviceId() + "intellesydetech";
        User a = sellerUserRepo.findBydeviceId(user.getDeviceId());


        if (a == null) {
            log.info("a is null");
            msgResponse.ValidationMessage = "incorrect";
            return SellerAdaptor.ToLoginResponseDTO(null, null, msgResponse);
        }

        Seller seller = sellerRepo.findByUseridContaining(a.getId());

        String encryptedInputPin = encrypt(user.getFirstPiN(), secretKey);

        String token = "";
        List<String> role = new ArrayList<>();

        if (a.getFirstPiN().equals(user.getFirstPiN()) || encryptedInputPin.equals(a.getFirstPiN())) {
            msgResponse.ValidationMessage = "correct";

            // JWT Token Generation
            HashMap<String, Object> map = new HashMap<>();
            List<String> roles = a.getRole();
            if (roles == null || roles.isEmpty()) {
                log.info("User has no roles assigned, setting default role.");
                roles = new ArrayList<>();
                roles.add("GUEST");
            }
            map.put("role", roles.get(0)); // Assuming role is a list
            map.put("deviceId", a.getDeviceId());
            map.put("platform", "MOBILE");

            String encryptedPin = a.getFirstPiN();
            String deviceKey = a.getDeviceId() + "intellesydetech";
            String decryptedPin = "";

            if (encryptedPin == null || encryptedPin.trim().isEmpty()) {
                log.info("Encrypted PIN is null or empty, cannot decrypt.");
            } else {
                decryptedPin = decrypt(encryptedPin, deviceKey);
                log.info("Decrypted PIN: " + decryptedPin);
            }

            token = jwtService.generateToken(
                    map,
                    encryptedPin,
                    a.getDeviceId(),
                    ""
            );

            role.addAll(roles);
            msgResponse.userid = String.valueOf(a.getId());
        } else {
            msgResponse.ValidationMessage = "pass incorrect";
        }

        UserSignupResponse ans = SellerAdaptor.ToLoginResponseDTO(seller, a, msgResponse);
        ans.token = token;
//        ans.role = role;
        return ans;
    }

    public List<SellerProductResponse> SearchProductsByName(SellerProductRequest sellerProductRequest) {
        System.out.println(sellerProductRequest.getSearchvalue());
        if (sellerProductRequest.getSearchvalue() == null || sellerProductRequest.getSearchvalue().isEmpty())
            return new ArrayList<SellerProductResponse>();
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where("productName").regex(sellerProductRequest.getSearchvalue(), "i")),
                Aggregation.lookup("inventory", "_id", "productid", "inventoryData"),
                Aggregation.unwind("inventoryData", true),

                project("_id", "productName", "productDescription", "productCategory", "imgurl", "unit", "Quickadd", "batch_unit", "batch_expiryDate")
                        .and(ConditionalOperators.when(Criteria.where("inventoryData.companyid").is(sellerProductRequest.getCompanynameid()))
                                .thenValueOf("inventoryData")
                                .otherwise(new Document()))
                        .as("inventoryData")
                        .and(ConditionalOperators.ifNull("inventoryData.defaultImage").then("default.jpg"))
                        .as("defaultImage"),

                Aggregation.sort(Sort.by(Sort.Direction.DESC, "inventoryData")),
                Aggregation.group("productName")
                        .first("_id").as("productid")
                        .first("productName").as("productName")
                        .first("productDescription").as("productDescription")
                        .first("productCategory").as("productCategory")
                        .first("imgurl").as("imgurl")
                        .first("unit").as("unit")
                        .first("Quickadd").as("Quickadd")
                        .first("batch_unit").as("batch_unit")
                        .first("batch_expiryDate").as("batch_expiryDate")
                        .first("inventoryData").as("inventoryData"),


                Aggregation.sort(Sort.by(Sort.Direction.DESC, "inventoryData.batchid.created_at"))

        );
        log.debug("{}", mongoTemplate.aggregate(aggregation, "products", Document.class).getMappedResults());
        List<SellerProductResponse> results = mongoTemplate.aggregate(aggregation, "products", SellerProductResponse.class).getMappedResults();

        return results;
    }

    /// /        List<ServiceInventory> allService = serviceInventoryRepo.findAll();
//        log.info("products Size: "+allProducts.size());
    public List<InitialServiceInventoryServiceInventoryResponse> searchServiceInventory(SellerProductRequest sellerProductRequest) {
        if (sellerProductRequest.searchvalue == null || sellerProductRequest.searchvalue.trim().isEmpty())
            return new ArrayList<InitialServiceInventoryServiceInventoryResponse>();

        Aggregation aggregation = Aggregation.newAggregation(
                // Match text search + company filter
                match(Criteria.where("servicename")
                        .regex(sellerProductRequest.searchvalue, "i")
                        .and("sellerid").is(new ObjectId(sellerProductRequest.companynameid))
                ),

                // Sort alphabetically or by most recently added — your choice
                Aggregation.sort(Sort.by(Sort.Direction.ASC, "servicename")),

                // Project only required fields
                project()
                        .and("_id").as("id")
                        .and("sellerid").as("sellerid")
                        .and("servicename").as("servicename")
                        .and("servicecost").as("servicecost")
                        .and("created_at").as("created_at")
                        .and("defaultImage").as("defaultImage")
                        .and("propertyAttributes").as("propertyAttributes")   // 🔥 important line
                        .and("isActive").as("isActive")
        );
        log.debug("Services found : ", mongoTemplate.aggregate(
                aggregation,
                "ServiceInventory",
                InitialServiceInventoryServiceInventoryResponse.class
        ).getMappedResults());
        List<ServiceInventory> a = mongoTemplate.aggregate(
                aggregation,
                "ServiceInventory",
                ServiceInventory.class
        ).getMappedResults();
        for (ServiceInventory i : a) {
            System.out.println(i.propertyAttributes.get(0).name);
        }

        return SellerAdaptor.ToInitialServiceInventoryServiceInventory(a);
    }

    /// /        log.info("service Size: "+allService.size());
//
//        List<Products> filtered_products = new ArrayList<Products>();
//        if (sellerProductRequest.isQI){  // products.json
//            filtered_products.addAll(allProducts.stream()
//                    .filter(p ->
//                            p.getProductName() != null &&
//                                    p.getProductName().toLowerCase()
//                                            .contains(sellerProductRequest.getSearchvalue().toLowerCase()) &&
//                                    p.getSeller_id().equals(new ObjectId(sellerProductRequest.companynameid))
//                                    && sellerProductRequest.sellerType != null && !sellerProductRequest.sellerType.isEmpty() &&
//                                    sellerProductRequest.sellerType.equals(p.getProduct_type())
//                    )
//                    .filter(p ->
//                            sellerProductRequest.categories == null ||
//                                    sellerProductRequest.categories.isEmpty() ||
//                                    sellerProductRequest.categories.contains(
//                                            p.getProductCategory().get_id().toString()
//                                    )
//                    )
//                    .limit(10)
//                    .collect(Collectors.toList()));
//        }
//        else {
//           filtered_products.addAll(allProducts.stream()  // FullProducts.json
//                    .filter(p ->
//                            p.getProductName() != null &&
//                                    p.getProductName().toLowerCase()
//                                            .contains(sellerProductRequest.getSearchvalue().toLowerCase()) && sellerProductRequest.sellerType != null && !sellerProductRequest.sellerType.isEmpty() &&
//                                    sellerProductRequest.sellerType.equals(p.getProduct_type())
//                    )
//
//                    .filter(p ->
//                            sellerProductRequest.categories == null ||
//                                    sellerProductRequest.categories.isEmpty() ||
//                                    sellerProductRequest.categories.contains(
//                                            p.getProductCategory().get_id().toString()
//                                    )
//                    )
//
//                    .limit(10)
//                    .collect(Collectors.toList()));
//        }
//        log.info("filter products: "+filtered_products.size());
//
    public BothSearchServiceInventoryandProductInventoryResponse BothSearchServiceInventoryandProductInventory(SellerProductRequest sellerProductRequest) {
        BothSearchServiceInventoryandProductInventoryResponse a = new BothSearchServiceInventoryandProductInventoryResponse();
        List<SellerProductResponse> products = SearchProductsByName(sellerProductRequest);
        List<InitialServiceInventoryServiceInventoryResponse> service = searchServiceInventory(sellerProductRequest);
        if (products != null && !products.isEmpty()) {
            a.sellerProductResponses.addAll(products);
        }
        if (service != null && !service.isEmpty()) {
            a.initialServiceInventoryServiceInventoryResponses.addAll(service);
        }
        return a;
    }

    /// /        List<ServiceInventory> filtered_service= allService.stream()
    /// /                .filter(p -> p.servicename != null &&
    /// /                        p.servicename.toLowerCase().contains(sellerProductRequest.getSearchvalue().toLowerCase()))
    /// /                .limit(5)
    /// /                .collect(Collectors.toList());
    /// /        log.info("filter service: "+filtered_service.size());
//        int max_size = Math.max(filtered_products.size(),0);
//
//        HashMap<String,List<String>> map = new HashMap<>();
//        for (int i = 0 ; i<max_size ; i++){
    public List<ProductsDTO> getProductsFromJsonFile(boolean isQI) {

        ObjectMapper mapper = new ObjectMapper();

        Path path = Paths.get(
                "uploads",
                "FilterProducts",
                isQI ? "products.json" : "FullProducts.json"
        );

        log.info("File taken from path: {}", path.toAbsolutePath());

        if (!Files.exists(path)) {
            log.warn("File not found: {}", path.toAbsolutePath());
            return Collections.emptyList();
        }

        try {
            ProductsDTO[] products = mapper.readValue(path.toFile(), ProductsDTO[].class);
            return Arrays.asList(products);
        } catch (IOException e) {
            log.error("Error reading JSON file", e);
            return Collections.emptyList();
        }
    }

    public List<ProductSuggestionDTO> LocalSuggestProductsByName(SellerProductRequest request) {

        final String rawSearch = request.getSearchvalue();
        if (rawSearch == null || rawSearch.isBlank()) {
            log.warn("SuggestProductsByName: empty/blank search value");
            return Collections.emptyList();
        }

        final boolean isQI = request.isQI;
        final String searchValue = rawSearch.toLowerCase(Locale.ROOT);

        final String companyNameId = request.getCompanynameid();

        if (companyNameId == null || companyNameId.isBlank()) {
            log.warn("SuggestProductsByName: companyNameId missing");
            return Collections.emptyList();
        }

        final ObjectId companyId;
        try {
            companyId = new ObjectId(companyNameId);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid company ID: {}", companyNameId);
            return Collections.emptyList();
        }

        final String requiredSellerType =
                request.sellerType != null && !request.sellerType.isBlank()
                        ? request.sellerType
                        : null;

        final Set<String> requiredCategories =
                request.categories != null && !request.categories.isEmpty()
                        ? new HashSet<>(request.categories)
                        : Collections.emptySet();

        final List<ProductSuggestionDTO> result = new ArrayList<>(10);

        final JsonFactory factory = new JsonFactory();
        factory.configure(
                JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS,
                true
        );

        final ObjectMapper mapper = new ObjectMapper(factory);
        final ObjectReader reader = mapper.readerFor(ReadProductFromJSON.class);

        // Local Inventory JSON path
        final Path inventoryPath = Paths.get(
                "uploads",
                "FilterProducts",
                "LocalInventoryData.json"
        );

        log.info(
                "SuggestProductsByName: reading local Inventory JSON from: {}",
                inventoryPath.toAbsolutePath()
        );

        if (!Files.exists(inventoryPath)) {
            log.error(
                    "InventoryData.json not found at: {}",
                    inventoryPath.toAbsolutePath()
            );
            return result;
        }

        int totalRecordsRead = 0;
        int nameMatchCount = 0;
        int sellerMatchCount = 0;

        try (
                InputStream inputStream = Files.newInputStream(inventoryPath);
                BufferedInputStream bis = new BufferedInputStream(inputStream);
                MappingIterator<ReadProductFromJSON> iterator =
                        reader.readValues(bis)
        ) {

            log.info("InventoryData.json opened successfully");

            while (iterator.hasNext() && result.size() < 10) {

                final ReadProductFromJSON p;

                try {
                    p = iterator.next();
                } catch (Exception e) {
                    log.warn(
                            "Skipping malformed record after {} records",
                            totalRecordsRead,
                            e
                    );
                    break;
                }

                totalRecordsRead++;

                if (p == null) {
                    continue;
                }

                final String productName = p.productName;

                if (productName == null || productName.isBlank()) {
                    continue;
                }

                if (!productName.toLowerCase(Locale.ROOT).contains(searchValue)) {
                    continue;
                }

                nameMatchCount++;

                final ObjectId sellerId = p.seller_id;

                if (sellerId == null || !sellerId.equals(companyId)) {
                    continue;
                }

                sellerMatchCount++;

                if (!isQI && requiredSellerType != null) {
                    if (p.product_type == null ||
                            !requiredSellerType.equals(p.product_type)) {
                        continue;
                    }
                }

                if (!requiredCategories.isEmpty()) {

                    if (p.productCategory == null ||
                            p.productCategory.get_id() == null) {
                        continue;
                    }

                    final String categoryId =
                            p.productCategory.get_id().toString();

                    if (!requiredCategories.contains(categoryId)) {
                        continue;
                    }
                }

                result.add(
                        new ProductSuggestionDTO(
                                p.inventory_id != null
                                        ? p.inventory_id.toString()
                                        : null,
                                p.productId != null ? p.productId.toString() : null,
                                productName,
                                p.sellingPrice,
                                p.offerPrice
                        )
                );
            }

            log.info(
                    "SuggestProductsByName: totalRecords={}, nameMatches={}, sellerMatches={}, results={}",
                    totalRecordsRead,
                    nameMatchCount,
                    sellerMatchCount,
                    result.size()
            );

        } catch (IOException e) {

            log.error(
                    "Error reading local InventoryData.json from: {}",
                    inventoryPath.toAbsolutePath(),
                    e
            );

        } catch (Exception e) {

            log.error(
                    "Unexpected error while suggesting products",
                    e
            );
        }

        return result;
    }

    public List<ProductSuggestionDTO> SuggestProductsByName(SellerProductRequest request) {

        final String rawSearch = request.getSearchvalue();
        if (rawSearch == null || rawSearch.isBlank()) {
            log.warn("SuggestProductsByName: empty/blank search value, returning empty list");
            return Collections.emptyList();
        }

        final boolean isQI = request.isQI;
        final String searchValue = rawSearch.toLowerCase(Locale.ROOT);

        final ObjectId companyId;
        final String companyNameId = request.getCompanynameid();

        log.info("SuggestProductsByName: search='{}', isQI={}, companyNameId={}, sellerType={}, categories={}",
                searchValue, isQI, companyNameId, request.sellerType, request.categories);

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

        final String requiredSellerType =
                request.sellerType != null && !request.sellerType.isBlank()
                        ? request.sellerType
                        : null;

        final Set<String> requiredCategories =
                request.categories != null && !request.categories.isEmpty()
                        ? new HashSet<>(request.categories)
                        : Collections.emptySet();

        final List<ProductSuggestionDTO> result = new ArrayList<>(10);

        final JsonFactory factory = new JsonFactory();
        factory.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        final ObjectMapper mapper = new ObjectMapper(factory);
        final ObjectReader reader = mapper.readerFor(ReadProductFromJSON.class);

        final String inventoryUrl =
                uploadHost + "/uploads/FilterProducts/InventoryData.json";

        log.info("Reading Inventory JSON from: {}", inventoryUrl);

        int totalRecordsRead = 0;
        int nameMatchCount = 0;
        int sellerMatchCount = 0;


        try (
                InputStream inputStream =
                        URI.create(inventoryUrl)
                                .toURL()
                                .openStream();

                BufferedInputStream bis =
                        new BufferedInputStream(inputStream);

                MappingIterator<ReadProductFromJSON> iterator =
                        reader.readValues(bis)
        ) {

            log.info("SuggestProductsByName: stream opened successfully, starting iteration");

            while (iterator.hasNext() && result.size() < 10) {

                final ReadProductFromJSON p;
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
                        p.productCategory != null ? p.productCategory.get_id() : null);

                final ObjectId sellerId = p.seller_id;

                if (sellerId == null || !sellerId.equals(companyId)) {
                    log.info("Record #{} '{}' REJECTED at seller filter: record sellerId={}, expected companyId={}",
                            totalRecordsRead, productName, sellerId, companyId);
                    continue;
                }

                sellerMatchCount++;

                if (!isQI && requiredSellerType != null) {
                    if (p.product_type == null || !requiredSellerType.equals(p.product_type)) {
                        log.info("Record #{} '{}' REJECTED at sellerType filter: record product_type={}, required={}",
                                totalRecordsRead, productName, p.product_type, requiredSellerType);
                        continue;
                    }
                }

                if (!requiredCategories.isEmpty()) {
                    if (p.productCategory == null || p.productCategory.get_id() == null) {
                        log.info("Record #{} '{}' REJECTED at category filter: productCategory or its _id is null",
                                totalRecordsRead, productName);
                        continue;
                    }

                    final String categoryId = p.productCategory.get_id().toString();

                    if (!requiredCategories.contains(categoryId)) {
                        log.info("Record #{} '{}' REJECTED at category filter: record categoryId={}, required={}",
                                totalRecordsRead, productName, categoryId, requiredCategories);
                        continue;
                    }
                }

                log.info("Record #{} '{}' ADDED to results (inventory_id={}, price={})",
                        totalRecordsRead, productName, p.inventory_id, p.sellingPrice);

                result.add(
                        new ProductSuggestionDTO(
                                p.inventory_id != null ? p.inventory_id.toString() : null,
                                p.productId != null ? p.productId.toString() : null,
                                productName,
                                p.sellingPrice,
                                p.offerPrice
                        )
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

        log.info("SuggestProductsByName: returning {} results for search='{}'", result.size(), searchValue);
        return result;
    }

    public List<SellerProductResponse> SearchInitial(SellerProductRequest sellerProductRequest) {

        if (sellerProductRequest.getCompanynameid() == null || sellerProductRequest.getCompanynameid().isEmpty()) {
            return new ArrayList<>();
        }

        Aggregation aggregation = Aggregation.newAggregation(

                Aggregation.lookup("inventory", "_id", "productid", "inventoryData"),

                Aggregation.unwind("inventoryData", true),

                match(
                        Criteria.where("inventoryData.companyid").is(sellerProductRequest.getCompanynameid())

                ),

                project(
                        "_id",
                        "productName",
                        "productDescription",
                        "productCategory",
                        "imgurl",
                        "unit",
                        "Quickadd",
                        "defaultImage",
                        "gst"
                )
                        .and("inventoryData").as("inventoryData")
                        .and("inventoryData.batchid.procurement_price").as("sellerprice")
                        .and("inventoryData.batchid.stock_availability").as("sellerstock")
                        .and("inventoryData.batchid.batch_unit").as("batch_unit")
                        .and("inventoryData.batchid.batch_expiryDate").as("batch_expiryDate")

        );

        log.debug("{}", mongoTemplate.aggregate(aggregation, "products", Document.class).getMappedResults());

        List<SellerProductResponse> results = mongoTemplate
                .aggregate(aggregation, "products", SellerProductResponse.class)
                .getMappedResults();


        return results;
    }

    private List<GetProductCategoryResponseDTO> buildCategoryTree(List<Document> allChildren, ObjectId parentId, ObjectId sellerId) {
        List<GetProductCategoryResponseDTO> result = new ArrayList<>();
        for (Document doc : allChildren) {
            if (parentId.equals(doc.getObjectId("parent_id"))) {
                List<ObjectId> sellerIds = doc.getList("sellerids", ObjectId.class);

                if (sellerId != null
                        && (sellerIds == null || !sellerIds.contains(sellerId))) {
                    continue;
                }
                GetProductCategoryResponseDTO dto = new GetProductCategoryResponseDTO();
                dto.category_Id = (doc.getObjectId("_id"));
                dto.category_name = (doc.getString("category_name"));

                dto.displayOnHomePage =
                        Boolean.TRUE.equals(doc.getBoolean("displayOnHomePage"));

                // recursion
                dto.children = buildCategoryTree(allChildren, doc.getObjectId("_id"), sellerId);
                result.add(dto);
            }
        }
        return result;
    }

    public GetCategoriesResponse GetCategories(GetCategoriesRequest request) {

        List<AggregationOperation> pipeline = new ArrayList<>();

        Criteria criteria = new Criteria();
        List<Criteria> conditions = new ArrayList<>();

//        conditions.add(
//                new Criteria().orOperator(
//                        Criteria.where("parent_id").is(null),
//                        Criteria.where("parent_id").is(""),
//                        Criteria.where("parent").is("")
//                )
//        );

        if (request.sellerId != null) {
            conditions.add(Criteria.where("sellerids").in(request.sellerId));
        }

        boolean isCategoryFilterPresent = request.categories_ID != null && !request.categories_ID.isEmpty();

        if (isCategoryFilterPresent) {
            conditions.add(Criteria.where("_id").in(request.categories_ID));
        }

        criteria.andOperator(conditions.toArray(new Criteria[0]));
        pipeline.add(Aggregation.match(criteria));

        pipeline.add(
                Aggregation.graphLookup("product_category")
                        .startWith("$_id")
                        .connectFrom("_id")
                        .connectTo("parent_id")
                        .as("children")
        );

        Aggregation aggregation = Aggregation.newAggregation(pipeline);

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "product_category", Document.class);

        List<Document> mappedResults = results.getMappedResults();
        if (mappedResults.isEmpty()) {
            return null;
        }

        if (isCategoryFilterPresent) {
            Document doc = mappedResults.get(0);

            GetCategoriesResponse response = new GetCategoriesResponse();
            response.category_Id = doc.getObjectId("_id");
            response.category_name = doc.getString("category_name");


            List<Document> children = (List<Document>) doc.getOrDefault("children", new ArrayList<>());

            response.childrensCategoryDTOList = buildCategoryTree(children, doc.getObjectId("_id"), request.sellerId);

            return response;
        }

        GetCategoriesResponse response = new GetCategoriesResponse();
        response.category_Id = null;
        response.category_name = "ALL_CATEGORIES";

        List<GetProductCategoryResponseDTO> rootCategories = new ArrayList<>();

        for (Document doc : mappedResults) {
            GetProductCategoryResponseDTO dto = new GetProductCategoryResponseDTO();
            dto.category_Id = doc.getObjectId("_id");
            dto.category_name = doc.getString("category_name");
            dto.displayOnHomePage =
                    Boolean.TRUE.equals(doc.getBoolean("displayOnHomePage"));

            List<Document> children = (List<Document>) doc.getOrDefault("children", new ArrayList<>());

            dto.children = buildCategoryTree(children, doc.getObjectId("_id"), request.sellerId);
            rootCategories.add(dto);
        }

        response.childrensCategoryDTOList = rootCategories;
        return response;
    }

    public GetPromoMessageResponse GetPromoMessage(GetPromoMessageRequest request) {
        Promo_messages promoMessages = promoMessageRepo.findBysellerid(new ObjectId(String.valueOf(request.sellerId)));
        if (promoMessages != null) {
            return SellerAdaptor.ToGetPromoMessageResponse(promoMessages);
        }
        return new GetPromoMessageResponse();
    }

    public List<Inventory> findActiveInventory(String companyid, String searchValue, List<String> categoryId) {

        Query query = new Query();

        List<Criteria> criteriaList = new ArrayList<>();


        // MUST company id
        criteriaList.add(
                Criteria.where("companyid").is(companyid)
        );


        // MUST active batch
        criteriaList.add(
                new Criteria().orOperator(
                        Criteria.where("batchid.active").is(true),
                        Criteria.where("variantBatches.active").is(true)
                )
        );


        // OPTIONAL search
        if (searchValue != null && !searchValue.trim().isEmpty()) {

            criteriaList.add(
                    new Criteria().orOperator(
                            Criteria.where("productName")
                                    .regex(searchValue.trim(), "i"),

                            Criteria.where("servicename")
                                    .regex(searchValue.trim(), "i")
                    )
            );
        }


        // OPTIONAL category
        if (categoryId != null && !categoryId.isEmpty()) {

            List<ObjectId> objectIds = categoryId.stream()
                    .map(ObjectId::new)
                    .toList();


            criteriaList.add(
                    new Criteria().orOperator(
                            Criteria.where("productCategory._id")
                                    .in(objectIds),

                            Criteria.where("categoryId")
                                    .in(objectIds),

                            Criteria.where("categorID")
                                    .in(objectIds)
                    )
            );
        }


        query.addCriteria(
                new Criteria().andOperator(
                        criteriaList.toArray(new Criteria[0])
                )
        );


        return mongoTemplate.find(query, Inventory.class);
    }

    public InitialGetAllProductsResponse searchProductsManageOrderBuyer(getSearchProductsRequest request) {
        InitialGetAllProductsResponse response = new InitialGetAllProductsResponse();

        List<InitialGetAllProductsResponse.ProductItem> resultList = new ArrayList<InitialGetAllProductsResponse.ProductItem>();
        // --- companyid present means
        if (request.companyId != null) {

            boolean inventoryExists = mongoTemplate.exists(
                    Query.query(
                            Criteria.where("companyid").is(request.companyId)
                                    .and("batchid.productname").regex(
                                            ".*" + Pattern.quote(request.searchValue) + ".*", "i"
                                    )
                                    .and("batchid.active").is(true)
                    ),
                    "inventory"
            );
            boolean ServiceInventoryExists = mongoTemplate.exists(
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

            System.out.println("inventoryExists : " + inventoryExists);
            System.out.println("ServiceInventoryExists : " + ServiceInventoryExists);


            if (inventoryExists || ServiceInventoryExists) {
                System.out.println("is exist in Inventory");

                List<AggregationOperation> pipeline = new ArrayList<>();
                pipeline.add(match(
                        Criteria.where("companyid").is(request.companyId)
                                .and("batchid.active").is(true)
                ));
                pipeline.add(lookup("products", "productid", "_id", "product"));
                pipeline.add(unwind("product", true));
                if (request.categories != null && !request.categories.isEmpty()) {
                    List<ObjectId> categoryIds = request.categories
                            .stream()
                            .map(ObjectId::new)
                            .toList();

                    pipeline.add(match(
                            Criteria.where("product.productCategory._id").in(categoryIds)
                    ));
                }
                pipeline.add(project()
                        .and("_id").as("_id")
                        .and("productid").as("ProductId")
                        .and("batchid.productname").as("ProductName")
                        .and("batchid.created_at").as("CreatedAt")
                        .and("batchid.stock_availability").as("stock_availability")
                        .and("batchid._id").as("batchid")
                        .and("batchid.seller_price").as("SellingPrice")
                        .and("defaultImage").as("defaultImage")
                        .and("product.productCategory._id").as("Category.CategoryId")
                        .and("product.productCategory.category_name").as("Category.CategoryName")
                );
                pipeline.add(AddFieldsOperation.addField("SellingPriceNumeric")
                        .withValue(
                                ConvertOperators.ToDecimal.toDecimal(
                                        ConditionalOperators.IfNull.ifNull("SellingPrice").then(0)
                                )
                        )
                        .build());

                pipeline.add(UnionWithOperation.unionWith("ServiceInventory")
                        .pipeline(
                                match(
                                        Criteria.where("sellerid")
                                                .is(new ObjectId(request.companyId))
                                                .and("isActive").is(true)
                                ),
                                project()
                                        .and("_id").as("_id")
                                        .and("servicename").as("ProductName")
                                        .and("propertyAttributes").as("propertyAttributes")
                                        .and("created_at").as("CreatedAt")
                                        .and("servicecost").as("SellingPrice")
                                        .and("defaultImage").as("defaultImage")
                        )
                );

                if (request.searchValue != null && !request.searchValue.isEmpty()) {
                    String regex = ".*" + Pattern.quote(request.searchValue) + ".*";
                    pipeline.add(match(
                            Criteria.where("ProductName").regex(regex, "i")
                    ));
                }

                pipeline.add(sort(Sort.by(Sort.Direction.ASC, "ProductName")));

                int page = request.page != null ? request.page : 0;
                int pageSize = request.pageSize != null ? request.pageSize : 20;

                pipeline.add(skip((long) page * pageSize));
                pipeline.add(limit(pageSize));

                Aggregation aggregation = newAggregation(pipeline);

                resultList = mongoTemplate.aggregate(
                        aggregation,
                        "inventory",
                        InitialGetAllProductsResponse.ProductItem.class
                ).getMappedResults();
            }
        }
        // --- companyid not present means
        else {
            log.info("No search match, returning all inventory");

            List<AggregationOperation> pipeline = new ArrayList<>();

            // ---------- INVENTORY (ALL PRODUCTS) Mattum ----------
            pipeline.add(match(
                    Criteria.where("batchid.active").is(true)
            ));

            pipeline.add(lookup("products", "productid", "_id", "product"));
            pipeline.add(unwind("product", true));

            // ---------- CATEGORY FILTER (OPTIONAL) ----------
            if (request.categories != null && !request.categories.isEmpty()) {
                List<ObjectId> categoryIds = request.categories
                        .stream()
                        .map(ObjectId::new)
                        .toList();

                pipeline.add(match(
                        Criteria.where("product.productCategory._id").in(categoryIds)
                ));
            }

            // ---------- SEARCH FILTER (OPTIONAL – PRODUCTS Mattum) ----------
            if (request.searchValue != null && !request.searchValue.isBlank()) {
                pipeline.add(match(
                        Criteria.where("batchid.productname")
                                .regex(Pattern.quote(request.searchValue), "i")
                ));
            }

            // ---------- PROJECT PRODUCTS ----------
            pipeline.add(project()
                    .and("_id").as("_id")
                    .and("productid").as("ProductId")
                    .and("batchid.productname").as("ProductName")
                    .and("created_at").as("CreatedAt")
                    .and("batchid.stock_availability").as("stock_availability")
                    .and("batchid.seller_price").as("SellingPrice")
                    .and("batchid._id").as("batchid")
                    .and("defaultImage").as("defaultImage")
                    .and("product.productCategory._id").as("Category.CategoryId")
                    .and("product.productCategory.category_name").as("Category.CategoryName")
            );

            pipeline.add(AddFieldsOperation.addField("SellingPriceNumeric")
                    .withValue(
                            ConvertOperators.ToDecimal.toDecimal(
                                    ConditionalOperators.IfNull.ifNull("SellingPrice").then(0)
                            )
                    )
                    .build());

            // ---------- SERVICE INVENTORY (ALL SERVICES) ----------
            pipeline.add(
                    UnionWithOperation.unionWith("ServiceInventory")
                            .pipeline(
                                    match(
                                            Criteria.where("isActive").is(true)
                                    ),
                                    project()
                                            .and("_id").as("_id")
                                            .and("servicename").as("ProductName")
                                            .and("propertyAttributes").as("propertyAttributes")
                                            .and("created_at").as("CreatedAt")
                                            .and("servicecost").as("SellingPrice")
                                            .and("defaultImage").as("defaultImage")
                            )
            );

            // ---------- SEARCH FILTER (SERVICES + PRODUCTS AFTER UNION) ----------
            if (request.searchValue != null && !request.searchValue.isBlank()) {
                pipeline.add(match(
                        Criteria.where("ProductName")
                                .regex(Pattern.quote(request.searchValue), "i")
                ));
            }

            // ---------- SORT + PAGINATION ----------
            pipeline.add(sort(Sort.by(Sort.Direction.ASC, "ProductName")));

            int page = request.page != null ? request.page : 0;
            int pageSize = request.pageSize != null ? request.pageSize : 20;

            pipeline.add(skip((long) page * pageSize));
            pipeline.add(limit(pageSize));

            Aggregation aggregation = newAggregation(pipeline);

            resultList = mongoTemplate.aggregate(
                    aggregation,
                    "inventory",
                    InitialGetAllProductsResponse.ProductItem.class
            ).getMappedResults();
        }


        response.setProducts(resultList);
        return response;
    }

//    public InitialGetAllProductsResponse searchProductsManageInventory(getSearchProductsRequest request) {
//        log.info("Page No : " + request.page);
//        log.info("IsToSeeInventory : " + request.seeInv);
//        log.info("IsToSeeProduct : " + request.seeProd);
//        log.info("IsToSeeService : " + request.seeServ);
//
//
//        List<Inventory> TotalSearchOrCategory_Inventories = findActiveInventory(request.companyId, request.searchValue, request.categories);
//        List<Inventory> My_Inventories = inventoryRepo.findByCompanyIdAndBatchActive(request.companyId);
//
//
//        List<ObjectId> inventoryProductIds = My_Inventories.stream()
//                .map(Inventory::getProductid)
//                .filter(Objects::nonNull)
//                .collect(Collectors.toList());
//
//
//        int Initial_Inv = TotalSearchOrCategory_Inventories.size();
//        int Initial_Serv = getServiceCount(
//                request.companyId,
//                request.searchValue,
//                request.categories
//        );
//
//
//        int productSize = getProductCount(
//                request.companyId,
//                request.searchValue,
//                request.categories
//        );
//        log.info("Total product Size : " + productSize);
//        int Initial_Prod = productSize - TotalSearchOrCategory_Inventories.size();
//
//        log.info("Initial -Inventory for this seller : " + Initial_Inv);
//        log.info("Initial -ServiceInventory for this seller : " + Initial_Serv);
//        log.info("Initial -Product for this seller : " + Initial_Prod);
//
//        int AlreadyTaken_Inv = request.invTaken;
//        int AlreadyTaken_Serv = request.servTaken;
//        int AlreadyTaken_Prod = request.prodTaken;
//
//        log.info("Already Taken Inv : " + AlreadyTaken_Inv);
//        log.info("Already Taken Serv Inv : " + AlreadyTaken_Serv);
//        log.info("Already Taken Prod : " + AlreadyTaken_Prod);
//
//
//        InitialGetAllProductsResponse response = new InitialGetAllProductsResponse();
//        boolean inventoryExists = false;
//        boolean ServiceInventoryExists = false;
//        boolean productExists = false;
//        if (!request.searchValue.trim().isEmpty() && request.searchValue != null && request.searchValue != "") {
//            log.info("Search Value : " + request.searchValue.trim());
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
//
//                                            Criteria.where("variantBatches.productname")
//                                                    .regex(".*" + Pattern.quote(request.searchValue) + ".*", "i"),
//                                            Criteria.where("variantBatches.variantName")
//                                                    .regex(".*" + Pattern.quote(request.searchValue) + ".*", "i")
//                                    )
//                            )
//                    ),
//                    "inventory"
//            );
//            if (!inventoryExists) {
//                productExists = mongoTemplate.exists(
//                        Query.query(
//                                Criteria.where("productName").regex(
//                                        ".*" + Pattern.quote(request.searchValue) + ".*", "i"
//                                )
//                        ),
//                        "products");
//            }
//
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
//
//        log.info("inventoryExists : " + inventoryExists);
//        log.info("ServiceInventoryExists : " + ServiceInventoryExists);
//        log.info("ProductExists : " + productExists);
//
//        List<InitialGetAllProductsResponse.ProductItem> resultList;
//
//        if (inventoryExists || ServiceInventoryExists || productExists) {
//            resultList = new ArrayList<>();
//
//
//            Set<String> seenProductIds = new HashSet<>();
//
//
//            int page = request.page != null ? request.page : 0;
//            int pageSize = request.pageSize != null && request.pageSize > 0
//                    ? request.pageSize
//                    : 20;
//            int remaining = pageSize;
//
//
//            if (request.seeInv == 1 && remaining > 0 && inventoryExists) {
//
//                List<AggregationOperation> pipeline = new ArrayList<>();
//
//                pipeline.add(match(
//                        Criteria.where("companyid").is(request.companyId)
////                                .orOperator(
////                                        Criteria.where("batchid.active").is(true),
////                                        Criteria.where("variantBatches.active").is(true)
////                                )
//                ));
//
//                pipeline.add(lookup("products", "productid", "_id", "product"));
//                pipeline.add(unwind("product", true));
//
//                if (request.categories != null && !request.categories.isEmpty()) {
//                    pipeline.add(match(
//                            Criteria.where("product.productCategory._id")
//                                    .in(request.categories.stream().map(ObjectId::new).toList())
//                    ));
//                }
//
//                if (request.searchValue != null && !request.searchValue.trim().isEmpty()) {
//
//                    String searchRegex =
//                            ".*" + Pattern.quote(request.searchValue.trim()) + ".*";
//
//                    pipeline.add(
//                            match(
//                                    new Criteria().orOperator(
//
//                                            // Main product name
//                                            Criteria.where("productName")
//                                                    .regex(searchRegex, "i"),
//                                            Criteria.where("variantBatches.productname")
//                                                    .regex(searchRegex, "i"),
//
//                                            // Variant names
//                                            Criteria.where("variantBatches.variantName")
//                                                    .regex(searchRegex, "i")
//                                    )
//                            )
//                    );
//                }
//
////                pipeline.add(project()
////                        .and("_id").as("_id")
////                        .and("productid").as("ProductId")
////                        .and("productCategory").as("productCategoryDTO")
////                        .and("batchid.productname").as("ProductName")
////                        .and("batchid.created_at").as("CreatedAt")
////                        .and("batchid.stock_availability").as("stock_availability")
////                        .and("batchid._id").as("batchid")
////                        .and("batchid.seller_price").as("SellingPrice")
////                        .and("product.defaultImage").as("defaultImage")
////                        .and("variantBatches").as("productVariantDTOS")
////                        .and("Quickadd").as("Quickadd")
////                        .and("product.productCategory._id").as("Category.CategoryId")
////                        .and("product.productCategory.category_name").as("Category.CategoryName")
////                        .and("product.unit").as("unit")
////                );
//                pipeline.add(
//                        project()
//                                .and("_id").as("_id")
//                                .and("productid").as("ProductId")
//                                .and("deals").as("deals")
//                                .and("newArrivals").as("newArrivals")
//                                .and("productCategory").as("productCategoryDTO")
//
//                                // ProductName
//                                .and(
//                                        ConditionalOperators.ifNull(
//                                                ArrayOperators.ArrayElemAt
//                                                        .arrayOf("variantBatches.productname")
//                                                        .elementAt(0)
//                                        ).thenValueOf("batchid.productname")
//                                ).as("ProductName")
//
//                                // stock_availability
//                                .and(
//                                        ConditionalOperators.ifNull(
//                                                ArrayOperators.ArrayElemAt
//                                                        .arrayOf("variantBatches.stock_availability")
//                                                        .elementAt(0)
//                                        ).thenValueOf("batchid.stock_availability")
//                                ).as("stock_availability")
//
//                                .and(
//                                        ConditionalOperators.ifNull(
//                                                ArrayOperators.ArrayElemAt
//                                                        .arrayOf("variantBatches.offerPrice")
//                                                        .elementAt(0)
//                                        ).thenValueOf("batchid.offerPrice")
//                                ).as("OfferPrice")
//
//                                .and(
//                                        ConditionalOperators.ifNull(
//                                                ArrayOperators.ArrayElemAt
//                                                        .arrayOf("variantBatches.discount")
//                                                        .elementAt(0)
//                                        ).thenValueOf("batchid.discount")
//                                ).as("DiscountPercent")
//
//                                // CreatedAt
//                                .and(
//                                        ConditionalOperators.ifNull(
//                                                ArrayOperators.ArrayElemAt
//                                                        .arrayOf("variantBatches.created_at")
//                                                        .elementAt(0)
//                                        ).thenValueOf("batchid.created_at")
//                                ).as("CreatedAt")
//
//                                // batchid
//                                .and(
//                                        ConditionalOperators.ifNull(
//                                                ArrayOperators.ArrayElemAt
//                                                        .arrayOf("variantBatches._id")
//                                                        .elementAt(0)
//                                        ).thenValueOf("batchid._id")
//                                ).as("batchid")
//
//                                .and("variantBatches").as("productVariantDTOS")
//                                .and("Quickadd").as("Quickadd")
//
//                                // SellingPrice
//                                .and(
//                                        ConditionalOperators.ifNull(
//                                                ArrayOperators.ArrayElemAt
//                                                        .arrayOf("variantBatches.seller_price")
//                                                        .elementAt(0)
//                                        ).thenValueOf("batchid.seller_price")
//                                ).as("SellingPrice")
//
//                                .and("product.defaultImage").as("defaultImage")
//                                .and("product.productCategory._id").as("Category.CategoryId")
//                                .and("product.productCategory.category_name").as("Category.CategoryName")
//                                .and("product.unit").as("unit")
//                );
//
//                pipeline.add(sort(Sort.by(Sort.Direction.ASC, "ProductName")));
//                pipeline.add(skip((long) request.invTaken));
//                pipeline.add(limit(remaining));
//
//                List<InitialGetAllProductsResponse.ProductItem> inventoryResults =
//                        mongoTemplate.aggregate(
//                                newAggregation(pipeline),
//                                "inventory",
//                                InitialGetAllProductsResponse.ProductItem.class
//                        ).getMappedResults();
//
//                inventoryResults.forEach(item -> {
//                    resultList.add(item);
//                    seenProductIds.add(item.getProductId());
//                });
//
//                remaining -= inventoryResults.size();
//                AlreadyTaken_Inv += inventoryResults.size();
//                log.info("Remaining size after Inventory found: " + remaining);
//            }
//
//
//            if (request.seeServ == 1 && remaining > 0 && ServiceInventoryExists) {
//
//                Criteria serviceCriteria = Criteria.where("sellerid")
//                        .is(new ObjectId(request.companyId));
////                        .and("isActive").is(true);
//
//                if (request.searchValue != null) {
//                    String search = request.searchValue.trim();
//
//                    if (!search.isEmpty()) {
//                        serviceCriteria = serviceCriteria.and("servicename")
//                                .regex(".*" + Pattern.quote(search) + ".*", "i");
//                    }
//                }
//
//                List<InitialGetAllProductsResponse.ProductItem> serviceResults =
//                        mongoTemplate.aggregate(
//                                newAggregation(
//                                        match(serviceCriteria),
//                                        project()
//                                                .and("_id").as("_id")
//                                                .and("servicename").as("ProductName")
//                                                .and("servicecost").as("SellingPrice")
//                                                .and("serviceDesc").as("serviceDesc")
//                                                .and("serviceNotes").as("serviceNotes")
//                                                .and("isActive").as("isActive")
//                                                .and("category").as("productCategoryDTO")
//                                                .and("created_at").as("CreatedAt")
//                                                .and("propertyAttributes").as("propertyAttributes")
//                                                .and("defaultImage").as("defaultImage"),
//                                        sort(Sort.by(Sort.Direction.ASC, "ProductName")),
//                                        skip((long) request.servTaken),
//                                        limit(remaining)
//                                ),
//                                "ServiceInventory",
//                                InitialGetAllProductsResponse.ProductItem.class
//                        ).getMappedResults();
//
//                resultList.addAll(serviceResults);
//                AlreadyTaken_Serv += serviceResults.size();
//                remaining -= serviceResults.size();
//                log.info("Remaining size after Service found: " + remaining);
//            }
//
//
//            if (request.seeProd == 1 && remaining > 0 && productExists) {
//
//                List<AggregationOperation> productPipeline = new ArrayList<>();
//
//
//                if (request.searchValue != null && !request.searchValue.trim().isEmpty()) {
//
//                    Criteria searchCriteria = Criteria.where("productName")
//                            .regex(".*" + Pattern.quote(request.searchValue.trim()) + ".*", "i");
//
//                    Criteria companyCriteria = Criteria.where("sellerLists")
//                            .elemMatch(
//                                    Criteria.where("companyid")
//                                            .is(new ObjectId(request.companyId.trim()))
//                            );
//
//                    Criteria excludeInventoryCriteria = Criteria.where("_id")
//                            .nin(inventoryProductIds);
//
//                    Criteria finalCriteria = new Criteria().andOperator(
//                            searchCriteria,
//                            companyCriteria,
//                            excludeInventoryCriteria
//                    );
//
//                    productPipeline.add(Aggregation.match(finalCriteria));
//                }
//                productPipeline.add(project()
//                        .and("_id").as("_id")
//                        .and("_id").as("ProductId")
//                        .and("productName").as("ProductName")
//                        .and("defaultImage").as("defaultImage")
//                        .and("Quickadd").as("Quickadd")
//                        .and("created_at").as("CreatedAt")
//                        .and("productCategory._id").as("Category.CategoryId")
//                        .and("productCategory.category_name").as("Category.CategoryName")
//                        .and("unit").as("unit")
//                );
//
//                productPipeline.add(sort(Sort.by(Sort.Direction.ASC, "ProductName")));
//                productPipeline.add(skip((long) request.prodTaken));
//                productPipeline.add(limit(remaining));
//
//                List<InitialGetAllProductsResponse.ProductItem> productResults =
//                        mongoTemplate.aggregate(
//                                newAggregation(productPipeline),
//                                "products",
//                                InitialGetAllProductsResponse.ProductItem.class
//                        ).getMappedResults();
//
//                resultList.addAll(productResults);
//                AlreadyTaken_Prod += productResults.size();
//            }
//
//
//            response.setProducts(resultList);
//        } else if (request.searchValue == null || request.searchValue.isEmpty()) {
//
//            log.info("No search match, returning inventory + services + products (priority)");
//
//            int page = request.page != null ? request.page : 0;
//            int pageSize = request.pageSize != null ? request.pageSize : 20;
//
//            List<InitialGetAllProductsResponse.ProductItem> finalResult = new ArrayList<>();
//
//            List<AggregationOperation> inventoryPipeline = new ArrayList<>();
//            if (request.seeInv == 1) {
//                inventoryPipeline.add(
//                        match(
//                                Criteria.where("companyid").is(request.companyId)

    /// /                                        .orOperator(
    /// /                                                Criteria.where("batchid.active").is(true),
    /// /                                                Criteria.where("variantBatches.active").is(true)
    /// /                                        )
//                        )
//                );
//
//                inventoryPipeline.add(lookup("products", "productid", "_id", "product"));
//                inventoryPipeline.add(unwind("product", true));
//
//                if (request.categories != null && !request.categories.isEmpty()) {
//                    inventoryPipeline.add(
//                            match(
//                                    Criteria.where("product.productCategory._id")
//                                            .in(
//                                                    request.categories
//                                                            .stream()
//                                                            .map(ObjectId::new)
//                                                            .toList()
//                                            )
//                            )
//                    );
//                }
//
//
//                inventoryPipeline.add(
//                        project()
//                                .and("_id").as("_id")
//                                .and("productid").as("ProductId")
//                                .and("deals").as("deals")
//                                .and("newArrivals").as("newArrivals")
//                                .and("productCategory").as("productCategoryDTO")
//
//                                // ProductName
//                                .and(
//                                        ConditionalOperators.ifNull(
//                                                ArrayOperators.ArrayElemAt
//                                                        .arrayOf("variantBatches.productname")
//                                                        .elementAt(0)
//                                        ).thenValueOf("batchid.productname")
//                                ).as("ProductName")
//
//                                // stock_availability
//                                .and(
//                                        ConditionalOperators.ifNull(
//                                                ArrayOperators.ArrayElemAt
//                                                        .arrayOf("variantBatches.stock_availability")
//                                                        .elementAt(0)
//                                        ).thenValueOf("batchid.stock_availability")
//                                ).as("stock_availability")
//
//                                .and(
//                                        ConditionalOperators.ifNull(
//                                                ArrayOperators.ArrayElemAt
//                                                        .arrayOf("variantBatches.offerPrice")
//                                                        .elementAt(0)
//                                        ).thenValueOf("batchid.offerPrice")
//                                ).as("OfferPrice")
//
//
//                                .and(
//                                        ConditionalOperators.ifNull(
//                                                ArrayOperators.ArrayElemAt
//                                                        .arrayOf("variantBatches.discount")
//                                                        .elementAt(0)
//                                        ).thenValueOf("batchid.discount")
//                                ).as("DiscountPercent")
//
//
//                                .and(
//                                        ConditionalOperators.ifNull(
//                                                ArrayOperators.ArrayElemAt
//                                                        .arrayOf("variantBatches.created_at")
//                                                        .elementAt(0)
//                                        ).thenValueOf("batchid.created_at")
//                                ).as("CreatedAt")
//
//                                // batchid
//                                .and(
//                                        ConditionalOperators.ifNull(
//                                                ArrayOperators.ArrayElemAt
//                                                        .arrayOf("variantBatches._id")
//                                                        .elementAt(0)
//                                        ).thenValueOf("batchid._id")
//                                ).as("batchid")
//
//                                .and("variantBatches").as("productVariantDTOS")
//                                .and("Quickadd").as("Quickadd")
//
//                                // SellingPrice
//                                .and(
//                                        ConditionalOperators.ifNull(
//                                                ArrayOperators.ArrayElemAt
//                                                        .arrayOf("variantBatches.seller_price")
//                                                        .elementAt(0)
//                                        ).thenValueOf("batchid.seller_price")
//                                ).as("SellingPrice")
//                                .and(
//                                        ConditionalOperators.ifNull(
//                                                ArrayOperators.ArrayElemAt
//                                                        .arrayOf("variantBatches.variantName")
//                                                        .elementAt(0)
//                                        ).thenValueOf("batchid.variantName")
//                                ).as("variantName")
//
//                                .and("product.defaultImage").as("defaultImage")
//                                .and("product.productCategory._id").as("Category.CategoryId")
//                                .and("product.productCategory.category_name").as("Category.CategoryName")
//                                .and("product.unit").as("unit")
//
//                );
//
//                inventoryPipeline.add(
//                        sort(
//                                Sort.by(
//                                        Sort.Order.desc("CreatedAt"),
//                                        Sort.Order.asc("ProductName") // optional secondary sort
//                                )
//                        )
//                );
//
//                inventoryPipeline.add(skip((long) request.invTaken));
//                inventoryPipeline.add(limit(pageSize));
//
//
//            }
//            if (!inventoryPipeline.isEmpty()) {
//                List<InitialGetAllProductsResponse.ProductItem> inventoryList =
//                        mongoTemplate.aggregate(
//                                newAggregation(inventoryPipeline),
//                                "inventory",
//                                InitialGetAllProductsResponse.ProductItem.class
//                        ).getMappedResults();
//
//                finalResult.addAll(inventoryList);
//
//            }
//            int remaining = pageSize - finalResult.size();
//            AlreadyTaken_Inv += finalResult.size();
//
//            log.info("Remaining size after Inventory found: " + remaining);
//
//
//            if (remaining > 0 && request.seeServ == 1) {
//                List<AggregationOperation> servicePipeline = new ArrayList<>();
//
//                servicePipeline.add(
//                        match(Criteria.where("sellerid")
//                                        .is(new ObjectId(request.companyId))
    public int getProductCount(String companyId, String searchValue, List<String> categoryId) {
        List<Criteria> criteriaList = new ArrayList<>();
        // MUST company id inside sellerLists
        criteriaList.add(
                Criteria.where("sellerLists.companyid")
                        .is(new ObjectId(companyId))
        );


        // OPTIONAL search
        if (searchValue != null && !searchValue.trim().isEmpty()) {

            criteriaList.add(
                    new Criteria().orOperator(
                            Criteria.where("productName")
                                    .regex(searchValue.trim(), "i"),

                            Criteria.where("productDescription")
                                    .regex(searchValue.trim(), "i"),

                            Criteria.where("brand")
                                    .regex(searchValue.trim(), "i")
                    )
            );
        }


        // OPTIONAL category
        if (categoryId != null && !categoryId.isEmpty()) {

            List<ObjectId> categoryIds = categoryId.stream()
                    .map(ObjectId::new)
                    .toList();


            criteriaList.add(
                    Criteria.where("productCategory._id")
                            .in(categoryIds)
            );
        }


        Query query = new Query();

        query.addCriteria(
                new Criteria().andOperator(
                        criteriaList.toArray(new Criteria[0])
                )
        );


        log.info("Product Count Query : {}", query);

        return (int) mongoTemplate.count(query, Products.class);
    }

    /// /                                .and("isActive").is(true)
//                        )
//                );
//                if (request.categories != null && !request.categories.isEmpty()) {
//                    servicePipeline.add(
//                            match(
//                                    Criteria.where("category._id")
//                                            .in(
//                                                    request.categories
//                                                            .stream()
//                                                            .map(ObjectId::new)
//                                                            .toList()
//                                            )
//                            )
//                    );
//                }
//
//                servicePipeline.add(project()
//                        .and("_id").as("_id")
//                        .and("servicename").as("ProductName")
//                        .and("servicecost").as("SellingPrice")
//                        .and("serviceDesc").as("serviceDesc")
//                        .and("serviceNotes").as("serviceNotes")
//                        .and("isActive").as("isActive")
//                        .and("propertyAttributes").as("propertyAttributes")
//                        .and("category").as("productCategoryDTO")
//                        .and("addOnIds").as("addOnIds")
//                        .and("defaultImage").as("defaultImage")
//                        .and("created_at").as("CreatedAt")
//                );
//
//                servicePipeline.add(sort(Sort.by("ProductName")));
//                servicePipeline.add(skip((long) request.servTaken));
//                servicePipeline.add(limit(remaining));
//
//
//                log.info("Service: ", mongoTemplate.aggregate(
//                        newAggregation(servicePipeline),
//                        "ServiceInventory",
//                        InitialGetAllProductsResponse.ProductItem.class
//                ).getMappedResults());
//
//                if (!servicePipeline.isEmpty()) {
//                    List<InitialGetAllProductsResponse.ProductItem> serviceList =
//                            mongoTemplate.aggregate(
//                                    newAggregation(servicePipeline),
//                                    "ServiceInventory",
//                                    InitialGetAllProductsResponse.ProductItem.class
//                            ).getMappedResults();
//
//                    finalResult.addAll(serviceList);
//                    remaining -= serviceList.size();
//                    AlreadyTaken_Serv += serviceList.size();
//                }
//            }
//
//            log.info("Remaining size after Service found: " + remaining);
//
//
    public int getServiceCount(String companyId, String searchValue, List<String> categoryId) {

        List<Criteria> criteriaList = new ArrayList<>();

        criteriaList.add(
                Criteria.where("sellerid")
                        .is(new ObjectId(companyId))
        );

        if (searchValue != null && !searchValue.trim().isEmpty()) {

            criteriaList.add(
                    Criteria.where("servicename")
                            .regex(searchValue.trim(), "i")
            );
        }

        if (categoryId != null && !categoryId.isEmpty()) {

            List<ObjectId> categoryIds = categoryId.stream()
                    .map(ObjectId::new)
                    .toList();


            criteriaList.add(
                    Criteria.where("category._id")
                            .in(categoryIds)
            );
        }

        Query query = new Query();
        query.addCriteria(
                new Criteria().andOperator(
                        criteriaList.toArray(new Criteria[0])
                )
        );
        return (int) mongoTemplate.count(query, ServiceInventory.class);
    }

    public InitialGetAllProductsResponse searchProductsManageInventory(getSearchProductsRequest request) {

        log.info("Page No : " + request.page);
        log.info("IsToSeeInventory : " + request.seeInv);
        log.info("IsToSeeProduct : " + request.seeProd);
        log.info("IsToSeeService : " + request.seeServ);

        final String search = request.searchValue == null ? "" : request.searchValue.trim();
        final boolean hasSearch = !search.isEmpty();

        final boolean hasCategories = request.categories != null && !request.categories.isEmpty();
        final List<ObjectId> categoryObjectIds = hasCategories
                ? request.categories.stream().map(ObjectId::new).toList()
                : Collections.emptyList();

        String sortField = "";
        String sortOrder = "";
        if (request.sort != null) {
            sortField = request.sort.getField() != null ? request.sort.getField().trim() : "";
            sortOrder = request.sort.getOrder() != null ? request.sort.getOrder().trim() : "";
        }

        Double priceMin = null;
        Double priceMax = null;
        List<String> brandFilter = Collections.emptyList();
        List<Double> offerThresholds = Collections.emptyList();

        if (request.filters != null) {
            if (request.filters.getPriceRange() != null) {
                priceMin = request.filters.getPriceRange().getMin();
                priceMax = request.filters.getPriceRange().getMax();
            }
            if (request.filters.getBrand() != null) {
                brandFilter = request.filters.getBrand();
            }
            if (request.filters.getOfferPercentage() != null) {
                offerThresholds = request.filters.getOfferPercentage().entrySet().stream()
                        .filter(Map.Entry::getValue)
                        .map(e -> {
                            try {
                                return Double.parseDouble(e.getKey());
                            } catch (NumberFormatException ex) {
                                log.info("Ignoring non-numeric offerPercentage key: " + e.getKey());
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .toList();
            }
        }

        StockFilter stockFilter = StockFilter.parse(request.stockSummary);
        log.info("StockFilter : " + stockFilter);

        List<Inventory> myInventories = inventoryRepo.findByCompanyIdAndBatchActive(request.companyId);
        List<ObjectId> inventoryProductIds = myInventories.stream()
                .map(Inventory::getProductid)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // ADD: stockFilter threaded into the count helpers so pagination totals stay accurate
        int initialInv = countActiveInventory(request.companyId, search, categoryObjectIds, stockFilter);
        int initialServ = getServiceCount(request.companyId, search, request.categories);
        int productSize = getProductCount(request.companyId, search, request.categories);

        // ADD: raw (non-inventoried) products only ever count toward OUT_OF_STOCK;
        // for BELOW/ABOVE stock they can never match, so there's nothing to page through.
        int initialProd = (stockFilter == StockFilter.BELOW_STOCK || stockFilter == StockFilter.ABOVE_STOCK)
                ? 0
                : Math.max(0, productSize - inventoryProductIds.size());

        log.info("Initial - Inventory for this seller : " + initialInv);
        log.info("Initial - ServiceInventory for this seller : " + initialServ);
        log.info("Initial - Product for this seller : " + initialProd);

        int alreadyTakenInv = request.invTaken;
        int alreadyTakenServ = request.servTaken;
        int alreadyTakenProd = request.prodTaken;

        log.info("Already Taken Inv : " + alreadyTakenInv);
        log.info("Already Taken Serv : " + alreadyTakenServ);
        log.info("Already Taken Prod : " + alreadyTakenProd);

        InitialGetAllProductsResponse response = new InitialGetAllProductsResponse();

        int pageSize = request.pageSize != null && request.pageSize > 0 ? request.pageSize : 20;

        boolean inventoryExists = false;
        boolean serviceExists = false;
        boolean productExists = false;

        if (hasSearch) {
            String searchRegex = ".*" + Pattern.quote(search) + ".*";

            inventoryExists = mongoTemplate.exists(
                    Query.query(

                            new Criteria().andOperator(
                                    Criteria.where("companyid").is(request.companyId),
                                    Criteria.where("isActive").is(true),
//                                    new Criteria().orOperator(

//                                    Criteria.where("batchid.active").is(true),
//                                    Criteria.where("variantBatches.active").is(true)
//                                    ),
                                    new Criteria().orOperator(
                                            Criteria.where("batchid.productname").regex(searchRegex, "i"),
                                            Criteria.where("variantBatches.productname").regex(searchRegex, "i"),
                                            Criteria.where("variantBatches.variantName").regex(searchRegex, "i")
                                    )
                            )),
                    "inventory"
            );

            productExists = mongoTemplate.exists(
                    Query.query(Criteria.where("productName").regex(searchRegex, "i")),
                    "products"
            );

            serviceExists = mongoTemplate.exists(
                    Query.query(
                            Criteria.where("sellerid").is(new ObjectId(request.companyId))
                                    .and("isActive").is(true)
                                    .and("servicename").regex(searchRegex, "i")
                    ),
                    "ServiceInventory"
            );
        }

        log.info("inventoryExists : " + inventoryExists);
        log.info("serviceExists : " + serviceExists);
        log.info("productExists : " + productExists);

        List<InitialGetAllProductsResponse.ProductItem> resultList = new ArrayList<>();

        if (!hasSearch) {
            log.info("No search term - returning inventory + services + products (priority)");

            if (request.seeInv == 1) {
                List<AggregationOperation> inventoryPipeline = buildInventoryPipeline(
                        request.companyId, null, categoryObjectIds, priceMin, priceMax, brandFilter, offerThresholds,
                        stockFilter, sortField, sortOrder, request.invTaken, pageSize, true
                );
                resultList.addAll(mongoTemplate.aggregate(
                        newAggregation(inventoryPipeline), "inventory",
                        InitialGetAllProductsResponse.ProductItem.class
                ).getMappedResults());
            }
            alreadyTakenInv += resultList.size();
            int remaining = pageSize - resultList.size();
            log.info("Remaining after inventory: " + remaining);

            if (remaining > 0 && request.seeServ == 1) {
                List<InitialGetAllProductsResponse.ProductItem> serviceList = fetchServices(
                        request.companyId, null, categoryObjectIds, request.servTaken, remaining, sortField, sortOrder
                );
                resultList.addAll(serviceList);
                alreadyTakenServ += serviceList.size();
                remaining -= serviceList.size();
            }
            log.info("Remaining after service: " + remaining);

            if (remaining > 0 && request.seeProd == 1) {
                List<InitialGetAllProductsResponse.ProductItem> productList = fetchProducts(
                        request.companyId, null, categoryObjectIds, inventoryProductIds,
                        priceMin, priceMax, brandFilter, stockFilter, request.prodTaken, remaining, sortField, sortOrder
                );
                resultList.addAll(productList);
                alreadyTakenProd += productList.size();
            }

        } else if (inventoryExists || serviceExists || productExists) {
            int remaining = pageSize;

            if (request.seeInv == 1 && remaining > 0 && inventoryExists) {
                List<AggregationOperation> inventoryPipeline = buildInventoryPipeline(
                        request.companyId, search, categoryObjectIds, priceMin, priceMax, brandFilter, offerThresholds,
                        stockFilter, sortField, sortOrder, request.invTaken, remaining, false
                );
                List<InitialGetAllProductsResponse.ProductItem> inventoryResults = mongoTemplate.aggregate(
                        newAggregation(inventoryPipeline), "inventory",
                        InitialGetAllProductsResponse.ProductItem.class
                ).getMappedResults();

                resultList.addAll(inventoryResults);
                alreadyTakenInv += inventoryResults.size();
                remaining -= inventoryResults.size();
                log.info("Remaining after inventory: " + remaining);
            }

            if (request.seeServ == 1 && remaining > 0 && serviceExists) {
                List<InitialGetAllProductsResponse.ProductItem> serviceResults = fetchServices(
                        request.companyId, search, categoryObjectIds, request.servTaken, remaining, sortField, sortOrder
                );
                resultList.addAll(serviceResults);
                alreadyTakenServ += serviceResults.size();
                remaining -= serviceResults.size();
                log.info("Remaining after service: " + remaining);
            }

            if (request.seeProd == 1 && remaining > 0 && productExists) {
                List<InitialGetAllProductsResponse.ProductItem> productResults = fetchProducts(
                        request.companyId, search, categoryObjectIds, inventoryProductIds,
                        priceMin, priceMax, brandFilter, stockFilter, request.prodTaken, remaining, sortField, sortOrder
                );
                resultList.addAll(productResults);
                alreadyTakenProd += productResults.size();
            }

        } else {
            response.setProducts(Collections.emptyList());
            response.invTaken = alreadyTakenInv;
            response.servTaken = alreadyTakenServ;
            response.prodTaken = alreadyTakenProd;
            response.seeInv = alreadyTakenInv >= initialInv ? 0 : 1;
            response.seeServ = alreadyTakenServ >= initialServ ? 0 : 1;
            response.seeProd = alreadyTakenProd >= initialProd ? 0 : 1;
            return response;
        }

        response.setProducts(resultList);
        response.invTaken = alreadyTakenInv;
        response.prodTaken = alreadyTakenProd;
        response.servTaken = alreadyTakenServ;
        response.seeInv = alreadyTakenInv >= initialInv ? 0 : 1;
        response.seeProd = alreadyTakenProd >= initialProd ? 0 : 1;
        response.seeServ = alreadyTakenServ >= initialServ ? 0 : 1;
        return response;
    }

    private AggregationOperation computeStockLevelsStage() {
        return Aggregation.stage("""
                {
                  $addFields: {
                    allStockLevels: {
                      $concatArrays: [
                        {
                          $map: {
                            input: { $ifNull: ["$variantBatches", []] },
                            as: "v",
                            in: "$$v.stock_availability"
                          }
                        },
                        {
                          $cond: [
                            { $ifNull: ["$batchid", false] },
                            [ "$batchid.stock_availability" ],
                            []
                          ]
                        }
                      ]
                    }
                  }
                }
                """);
    }

    private AggregationOperation stockFilterMatchStage(StockFilter stockFilter) {
        String cond = switch (stockFilter) {
            case OUT_OF_STOCK -> "{ $lte: [\"$$s\", 0] }";
            case BELOW_STOCK -> "{ $and: [ { $gt: [\"$$s\", 0] }, { $lt: [\"$$s\", " + STOCK_THRESHOLD + "] } ] }";
            case ABOVE_STOCK -> "{ $gte: [\"$$s\", " + STOCK_THRESHOLD + "] }";
            case NONE -> null;
        };
        if (cond == null) return null;

        return Aggregation.stage("""
                {
                  $match: {
                    $expr: {
                      $gt: [
                        { $size: { $filter: { input: "$allStockLevels", as: "s", cond: %s } } },
                        0
                      ]
                    }
                  }
                }
                """.formatted(cond));
    }

    private Criteria stockFilterCriteria(StockFilter stockFilter) {
        return switch (stockFilter) {
            case OUT_OF_STOCK -> Criteria.where("stock_availability").lte(0);
            case BELOW_STOCK -> Criteria.where("stock_availability").gt(0).lt(STOCK_THRESHOLD);
            case ABOVE_STOCK -> Criteria.where("stock_availability").gte(STOCK_THRESHOLD);
            case NONE -> null;
        };
    }

    private List<AggregationOperation> buildInventoryPipeline(
            String companyId, String search, List<ObjectId> categoryObjectIds,
            Double priceMin, Double priceMax, List<String> brandFilter, List<Double> offerThresholds,
            StockFilter stockFilter, // ADD
            String sortField, String sortOrder, int taken, int limitSize, boolean newestFirst) {

        List<AggregationOperation> pipeline = new ArrayList<>();

        pipeline.add(
                match(
                        new Criteria().andOperator(
                                Criteria.where("companyid").is(companyId),
                                Criteria.where("isActive").is(true)
//                                new Criteria().orOperator(
//                                        Criteria.where("batchid.active").is(true),
//                                        Criteria.where("variantBatches.active").is(true)
//                                )
                        )
                ));

        pipeline.add(lookup("products", "productid", "_id", "product"));
        pipeline.add(unwind("product", true));

        if (!categoryObjectIds.isEmpty()) {
            pipeline.add(match(Criteria.where("product.productCategory._id").in(categoryObjectIds)));
        }

        if (search != null && !search.isEmpty()) {
            String regex = ".*" + Pattern.quote(search) + ".*";
            pipeline.add(match(new Criteria().orOperator(
                    Criteria.where("batchid.productname").regex(regex, "i"),
                    Criteria.where("variantBatches.productname").regex(regex, "i"),
                    Criteria.where("variantBatches.variantName").regex(regex, "i")
            )));
        }

        if (!brandFilter.isEmpty()) {
            pipeline.add(match(Criteria.where("product.brand").in(brandFilter)));
        }
        if (stockFilter != StockFilter.NONE) {
            pipeline.add(computeStockLevelsStage());
            pipeline.add(stockFilterMatchStage(stockFilter));
        }

        pipeline.add(
                project()
                        .and("_id").as("_id")
                        .and("productid").as("ProductId")
                        .and("deals").as("deals")
                        .and("newArrivals").as("newArrivals")
                        .and("productCategory").as("productCategoryDTO")
                        .and(ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.productname").elementAt(0))
                                .thenValueOf("batchid.productname")).as("ProductName")
                        .and(ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.stock_availability").elementAt(0))
                                .thenValueOf("batchid.stock_availability")).as("stock_availability")
                        .and(ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.offerPrice").elementAt(0))
                                .thenValueOf("batchid.offerPrice")).as("OfferPrice")
                        .and(ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.discount").elementAt(0))
                                .thenValueOf("batchid.discount")).as("DiscountPercent")
                        .and(ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.created_at").elementAt(0))
                                .thenValueOf("batchid.created_at")).as("CreatedAt")
                        .and(ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches._id").elementAt(0))
                                .thenValueOf("batchid._id")).as("batchid")
                        .and("variantBatches").as("productVariantDTOS")
                        .and("Quickadd").as("Quickadd")
                        .and(ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.seller_price").elementAt(0))
                                .thenValueOf("batchid.seller_price")).as("SellingPrice")
                        .and(ConditionalOperators.ifNull(
                                        ArrayOperators.ArrayElemAt.arrayOf("variantBatches.variantName").elementAt(0))
                                .thenValueOf("batchid.variantName")).as("variantName")
                        .and("product.defaultImage").as("defaultImage")
                        .and("product.productCategory._id").as("Category.CategoryId")
                        .and("product.productCategory.category_name").as("Category.CategoryName")
                        .and("product.unit").as("unit")
                        .and("product.brand").as("brand")
        );

        if (priceMin != null || priceMax != null) {
            Criteria priceCriteria = Criteria.where("SellingPrice");
            if (priceMin != null) priceCriteria = priceCriteria.gte(priceMin);
            if (priceMax != null) priceCriteria = priceCriteria.lte(priceMax);
            pipeline.add(match(priceCriteria));
        }

        if (!offerThresholds.isEmpty()) {
            Criteria[] offerCriteria = offerThresholds.stream()
                    .map(t -> Criteria.where("DiscountPercent").gte(t))
                    .toArray(Criteria[]::new);
            pipeline.add(match(new Criteria().orOperator(offerCriteria)));
        }


        if (!sortField.isEmpty()) {
            Sort.Direction dir = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
            pipeline.add(sort(Sort.by(dir, sortField)));
        } else if (newestFirst) {
            pipeline.add(sort(Sort.by(Sort.Order.desc("CreatedAt"), Sort.Order.asc("ProductName"))));
        } else {
            pipeline.add(sort(Sort.by(Sort.Direction.ASC, "ProductName")));
        }

        pipeline.add(skip((long) taken));
        pipeline.add(limit(limitSize));

        return pipeline;
    }

    private List<InitialGetAllProductsResponse.ProductItem> fetchServices(
            String companyId, String search, List<ObjectId> categoryObjectIds,
            int taken, int limitSize, String sortField, String sortOrder) {
        // unchanged — stockSummary intentionally not applied to services; confirm if that's wrong

        Criteria criteria = Criteria.where("sellerid").is(new ObjectId(companyId));

        if (search != null && !search.isEmpty()) {
            criteria = criteria.and("servicename").regex(".*" + Pattern.quote(search) + ".*", "i");
        }

        List<AggregationOperation> pipeline = new ArrayList<>();
        pipeline.add(match(criteria));

        if (!categoryObjectIds.isEmpty()) {
            pipeline.add(match(Criteria.where("category._id").in(categoryObjectIds)));
        }

        pipeline.add(project()
                .and("_id").as("_id")
                .and("servicename").as("ProductName")
                .and("servicecost").as("SellingPrice")
                .and("serviceDesc").as("serviceDesc")
                .and("serviceNotes").as("serviceNotes")
                .and("isActive").as("isActive")
                .and("category").as("productCategoryDTO")
                .and("addOnIds").as("addOnIds")
                .and("propertyAttributes").as("propertyAttributes")
                .and("defaultImage").as("defaultImage")
                .and("created_at").as("CreatedAt")
        );

        if (!sortField.isEmpty()) {
            Sort.Direction dir = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
            pipeline.add(sort(Sort.by(dir, sortField)));
        } else {
            pipeline.add(sort(Sort.by(Sort.Direction.ASC, "ProductName")));
        }

        pipeline.add(skip((long) taken));
        pipeline.add(limit(limitSize));

        return mongoTemplate.aggregate(
                newAggregation(pipeline), "ServiceInventory",
                InitialGetAllProductsResponse.ProductItem.class
        ).getMappedResults();
    }

    private List<InitialGetAllProductsResponse.ProductItem> fetchProducts(
            String companyId, String search, List<ObjectId> categoryObjectIds,
            List<ObjectId> inventoryProductIds, Double priceMin, Double priceMax,
            List<String> brandFilter, StockFilter stockFilter, // ADD
            int taken, int limitSize, String sortField, String sortOrder) {

        // ADD: standalone catalog products have no stock record at all, so they can never
        // satisfy BELOW_STOCK/ABOVE_STOCK — short-circuit instead of running a query that
        // would (correctly) always return nothing, and instead of pretending they have stock.
        if (stockFilter == StockFilter.BELOW_STOCK || stockFilter == StockFilter.ABOVE_STOCK) {
            return Collections.emptyList();
        }

        List<Criteria> criteriaList = new ArrayList<>();

        if (search != null && !search.isEmpty()) {
            criteriaList.add(Criteria.where("productName").regex(".*" + Pattern.quote(search) + ".*", "i"));
        }

        criteriaList.add(Criteria.where("sellerLists").elemMatch(
                Criteria.where("companyid").is(new ObjectId(companyId.trim()))
        ));

        if (!categoryObjectIds.isEmpty()) {
            criteriaList.add(Criteria.where("productCategory._id").in(categoryObjectIds));
        }

        if (!brandFilter.isEmpty()) {
            criteriaList.add(Criteria.where("brand").in(brandFilter));
        }

        if (priceMin != null || priceMax != null) {
            Criteria priceCriteria = Criteria.where("price"); // NOTE: confirm actual field name on Products
            if (priceMin != null) priceCriteria = priceCriteria.gte(priceMin);
            if (priceMax != null) priceCriteria = priceCriteria.lte(priceMax);
            criteriaList.add(priceCriteria);
        }

        criteriaList.add(Criteria.where("_id").nin(inventoryProductIds));

        List<AggregationOperation> pipeline = new ArrayList<>();
        pipeline.add(match(new Criteria().andOperator(criteriaList.toArray(new Criteria[0]))));

        pipeline.add(project()
                .and("_id").as("_id")
                .and("_id").as("ProductId")
                .and("productCategory").as("productCategoryDTO")
                .and("Quickadd").as("Quickadd")
                .and("productName").as("ProductName")
                .and("defaultImage").as("defaultImage")
                .and("created_at").as("CreatedAt")
                .and("productCategory._id").as("Category.CategoryId")
                .and("productCategory.category_name").as("Category.CategoryName")
                .and("unit").as("unit")
                .and("brand").as("brand")
        );

        if (!sortField.isEmpty()) {
            Sort.Direction dir = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
            pipeline.add(sort(Sort.by(dir, sortField)));
        } else {
            pipeline.add(sort(Sort.by(Sort.Direction.ASC, "ProductName")));
        }

        pipeline.add(skip((long) taken));
        pipeline.add(limit(limitSize));

        return mongoTemplate.aggregate(
                newAggregation(pipeline), "products",
                InitialGetAllProductsResponse.ProductItem.class
        ).getMappedResults();
    }

    private int countActiveInventory(String companyId, String search, List<ObjectId> categoryObjectIds,
                                     StockFilter stockFilter) { // ADD
        List<AggregationOperation> pipeline = new ArrayList<>();

        pipeline.add(match(new Criteria().andOperator(
                Criteria.where("companyid").is(companyId),
                new Criteria().orOperator(
                        Criteria.where("isActive").is(true)
                )
        )));

        pipeline.add(lookup("products", "productid", "_id", "product"));
        pipeline.add(unwind("product", true));

        if (!categoryObjectIds.isEmpty()) {
            pipeline.add(match(Criteria.where("product.productCategory._id").in(categoryObjectIds)));
        }

        if (search != null && !search.isEmpty()) {
            String regex = ".*" + Pattern.quote(search) + ".*";
            pipeline.add(match(new Criteria().orOperator(
                    Criteria.where("batchid.productname").regex(regex, "i"),
                    Criteria.where("variantBatches.productname").regex(regex, "i"),
                    Criteria.where("variantBatches.variantName").regex(regex, "i")
            )));
        }

        // ADD: project stock_availability the same way the real fetch pipeline does,
        // so the count reflects the same records the stock filter would actually return
//        if (stockFilter != StockFilter.NONE) {
//            pipeline.add(project()
//                    .and(ConditionalOperators.ifNull(
//                                    ArrayOperators.ArrayElemAt.arrayOf("variantBatches.stock_availability").elementAt(0))
//                            .thenValueOf("batchid.stock_availability")).as("stock_availability")
//            );
//            Criteria stockCriteria = stockFilterCriteria(stockFilter);
//            pipeline.add(match(stockCriteria));
//        }
        if (stockFilter != StockFilter.NONE) {
            pipeline.add(computeStockLevelsStage());
            pipeline.add(stockFilterMatchStage(stockFilter));
        }
        pipeline.add(Aggregation.count().as("total"));

        AggregationResults<org.bson.Document> results = mongoTemplate.aggregate(
                newAggregation(pipeline), "inventory", org.bson.Document.class
        );
        org.bson.Document doc = results.getUniqueMappedResult();
        return doc != null ? doc.getInteger("total", 0) : 0;
    }

    public InitialGetAllProductsResponse searchProductsManageOrderSeller(getSearchProductsRequest request) {
        log.info("Page No : " + request.page);
        log.info("IsToSeeInventory : " + request.seeInv);
        log.info("IsToSeeProduct : " + request.seeProd);
        log.info("IsToSeeService : " + request.seeServ);
        log.info("Taken -Inventory : " + request.invTaken);
        log.info("Taken -Service : " + request.servTaken);
        log.info("Taken -Product : " + request.prodTaken);

        InitialGetAllProductsResponse response = new InitialGetAllProductsResponse();

        if (request.companyId == null || request.companyId.isBlank()) {
            throw new IllegalArgumentException("companyId is required");
        }

        List<Inventory> Total_Inventories = inventoryRepo.findByCompanyIdAndBatchActive(request.companyId);

        List<Inventory> TotalSearchOrCategory_Inventories = findActiveInventory(request.companyId, request.searchValue, request.categories);


        int Initial_Inv = TotalSearchOrCategory_Inventories.size();
        int Initial_Serv = getServiceCount(request.companyId, request.searchValue, request.categories);

        log.info("Initial -Inventory for this seller : " + Initial_Inv);
        log.info("Initial -ServiceInventory for this seller : " + Initial_Serv);


        int AlreadyTaken_Inv = request.invTaken;
        int AlreadyTaken_Serv = request.servTaken;


        boolean inventoryExists = false;
        boolean ServiceInventoryExists = false;

        if (!request.searchValue.trim().isEmpty() && request.searchValue != null && request.searchValue != "") {
            inventoryExists = mongoTemplate.exists(
                    Query.query(
                            new Criteria().andOperator(
                                    Criteria.where("companyid").is(request.companyId)
                                            .orOperator(
                                                    Criteria.where("isActive").is(true),
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


        List<InitialGetAllProductsResponse.ProductItem> resultList = new ArrayList<>();

        if (inventoryExists || ServiceInventoryExists) {
            List<ObjectId> inventoryProductIds = Total_Inventories.stream()
                    .map(Inventory::getProductid)
                    .collect(Collectors.toList());

            int pageSize = request.pageSize != null && request.pageSize > 0
                    ? request.pageSize
                    : 20;

            // ================= INVENTORY =================
            List<AggregationOperation> pipeline = new ArrayList<>();
            if (request.seeInv == 1) {

                pipeline.add(
                        match(
                                new Criteria().andOperator(
                                        Criteria.where("companyid").is(request.companyId),
                                        Criteria.where("isActive").is(true),
                                        new Criteria().orOperator(
                                                Criteria.where("batchid.active").is(true),
                                                Criteria.where("variantBatches.active").is(true)
                                        )
                                )
                        ));

                pipeline.add(lookup("products", "productid", "_id", "product"));
                pipeline.add(unwind("product", true));

                if (request.categories != null && !request.categories.isEmpty()) {
                    pipeline.add(match(
                            Criteria.where("product.productCategory._id")
                                    .in(request.categories.stream().map(ObjectId::new).toList())
                    ));
                }

                if (request.searchValue != null && !request.searchValue.trim().isEmpty()) {

                    String searchRegex =
                            ".*" + Pattern.quote(request.searchValue.trim()) + ".*";

                    pipeline.add(
                            match(
                                    new Criteria().orOperator(

                                            // Main product name
                                            Criteria.where("productName")
                                                    .regex(searchRegex, "i"),
                                            Criteria.where("variantBatches.productname")
                                                    .regex(searchRegex, "i"),

                                            // Variant names
                                            Criteria.where("variantBatches.variantName")
                                                    .regex(searchRegex, "i")
                                    )
                            )
                    );
                }

                pipeline.add(
                        project()
                                .and("_id").as("_id")
                                .and("productid").as("ProductId")
                                .and("product.brand").as("brand")
                                .and("productCategory").as("productCategoryDTO")

                                // ProductName
                                .and(
                                        ConditionalOperators.ifNull(
                                                ArrayOperators.ArrayElemAt
                                                        .arrayOf("variantBatches.productname")
                                                        .elementAt(0)
                                        ).thenValueOf("batchid.productname")
                                ).as("ProductName")

                                // stock_availability
                                .and(
                                        ConditionalOperators.ifNull(
                                                ArrayOperators.ArrayElemAt
                                                        .arrayOf("variantBatches.stock_availability")
                                                        .elementAt(0)
                                        ).thenValueOf("batchid.stock_availability")
                                ).as("stock_availability")

                                // CreatedAt
                                .and(
                                        ConditionalOperators.ifNull(
                                                ArrayOperators.ArrayElemAt
                                                        .arrayOf("variantBatches.created_at")
                                                        .elementAt(0)
                                        ).thenValueOf("batchid.created_at")
                                ).as("CreatedAt")

                                // batchid
                                .and(
                                        ConditionalOperators.ifNull(
                                                ArrayOperators.ArrayElemAt
                                                        .arrayOf("variantBatches._id")
                                                        .elementAt(0)
                                        ).thenValueOf("batchid._id")
                                ).as("batchid")

                                .and("variantBatches").as("productVariantDTOS")
                                .and("Quickadd").as("Quickadd")
                                .and(
                                        ConditionalOperators.ifNull(
                                                ArrayOperators.ArrayElemAt
                                                        .arrayOf("variantBatches.offerPrice")
                                                        .elementAt(0)
                                        ).thenValueOf("batchid.offerPrice")
                                ).as("OfferPrice")

                                // SellingPrice
                                .and(
                                        ConditionalOperators.ifNull(
                                                ArrayOperators.ArrayElemAt
                                                        .arrayOf("variantBatches.seller_price")
                                                        .elementAt(0)
                                        ).thenValueOf("batchid.seller_price")
                                ).as("SellingPrice")

                                .and("product.defaultImage").as("defaultImage")
                                .and("product.productCategory._id").as("Category.CategoryId")
                                .and("product.productCategory.category_name").as("Category.CategoryName")
                                .and("product.unit").as("unit")
                );

                pipeline.add(sort(Sort.by(Sort.Direction.ASC, "ProductName")));
                pipeline.add(skip((long) request.invTaken));
                pipeline.add(limit(pageSize));

                List<InitialGetAllProductsResponse.ProductItem> inventoryResults =
                        mongoTemplate.aggregate(
                                newAggregation(pipeline),
                                "inventory",
                                InitialGetAllProductsResponse.ProductItem.class
                        ).getMappedResults();

                for (InitialGetAllProductsResponse.ProductItem item : inventoryResults) {
                    resultList.add(item);

                }
                AlreadyTaken_Inv += inventoryResults.size();
            }


            int remaining = pageSize - resultList.size();
            log.info("Remaining size after Inventory found: " + remaining);

            if (remaining <= 0) {
                response.setProducts(resultList);
                return response;
            }

            Criteria baseCriteria = Criteria.where("sellerid")
                    .is(new ObjectId(request.companyId))
                    .and("isActive").is(true);

            if (request.searchValue != null && !request.searchValue.trim().isEmpty()) {
                baseCriteria = baseCriteria.and("servicename")
                        .regex(request.searchValue, "i"); // case-insensitive
            }
            if (request.categories != null && !request.categories.isEmpty()) {

                baseCriteria.and("category._id")
                        .in(
                                request.categories
                                        .stream()
                                        .map(ObjectId::new)
                                        .toList()
                        );
            }

            // ================= SERVICE INVENTORY =================
            List<InitialGetAllProductsResponse.ProductItem> serviceResults =
                    mongoTemplate.aggregate(
                            newAggregation(
                                    match(baseCriteria),
                                    project()
                                            .and("_id").as("_id")
                                            .and("servicename").as("ProductName")
                                            .and("servicecost").as("SellingPrice")
                                            .and("category").as("serviceCategory")
                                            .and("created_at").as("CreatedAt")
                                            .and("propertyAttributes").as("propertyAttributes")
                                            .and("defaultImage").as("defaultImage"),
                                    sort(Sort.by(Sort.Direction.ASC, "ProductName")),
                                    skip((long) request.servTaken),
                                    limit(remaining)
                            ),
                            "ServiceInventory",
                            InitialGetAllProductsResponse.ProductItem.class
                    ).getMappedResults();
            if (!serviceResults.isEmpty()) {
                AlreadyTaken_Serv += serviceResults.size();
            }

            resultList.addAll(serviceResults);

            remaining = pageSize - resultList.size();
            log.info("Remaining size after Service found: " + remaining);
            if (remaining <= 0) {
                response.setProducts(resultList);
                return response;
            }
        } else if (request.searchValue == null || request.searchValue.isEmpty()) {

            log.info("No search match, returning inventory + services + products (priority)");

            int pageSize = request.pageSize != null ? request.pageSize : 20;

            List<InitialGetAllProductsResponse.ProductItem> finalResult = new ArrayList<>();

            List<AggregationOperation> inventoryPipeline = new ArrayList<>();

            if (request.seeInv == 1) {
                inventoryPipeline.add(
                        match(
                                new Criteria().andOperator(
                                        Criteria.where("companyid").is(request.companyId),
                                        Criteria.where("isActive").is(true),
                                        new Criteria().orOperator(
                                                Criteria.where("batchid.active").is(true),
                                                Criteria.where("variantBatches.active").is(true)
                                        )
                                )
                        )
                );

                inventoryPipeline.add(lookup("products", "productid", "_id", "product"));
                inventoryPipeline.add(unwind("product", true));

                if (request.categories != null && !request.categories.isEmpty()) {
                    inventoryPipeline.add(
                            match(
                                    Criteria.where("product.productCategory._id")
                                            .in(
                                                    request.categories
                                                            .stream()
                                                            .map(ObjectId::new)
                                                            .toList()
                                            )
                            )
                    );
                }


                inventoryPipeline.add(
                        project()
                                .and("_id").as("_id")
                                .and("productid").as("ProductId")
                                .and("productCategory").as("productCategoryDTO")
                                .and("product.brand").as("brand")
                                // ProductName
                                .and(
                                        ConditionalOperators.ifNull(
                                                ArrayOperators.ArrayElemAt
                                                        .arrayOf("variantBatches.productname")
                                                        .elementAt(0)
                                        ).thenValueOf("batchid.productname")
                                ).as("ProductName")

                                // stock_availability
                                .and(
                                        ConditionalOperators.ifNull(
                                                ArrayOperators.ArrayElemAt
                                                        .arrayOf("variantBatches.stock_availability")
                                                        .elementAt(0)
                                        ).thenValueOf("batchid.stock_availability")
                                ).as("stock_availability")

                                // CreatedAt
                                .and(
                                        ConditionalOperators.ifNull(
                                                ArrayOperators.ArrayElemAt
                                                        .arrayOf("variantBatches.created_at")
                                                        .elementAt(0)
                                        ).thenValueOf("batchid.created_at")
                                ).as("CreatedAt")

                                // batchid
                                .and(
                                        ConditionalOperators.ifNull(
                                                ArrayOperators.ArrayElemAt
                                                        .arrayOf("variantBatches._id")
                                                        .elementAt(0)
                                        ).thenValueOf("batchid._id")
                                ).as("batchid")
                                .and(
                                        ConditionalOperators.ifNull(
                                                ArrayOperators.ArrayElemAt
                                                        .arrayOf("variantBatches.offerPrice")
                                                        .elementAt(0)
                                        ).thenValueOf("batchid.offerPrice")
                                ).as("OfferPrice")

                                .and("variantBatches").as("productVariantDTOS")
                                .and("Quickadd").as("Quickadd")

                                // SellingPrice
                                .and(
                                        ConditionalOperators.ifNull(
                                                ArrayOperators.ArrayElemAt
                                                        .arrayOf("variantBatches.seller_price")
                                                        .elementAt(0)
                                        ).thenValueOf("batchid.seller_price")
                                ).as("SellingPrice")

                                .and("product.defaultImage").as("defaultImage")
                                .and("product.productCategory._id").as("Category.CategoryId")
                                .and("product.productCategory.category_name").as("Category.CategoryName")
                                .and("product.unit").as("unit")
                );

                inventoryPipeline.add(
                        sort(
                                Sort.by(
                                        Sort.Order.desc("CreatedAt"),
                                        Sort.Order.asc("ProductName") // optional secondary sort
                                )
                        )
                );

                inventoryPipeline.add(skip((long) request.invTaken));
                inventoryPipeline.add(limit(pageSize));


            }

            if (!inventoryPipeline.isEmpty()) {
                List<InitialGetAllProductsResponse.ProductItem> inventoryList =
                        mongoTemplate.aggregate(
                                newAggregation(inventoryPipeline),
                                "inventory",
                                InitialGetAllProductsResponse.ProductItem.class
                        ).getMappedResults();

                finalResult.addAll(inventoryList);

            }
            int remaining = pageSize - finalResult.size();
            AlreadyTaken_Inv += finalResult.size();

            log.info("Remaining size after Inventory found: " + remaining);


            if (remaining > 0 && request.seeServ == 1) {
                List<AggregationOperation> servicePipeline = new ArrayList<>();

                servicePipeline.add(
                        match(Criteria.where("sellerid")
                                .is(new ObjectId(request.companyId))
                                .and("isActive").is(true))
                );
                if (request.categories != null && !request.categories.isEmpty()) {
                    servicePipeline.add(
                            match(
                                    Criteria.where("category._id")
                                            .in(
                                                    request.categories
                                                            .stream()
                                                            .map(ObjectId::new)
                                                            .toList()
                                            )
                            )
                    );
                }

                servicePipeline.add(project()
                        .and("_id").as("_id")
                        .and("servicename").as("ProductName")
                        .and("servicecost").as("SellingPrice")
                        .and("category").as("serviceCategory")
                        .and("propertyAttributes").as("propertyAttributes")
                        .and("defaultImage").as("defaultImage")
                        .and("created_at").as("CreatedAt")
                );

                servicePipeline.add(sort(Sort.by("ProductName")));
                servicePipeline.add(skip((long) request.servTaken));
                servicePipeline.add(limit(remaining));


                log.debug("Service: ", mongoTemplate.aggregate(
                        newAggregation(servicePipeline),
                        "ServiceInventory",
                        InitialGetAllProductsResponse.ProductItem.class
                ).getMappedResults());

                if (!servicePipeline.isEmpty()) {
                    List<InitialGetAllProductsResponse.ProductItem> serviceList =
                            mongoTemplate.aggregate(
                                    newAggregation(servicePipeline),
                                    "ServiceInventory",
                                    InitialGetAllProductsResponse.ProductItem.class
                            ).getMappedResults();

                    finalResult.addAll(serviceList);
                    remaining -= serviceList.size();
                    AlreadyTaken_Serv += serviceList.size();
                }
            }
            log.info("Remaining size after Service found: " + remaining);
            resultList = finalResult;
        } else {
            response.setProducts(Collections.emptyList());
            return response;
        }

        response.setProducts(resultList);


        response.invTaken = AlreadyTaken_Inv;

        response.servTaken = AlreadyTaken_Serv;

        response.seeInv = AlreadyTaken_Inv >= Initial_Inv ? 0 : 1;

        response.seeServ = AlreadyTaken_Serv >= Initial_Serv ? 0 : 1;
        return response;
    }

    public InitialGetAllProductsResponse searchProductsManageInventoryOld(getSearchProductsRequest request) {
        log.info("Page No : " + request.page);
        log.info("IsToSeeInventory : " + request.seeInv);
        log.info("IsToSeeProduct : " + request.seeProd);
        log.info("IsToSeeService : " + request.seeServ);


        List<Inventory> Total_Inventories = inventoryRepo.findByCompanyIdAndBatchActive(request.companyId);


        List<ObjectId> inventoryProductIds = Total_Inventories.stream()
                .map(Inventory::getProductid)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


        int Initial_Inv = Total_Inventories.size();
        int Initial_Serv = serviceInventoryRepo.findBySellerid(new ObjectId(request.companyId)).size();
        int productSize = productRepo.findByCompanyIdInSellerLists(new ObjectId(request.companyId)).size();
        log.info("Total product Size : " + productSize);
        int Initial_Prod = productSize - Total_Inventories.size();

        log.info("Initial -Inventory for this seller : " + Initial_Inv);
        log.info("Initial -ServiceInventory for this seller : " + Initial_Serv);
        log.info("Initial -Product for this seller : " + Initial_Prod);

        int AlreadyTaken_Inv = request.invTaken;
        int AlreadyTaken_Serv = request.servTaken;
        int AlreadyTaken_Prod = request.prodTaken;

        log.info("Already Taken Inv : " + AlreadyTaken_Inv);
        log.info("Already Taken Serv Inv : " + AlreadyTaken_Serv);
        log.info("Already Taken Prod : " + AlreadyTaken_Prod);


        InitialGetAllProductsResponse response = new InitialGetAllProductsResponse();
        boolean inventoryExists = false;
        boolean ServiceInventoryExists = false;
        boolean productExists = false;
        if (!request.searchValue.trim().isEmpty() && request.searchValue != null && request.searchValue != "") {
            log.info("Search Value : " + request.searchValue.trim());
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
            if (!inventoryExists) {
                productExists = mongoTemplate.exists(
                        Query.query(
                                Criteria.where("productName").regex(
                                        ".*" + Pattern.quote(request.searchValue) + ".*", "i"
                                )
                        ),
                        "products");
            }


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
        log.info("ServiceInventoryExists : " + ServiceInventoryExists);
        log.info("ProductExists : " + productExists);

        List<InitialGetAllProductsResponse.ProductItem> resultList;

        if (inventoryExists || ServiceInventoryExists || productExists) {
            resultList = new ArrayList<>();


            Set<String> seenProductIds = new HashSet<>();


            int page = request.page != null ? request.page : 0;
            int pageSize = request.pageSize != null && request.pageSize > 0
                    ? request.pageSize
                    : 20;
            int remaining = pageSize;


            if (request.seeInv == 1 && remaining > 0 && inventoryExists) {

                List<AggregationOperation> pipeline = new ArrayList<>();

                pipeline.add(match(
                        Criteria.where("companyid").is(request.companyId)
//                                .orOperator(
//                                        Criteria.where("batchid.active").is(true),
//                                        Criteria.where("variantBatches.active").is(true)
//                                )
                ));

                pipeline.add(lookup("products", "productid", "_id", "product"));
                pipeline.add(unwind("product", true));

                if (request.categories != null && !request.categories.isEmpty()) {
                    pipeline.add(match(
                            Criteria.where("product.productCategory._id")
                                    .in(request.categories.stream().map(ObjectId::new).toList())
                    ));
                }

                if (request.searchValue != null && !request.searchValue.trim().isEmpty()) {

                    String searchRegex =
                            ".*" + Pattern.quote(request.searchValue.trim()) + ".*";

                    pipeline.add(
                            match(
                                    new Criteria().orOperator(

                                            // Main product name
                                            Criteria.where("productName")
                                                    .regex(searchRegex, "i"),
                                            Criteria.where("variantBatches.productname")
                                                    .regex(searchRegex, "i"),

                                            // Variant names
                                            Criteria.where("variantBatches.variantName")
                                                    .regex(searchRegex, "i")
                                    )
                            )
                    );
                }

//                pipeline.add(project()
//                        .and("_id").as("_id")
//                        .and("productid").as("ProductId")
//                        .and("productCategory").as("productCategoryDTO")
//                        .and("batchid.productname").as("ProductName")
//                        .and("batchid.created_at").as("CreatedAt")
//                        .and("batchid.stock_availability").as("stock_availability")
//                        .and("batchid._id").as("batchid")
//                        .and("batchid.seller_price").as("SellingPrice")
//                        .and("product.defaultImage").as("defaultImage")
//                        .and("variantBatches").as("productVariantDTOS")
//                        .and("Quickadd").as("Quickadd")
//                        .and("product.productCategory._id").as("Category.CategoryId")
//                        .and("product.productCategory.category_name").as("Category.CategoryName")
//                        .and("product.unit").as("unit")
//                );
                pipeline.add(
                        project()
                                .and("_id").as("_id")
                                .and("productid").as("ProductId")
                                .and("deals").as("deals")
                                .and("newArrivals").as("newArrivals")
                                .and("productCategory").as("productCategoryDTO")

                                // ProductName
                                .and(
                                        ConditionalOperators.ifNull(
                                                ArrayOperators.ArrayElemAt
                                                        .arrayOf("variantBatches.productname")
                                                        .elementAt(0)
                                        ).thenValueOf("batchid.productname")
                                ).as("ProductName")

                                // stock_availability
                                .and(
                                        ConditionalOperators.ifNull(
                                                ArrayOperators.ArrayElemAt
                                                        .arrayOf("variantBatches.stock_availability")
                                                        .elementAt(0)
                                        ).thenValueOf("batchid.stock_availability")
                                ).as("stock_availability")

                                .and(
                                        ConditionalOperators.ifNull(
                                                ArrayOperators.ArrayElemAt
                                                        .arrayOf("variantBatches.offerPrice")
                                                        .elementAt(0)
                                        ).thenValueOf("batchid.offerPrice")
                                ).as("OfferPrice")

                                .and(
                                        ConditionalOperators.ifNull(
                                                ArrayOperators.ArrayElemAt
                                                        .arrayOf("variantBatches.discount")
                                                        .elementAt(0)
                                        ).thenValueOf("batchid.discount")
                                ).as("DiscountPercent")

                                // CreatedAt
                                .and(
                                        ConditionalOperators.ifNull(
                                                ArrayOperators.ArrayElemAt
                                                        .arrayOf("variantBatches.created_at")
                                                        .elementAt(0)
                                        ).thenValueOf("batchid.created_at")
                                ).as("CreatedAt")

                                // batchid
                                .and(
                                        ConditionalOperators.ifNull(
                                                ArrayOperators.ArrayElemAt
                                                        .arrayOf("variantBatches._id")
                                                        .elementAt(0)
                                        ).thenValueOf("batchid._id")
                                ).as("batchid")

                                .and("variantBatches").as("productVariantDTOS")
                                .and("Quickadd").as("Quickadd")

                                // SellingPrice
                                .and(
                                        ConditionalOperators.ifNull(
                                                ArrayOperators.ArrayElemAt
                                                        .arrayOf("variantBatches.seller_price")
                                                        .elementAt(0)
                                        ).thenValueOf("batchid.seller_price")
                                ).as("SellingPrice")

                                .and("product.defaultImage").as("defaultImage")
                                .and("product.productCategory._id").as("Category.CategoryId")
                                .and("product.productCategory.category_name").as("Category.CategoryName")
                                .and("product.unit").as("unit")
                );

                pipeline.add(sort(Sort.by(Sort.Direction.ASC, "ProductName")));
                pipeline.add(skip((long) request.invTaken));
                pipeline.add(limit(remaining));

                List<InitialGetAllProductsResponse.ProductItem> inventoryResults =
                        mongoTemplate.aggregate(
                                newAggregation(pipeline),
                                "inventory",
                                InitialGetAllProductsResponse.ProductItem.class
                        ).getMappedResults();

                inventoryResults.forEach(item -> {
                    resultList.add(item);
                    seenProductIds.add(item.getProductId());
                });

                remaining -= inventoryResults.size();
                AlreadyTaken_Inv += inventoryResults.size();
                log.info("Remaining size after Inventory found: " + remaining);
            }


            if (request.seeServ == 1 && remaining > 0 && ServiceInventoryExists) {

                Criteria serviceCriteria = Criteria.where("sellerid")
                        .is(new ObjectId(request.companyId));
//                        .and("isActive").is(true);

                if (request.searchValue != null) {
                    String search = request.searchValue.trim();

                    if (!search.isEmpty()) {
                        serviceCriteria = serviceCriteria.and("servicename")
                                .regex(".*" + Pattern.quote(search) + ".*", "i");
                    }
                }

                List<InitialGetAllProductsResponse.ProductItem> serviceResults =
                        mongoTemplate.aggregate(
                                newAggregation(
                                        match(serviceCriteria),
                                        project()
                                                .and("_id").as("_id")
                                                .and("servicename").as("ProductName")
                                                .and("servicecost").as("SellingPrice")
                                                .and("serviceDesc").as("serviceDesc")
                                                .and("serviceNotes").as("serviceNotes")
                                                .and("isActive").as("isActive")
                                                .and("category").as("productCategoryDTO")
                                                .and("created_at").as("CreatedAt")
                                                .and("propertyAttributes").as("propertyAttributes")
                                                .and("defaultImage").as("defaultImage"),
                                        sort(Sort.by(Sort.Direction.ASC, "ProductName")),
                                        skip((long) request.servTaken),
                                        limit(remaining)
                                ),
                                "ServiceInventory",
                                InitialGetAllProductsResponse.ProductItem.class
                        ).getMappedResults();

                resultList.addAll(serviceResults);
                AlreadyTaken_Serv += serviceResults.size();
                remaining -= serviceResults.size();
                log.info("Remaining size after Service found: " + remaining);
            }


            if (request.seeProd == 1 && remaining > 0 && productExists) {

                List<AggregationOperation> productPipeline = new ArrayList<>();


                if (request.searchValue != null && !request.searchValue.trim().isEmpty()) {

                    Criteria searchCriteria = Criteria.where("productName")
                            .regex(".*" + Pattern.quote(request.searchValue.trim()) + ".*", "i");

                    Criteria companyCriteria = Criteria.where("sellerLists")
                            .elemMatch(
                                    Criteria.where("companyid")
                                            .is(new ObjectId(request.companyId.trim()))
                            );

                    Criteria excludeInventoryCriteria = Criteria.where("_id")
                            .nin(inventoryProductIds);

                    Criteria finalCriteria = new Criteria().andOperator(
                            searchCriteria,
                            companyCriteria,
                            excludeInventoryCriteria
                    );

                    productPipeline.add(Aggregation.match(finalCriteria));
                }
                productPipeline.add(project()
                        .and("_id").as("_id")
                        .and("_id").as("ProductId")
                        .and("productName").as("ProductName")
                        .and("defaultImage").as("defaultImage")
                        .and("Quickadd").as("Quickadd")
                        .and("created_at").as("CreatedAt")
                        .and("productCategory._id").as("Category.CategoryId")
                        .and("productCategory.category_name").as("Category.CategoryName")
                        .and("unit").as("unit")
                );

                productPipeline.add(sort(Sort.by(Sort.Direction.ASC, "ProductName")));
                productPipeline.add(skip((long) request.prodTaken));
                productPipeline.add(limit(remaining));

                List<InitialGetAllProductsResponse.ProductItem> productResults =
                        mongoTemplate.aggregate(
                                newAggregation(productPipeline),
                                "products",
                                InitialGetAllProductsResponse.ProductItem.class
                        ).getMappedResults();

                resultList.addAll(productResults);
                AlreadyTaken_Prod += productResults.size();
            }


            response.setProducts(resultList);
        } else if (request.searchValue == null || request.searchValue.isEmpty()) {

            log.info("No search match, returning inventory + services + products (priority)");

            int page = request.page != null ? request.page : 0;
            int pageSize = request.pageSize != null ? request.pageSize : 20;

            List<InitialGetAllProductsResponse.ProductItem> finalResult = new ArrayList<>();

            List<AggregationOperation> inventoryPipeline = new ArrayList<>();
            if (request.seeInv == 1) {
                inventoryPipeline.add(
                        match(
                                Criteria.where("companyid").is(request.companyId)
//                                        .orOperator(
//                                                Criteria.where("batchid.active").is(true),
//                                                Criteria.where("variantBatches.active").is(true)
//                                        )
                        )
                );

                inventoryPipeline.add(lookup("products", "productid", "_id", "product"));
                inventoryPipeline.add(unwind("product", true));

                if (request.categories != null && !request.categories.isEmpty()) {
                    inventoryPipeline.add(
                            match(
                                    Criteria.where("product.productCategory._id")
                                            .in(
                                                    request.categories
                                                            .stream()
                                                            .map(ObjectId::new)
                                                            .toList()
                                            )
                            )
                    );
                }

//                inventoryPipeline.add(
//                        project()
//                                .and("_id").as("_id")
//                                .and("productid").as("ProductId")
//                                .and("productCategory").as("productCategoryDTO")
//                                .and("batchid.productname").as("ProductName")
//                                .and("batchid.stock_availability").as("stock_availability")
//                                .and("batchid.created_at").as("CreatedAt")
//                                .and("batchid._id").as("batchid")
//                                .and("variantBatches").as("productVariantDTOS")
//                                .and("Quickadd").as("Quickadd")
//                                .and("batchid.seller_price").as("SellingPrice")
//                                .and("product.defaultImage").as("defaultImage")
//                                .and("product.productCategory._id").as("Category.CategoryId")
//                                .and("product.productCategory.category_name").as("Category.CategoryName")
//                                .and("product.unit").as("unit")
//                );

                inventoryPipeline.add(
                        project()
                                .and("_id").as("_id")
                                .and("productid").as("ProductId")
                                .and("deals").as("deals")
                                .and("newArrivals").as("newArrivals")
                                .and("productCategory").as("productCategoryDTO")

                                // ProductName
                                .and(
                                        ConditionalOperators.ifNull(
                                                ArrayOperators.ArrayElemAt
                                                        .arrayOf("variantBatches.productname")
                                                        .elementAt(0)
                                        ).thenValueOf("batchid.productname")
                                ).as("ProductName")

                                // stock_availability
                                .and(
                                        ConditionalOperators.ifNull(
                                                ArrayOperators.ArrayElemAt
                                                        .arrayOf("variantBatches.stock_availability")
                                                        .elementAt(0)
                                        ).thenValueOf("batchid.stock_availability")
                                ).as("stock_availability")

                                .and(
                                        ConditionalOperators.ifNull(
                                                ArrayOperators.ArrayElemAt
                                                        .arrayOf("variantBatches.offerPrice")
                                                        .elementAt(0)
                                        ).thenValueOf("batchid.offerPrice")
                                ).as("OfferPrice")


                                .and(
                                        ConditionalOperators.ifNull(
                                                ArrayOperators.ArrayElemAt
                                                        .arrayOf("variantBatches.discount")
                                                        .elementAt(0)
                                        ).thenValueOf("batchid.discount")
                                ).as("DiscountPercent")


                                .and(
                                        ConditionalOperators.ifNull(
                                                ArrayOperators.ArrayElemAt
                                                        .arrayOf("variantBatches.created_at")
                                                        .elementAt(0)
                                        ).thenValueOf("batchid.created_at")
                                ).as("CreatedAt")

                                // batchid
                                .and(
                                        ConditionalOperators.ifNull(
                                                ArrayOperators.ArrayElemAt
                                                        .arrayOf("variantBatches._id")
                                                        .elementAt(0)
                                        ).thenValueOf("batchid._id")
                                ).as("batchid")

                                .and("variantBatches").as("productVariantDTOS")
                                .and("Quickadd").as("Quickadd")

                                // SellingPrice
                                .and(
                                        ConditionalOperators.ifNull(
                                                ArrayOperators.ArrayElemAt
                                                        .arrayOf("variantBatches.seller_price")
                                                        .elementAt(0)
                                        ).thenValueOf("batchid.seller_price")
                                ).as("SellingPrice")
                                .and(
                                        ConditionalOperators.ifNull(
                                                ArrayOperators.ArrayElemAt
                                                        .arrayOf("variantBatches.variantName")
                                                        .elementAt(0)
                                        ).thenValueOf("batchid.variantName")
                                ).as("variantName")

                                .and("product.defaultImage").as("defaultImage")
                                .and("product.productCategory._id").as("Category.CategoryId")
                                .and("product.productCategory.category_name").as("Category.CategoryName")
                                .and("product.unit").as("unit")

                );

                inventoryPipeline.add(
                        sort(
                                Sort.by(
                                        Sort.Order.desc("CreatedAt"),
                                        Sort.Order.asc("ProductName") // optional secondary sort
                                )
                        )
                );

                inventoryPipeline.add(skip((long) request.invTaken));
                inventoryPipeline.add(limit(pageSize));


            }
            if (!inventoryPipeline.isEmpty()) {
                List<InitialGetAllProductsResponse.ProductItem> inventoryList =
                        mongoTemplate.aggregate(
                                newAggregation(inventoryPipeline),
                                "inventory",
                                InitialGetAllProductsResponse.ProductItem.class
                        ).getMappedResults();

                finalResult.addAll(inventoryList);

            }
            int remaining = pageSize - finalResult.size();
            AlreadyTaken_Inv += finalResult.size();

            log.info("Remaining size after Inventory found: " + remaining);


            if (remaining > 0 && request.seeServ == 1) {
                List<AggregationOperation> servicePipeline = new ArrayList<>();

                servicePipeline.add(
                        match(Criteria.where("sellerid")
                                        .is(new ObjectId(request.companyId))
//                                .and("isActive").is(true)
                        )
                );
                if (request.categories != null && !request.categories.isEmpty()) {
                    servicePipeline.add(
                            match(
                                    Criteria.where("category._id")
                                            .in(
                                                    request.categories
                                                            .stream()
                                                            .map(ObjectId::new)
                                                            .toList()
                                            )
                            )
                    );
                }

                servicePipeline.add(project()
                        .and("_id").as("_id")
                        .and("servicename").as("ProductName")
                        .and("servicecost").as("SellingPrice")
                        .and("serviceDesc").as("serviceDesc")
                        .and("serviceNotes").as("serviceNotes")
                        .and("isActive").as("isActive")
                        .and("propertyAttributes").as("propertyAttributes")
                        .and("category").as("productCategoryDTO")
                        .and("addOnIds").as("addOnIds")
                        .and("defaultImage").as("defaultImage")
                        .and("created_at").as("CreatedAt")
                );

                servicePipeline.add(sort(Sort.by("ProductName")));
                servicePipeline.add(skip((long) request.servTaken));
                servicePipeline.add(limit(remaining));


                log.info("Service: ", mongoTemplate.aggregate(
                        newAggregation(servicePipeline),
                        "ServiceInventory",
                        InitialGetAllProductsResponse.ProductItem.class
                ).getMappedResults());

                if (!servicePipeline.isEmpty()) {
                    List<InitialGetAllProductsResponse.ProductItem> serviceList =
                            mongoTemplate.aggregate(
                                    newAggregation(servicePipeline),
                                    "ServiceInventory",
                                    InitialGetAllProductsResponse.ProductItem.class
                            ).getMappedResults();

                    finalResult.addAll(serviceList);
                    remaining -= serviceList.size();
                    AlreadyTaken_Serv += serviceList.size();
                }
            }

            log.info("Remaining size after Service found: " + remaining);


            if (remaining > 0 && request.seeProd == 1) {

                List<AggregationOperation> productPipeline = new ArrayList<>();

                Criteria searchCriteria = Criteria.where("productName")
                        .regex(".*" + Pattern.quote(request.searchValue.trim()) + ".*", "i");

                Criteria companyCriteria = Criteria.where("sellerLists")
                        .elemMatch(
                                Criteria.where("companyid")
                                        .is(new ObjectId(request.companyId.trim()))
                        );
                Criteria excludeInventoryCriteria = Criteria.where("_id")
                        .nin(inventoryProductIds);

                Criteria finalCriteria = new Criteria().andOperator(
                        searchCriteria,
                        companyCriteria,
                        excludeInventoryCriteria
                );


                productPipeline.add(Aggregation.match(finalCriteria));

                productPipeline.add(
                        project()
                                .and("_id").as("_id")
                                .and("_id").as("ProductId")
                                .and("productCategory").as("productCategoryDTO")
                                .and("Quickadd").as("Quickadd")
                                .and("productName").as("ProductName")
                                .and("defaultImage").as("defaultImage")
                                .and("created_at").as("CreatedAt")
                                .and("productCategory._id").as("Category.CategoryId")
                                .and("productCategory.category_name").as("Category.CategoryName")
                                .and("unit").as("unit")
                );

                productPipeline.add(sort(Sort.by("ProductName")));
                productPipeline.add(skip((long) request.prodTaken));
                productPipeline.add(limit(remaining));

                if (!productPipeline.isEmpty()) {
                    List<InitialGetAllProductsResponse.ProductItem> productList =
                            mongoTemplate.aggregate(
                                    newAggregation(productPipeline),
                                    "products",
                                    InitialGetAllProductsResponse.ProductItem.class
                            ).getMappedResults();

                    finalResult.addAll(productList);
                    AlreadyTaken_Prod += productList.size();
                }

            }


            resultList = finalResult;
        } else {
            resultList = new ArrayList<>();
            response.setProducts(Collections.emptyList());
            return response;
        }

        response.setProducts(resultList);
        response.invTaken = AlreadyTaken_Inv;
        response.prodTaken = AlreadyTaken_Prod;
        response.servTaken = AlreadyTaken_Serv;

        response.seeInv = AlreadyTaken_Inv >= Initial_Inv ? 0 : 1;
        response.seeProd = AlreadyTaken_Prod >= Initial_Prod ? 0 : 1;
        response.seeServ = AlreadyTaken_Serv >= Initial_Serv ? 0 : 1;
        return response;
    }

    public GetViewProductDetailsResponse GetViewProductDetails(GetViewProductDetailsRequest request) {
        GetViewProductDetailsResponse getViewProductDetailsResponse = new GetViewProductDetailsResponse();
        if (request.productId != null) {
            Optional<Products> products = productRepo.findById(request.productId);
            log.info("isProduct Present : " + products.isPresent());
            if (products.isPresent()) {
                getViewProductDetailsResponse.productsDTO = SellerAdaptor.FromProducttoProductDTO(products.get());
            }
        }
        if (request.productId != null && request.sellerId != null) {

            Inventory inventory = inventoryRepo.findByProductidAndCompanyid(
                    new ObjectId(request.productId),
                    request.sellerId
            );

            log.info("Inventory found: {}", inventory != null);

            if (inventory != null) {

                List<Inventory> inventories = new ArrayList<>();
                inventories.add(inventory);

                List<InventoryDTO> inventoryDTOs =
                        SellerAdaptor.FromInventorytoInventoryDTO(
                                inventories,
                                new HashMap<>()
                        );

                if (!inventoryDTOs.isEmpty()) {
                    getViewProductDetailsResponse.inventoryDTO = inventoryDTOs.get(0);
                }

            } else {
                log.warn(
                        "Inventory not found for productId: {} and sellerId: {}",
                        request.productId,
                        request.sellerId
                );
            }
        }
        return getViewProductDetailsResponse;
    }

    public GetSalesFeatureResponse getSalesFeatures(GetSalesFeatureRequest request) {
        GetSalesFeatureResponse getSalesFeatureResponse = new GetSalesFeatureResponse();
        if (request.sellerId != null) {
            List<Sale_features> sale_features = salefeaturesRepo.findBysellerid(new ObjectId(String.valueOf(request.sellerId)));
            if (!sale_features.isEmpty()) {
                for (Sale_features i : sale_features) {
                    getSalesFeatureResponse.features.add(new GetSalesFeatureResponse.features(i.get_id(), i.getFeature_name()));
                }

            }
        }
        return getSalesFeatureResponse;

    }

    public List<SellerServiceResponse> initialOrderServiceLoad(SellerServiceRequest sellerserviceRequest) {
        List<ServiceInventory> a = serviceInventoryRepo.findBySelleridAndIsActive(sellerserviceRequest.sellerid, true);
        if (a.size() != 0 && !a.isEmpty()) {
            return SellerAdaptor.ToSellerServiceResponse(a);
        }
        return new ArrayList<SellerServiceResponse>();
    }

    @Transactional
    public String Quickadd(List<Batches> batches, LinkedHashMap<String, String> map, HashMap<String, ProductCategory> productCategoryHashMap) {
        MessageResponse msgResponse = new MessageResponse();
        ClientSession session = null;

        try {
            session = mongoClient.startSession();
            session.startTransaction();

            Collection<Batches> insertedBatches = mongoTemplate.withSession(session).insertAll(batches);
            log.info("insertedBatches size: " + insertedBatches.size());

            List<Inventory> inventoryList = new ArrayList<>();

            int ind = 0;

            for (Batches batch : insertedBatches) {
                Inventory inventory = inventoryRepo.findByProductidAndCompanyid(batch.getProductid(), batch.getCompanyid());
                if (inventory != null && inventory.getBatchid() != null && inventory.getBatchid().getStock_availability() == 0) {
                    inventory.setBatchid(batch);
                    mongoTemplate.withSession(session).save(batch);
                    mongoTemplate.withSession(session).save(inventory);
                } else {
                    if (inventory == null) inventory = new Inventory();
                    inventory.setCompanyid(batch.getCompanyid());
                    inventory.setProductCategory(productCategoryHashMap.get(String.valueOf(batch.getProductid())));
                    inventory.setSellerprice(batch.getSeller_price());
                    inventory.setProductName(batch.getProductname());
                    inventory.setBatchid(batch);
                    SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddHHmmss");


                    inventory.setCreated_at(sdf.format(new Date()));

                    inventory.setProductid(batch.getProductid());
                    if (map.containsKey(batch.getProductid().toString())) {
                        inventory.setGst(map.get(batch.getProductid().toString()));
                    }
                    inventoryList.add(inventory);
                }
//                System.out.println(ind);
                ind++;
            }
            log.info("inventoryList size: " + inventoryList.size());

            mongoTemplate.withSession(session).insertAll(inventoryList);

            session.commitTransaction();
            session.close();
            msgResponse.setValidationMessage("Successfully registered inventory and batches.");

        } catch (DuplicateKeyException e) {
            log.error("error : " + e);
            if (session != null) session.abortTransaction();
            msgResponse.setErrorMsg("Duplicate Key Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("error : " + e);
            if (session != null) session.abortTransaction();
            msgResponse.setErrorMsg("Transaction failed: " + e.getMessage());
        } finally {
            if (session != null) session.close();
        }

        return msgResponse.getValidationMessage();
    }

    @Transactional
    public String AddInventory(Batches batches, SellerBatchesRequest request) {
        MessageResponse msgResponse = new MessageResponse();
        log.info("In Add - Inventory");
        try (ClientSession session = mongoClient.startSession()) {
            session.startTransaction();
            System.out.println(batches.getProductid());
            System.out.println(batches.getCompanyid());

            Inventory inventory =
                    inventoryRepo.findByProductidAndCompanyid(
                            batches.getProductid(), batches.getCompanyid()
                    );
            log.info("Inventory : " + inventory);

            if (inventory != null) {
                int index = -1;
                int k = 0;
                if (inventory.getVariantBatches() != null) {
                    for (Batches i : inventory.getVariantBatches()) {
                        if (batches.getVariantName().equals(i.getVariantName()) && !i.isActive()) {
                            index = k;
                            break;
                        }
                        k++;
                    }
                }
                if ((inventory.getBatchid() != null && inventory.getBatchid().getStock_availability() == 0)) {
                    batches.setActive(true);
                    inventory.setBatchid(batches);
                    mongoTemplate.withSession(session).insert(batches);
                    mongoTemplate.withSession(session).save(inventory);
                    msgResponse.ValidationMessage = "Successfully registered batches and save inventory.";
                } else if (inventory.getVariantBatches() != null && index != -1) {
                    batches.setActive(true);
                    inventory.getVariantBatches().set(index, batches);
                    mongoTemplate.withSession(session).insert(batches);
                    mongoTemplate.withSession(session).save(inventory);
                    msgResponse.ValidationMessage = "Successfully registered batches and save inventory.";
                } else {
                    batches.setActive(false);
                    mongoTemplate.withSession(session).insert(batches);
                    msgResponse.ValidationMessage = "Successfully registered Only batches.";
                }

            } else {
                log.info("creating inventory");

                Inventory newInventory = new Inventory();
                newInventory.setCompanyid(batches.getCompanyid());
                newInventory.setProductid(batches.getProductid());
                newInventory.setProductCategory(SellerAdaptor.FromProductCategoryDTOtoProductCategory(request.productCategoryDTO));
                newInventory.setBatchid(batches);
                if (request.getGst() != null && !request.getGst().isEmpty()) newInventory.setGst(request.getGst());

                batches.setActive(true);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYYMMddHHmmss");

                inventory.setCreated_at(LocalDateTime.now().format(formatter));


                mongoTemplate.withSession(session).insert(batches);
                mongoTemplate.withSession(session).insert(newInventory);

                msgResponse.ValidationMessage =
                        "Successfully registered inventory and batches.";
            }

            session.commitTransaction();
        } catch (Exception e) {
            log.error("error : " + e);
            msgResponse.ErrorMsg = "Transaction failed: " + e.getMessage();
        }

        return msgResponse.getValidationMessage();
    }

    public SellerGetBatchesResponse GetBatches(SellerGetBatchesRequest sellerGetBatchesRequest) { //pending
        String companyId = sellerGetBatchesRequest.getCompanyid();
        log.info("Update companyid: " + companyId);
        log.info("Update productID: " + sellerGetBatchesRequest.getProductid());
        List<Batches> ans = new ArrayList<>();
        if (sellerGetBatchesRequest.variantName != null && sellerGetBatchesRequest.variantName != "") {
            log.info("Variant name is available");
            ans = batchesRepo.findByListProductidAndCompanyidAndVariantName(
                    new ObjectId(sellerGetBatchesRequest.getProductid()),
                    sellerGetBatchesRequest.companyid,
                    sellerGetBatchesRequest.variantName
            );
        } else {
            ans = batchesRepo.findByProductidAndCompanyid(
                    new ObjectId(sellerGetBatchesRequest.getProductid()),
                    companyId,
                    Sort.by(Sort.Direction.ASC, "created_at"));
        }
        log.info("ans : " + ans.size());
        log.info("Productid  : " + sellerGetBatchesRequest.productid);
        Inventory inventory = inventoryRepo.findByProductidAndCompanyid(new ObjectId(sellerGetBatchesRequest.getProductid()), companyId);
        log.info("Inventory found: " + inventory);
        List<Batches> a = new ArrayList<>();
        for (Batches batch : ans) {
            a.add(SellerAdaptor.ToResponseBatch(batch));

        }
        return SellerAdaptor.ToSellerGetBatchesResponse(a, inventory.getVersion());
    }

    //    public void soldout(Batches batch, Inventory inventory, List<Batches> batchList) {
//
//        // 1️⃣ Mark current batch as sold out
//        batch.setActive(false);
//        batch.setStock_availability(0);
//        mongoTemplate.save(batch);
//
//        // 2️⃣ Sort batches by createdAt (ascending)
//        List<Batches> sortedBatches = batchList.stream()
//                .filter(b -> Boolean.TRUE.equals(b.getActive()) || b.get_id().equals(batch.get_id()))
//                .sorted(Comparator.comparing(Batches::getCreatedAt))
//                .toList();
//
//
//
//        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
//        LocalDateTime currentCreatedAt =
//                LocalDateTime.parse(batch.getCreatedAt(), formatter);
//
//        Batches nextAvailableBatch = batchList.stream()
//                // ✅ SAME PRODUCT ONLY
//                .filter(b ->
//                        Objects.equals(b.getProductid(), batch.getProductid())
//                )
//                .filter(b ->
//                        Objects.equals(b.getVariantName(), batch.getVariantName())
//                )
//                // ✅ AFTER current batch
//                .filter(b ->
//                        LocalDateTime.parse(b.getCreatedAt(), formatter)
//                                .isAfter(currentCreatedAt)
//                )
//                // ✅ OPTIONAL: must have stock
//                .filter(b -> b.getStock_availability() > 0)
//                // ✅ earliest next batch
//                .sorted(Comparator.comparing(
//                        b -> LocalDateTime.parse(b.getCreatedAt(), formatter)
//                ))
//                .findFirst()
//                .orElse(null);
//
//        // 4️⃣ Activate next batch & update inventory
//        if (nextAvailableBatch != null) {
//            nextAvailableBatch.setActive(true);
//            mongoTemplate.save(nextAvailableBatch);
//
//            if (inventory.getVariantBatches()!=null && inventory.getVariantBatches().size()>1){
//                int index = -1 ;
//                int p = 0 ;
//                for (Batches i : inventory.getVariantBatches()){
//                    if (batch.get_id().equals(i.get_id())){
//                        index= p ;
//                        break;
//                    }
//                    p++;
//                }
//                log.info("Index : "+index);
//                List<Batches> exist = inventory.getVariantBatches();
//                exist.set(index,nextAvailableBatch);
//
//            }
//            else {
//                inventory.setBatchid(nextAvailableBatch);
//                inventory.setSellerprice(Integer.parseInt(nextAvailableBatch.getSeller_price()));
//            }
//            mongoTemplate.save(inventory);
//
//            log.info("Activated next batch: {}", nextAvailableBatch.get_id());
//        }
//        else {
//            // 5️⃣ No next batch → inventory sold out
//            if (inventory.getVariantBatches()!=null && inventory.getVariantBatches().size()>1){
//                int index = -1 ;
//                int p = 0 ;
//                for (Batches i : inventory.getVariantBatches()){
//                    if (batch.get_id().equals(i.get_id())){
//                        index= p ;
//                        break;
//                    }
//                    p++;
//                }
//                log.info("Index : "+index);
//                List<Batches> exist = inventory.getVariantBatches();
//                exist.set(index,batch);
//
//            }
//            else {
//                inventory.getBatchid().setActive(false);
//                inventory.getBatchid().setStock_availability(0);
//                inventory.setSellerprice(0);
//
//            }
//            mongoTemplate.save(inventory);
//
//            log.info("No next batch found. Inventory marked sold out.");
//        }
//    }
    public void soldout(Batches batch, Inventory inventory, List<Batches> batchList) {

        Batches currentBatch = mongoTemplate.findById(
                batch.get_id(),
                Batches.class
        );

        if (currentBatch == null) {
            log.error("Batch not found: {}", batch.get_id());
            return;
        }


        // Mark current batch sold out
        currentBatch.setActive(false);
        currentBatch.setStock_availability(0);
        mongoTemplate.save(currentBatch);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        final LocalDateTime currentCreatedAt =
                LocalDateTime.parse(currentBatch.getCreatedAt(), formatter);

        final ObjectId currentProductId = currentBatch.getProductid();
        final ObjectId currentBatchId = new ObjectId(currentBatch.get_id());
        log.info("Current batch id : " + currentBatchId);

        final String currentVariant =
                StringUtils.hasText(currentBatch.getVariantName())
                        ? currentBatch.getVariantName().trim()
                        : "";

        Batches nextAvailableBatch = batchList.stream()
                // Same product
                .filter(b -> Objects.equals(
                        b.getProductid(),
                        currentProductId))

                // Exclude current batch
                .filter(b -> !Objects.equals(
                        b.get_id(),
                        currentBatchId))

                // Same variant
                .filter(b -> {
                    String variant =
                            StringUtils.hasText(b.getVariantName())
                                    ? b.getVariantName().trim()
                                    : "";

                    return variant.equalsIgnoreCase(currentVariant);
                })

                // Must have stock
                .filter(b -> b.getStock_availability() > 0)

                // Created after current batch
                .filter(b ->
                        LocalDateTime.parse(
                                b.getCreatedAt(),
                                formatter
                        ).isAfter(currentCreatedAt))

                .sorted(Comparator.comparing(
                        b -> LocalDateTime.parse(
                                b.getCreatedAt(),
                                formatter)
                ))
                .findFirst()
                .orElse(null);

        if (nextAvailableBatch != null) {

            Batches latestNextBatch = mongoTemplate.findById(
                    nextAvailableBatch.get_id(),
                    Batches.class
            );

            if (latestNextBatch != null) {

                latestNextBatch.setActive(true);
                mongoTemplate.save(latestNextBatch);


                if (inventory.getVariantBatches() != null && inventory.getVariantBatches().size() > 1) {
                    log.info("Variant batch available");
                    int index = -1;
                    int p = 0;
                    for (Batches i : inventory.getVariantBatches()) {
                        if (batch.get_id().equals(i.get_id())) {
                            index = p;
                            break;
                        }
                        p++;
                    }
                    log.info("Variant batch index : " + index);
                    if (index >= 0) {
                        inventory.getVariantBatches().set(index, latestNextBatch);
                    }

                } else {
                    log.info("Only batch available");
                    inventory.setBatchid(latestNextBatch);
                    inventory.setSellerprice(latestNextBatch.getSeller_price());
                }
                mongoTemplate.save(inventory);
                log.info("Activated next batch: {}",
                        latestNextBatch.get_id());
            }

        } else {

            if (currentBatchId != null && inventory.getVariantBatches() != null && inventory.getVariantBatches().size() > 1) {

                int index = -1;

                for (int i = 0; i < inventory.getVariantBatches().size(); i++) {

                    String batchId = String.valueOf(
                            inventory.getVariantBatches().get(i).get_id()
                    );

                    String currentId = String.valueOf(currentBatchId);

                    log.info("Batch ID   : {}", batchId);
                    log.info("Current ID : {}", currentId);
                    log.info("Types      : {} / {}",
                            inventory.getVariantBatches().get(i).get_id().getClass().getName(),
                            currentBatchId.getClass().getName());

                    if (batchId.equals(currentId)) {
                        index = i;
                        break;
                    }
                }
                log.info("Index: " + index);
                if (index >= 0) {
                    inventory.getVariantBatches()
                            .set(index, currentBatch);
                }
                for (Batches i : inventory.getVariantBatches()) {
                    log.info("ID: " + i.get_id());
                    log.info("Active :" + i.isActive());
                    log.info("Stock :" + i.getStock_availability());
                }

            } else {

                if (inventory.getBatchid() != null) {
                    inventory.getBatchid().setActive(false);
                    inventory.getBatchid().setStock_availability(0);
                }

                inventory.setSellerprice(0);
            }

            mongoTemplate.save(inventory);

            log.info("No next batch found. Inventory marked sold out.");
        }
    }

    @Transactional
    public String Update(SellerUpdateRequest sellerUpdateRequest) {
        ClientSession session = null;
        log.info("In update ");

        try {
            session = mongoClient.startSession();
            session.startTransaction();

            LocalDateTime now = LocalDateTime.now();

            double procurementPrice = sellerUpdateRequest.getProcumentprice();
            double priceDouble = sellerUpdateRequest.seller_price;
            int sellerPrice = (int) priceDouble;
            log.info("seller price: " + sellerPrice);

            List<Batches> batchList = batchesRepo.findByProductidAndCompanyid(
                    new ObjectId(sellerUpdateRequest.getProductid()),
                    sellerUpdateRequest.getCompanyid(),
                    Sort.by(Sort.Direction.ASC, "created_at")
            );

            Inventory inventory = inventoryRepo.findByProductidAndCompanyid(
                    new ObjectId(sellerUpdateRequest.getProductid()),
                    sellerUpdateRequest.getCompanyid()
            );

            Batches batch = batchList.stream()
                    .filter(b -> b.get_id().toString().equals(sellerUpdateRequest.get_id()))
                    .findFirst()
                    .orElse(null);

            if (batch == null || inventory == null) {
                return "Invalid batch or inventory reference.";
            }

            // Version check
            if (!Objects.equals(sellerUpdateRequest.getBatch_version(), batch.getVersion()) ||
                    !Objects.equals(sellerUpdateRequest.getInventory_version(), inventory.getVersion())) {
                return "Please go back and reopen the page to refresh data!";
            }

            if (sellerUpdateRequest.getSoldout()) {
                soldout(batch, inventory, batchList);
            } else {
                if (sellerUpdateRequest.getQuantity() == 0) {
                    return "You selected zero quantity. Please select another batch.";
                }

                boolean shouldBeActive = "Active".equalsIgnoreCase(sellerUpdateRequest.getStatus());
                boolean shouldBeInactive = "Inactive".equalsIgnoreCase(sellerUpdateRequest.getStatus());

                if (shouldBeInactive && batch.isActive()) {
                    return "You have an active batch. Please set another batch to active before marking this one inactive.";
                }

                if (shouldBeActive && !batch.isActive()) {
                    ClientSession finalSession = session;
                    batchList.stream()
                            .filter(Batches::isActive)
                            .filter(b -> !b.get_id().equals(batch.get_id()))
                            .findFirst()
                            .ifPresent(activeBatch -> {
                                activeBatch.setActive(false);
                                mongoTemplate.withSession(finalSession).save(activeBatch);
                            });

                    batch.setActive(true);
                }

                // Update batch
                if (sellerUpdateRequest.getGst() != null && !sellerUpdateRequest.getGst().equals(""))
                    inventory.setGst(sellerUpdateRequest.getGst());
                batch.setBatch_unit(sellerUpdateRequest.getBatch_unit());
                batch.setBatch_expiryDate(sellerUpdateRequest.getBatch_expiryDate());
                batch.setStock_availability(sellerUpdateRequest.getQuantity());
                batch.setUpdatedAt(now.toString());
                batch.setDiscount(sellerUpdateRequest.discount);
                batch.setOfferPrice(sellerUpdateRequest.offerPrice);
                batch.setExpiry_date(sellerUpdateRequest.getExpiry());
                batch.setProcurement_price(procurementPrice);
                batch.setMinimum_order(sellerUpdateRequest.getMinimumorder());
                if (sellerUpdateRequest.margin != 0) batch.setMargin_percentage(sellerUpdateRequest.getMargin());
                batch.setUnit(sellerUpdateRequest.getUnit());
                batch.setSeller_price(sellerPrice);

                mongoTemplate.withSession(session).save(batch);

                if (inventory.getVariantBatches() != null && !inventory.getVariantBatches().isEmpty()) {
                    List<Batches> ExistvariantBatches = inventory.getVariantBatches();
                    int ind = 0;
                    for (Batches k : ExistvariantBatches) {
                        if (k.getVariantName().equals(batch.getVariantName())) {
                            ExistvariantBatches.set(ind, batch);
                        }
                        ind++;
                    }
                    inventory.setSellerprice(ExistvariantBatches.get(0).getSeller_price());
                    inventory.setVariantBatches(ExistvariantBatches);

                } else {
                    inventory.setBatchid(batch);
                    inventory.setSellerprice(sellerPrice);
                }


                mongoTemplate.withSession(session).save(inventory);
            }

            session.commitTransaction();
            return "Successfully updated batch details.";
        } catch (DuplicateKeyException e) {
            log.error("error : " + e);
            if (session != null) session.abortTransaction();
            return "Duplicate Key Error: " + e.getMessage();
        } catch (Exception e) {
            log.error("error : " + e);
            if (session != null) session.abortTransaction();
            return "Transaction failed: " + e.getMessage();
        } finally {
            if (session != null) session.close();
        }
    }

    public List<InventoryDTO> Precheck(List<Inventory> inventoryList, List<InventoryDTO> requestedItems, boolean isFromQuickInvoice) {
        log.info("Inside precheck for {} requested items", requestedItems.size());

        requestedItems.forEach(item -> {

            Inventory matchedInventory = inventoryList.stream()
                    .filter(inv ->
                            inv.getProductid() != null &&
                                    item.getProductid() != null &&
                                    inv.getProductid().equals(item.getProductid()) &&
                                    inv.getCompanyid() != null &&
                                    inv.getCompanyid().equals(item.getCompanyid()))
                    .findFirst()
                    .orElse(null);

            if (matchedInventory == null) {
                item.setMessage("Inventory missing. Please refresh and try again.");
                return;
            }

            Batches matchedBatch = null;

            if (matchedInventory.getBatchid() != null
                    && matchedInventory.getBatchid().get_id().equals(item.batch_id)) {
                matchedBatch = matchedInventory.getBatchid();
            }

            if (matchedBatch == null
                    && matchedInventory.getVariantBatches() != null
                    && !matchedInventory.getVariantBatches().isEmpty()) {

                matchedBatch = matchedInventory.getVariantBatches().stream()
                        .filter(batch -> batch.get_id().equals(item.batch_id))
                        .findFirst()
                        .orElse(null);
            }

            if (matchedBatch == null) {
                item.setMessage("Inventory details changed, Please reload the page to complete order");
                return;
            }

            int dbQty = matchedBatch.getStock_availability();
            int orderQty = item.getOrder_quantity();

            double unitSellingPrice = isFromQuickInvoice ? item.procurement_price : item.getSellerprice();
            double unitOfferPrice = (item.variantBatches != null && !item.variantBatches.isEmpty())
                    ? item.variantBatches.get(0).offerPrice
                    : item.offerPrice;

            double dbSellingPrice = matchedBatch.getSeller_price();
            double dbOfferPrice = matchedBatch.getOfferPrice();

            log.info("Product {}: ordered={}, available={}, unitPrice={}, dbPrice={}",
                    item.getProductid(), orderQty, dbQty, unitSellingPrice, dbSellingPrice);

            boolean offerMatches = unitOfferPrice > 0 && dbOfferPrice > 0 && pricesEqual(unitOfferPrice, dbOfferPrice);
            boolean priceMatches = pricesEqual(unitSellingPrice, dbSellingPrice) || offerMatches;

            // Price and quantity checks
            if (orderQty <= dbQty && priceMatches) {
                item.setMessage("");
                item.setRemaining_quantity(dbQty - orderQty);

            } else if (orderQty <= dbQty) { // qty ok, price changed
                item.setMessage(item.productname + " - Qty is available but price changed. Please reload the page to complete order.");

            } else if (!priceMatches) { // qty short AND price changed
                item.setMessage(item.productname + " - Quantity and Price updated for some items. Please reload the page to complete order");

            } else { // qty short, price matches -> check if next batch can cover it
                List<Batches> batchList = batchesRepo.findByProductidAndCompanyid(
                        item.getProductid(), item.getCompanyid(), Sort.by(Sort.Direction.ASC, "created_at"));

                int index = batchList.indexOf(matchedBatch);
                if (index != -1 && index + 1 < batchList.size()) {
                    Batches nextBatch = batchList.get(index + 1);
                    if (nextBatch.getStock_availability() >= orderQty
                            && pricesEqual(nextBatch.getProcurement_price(), unitSellingPrice)) {
                        item.setMessage(item.productname + " - Lesser Items in current batch, Review Order quantity/inventory to complete order");
                        return;
                    }
                }

                item.setMessage(item.productname + " - Less items in stock. Review Order quantity to complete order");
            }
        });

        return requestedItems;
    }

    private boolean pricesEqual(double a, double b) {
        return Math.abs(a - b) < 0.001;
    }

    public ResponseEntity<?> uploadServicePropertyImages(Map<String, Object> payload, String orderNo) {

        try {

            String serviceId = (String) payload.get("id");
            String userFolder = (String) payload.get("userfolder");
            String serviceName = (String) payload.get("servicename");
            String uniqueID = (String) payload.get("uniqueID");

            log.info("serviceId: {} userFolder: {} serviceName: {}", serviceId, userFolder, serviceName);

            if (userFolder == null || serviceId == null || serviceName == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Required fields missing"));
            }
            serviceName = ((String) payload.get("servicename"))
                    .trim()
                    .replaceAll("[/()]", "")
                    .replaceAll("\\s+", "-");

            List<Map<String, Object>> images =
                    (List<Map<String, Object>>) payload.get("images");

            if (images == null || images.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No images provided"));
            }

            String baseFolderPath = "uploads"
                    + File.separator + "uploadsServiceProperty"
                    + File.separator + userFolder
                    + File.separator + orderNo
                    + File.separator + serviceName + "_" + uniqueID;

            File baseFolder = new File(baseFolderPath);
            if (!baseFolder.exists()) {
                baseFolder.mkdirs();
            }

            List<Map<String, String>> savedImages = new ArrayList<>();

            // ✅ Track duplicate names in this request
            Map<String, Integer> fileNameCount = new HashMap<>();

            for (Map<String, Object> img : images) {

                String base64 = (String) img.get("base64");
                String fileName = (String) img.get("fileName");

                if (base64 == null || fileName == null || base64.isBlank()) {
                    log.warn("Skipping invalid image entry");
                    continue;
                }


                log.info("Incoming fileName: {}", fileName);

                // Validate extension
                String lower = fileName.toLowerCase();
                if (!(lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png"))) {
                    log.warn("Invalid file type: {}", fileName);
                    continue;
                }

                // Split name and extension
                int dotIndex = fileName.lastIndexOf(".");
                if (dotIndex == -1) {
                    log.warn("Invalid filename format: {}", fileName);
                    continue;
                }

                String nameWithoutExt = fileName.substring(0, dotIndex);
                String extension = fileName.substring(dotIndex);

                // ✅ Handle duplicates inside request
                int count = fileNameCount.getOrDefault(fileName, 0);

                String finalName = (count == 0)
                        ? fileName
                        : nameWithoutExt + "_" + count + extension;

                fileNameCount.put(fileName, count + 1);

                // Clean base64
                String cleaned = base64.contains(",")
                        ? base64.substring(base64.indexOf(",") + 1)
                        : base64;


                byte[] imageBytes;
                try {
                    imageBytes = Base64.getDecoder().decode(cleaned);
                    // Scan + compress
                    imageBytes = imageSecurityService.scanAndCompress(imageBytes, extension);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid Base64 for {}", fileName);
                    continue;
                }

                File outputFile = new File(baseFolder, finalName);

                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    fos.write(imageBytes);
                    log.info("Saved file: {}", finalName);
                }

                savedImages.add(Map.of(
                        "fileName", finalName,
                        "path", outputFile.getAbsolutePath()
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Images uploaded successfully",
                    "images", savedImages
            ));

        } catch (Exception e) {
            log.error("uploadServicePropertyImages error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    public SaveOrderResponse Save_Order(SellerOrderRequest sellerOrderRequest) {

        try {
            List<ObjectId> ProductinventoryIds = new ArrayList<>();

            List<InventoryDTO> Serviceinventory = new ArrayList<>();
            List<InventoryDTO> Productinventory = new ArrayList<>();


            for (InventoryDTO i : sellerOrderRequest.getInventory()) {
                log.info("productname: " + i.productname);
                log.info("that product stock : " + i.sellerstock);
                log.info("that product stock : " + i.procurement_price);
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

            List<Inventory> Productinventories = inventoryRepo.findByIdInAndCompanyId(String.valueOf(sellerOrderRequest.companyid), ProductinventoryIds);
            log.info("ProductInventories from Repo: " + Productinventories.size());

            List<InventoryDTO> checkedItems = new ArrayList<>();
            List<InventoryDTO> validItems = new ArrayList<>();

            if (!Productinventories.isEmpty()) {
                checkedItems = Precheck(Productinventories, Productinventory, false);
                log.info("CheckedItems: " + checkedItems.size());

                validItems = checkedItems.stream()
                        .filter(item -> item.getMessage() == null || item.getMessage().isEmpty())
                        .collect(Collectors.toList());

                // Only return error if we had products and some of them failed
                if (validItems.size() != Productinventory.size()) {
                    return SellerAdaptor.ToSaveResponse(null, checkedItems, null, null, null, null, null);
                }
            }

            // Add services to validItems regardless
            validItems.addAll(Serviceinventory);
            log.info("validItems: " + validItems.size());

            List<Inventory> finalItems = SellerAdaptor.FromInventoryDTOtoInventory(validItems);
            log.info("FinalItems: " + finalItems.size());

            String refno = sellerOrderRequest.getRefno();
            log.info("can i get refno from UI:" + refno);
            log.info("can i get refno from Save_id:" + sellerOrderRequest.save_id);
            LocalDateTime now = LocalDateTime.now();

            List<Save> all = saveOrderRepo.findAppOrdersByCompanyid(sellerOrderRequest.companyid);

            all.sort(Comparator.comparing(Save::getOrderDate).reversed());

            if (all.size() > 0) log.info("Last refno : " + all.get(0).getRefno());

            Save latest = all.isEmpty() ? null : all.get(0);

            Save newSave = SellerAdaptor.TosaveModel(sellerOrderRequest, finalItems, now.toString(), refno, latest);

            if (sellerOrderRequest.customerInformationDTO != null) {
                String name = sellerOrderRequest.customerInformationDTO.customerName;
                String mobileNum = sellerOrderRequest.customerInformationDTO.customerMobileNum;
                String Address = sellerOrderRequest.customerInformationDTO.deliveryAddress;
                String city = sellerOrderRequest.customerInformationDTO.city;
                String postalCode = sellerOrderRequest.customerInformationDTO.postalCode;
                String email = sellerOrderRequest.customerInformationDTO.email;
                String state = sellerOrderRequest.customerInformationDTO.state;
                CustomerInformation a = new CustomerInformation(name, mobileNum, Address, city, postalCode, state, email);
                newSave.setCustomerInformation(a);
            }
            if (sellerOrderRequest.delivery_details != null) {
                newSave.setDelivery_details(sellerOrderRequest.delivery_details);
            }
            if (sellerOrderRequest.delivery_mode != null) {
                newSave.setDelivery_mode(sellerOrderRequest.delivery_mode);
            }

            if (newSave.getRefno() != null && !Serviceinventory.isEmpty()) {

                ObjectMapper mapper = new ObjectMapper();

                for (InventoryDTO serviceItem : Serviceinventory) {

                    Map<String, String> properties = serviceItem.getPropertyAttributes();

                    if (properties == null || properties.isEmpty()) {
                        log.info("PropertyAttributes is empty or null");
                        continue;
                    }

                    List<Map<String, Object>> images = new ArrayList<>();

                    for (Map.Entry<String, String> entry : properties.entrySet()) {

                        String attributeName = entry.getKey();
                        String value = entry.getValue();

                        if (value == null || value.isBlank()) continue;

                        try {
                            String base64Value = null;
                            String fileName = attributeName + ".jpg";

                            log.info("File name:" + fileName);

                            // CASE 1: Raw base64 (direct)
                            if (value.startsWith("data:image")) {

                                base64Value = value;
                                log.info("value.startsWith(\"data:image\")" + base64Value);
                                // Only assign name if not already set
                                if (fileName == null || fileName.isBlank()) {

                                    if (value.startsWith("data:image/png"))
                                        fileName = attributeName + ".png";
                                    else
                                        fileName = attributeName + ".jpg";
                                }
                            }

                            // CASE 2: JSON string containing base64
                            else if (value.startsWith("{")) {

                                JsonNode node = mapper.readTree(value);

                                if (node.has("base64")) {
                                    base64Value = node.get("base64").asText();
                                }
                                log.info("value.startsWith(\"{\")" + base64Value);

//                                if (node.has("name")) {
//                                    fileName = node.get("name").asText(); // keep original name
//                                }
                            }

                            if (base64Value != null && base64Value.startsWith("data:image")) {

                                try {
                                    String base64Data = base64Value.substring(base64Value.indexOf(",") + 1);

                                    byte[] imageBytes = Base64.getDecoder().decode(base64Data);

                                    if (imageBytes.length > MAX_IMAGE_SIZE) {

                                        log.warn("Image size exceeds 2.5MB for attribute: {}", attributeName);

                                        serviceItem.setMessage("Image size must be less than 2.5MB for attribute: " + attributeName);

                                        return SellerAdaptor.ToSaveResponse(
                                                null,
                                                List.of(serviceItem),
                                                null,
                                                null,
                                                null,
                                                null,
                                                null
                                        );
                                    }

                                } catch (IllegalArgumentException e) {
                                    log.warn("Invalid base64 format for attribute: {}", attributeName);
                                    continue;
                                }

                                Map<String, Object> imageData = new HashMap<>();
                                imageData.put("base64", base64Value);
                                imageData.put("fileName", fileName);

                                images.add(imageData);
                            }
                            log.info("Final file name: " + fileName);
                        } catch (Exception ex) {
                            log.warn("Skipping invalid image for attribute: {}", attributeName);
                        }
                    }


                    if (!images.isEmpty()) {

                        Map<String, Object> payload = new HashMap<>();
                        payload.put("id", serviceItem.get_id());
                        payload.put("userfolder", sellerOrderRequest.userFolder);
                        payload.put("servicename", serviceItem.getServicename());
                        payload.put("images", images);
                        payload.put("uniqueID", serviceItem.
                                uniqueID.substring(serviceItem.uniqueID.indexOf("_") + 1, serviceItem.uniqueID.length()));
                        payload.put("deleteimages", new ArrayList<>()); // never null

                        ResponseEntity<?> uploadResponse =
                                uploadServicePropertyImages(payload, newSave.getRefno());

                        if (!uploadResponse.getStatusCode().is2xxSuccessful()) {
                            return SellerAdaptor.ToSaveResponse(
                                    null, null, null, null, null, null, null
                            );
                        }
                        if (uploadResponse.getStatusCode().is2xxSuccessful()) {

                            Map<String, Object> responseBody = (Map<String, Object>) uploadResponse.getBody();
                            List<Map<String, String>> savedImages =
                                    (List<Map<String, String>>) responseBody.get("images");
                            String servicename = serviceItem.getServicename()
                                    .trim()
                                    .replaceAll("[/()]", "")
                                    .replaceAll("\\s+", "-");

                            String folderPath = "uploads" + File.separator + "uploadsServiceProperty"
                                    + File.separator + sellerOrderRequest.userFolder
                                    + File.separator + newSave.getRefno()
                                    + File.separator + servicename + "_"
                                    + serviceItem.uniqueID.substring(serviceItem.uniqueID.indexOf("_") + 1);

                            // 🔥 Start from existing attributes
                            Map<String, String> updatedProperties =
                                    new HashMap<>(serviceItem.getPropertyAttributes());

                            for (Map<String, String> img : savedImages) {

                                String fileName = img.get("fileName");

                                // attributeName = filename without extension
                                String attributeName = fileName.contains(".")
                                        ? fileName.substring(0, fileName.lastIndexOf("."))
                                        : fileName;

                                String fullPath = folderPath + File.separator + fileName;

                                // 🔥 Update only image attributes
                                if (updatedProperties.containsKey(attributeName)) {
                                    updatedProperties.put(attributeName, fullPath);
                                }
                            }

                            serviceItem.setPropertyAttributes(updatedProperties);

                            for (Inventory item : newSave.getSeller_items()) {
                                if (item.getUniqueID().equals(serviceItem.uniqueID)) {
                                    item.setPropertyAttributes(updatedProperties);
                                    log.info("Updated propertyAttributes inside newSave for uniqueID: {}", item.getUniqueID());
                                    break;
                                }
                            }
                        }

                    }
                }


            }


            if (sellerOrderRequest.save_id != null && !sellerOrderRequest.save_id.trim().isEmpty()) {
                log.info("Inside already have save property");
                if (!ObjectId.isValid(sellerOrderRequest.save_id)) {
                    throw new IllegalArgumentException("Invalid save_id");
                }
                ObjectId saveId = new ObjectId(sellerOrderRequest.save_id);
                Save existing = mongoTemplate.findById(saveId, Save.class);
                if (existing == null) {
                    throw new RuntimeException("Order not found with id: " + saveId);
                }

                existing.setOrderDate(newSave.getOrderDate());
                existing.setOrderStatus(newSave.getOrderStatus());
                existing.setCompanyid(newSave.getCompanyid());
                existing.setUpdatedAt(newSave.getUpdatedAt());
                existing.setSeller_items(newSave.getSeller_items());

                mongoTemplate.save(existing);

                return SellerAdaptor.ToSaveResponse(
                        existing.getRefno(),
                        validItems,
                        existing.get_id(),
                        existing.getIsgst(),
                        existing.getIspriceInclusive(),
                        String.valueOf(existing.getOrderDate()),
                        existing
                );
            } else {
                log.info("Iteam insert/Save");
                mongoTemplate.insert(newSave);
            }

            return SellerAdaptor.ToSaveResponse(newSave.getRefno(), validItems, newSave.get_id(), newSave.getIsgst(), newSave.getIspriceInclusive(), String.valueOf(newSave.getOrderDate()), newSave);

        } catch (DuplicateKeyException e) {
            return SellerAdaptor.ToSaveResponse(null, null, null, null, null, null, null);
        } catch (Exception e) {
            e.printStackTrace(); // ← Add this line for debugging
            return SellerAdaptor.ToSaveResponse(null, null, null, null, null, null, null);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public SaveAndExecuteQuickInvoiceResponse ToSaveAndExecuteQuickInvoice(SaveAndExecuteQuickInvoiceRequest request) throws JsonProcessingException {

        SaveAndExecuteQuickInvoiceResponse response = new SaveAndExecuteQuickInvoiceResponse();

        SellerOrderRequest sellerOrderRequest = SellerAdaptor.toSellerOrderRequest(request);

        SaveOrderResponse saveResponse;

        List<ObjectId> ProductinventoryIds = new ArrayList<>();
        List<InventoryDTO> Serviceinventory = new ArrayList<>();
        List<InventoryDTO> Productinventory = new ArrayList<>();

        for (InventoryDTO i : sellerOrderRequest.getInventory()) {
            log.info("productname: " + i.productname);
            log.info("that product stock : " + i.sellerstock);
            String flag = i.flag != null ? i.flag : "";

            if ("isService".equals(flag)) {
                Serviceinventory.add(i);
            } else {
                if (i._id == null || i._id.trim().isEmpty()) {
                    throw new RuntimeException("Product item missing _id: " + i.productname);
                }
                Productinventory.add(i);
                ProductinventoryIds.add(new ObjectId(i._id));
            }
        }
        log.info("Productinventory size : " + Productinventory.size());
        log.info("Serviceinventory size : " + Serviceinventory.size());

        List<Inventory> Productinventories = inventoryRepo.findByIdInAndCompanyId(String.valueOf(request.companyid), ProductinventoryIds);
        log.debug("ProductInventories from Repo: {}", Productinventories.size());

        List<InventoryDTO> checkedItems = new ArrayList<>();
        List<InventoryDTO> validItems = new ArrayList<>();

        if (!Productinventory.isEmpty()) {
            if (Productinventories.isEmpty()) {
                // Requested products but none of them resolved against the DB at all
                throw new RuntimeException("Requested product(s) not found for this company");
            }

            checkedItems = Precheck(Productinventories, Productinventory, true);
            log.info("CheckedItems size: {}", checkedItems.size());

            log.info("CheckedItems messages: {}",
                    checkedItems.stream()
                            .map(InventoryDTO::getMessage)
                            .collect(Collectors.toList()));

            validItems = checkedItems.stream()
                    .filter(item -> item.getMessage() == null || item.getMessage().isEmpty())
                    .collect(Collectors.toList());
            log.info("validItems: " + validItems.size());

            // Stop here instead of silently proceeding with a partial item list
            if (validItems.size() != Productinventory.size()) {
                response.inventory = checkedItems;
                throw new RuntimeException("Inventory validation failed on save step");
            }
        }

        // Add services to validItems regardless
        validItems.addAll(Serviceinventory);

        List<Inventory> finalItems = SellerAdaptor.FromInventoryDTOtoInventory(validItems);
        log.info("FinalItems: " + finalItems.size());

        String refno = sellerOrderRequest.getRefno();
        log.info("can i get refno from UI:" + refno);
        LocalDateTime now = LocalDateTime.now();

        Query latestQuery = new Query(Criteria.where("companyid").is(sellerOrderRequest.getCompanyid()))
                .with(Sort.by(Sort.Direction.DESC, "orderDate"))
                .limit(1);
        Save latest = mongoTemplate.findOne(latestQuery, Save.class);
        if (latest != null) {
            log.info("last Order refno : " + latest.getRefno());
        } else {
            log.info("No previous order found for this company");
        }

        Save newSave = SellerAdaptor.TosaveModel(sellerOrderRequest, finalItems, now.toString(), refno, latest);

        log.info("Before insert refno NUm : " + newSave.getRefno());
        if (refno != null && !refno.trim().isEmpty()) {
            log.info("Inside already have save property");

            Query query = new Query(Criteria.where("refno").is(refno));
            Update update = new Update()
                    .set("order_date", newSave.getOrderDate())
                    .set("order_status", newSave.getOrderStatus())
                    .set("companyid", newSave.getCompanyid())
                    .set("updatedAt", newSave.getUpdatedAt())
                    .set("seller_items", newSave.getSeller_items());

            mongoTemplate.upsert(query, update, Save.class);

        } else {
            log.info("Iteam insert/Save");
            mongoTemplate.insert(newSave);
        }

        saveResponse = SellerAdaptor.ToSaveResponse(newSave.getRefno(), validItems, newSave.get_id(), newSave.getIsgst(), newSave.getIspriceInclusive(), String.valueOf(newSave.getOrderDate()), newSave);

        if (saveResponse == null || saveResponse.save_id == null) {
            throw new RuntimeException("Save failed. Execute will not proceed.");
        }
        log.info("After Save order refno : " + newSave.getRefno());

        request.refno = newSave.getRefno();
        Save save = newSave;

        ExecuteOrderRequest executeOrderRequest = SellerAdaptor.toExecuteOrderRequest(request);

        if (executeOrderRequest.companyid == null) {
            throw new RuntimeException("CompanyId is null");
        }

        List<InventoryDTO> inventoryDTOs =
                Optional.ofNullable(executeOrderRequest.inventory)
                        .orElseGet(ArrayList::new);

        // Fail fast on malformed items instead of silently dropping them from lookups
        for (InventoryDTO i : inventoryDTOs) {
            if (i._id == null) {
                throw new RuntimeException("Inventory item missing _id: " + i.productname);
            }
            if (i.productid == null) {
                throw new RuntimeException("Inventory item missing productid: " + i.productname);
            }
        }

        List<ObjectId> inventoryIds = inventoryDTOs.stream()
                .map(i -> new ObjectId(i._id))
                .toList();

        List<ObjectId> productIds = inventoryDTOs.stream()
                .map(i -> i.productid)
                .toList();

        List<Inventory> inventories =
                mongoTemplate.find(
                        Query.query(Criteria.where("_id").in(inventoryIds)),
                        Inventory.class);

        List<Batches> batches =
                mongoTemplate.find(
                        Query.query(
                                Criteria.where("companyid")
                                        .is(executeOrderRequest.companyid.toString())
                                        .and("productid").in(productIds)),
                        Batches.class);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        save.setDeliveryDate(LocalDateTime.now().format(formatter));
        save.setTotal_Amount(executeOrderRequest.paymentsDTO.total_amount);
        save.setTotal_Paid(executeOrderRequest.paymentsDTO.payment_amount);
        save.setTotal_Discount(executeOrderRequest.paymentsDTO.discount);
        save.setDelivered(true);

        if ("Draft".equals(save.getOrderStatus()) || "In-Progress".equals(save.getOrderStatus())) {

            Payments currentPayments =
                    SellerAdaptor.fromPaymentsDTOtoPayments(
                            executeOrderRequest.paymentsDTO);

            if (save.getPaymentDetails() == null) {
                // New payment
                currentPayments.setPaid_amount(
                        currentPayments.getCurrent_payment_amount());

                currentPayments.setPayment_mode(
                        executeOrderRequest.payment_mode);

                if (Math.abs(currentPayments.getOutstanding_amount()) < 0.001) {
                    currentPayments.setPayment_status("paid");
                    save.setPayment_status("Paid");
                    save.setOrderStatus("Completed");
                } else {
                    save.setOrderStatus("In-Progress");
                    save.setPayment_status("Partially paid");
                }

                PaymentDetails pd = new PaymentDetails();
                pd.setOrderId(save.get_id());
                pd.setPayments(List.of(currentPayments));

                mongoTemplate.save(pd);
                log.info("Outstanding amount after first payment: " + currentPayments.getOutstanding_amount());
                log.info("isEMI: " + currentPayments.isEMI());
                save.setPaymentDetails(currentPayments);
            }
        }

        List<InventoryDTO> Execute_checkedItems = Precheck(inventories, inventoryDTOs, true);
        Execute_checkedItems.forEach(dto ->
                log.info("  ProductId: {}, BatchId: {}, OrderQty: {}, Message: {}",
                        dto.getProductid(),
                        dto.batch_id,
                        dto.getOrder_quantity(),
                        dto.getMessage())
        );

        boolean hasError = Execute_checkedItems.stream().anyMatch(i -> i.getMessage() != null && !i.getMessage().isEmpty());

        if (hasError) {
            response.inventory = Execute_checkedItems;
            throw new RuntimeException("Inventory validation failed");
        }

        for (InventoryDTO dto : Execute_checkedItems) {

            ObjectId batchObjectId = new ObjectId(dto.batch_id);

            // 1) Try the single embedded batch (batchid) first — match by its own _id,
            //    not just presence, so we never touch the wrong batch.
            Query singleBatchQuery = Query.query(
                    Criteria.where("productid").is(dto.getProductid())
                            .and("companyid").is(dto.getCompanyid())
                            .and("batchid._id").is(batchObjectId)
                            .and("batchid.stock_availability").gte(dto.getOrder_quantity())
            );

            Update singleBatchUpdate = new Update()
                    .inc("batchid.stock_availability", -dto.getOrder_quantity());

            UpdateResult r = mongoTemplate.updateFirst(singleBatchQuery, singleBatchUpdate, Inventory.class);

            // 2) If nothing matched via batchid (either it's null, or this batch_id
            //    lives in variantBatches instead), try the variantBatches array.
            if (r.getModifiedCount() == 0) {
                Query variantBatchQuery = Query.query(
                        Criteria.where("productid").is(dto.getProductid())
                                .and("companyid").is(dto.getCompanyid())
                                .and("variantBatches").elemMatch(
                                        Criteria.where("_id").is(batchObjectId)
                                                .and("stock_availability").gte(dto.getOrder_quantity())
                                )
                );

                // positional operator updates only the matched array element
                Update variantBatchUpdate = new Update()
                        .inc("variantBatches.$.stock_availability", -dto.getOrder_quantity());

                r = mongoTemplate.updateFirst(variantBatchQuery, variantBatchUpdate, Inventory.class);
            }

            log.info("dto.getBatchid(): " + dto.getBatchid());

            // Separate "batches" collection stays as its own authoritative update,
            // matched by the batch's own _id (unrelated to the embedded copy above).
            Query batchQuery = Query.query(
                    Criteria.where("_id").is(batchObjectId)
                            .and("productid").is(dto.getProductid())
                            .and("companyid").is(dto.getCompanyid())
                            .and("stock_availability").gte(dto.getOrder_quantity())
            );

            Update batchUpdate = new Update()
                    .inc("stock_availability", -dto.getOrder_quantity());

            UpdateResult batchResult =
                    mongoTemplate.updateFirst(batchQuery, batchUpdate, Batches.class);

            if (r.getModifiedCount() == 0 || batchResult.getModifiedCount() == 0) {
                throw new RuntimeException(
                        dto.productname + " stock changed. Retry.");
            }
        }
        //CutomerInfo
        String name = executeOrderRequest.customerInformationDTO.customerName;
        String mobileNum = executeOrderRequest.customerInformationDTO.customerMobileNum;


        CustomerInformation a = new CustomerInformation();
        if (name != null) a.setCustomerName(name);
        if (mobileNum != null) a.setCustomerMobileNum(mobileNum);

//        save.setDelivery_details(add + "," + city + "," + state + "," + postalCode);
        save.setCustomerInformation(a);
        if (mobileNum != null && !mobileNum.equals("")) {
            Buyer buyer = markRewardsToBuyer(name, mobileNum, executeOrderRequest.rewards, save.getTotal_Paid(),
                    executeOrderRequest.companyid, executeOrderRequest.companyName
                    , save.getOrderStatus());
            save.setBuyerid(buyer);
        }
        save.setRewards(new Save.Rewards());
        save.setRewards(new Save.Rewards(executeOrderRequest.rewards.RewardsEarned,
                executeOrderRequest.rewards.rewardsRedeemed,
                save.getOrderStatus().equals("Completed") ? true : false));
        save.setPayment_mode(executeOrderRequest.payment_mode);
        save.setDelivery_details(executeOrderRequest.address);
        mongoTemplate.save(save);

        response.refno = saveResponse.refno;
        response.save_id = saveResponse.save_id;
        response.inventory = saveResponse.getInventory();
        response.isgst = saveResponse.isgst;
        response.ispriceInclusive = saveResponse.ispriceInclusive;
        response.order_date = saveResponse.order_date;

        return response;
    }

    @Transactional
    public SaveOrderResponse Execute(ExecuteOrderRequest executeOrderRequest) throws JsonProcessingException {
        System.out.println("Requested InventoryDTO: " + new ObjectMapper().writeValueAsString(executeOrderRequest.inventory));
        System.out.println("Requested paymentDTO: " + new ObjectMapper().writeValueAsString(executeOrderRequest.paymentsDTO));
        log.info("IsDelivered: " + executeOrderRequest.isDelivered);

        log.info("Execute Service");
        log.info("Draft active status: " + executeOrderRequest.draft_active);

        ClientSession session = null;

        try {
            session = mongoClient.startSession();
            session.startTransaction();

            Save save = null;

            if (StringUtils.hasText(executeOrderRequest.save_id)) {

                try {
                    ObjectId saveId = new ObjectId(executeOrderRequest.save_id);

                    save = mongoTemplate
                            .withSession(session)
                            .findOne(
                                    Query.query(Criteria.where("_id").is(saveId)),
                                    Save.class
                            );

                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Invalid save_id format");
                }
            }


            if (save == null) {
                log.error("No Save found for refno: " + executeOrderRequest.refno);
                session.abortTransaction();
                throw new RuntimeException("Save not found for refno " + executeOrderRequest.refno);
            }

            log.debug("Saved Inventory: {}",
                    new ObjectMapper().writeValueAsString(save.getSeller_items()));


            if (save == null) {
                log.info("No save found for refno: " + executeOrderRequest.refno);
                session.abortTransaction();
                return SellerAdaptor.ToSaveResponse(null, null, null, null, null, null, null);
            }

//            if (!executeOrderRequest.draft_active) {
//                save.setDraft_active(false);
//                mongoTemplate.withSession(session).save(save);
//                  session.commitTransaction();
//                return SellerAdaptor.ToSaveResponse(null, null, save.get_id(),null,null,null,null);
//            }

            List<InventoryDTO> inventoryDTOs = Optional.ofNullable(executeOrderRequest.inventory)
                    .orElseGet(ArrayList::new);

            List<ObjectId> inventoryIds = inventoryDTOs.stream()
                    .filter(i -> i._id != null)
                    .map(i -> new ObjectId(i._id))
                    .collect(Collectors.toList());

            List<ObjectId> productIds = inventoryDTOs.stream()
                    .filter(i -> i.productid != null)
                    .map(i -> i.productid)
                    .collect(Collectors.toList());

            List<Inventory> inventories = mongoTemplate.withSession(session)
                    .find(Query.query(Criteria.where("_id").in(inventoryIds)), Inventory.class);

            System.out.println("inventory size: " + inventories.size());
            if (executeOrderRequest.companyid == null) {
                log.info("Error: companyid is null in ExecuteOrderRequest");
                session.abortTransaction();
                return SellerAdaptor.ToSaveResponse(null, null, null, null, null, null, null);
            }


            List<Batches> batches = mongoTemplate.withSession(session)
                    .find(Query.query(Criteria.where("companyid").is(executeOrderRequest.companyid.toString())
                            .and("productid").in(productIds)), Batches.class);

            log.info("Batches size : " + batches.size());

            // ✅ Null-safe filtering (critical fix)
            List<InventoryDTO> productInventory = inventoryDTOs.stream()
                    .filter(i -> i.flag == null || !"isService".equals(i.flag))
                    .collect(Collectors.toList());
            List<InventoryDTO> serviceInventory = inventoryDTOs.stream()
                    .filter(i -> i.flag == "isService" || "isService".equals(i.flag))
                    .collect(Collectors.toList());


            log.info("Productinventory size : " + productInventory.size());
            log.info("Serviceinventory size : " + serviceInventory.size());


            save.getSeller_items().removeIf(item -> {
                if (item.getFlag() == null) {
                    return productInventory.stream().noneMatch(i ->
                            i.flag == null && i.get_id() != null && i.get_id().equals(item.get_id())
                    );
                }
                return false;
            });

            save.getSeller_items().removeIf(item -> {
                if ("isService".equals(item.getFlag())) {
                    return serviceInventory.stream().noneMatch(i ->
                            "isService".equals(i.flag) &&
                                    Objects.equals(i.get_id(), item.get_id()) &&
                                    isSame(i.getPropertyAttributes(), item.getPropertyAttributes())
                    );
                }
                return false;
            });

            // ordered Quantity update
            for (Inventory item : save.getSeller_items()) {
                if (item.getFlag() == null && item.getUniqueID() != null) {
                    productInventory.stream()
                            .filter(i ->
                                    i.flag == null &&
                                            i.uniqueID != null &&
                                            i.uniqueID.equals(item.getUniqueID())
                            )
                            .findFirst()
                            .ifPresent(matchingInventory -> {
                                Integer newQty = matchingInventory.getOrder_quantity();
                                Integer currentQty = item.getOrder_quantity();
                                log.info("currentQty : " + currentQty);
                                log.info("newQty : " + newQty);
                                if (!Objects.equals(currentQty, newQty)) {
                                    item.setOrder_quantity(newQty);
                                }
                            });
                } else if (item.getFlag() != null && item.getFlag().equals("isService") && item.getUniqueID() != null) {

                    serviceInventory.stream()
                            .filter(i ->
                                    i.flag.equals("isService") &&
                                            i.uniqueID != null &&
                                            i.uniqueID.equals(item.getUniqueID())
                            )
                            .findFirst()
                            .ifPresent(matchingInventory -> {

                                Integer newQty = matchingInventory.getOrder_quantity();
                                Integer currentQty = item.getOrder_quantity();
                                log.info("currentQty : " + currentQty);
                                log.info("newQty : " + newQty);

                                if (!Objects.equals(currentQty, newQty)) {
                                    item.setOrder_quantity(newQty);
                                }
                            });
                }

            }


            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            if (!save.isDelivered() && executeOrderRequest.isDelivered) {
                save.setDeliveryDate(LocalDateTime.now().format(formatter));
            }
            if (save.getTotal_Amount() == 0) {
                save.setTotal_Amount(executeOrderRequest.paymentsDTO.total_amount);
            }
            if (save.getOrderStatus().equals("Draft") || save.getOrderStatus().equals("In-Progress") || save.getOrderStatus().equals("Ready for delivery")) {
                Payments Current_payments = SellerAdaptor.fromPaymentsDTOtoPayments(executeOrderRequest.paymentsDTO);
                System.out.println("Adaptor  payment: " + new ObjectMapper().writeValueAsString(Current_payments));
                log.info("Current_payments : " + Current_payments);

                //eea755148a615f8a88
                if (save.getPaymentDetails() == null) {
                    // new payment
                    if (Current_payments.isAdvance()) {
                        double nextDue = Current_payments.getTotal_amount() - Current_payments.getDiscount();
                        nextDue -= Current_payments.getAdvanceAmt();

                        Current_payments.setAmountDue(nextDue);

                        save.setTotal_Advance(Current_payments.getAdvanceAmt());
                        save.setTotal_AmountDue(nextDue);
                        if (nextDue == 0 && executeOrderRequest.isDelivered) {
                            Current_payments.setPayment_status("paid");
                            Current_payments.setNext_payment_due(null);
                            save.setPayment_status("Paid");
                            save.setOrderStatus("Completed");
                        } else if (nextDue == 0 && !executeOrderRequest.isDelivered) {
                            Current_payments.setPayment_status("paid");
                            Current_payments.setNext_payment_due(null);
                            save.setPayment_status("Paid");
                            save.setOrderStatus("In-Progress");
                        } else if (Current_payments.getAdvanceAmt() == 0) {
                            Current_payments.setPayment_status("Zero Advance Paid");
                            save.setPayment_status("UnPaid");
                            save.setOrderStatus("In-Progress");
                        } else {
                            Current_payments.setPayment_status("Advance paid");
                            save.setPayment_status("Partially paid");
                            save.setOrderStatus("In-Progress");
                        }
                    } else {
                        double outstand = (Current_payments.getTotal_amount() - Current_payments.getDiscount()) - Current_payments.getCurrent_payment_amount();
                        if (Current_payments.isEMI()) {
                            Current_payments.setOutstanding_amount(outstand);
                            Current_payments.setOngoing_installment(1);
                        }
                        Current_payments.setPaid_amount(Current_payments.getCurrent_payment_amount());
                        Current_payments.setOutstanding_amount(outstand);

                        if (Current_payments.getOutstanding_amount() == 0 && executeOrderRequest.isDelivered) {
                            Current_payments.setPayment_status("paid");
                            Current_payments.setNext_payment_due(null);
                            save.setPayment_status("Paid");
                            save.setOrderStatus("Completed");
                        } else if (Current_payments.getOutstanding_amount() == 0 && !executeOrderRequest.isDelivered) {
                            Current_payments.setPayment_status("paid");
                            save.setPayment_status("Partially paid");
                            save.setOrderStatus("In-Progress");

                        } else if (Current_payments.isEMI()) {
                            Current_payments.setPayment_status("Partially paid / EMI");
                            save.setPayment_status("Partially paid");
                            save.setOrderStatus("In-Progress");
                            Current_payments.setOngoing_installment(Current_payments.getOngoing_installment() + 1);
                        }
                    }

                    Current_payments.setPayment_mode(executeOrderRequest.payment_mode);


                    save.setTotal_Discount(Current_payments.getDiscount());
                    save.setTotal_Paid(Current_payments.getCurrent_payment_amount());

                    PaymentDetails paymentDetails1 = new PaymentDetails();
                    paymentDetails1.setOrderId(save.get_id());
                    paymentDetails1.setPayments(new ArrayList<>(Collections.singletonList(Current_payments)));

                    mongoTemplate.withSession(session).save(paymentDetails1);

                    log.info("Current_payments.setOutstanding_amount= " + Current_payments.getOutstanding_amount());
                    log.info("Current_payments.isEMI= " + Current_payments.isEMI());

                    save.setPaymentDetails(Current_payments);

                } else {
                    Payments Existing_payments = save.getPaymentDetails();
                    Existing_payments.setAdvance(Current_payments.isAdvance());
                    Existing_payments.setEMI(Current_payments.isEMI());
                    Existing_payments.setAdvance(Current_payments.isAdvance());
                    log.info("Current payment mode :" + Current_payments.getPayment_mode());
                    Existing_payments.setPayment_mode(Current_payments.getPayment_mode());
                    log.info("After set payment mode :" + Existing_payments.getPayment_mode());
                    if (save.getTotal_AmountDue() == 0 && Current_payments.getCurrent_payment_amount() == 0) {
                        save.setDelivered(executeOrderRequest.isDelivered);
                        save.setDeliveryDueDate(executeOrderRequest.deliveryDueDate);
                        boolean isValid = Current_payments.getCurrent_payment_amount() == 0 && Current_payments.getTotal_amount() == 0;
                        if ((Existing_payments.getAmountDue() == 0 || isValid) && executeOrderRequest.isDelivered) {
                            save.setPayment_status("Paid");
                            save.setOrderStatus("Completed");
                        } else if (Existing_payments.getAmountDue() == 0 && !executeOrderRequest.isDelivered) {
                            save.setPayment_status("Paid");
                            save.setOrderStatus("In-Progress");
                        }


                        save.setPayment_mode(executeOrderRequest.payment_mode);
                        save.setUpdatedAt(LocalDateTime.now().format(formatter));

                        mongoTemplate.withSession(session).save(save);
                        log.info("Save updated");

                        session.commitTransaction();
                        return SellerAdaptor.ToSaveResponse(save.getRefno(), new ArrayList<InventoryDTO>(), save.get_id(), save.getIsgst(), save.getIspriceInclusive(), String.valueOf(save.getOrderDate()), save);
                    }

                    if (Current_payments.isAdvance()) {

                        double Total_current_Advance = Existing_payments.getAdvanceAmt() + Current_payments.getAdvanceAmt();
                        log.info("Total_current_Advance : " + Total_current_Advance);
                        log.info("AmountDue: " + (Existing_payments.getTotal_amount() - Total_current_Advance));

                        double balance_Due = (Existing_payments.getTotal_amount() - Total_current_Advance) - (save.getTotal_Discount() + Current_payments.getDiscount());

                        save.setTotal_Advance(Total_current_Advance);
                        save.setTotal_Paid(save.getTotal_Paid() + Current_payments.getCurrent_payment_amount());
                        save.setTotal_AmountDue(balance_Due);
                        save.setTotal_Discount(save.getTotal_Discount() + Current_payments.getDiscount());

                        Existing_payments.setAdvanceAmt(Total_current_Advance);
                        Existing_payments.setAmountDue(balance_Due);
                        Existing_payments.setDiscount(Current_payments.getDiscount());
                        Existing_payments.setPayment_date(LocalDateTime.now().toString());
                        Existing_payments.setCurrent_payment_amount(Current_payments.getCurrent_payment_amount());

                        if (Existing_payments.getAmountDue() == 0 && executeOrderRequest.isDelivered) {
                            Existing_payments.setPayment_status("paid");
                            save.setPayment_status("Paid");
                            save.setOrderStatus("Completed");
                        } else if (Existing_payments.getAmountDue() == 0 && !executeOrderRequest.isDelivered) {
                            Existing_payments.setPayment_status("paid");
                            save.setPayment_status("Paid");

                            save.setOrderStatus("In-Progress");
                        } else {
                            Existing_payments.setPayment_status("Partially paid");
                            save.setOrderStatus("In-Progress");
                        }
                    } else {

                        // In this case currentpayment is exclude with discount
                        double paid = Existing_payments.getPaid_amount() + Current_payments.getCurrent_payment_amount();
                        if (Existing_payments.isEMI()) {
                            Existing_payments.setPaid_amount(paid);
                        }

                        save.setTotal_Paid(save.getTotal_Paid() + Current_payments.getCurrent_payment_amount());

                        double balance_Due = save.getTotal_AmountDue() - (Current_payments.getCurrent_payment_amount() + Current_payments.getDiscount());
                        save.setTotal_Discount(save.getTotal_Discount() + Current_payments.getDiscount());
                        save.setTotal_AmountDue(Math.max(balance_Due, 0));
                        Existing_payments.setDiscount(Current_payments.getDiscount());
                        if (Existing_payments.getAmountDue() > 0) {
                            log.info("Yes Existing_payments.getAmountDue()>0");
                            double a = Existing_payments.getAmountDue() - (Current_payments.getCurrent_payment_amount() + Current_payments.getDiscount());
                            Existing_payments.setAmountDue(Math.max(a, 0));
                        }

                        double remaining_amtToPay = Existing_payments.getTotal_amount() - paid;
                        if (Existing_payments.isEMI()) {
                            Existing_payments.setOutstanding_amount(remaining_amtToPay);
                        }
                        Existing_payments.setPayment_date(LocalDateTime.now().toString());

                        Existing_payments.setPayment_mode(executeOrderRequest.payment_mode);
                        Existing_payments.setCurrent_payment_amount(Current_payments.getCurrent_payment_amount());
                        if ((balance_Due == 0 || Existing_payments.getOutstanding_amount() == 0) && executeOrderRequest.isDelivered) {
                            Existing_payments.setPayment_status("paid");
                            save.setPayment_status("Paid");
                            save.setOrderStatus("Completed");
                            Existing_payments.setNo_of_installments(Existing_payments.getNo_of_installments() + 1);
                        } else if ((balance_Due == 0 || Existing_payments.getOutstanding_amount() == 0) && !executeOrderRequest.isDelivered) {
                            Existing_payments.setPayment_status("paid");
                            save.setPayment_status("Paid");

                            save.setOrderStatus(save.getOrderStatus().equals("Ready for delivery") ? "Ready for delivery" : "In-Progress");

                            Existing_payments.setNext_payment_due(Current_payments.getNext_payment_due());
                            Existing_payments.setOngoing_installment(Existing_payments.getOngoing_installment() + 1);
                        } else {
                            Existing_payments.setPayment_status("Partially paid");
                            save.setOrderStatus(save.getOrderStatus().equals("Ready for delivery") ? "Ready for delivery" : "In-Progress");

                            Existing_payments.setNext_payment_due(Current_payments.getNext_payment_due());
                            Existing_payments.setOngoing_installment(Existing_payments.getOngoing_installment() + 1);
                        }
                    }

                    if (Current_payments.getCurrent_payment_amount() != 0) {
                        PaymentDetails a = paymentDetailsRepo.findByOrderId(save.get_id());
                        List<Payments> existing_payments = a.getPayments();
                        existing_payments.add(Existing_payments);
                        mongoTemplate.withSession(session).save(a);
                        log.info("Payment updated");
                        save.setPaymentDetails(Existing_payments);
                    }

                    save.setPayment_mode(executeOrderRequest.payment_mode);
                    save.setDelivery_details(executeOrderRequest.address);

                    save.setDelivered(executeOrderRequest.isDelivered);
                    save.setDeliveryDueDate(executeOrderRequest.deliveryDueDate);

                    save.setUpdatedAt(LocalDateTime.now().format(formatter));

                    String name = executeOrderRequest.customerInformationDTO.customerName;
                    String mobileNum = executeOrderRequest.customerInformationDTO.customerMobileNum;
                    String add = executeOrderRequest.customerInformationDTO.deliveryAddress;
                    CustomerInformation a = customerInfoRepo.findByCustomerNameAndCustomerMobileNum(name, mobileNum);
                    if (a == null) {
                        CustomerInformation savingdata = customerInfoRepo.save(SellerAdaptor.FromCustomerInformationDTOtoCustomerInformation(executeOrderRequest.customerInformationDTO));
                        save.setCustomerInformation(savingdata);
                    }
                    save.setDelivery_mode(executeOrderRequest.delivery_mode);

                    mongoTemplate.withSession(session).save(save);
                    log.info("Save updated");

                    session.commitTransaction();
                    return SellerAdaptor.ToSaveResponse(save.getRefno(), new ArrayList<InventoryDTO>(), save.get_id(), save.getIsgst(), save.getIspriceInclusive(), String.valueOf(save.getOrderDate()), save);


                }
            }


            List<InventoryDTO> checkedItems = Precheck(inventories, productInventory, false);
            log.info("CheckedItems size : " + checkedItems.size());
            checkedItems.forEach(item -> {
                log.info(
                        "Product: {} | Message: {}",
                        item.productname,
                        item.getMessage()
                );
            });

            List<InventoryDTO> validItems = checkedItems.stream()
                    .filter(item -> item.getMessage() == null || item.getMessage().isEmpty())
                    .collect(Collectors.toList());

            log.info("Validitems size : " + validItems.size());

            if (validItems.size() != checkedItems.size()) {
                session.abortTransaction();
                return SellerAdaptor.ToSaveResponse(null, checkedItems, null, null, null, null, null);
            }


            for (InventoryDTO dto : validItems) {
                for (Inventory inv : inventories) {
                    if (dto.getProductid().equals(inv.getProductid()) && dto.getCompanyid().equals(inv.getCompanyid())) {
                        Batches matchedBatch = null;
                        if (inv.getBatchid() != null) {
                            if (inv.getBatchid().get_id().equals(dto.batch_id)) {
                                matchedBatch = inv.getBatchid();
                            }
                        }

                        if (matchedBatch == null && inv.getVariantBatches() != null && !inv.getVariantBatches().isEmpty()) {
                            matchedBatch = inv.getVariantBatches()
                                    .stream()
                                    .filter(batch ->
                                            batch.get_id() != null &&
                                                    batch.get_id().equals(dto.batch_id)

                                    )
                                    .findFirst()
                                    .orElse(null);
                        }

                        if (matchedBatch == null) continue;


                        int existingStock = matchedBatch.getStock_availability();

                        int remaining = existingStock - dto.getOrder_quantity();

                        log.info("Existing Stock: {}", existingStock);

                        log.info("Remaining Stock: {}", remaining);


                        if (remaining <= 0) {
                            log.info("Entering Soldout");
                            soldout(matchedBatch, inv, batches);
                            continue;
                        }


                        if (batches != null && !batches.isEmpty()) {
                            log.info("Updating Batch Qty");
                            log.info("Inventory batch id : {}", matchedBatch.get_id());
                            final Batches finalMatchedBatch = matchedBatch;

                            Batches batch = batches.stream()
                                    .filter(b ->

                                            Boolean.TRUE.equals(b.getActive()) &&

                                                    Objects.equals(
                                                            b.get_id(),
                                                            finalMatchedBatch.get_id()
                                                    ) &&

                                                    Objects.equals(
                                                            b.getCompanyid(),
                                                            finalMatchedBatch.getCompanyid()
                                                    ) &&

                                                    Objects.equals(
                                                            b.getProductid(),
                                                            inv.getProductid()
                                                    )

                                    )
                                    .findFirst()
                                    .orElse(null);

                            if (batch == null) continue;


                            log.info("Batch found: {}", batch.getProductname());

                            batch.setStock_availability(remaining);
                            mongoTemplate.withSession(session).save(batch);


                            if (inv.getBatchid() != null &&
                                    Objects.equals(
                                            inv.getBatchid().get_id(),
                                            batch.get_id()
                                    )) {

                                inv.setBatchid(batch);
                            }

                            // Variant inventory
                            if (inv.getVariantBatches() != null &&
                                    !inv.getVariantBatches().isEmpty()) {

                                List<Batches> updatedVariants =
                                        inv.getVariantBatches()
                                                .stream()
                                                .map(v -> {

                                                    if (Objects.equals(
                                                            v.get_id(),
                                                            batch.get_id()
                                                    )) {

                                                        return batch;
                                                    }

                                                    return v;
                                                })
                                                .collect(Collectors.toList());

                                inv.setVariantBatches(updatedVariants);
                            }

                            mongoTemplate.withSession(session).save(inv);
                        }
                    }
                }
            }

            save.setRewards(new Save.Rewards());

            save.setDelivered(executeOrderRequest.isDelivered);
            save.setDeliveryDueDate(executeOrderRequest.deliveryDueDate);
            save.setRewards(new Save.Rewards(executeOrderRequest.rewards.RewardsEarned,
                    executeOrderRequest.rewards.rewardsRedeemed,
                    save.getOrderStatus().equals("Completed") ? true : false));

            //CutomerInfo
            String name = executeOrderRequest.customerInformationDTO.customerName;
            String mobileNum = executeOrderRequest.customerInformationDTO.customerMobileNum;
            String add = executeOrderRequest.customerInformationDTO.deliveryAddress;
            String email = executeOrderRequest.customerInformationDTO.email;
            String state = executeOrderRequest.customerInformationDTO.state;
            String city = executeOrderRequest.customerInformationDTO.city;
            String postalCode = executeOrderRequest.customerInformationDTO.postalCode;


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

            save.setDelivery_details(add + "," + city + "," + state + "," + postalCode);
            save.setCustomerInformation(a);
            if (executeOrderRequest.delivery_mode != null) {
                if (executeOrderRequest.delivery_mode.equals("Home Delivery")) {
                    save.setDelivery_mode("DELIVERY");
                } else {
                    save.setDelivery_mode("PICKUP");
                }
            }
//            CustomerInformation a = customerInfoRepo.findByCustomerNameAndCustomerMobileNum(name, mobileNum);
//            if (a == null) {
//                CustomerInformation savingdata = customerInfoRepo.save(SellerAdaptor.FromCustomerInformationDTOtoCustomerInformation(executeOrderRequest.customerInformationDTO));
//                save.setCustomerInformation(savingdata);
//            }
//            else {
////                log.info("Customer found {} : "+a);
//                if (executeOrderRequest.customerInformationDTO.deliveryAddress != null) {
//                    a.setDeliveryAddress(executeOrderRequest.customerInformationDTO.deliveryAddress);
//                }
//                save.setCustomerInformation(a);
//            }

            if (mobileNum != null && !mobileNum.equals("")) {
                Buyer buyer = markRewardsToBuyer(name, mobileNum, executeOrderRequest.rewards, save.getTotal_Paid(),
                        executeOrderRequest.companyid, executeOrderRequest.companyName
                        , save.getOrderStatus());
                save.setBuyerid(buyer);
            }


            // Save order details
            save.setUpdatedAt(LocalDateTime.now().format(formatter));
            save.setPayment_mode(executeOrderRequest.payment_mode);
            save.setDelivery_details(executeOrderRequest.address);
            save.setDelivery_mode(executeOrderRequest.delivery_mode);
            mongoTemplate.withSession(session).save(save);

            log.info("Execute completed successfully");

            session.commitTransaction();
            return SellerAdaptor.ToSaveResponse(save.getRefno(), checkedItems, save.get_id(), save.getIsgst(), save.getIspriceInclusive(), String.valueOf(save.getOrderDate()), save);

        } catch (DuplicateKeyException e) {
            log.error("error : " + e);
            abortSession(session, "DuplicateKeyException");
            return SellerAdaptor.ToSaveResponse(null, null, null, null, null, null, null);
        } catch (Exception e) {
            log.error("Execute error: " + e);
            session.abortTransaction(); // rollback if using outer session
            e.printStackTrace();
            return SellerAdaptor.ToSaveResponse(null, null, null, null, null, null, null);
        } finally {
            session.close(); // close only if Execute created session
        }
    }

    public Buyer markRewardsToBuyer(
            String name,
            String mobile,
            ExecuteOrderRequest.Rewards rewards,
            double totalPaid,
            ObjectId companyid,
            String companyname,
            String status) {

        if (mobile == null || mobile.trim().isEmpty()) {
            return null;
        }

        if (companyid == null) {
            return null;
        }

        Optional<Seller> sellerOptional = sellerRepo.findById(String.valueOf(companyid));

        if (sellerOptional.isEmpty()) {
            return null;
        }

        Seller seller = sellerOptional.get();

        Buyer buyer = buyerRepo.findByMobileNumAndBuyerSiteid(mobile, companyid);

        boolean isCompleted = "Completed".equalsIgnoreCase(status);

        /*
         * Calculate rewards only when order is completed
         */
        int earnedRewards = 0;

        if (isCompleted
                && seller.getRewards() != null
                && seller.getRewards().getRewardAmount() > 0
                && totalPaid > 0) {

            double calculatedRewards =
                    (totalPaid
                            / (double) seller.getRewards().getRewardAmount())
                            * seller.getRewards().getRewardPoints();

            earnedRewards = (int) calculatedRewards;
        }

        if (buyer != null) {

            if (buyer.getRewards() == null) {
                buyer.setRewards(
                        new Buyer.Rewards(
                                0,
                                0,
                                0,
                                0,
                                0
                        )
                );
            }

            if (isCompleted && earnedRewards > 0) {

                int existingCurrent =
                        buyer.getRewards().getCurrentRewards();

                int existingTotal =
                        buyer.getRewards().getTotalRewards();

                buyer.getRewards().setCurrentRewards(
                        existingCurrent + earnedRewards
                );

                buyer.getRewards().setTotalRewards(
                        existingTotal + earnedRewards
                );

                buyer.getRewards().setLastEarned(
                        earnedRewards
                );
            }

            if (!buyer.isUser()) {
                buyer.setFirstName(name);
            }

            buyer.setUpdateAT(new Date());

            return buyerRepo.save(buyer);
        }

        Buyer newBuyer = new Buyer(name, null, null, mobile, null, new Date(), new Date());
        newBuyer.setBuyerUseSites(
                new Buyer.BuyerUseSites(
                        companyid,
                        companyname
                )
        );

        newBuyer.setUser(false);
        newBuyer.setUpdateAT(new Date());
        if (isCompleted) {

            newBuyer.setRewards(
                    new Buyer.Rewards(
                            earnedRewards, // current rewards
                            earnedRewards, // total rewards
                            0,
                            earnedRewards, // last earned
                            0
                    )
            );
        } else {
            newBuyer.setRewards(
                    new Buyer.Rewards(
                            0,
                            0,
                            0,
                            0,
                            0
                    )
            );
        }

        return buyerRepo.insert(newBuyer);
    }

    public ViewOrderListResponse RepeatOrder(ViewOrderRequest viewOrderRequest) throws JsonProcessingException {

        ViewOrderListResponse ans = View(viewOrderRequest);
        ans.order_status = "Draft";
//        ObjectMapper mapper = new ObjectMapper();
//        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ans));

        if (ans != null) {

            if (ans.inventory.isEmpty()) return new ViewOrderListResponse();

            List<ObjectId> productIds = new ArrayList<>();

            for (InventoryDTO i : ans.inventory) {
                productIds.add(new ObjectId(i.productid.toHexString()));
            }
            for (ObjectId i : productIds) {
                log.info("Product IDs  : " + i);
            }
            log.info("Company id : " + ans.inventory.get(0).companyid);

            List<Inventory> inventories =
                    inventoryRepo.findByCompanyIdAndProductidAndVariantStockAvailability(
                            ans.inventory.get(0).companyid,
                            productIds
                    );

            log.info("Get Inventory from DB : " + inventories.size());

            Map<String, Inventory> inventoryMap = inventories.stream().collect(Collectors.toMap(Inventory::get_id, inv -> inv));

            List<InventoryDTO> finalInventories = new ArrayList<>();

            for (InventoryDTO i : ans.inventory) {

                if (inventoryMap.containsKey(i.get_id())) {

                    Inventory latestInventory = inventoryMap.get(i.get_id());

                    i.setSellerprice(latestInventory.getSellerprice());

                    finalInventories.add(i);

                } else if ("isService".equals(i.flag)) {
                    finalInventories.add(i);
                }
            }

            ans.inventory = finalInventories;

            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ans.inventory));

            return ans;
        }

        return new ViewOrderListResponse();
    }

    public ViewOrderListResponse View(ViewOrderRequest viewOrderRequest) throws JsonProcessingException {

        Optional<Save> optionalSave = saveOrderRepo.findById(String.valueOf(viewOrderRequest.id));

        if (optionalSave.isEmpty()) {
            return new ViewOrderListResponse();
        }

        Save save = optionalSave.get();
        System.out.println(save.getRefno());


        List<ObjectId> ids = new ArrayList<>();

        if (save != null) {
            for (Inventory i : save.getSeller_items()) {
                String batchId = i.getBatch_Id();
                if (batchId != null && ObjectId.isValid(batchId)) {
                    ids.add(new ObjectId(batchId));
                } else if (batchId != null) {
                    log.warn("Skipping invalid batch_Id '{}' for order {}", batchId, save.getRefno());
                }
            }
        }

        HashMap<String, Integer> map = new HashMap<>();
        if (ids.size() > 0) {
            List<Batches> ans = batchesRepo.findBatchesByIds(ids);
            log.info("DB inventory size : " + ans.size());
            for (Batches i : ans) {
                map.put(String.valueOf(i.getProductid()), i.getStock_availability());
            }
        }

//        for (Map.Entry<String, Integer> entry : map.entrySet()) {
//            log.info("ProductId: {}, Stock: {}", entry.getKey(), entry.getValue());
//        }

        ViewOrderListResponse res = SellerAdaptor.ToViewOrderResponse(save, map);
        ObjectMapper mapper = new ObjectMapper();
        String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(res);

        log.info("ViewOrderResponse:\n{}", prettyJson);
        return res;
    }

    public QuickInvoiceResponse ViewQuickInvoice(ViewQuickInvoiceRequest viewQuickInvoiceRequest) throws IOException {
        QuickInvoice lastInvoice = quickInvoiceRepo.findById(viewQuickInvoiceRequest.id).orElse(null);


        Optional<Seller> seller = sellerRepo.findById(String.valueOf(lastInvoice.getCompanyid()));
        if (!seller.isPresent()) {
            throw new RuntimeException("Seller not found for company ID: " + lastInvoice.getCompanyid());
        }


        return SellerAdaptor.ToQuickInvoiceResponse(lastInvoice, "invoice_template_4", seller.get());

    }

    public List<ViewOrderListResponse> ViewOrderList(ViewOrderListRequest viewOrderListRequest) {
        List<Save> save = saveOrderRepo.findByCompanyid(viewOrderListRequest.companyid);
        List<ViewOrderListResponse> a = new ArrayList<ViewOrderListResponse>();

        List<String> orderStatuses = viewOrderListRequest.order_status;

        ObjectMapper mapper = new ObjectMapper();

        for (Save i : save) {
//            if (!i.isDraft_active()) continue;

            try {
                log.debug("Full Save Object JSON: ", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(i));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (orderStatuses != null && orderStatuses.contains(i.getOrderStatus())) {
                a.add(SellerAdaptor.ToViewOrderResponse(i, new HashMap<>()));
            }


        }
        log.debug("", a);

        return a;
    }

    //    public List<ViewOrderListResponse> SearchOrders(SearchOrdersRequest searchOrdersRequest) {
//
//        String searchValue = searchOrdersRequest.value != null
//                ? searchOrdersRequest.value
//                : "";
//
//        List<Save> save = saveOrderRepo.findByCompanyAndNameOrMobile(
//                new ObjectId(searchOrdersRequest._id),
//                searchValue,
//                0
//        );
//
//        List<String> orderStatuses = searchOrdersRequest.order_status;
//        List<ViewOrderListResponse> responseList = new ArrayList<>();
//
//        if (save == null || save.isEmpty()) {
//            return new ArrayList<>();
//        }
//
//        for (Save i : save) {
//            if (orderStatuses == null || orderStatuses.isEmpty() || orderStatuses.contains(i.getOrderStatus())) {
//                responseList.add(SellerAdaptor.ToViewOrderResponse(i, new HashMap<>()));
//            }
//        }
//
//        return responseList;
//    }
    public List<ViewOrderListResponse> SearchOrders(SearchOrdersRequest request) {

        String searchValue = request.value != null ? request.value.trim() : "";

        List<String> orderStatuses = request.order_status;

        if (request._id == null || request._id.trim().isEmpty()) {
            return Collections.emptyList();
        }
        ObjectId companyId;
        try {
            companyId = new ObjectId(request._id.trim());
        } catch (IllegalArgumentException e) {
            return Collections.emptyList();
        }

        Criteria criteria = Criteria.where("companyid").is(companyId);

        if (!searchValue.isEmpty()) {
            String safeValue = Pattern.quote(searchValue);
            Criteria searchCriteria = new Criteria().orOperator(
                    Criteria.where("customerInformation.customerName").regex(safeValue, "i"),
                    Criteria.where("customerInformation.customerMobileNum").regex(safeValue, "i"),
                    Criteria.where("refno").regex(safeValue, "i")
            );
            criteria = new Criteria().andOperator(criteria, searchCriteria);
        }

        if (orderStatuses != null && !orderStatuses.isEmpty()) {
            criteria = new Criteria().andOperator(
                    criteria,
                    Criteria.where("order_status").in(orderStatuses)
            );
        }

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.sort(Sort.Direction.DESC, "orderDate")
        );

        List<Save> orders = mongoTemplate
                .aggregate(aggregation, "sellerorder", Save.class)
                .getMappedResults();

        if (orders.isEmpty()) {
            return Collections.emptyList();
        }

        return orders.stream()
                .map(order ->
                        SellerAdaptor.ToViewOrderResponse(order, new HashMap<>())
                )
                .collect(Collectors.toList());
    }

    public String DeleteOrders(SearchOrdersRequest searchOrdersRequest) {
        ObjectId objectId = new ObjectId(searchOrdersRequest._id);

        Optional<Save> save = saveOrderRepo.findById(String.valueOf(objectId));

        if (save.isPresent()) {
            saveOrderRepo.delete(save.get());
            return "Order Deleted";
        }

        return "Order Not Found";
    }

    public String UpdateOrderStatus(UpdateOrderStatusRequest updateOrderStatusRequest) {
        log.info("Delivery Due Date: " + updateOrderStatusRequest.deliveryDueDate);
        Optional<Save> save = saveOrderRepo.findById(updateOrderStatusRequest._id);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        if (save.isPresent()) {
            if (updateOrderStatusRequest.oldStatus != null && updateOrderStatusRequest.newStatus != null) {
                if (updateOrderStatusRequest.oldStatus.equals("In-Progress")) {
                    if (!updateOrderStatusRequest.isDelayclicked)
                        save.get().setOrderStatus(updateOrderStatusRequest.newStatus);
                    if (updateOrderStatusRequest.deliveryDueDate != null && updateOrderStatusRequest.deliveryDueDate != "") {
                        save.get().setDeliveryDueDate(updateOrderStatusRequest.deliveryDueDate);
                    }
                    save.get().setUpdatedAt(LocalDateTime.now().format(formatter));
                    saveOrderRepo.save(save.get());
                    return "Changed Order Due date";
//                    return "Changed Order Status";
                } else if (updateOrderStatusRequest.oldStatus.equals("Ready for delivery")) {
                    save.get().setOrderStatus(updateOrderStatusRequest.newStatus);
                    if (updateOrderStatusRequest.deliveryDueDate != null && updateOrderStatusRequest.deliveryDueDate != "") {
                        save.get().setDeliveryDueDate(updateOrderStatusRequest.deliveryDueDate);
                    }
                    save.get().setDeliveryDate(LocalDateTime.now().format(formatter));
                    save.get().setDelivered(true);
                    save.get().setUpdatedAt(LocalDateTime.now().format(formatter));
                    saveOrderRepo.save(save.get());
                    return "Changed Order Status";
                } else {
                    return "Order status not passed from UI";
                }
            }
            if (updateOrderStatusRequest.deliveryDueDate != null && updateOrderStatusRequest.deliveryDueDate != "") {
                save.get().setDeliveryDueDate(updateOrderStatusRequest.deliveryDueDate);
                saveOrderRepo.save(save.get());
                return "Changed Order Due date";
            }

            return "Nothing changed";
        }
        return null;
    }

    private boolean isSame(Map<String, String> a, Map<String, String> b) {
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private void abortSession(ClientSession session, String reason) {
        try {
            if (session != null && session.hasActiveTransaction()) {
                session.abortTransaction();
            }
        } catch (IllegalStateException e) {
            log.error("error : " + e);
            log.warn(reason + ": transaction already aborted or inactive", e);
        }
    }

    public InvoiceResponse GenerateInvoice(InvoiceRequest invoiceRequest) throws Exception {
        InvoiceResponse invoiceResponse = new InvoiceResponse();

        log.info("Invoice Id: " + invoiceRequest.id);
        log.info("Invoice Template from UI: " + invoiceRequest.template);
        Optional<Save> optionalSave = saveOrderRepo.findById(String.valueOf(invoiceRequest.id));
        log.info("isSavePresent :", optionalSave.isPresent());
        if (!optionalSave.isPresent()) {
            return new InvoiceResponse();
        }
        Optional<Seller> seller = sellerRepo.findById(String.valueOf(optionalSave.get().getCompanyid()));
        if (!seller.isPresent()) {
            return new InvoiceResponse();
        }
        Save save = optionalSave.get();
        System.out.println(save.getRefno());


        return SellerAdaptor.ToInvoiceResponse(save, invoiceRequest.template, seller.get(), null);
    }

    public InvoiceResponse InvoiceData(InvoiceRequest invoiceRequest) throws Exception {
        log.info("Invoice Id: " + invoiceRequest.id);
        log.info("Invoice Template from UI: " + invoiceRequest.template);
        Optional<Save> optionalSave = saveOrderRepo.findById(String.valueOf(invoiceRequest.id));
        log.info("isSavePresent :", optionalSave.isPresent());
        if (optionalSave.isEmpty()) {
            return new InvoiceResponse();
        }
        Optional<Seller> seller = sellerRepo.findById(String.valueOf(optionalSave.get().getCompanyid()));
        if (!seller.isPresent()) {
            seller = Optional.of(new Seller());
        }
        Save save = optionalSave.get();
        System.out.println(save.getRefno());

        return SellerAdaptor.ToInvoiceDataResponse(save, invoiceRequest.template, seller.get(), null);
    }

    @Transactional(rollbackFor = Exception.class)
    public QuickInvoiceResponse processQuickInvoice(QuickInvoiceRequest request) throws IOException {

        // 1. Delete Products
        DeleteQuickInvoiceRequest deleteReq = new DeleteQuickInvoiceRequest();
        deleteReq.user_id = request.user_id;
        deleteReq.quickProductsDeleteDTO = request.quickProductsDeleteDTO == null ? null : request.quickProductsDeleteDTO;
        String deleteResponse = "";
//        log.info("Delete invoice from DTO Size : "+request.quickProductsDeleteDTO.size());
//        log.info("Delete invoice : "+deleteReq);
        if (deleteReq.quickProductsDeleteDTO != null && !deleteReq.quickProductsDeleteDTO.isEmpty())
            deleteResponse = DeleteQuickInvoiceProducts(deleteReq);

        // 2. Create or Get Customer
        QICustomerRequestDTO customerReq = new QICustomerRequestDTO();
        customerReq._id = request._id;
        customerReq.name = request.name;
        customerReq.mobile_num = request.mobile_num;
        customerReq.address = request.address;

        ObjectId customerId = QICustomer(customerReq);

        // 3. Create Invoice
        QuickInvoiceResponse response = QuickInvoice(request);

        // Final response data
        response.deletedproductsresponse = deleteResponse;
        response.QICustomerID = customerId;

        return response;
    }

    public QuickInvoiceResponse QuickInvoice(QuickInvoiceRequest quickInvoiceRequest) throws IOException {
        log.info("Entered in QuickInvoice service");

        QuickInvoice lastInvoice = quickInvoiceRepo.findTopByCompanyidOrderByOrderDateDesc(quickInvoiceRequest.companyid).orElse(null);
        log.info("Ordervalue: " + quickInvoiceRequest.ordervalue);

        QuickInvoice newInvoice = SellerAdaptor.ToQuickInvoiceModel(quickInvoiceRequest, lastInvoice);

        Optional<Seller> seller = sellerRepo.findById(String.valueOf(quickInvoiceRequest.companyid));
        if (!seller.isPresent()) {
            throw new RuntimeException("Seller not found for company ID: " + newInvoice.getCompanyid());
        }
        quickInvoiceRepo.save(newInvoice);

        return SellerAdaptor.ToQuickInvoiceResponse(newInvoice, quickInvoiceRequest.template, seller.get());
    }

    public String DeleteQuickInvoiceProducts(DeleteQuickInvoiceRequest deleteQuickInvoiceRequest) throws IOException {
        log.info("Enterer in DeleteQuickInvoiceProducts Service");
        if (deleteQuickInvoiceRequest.user_id == null) {
            log.info("Invalid ObjectId provided: " + deleteQuickInvoiceRequest.user_id);
            return "Invalid user_id";
        }
        List<QuickInvoice> invoices = quickInvoiceRepo.findByCompanyid(new ObjectId(deleteQuickInvoiceRequest.user_id));

        if (invoices == null || invoices.isEmpty()) {
            log.warn("Warning: No QuickInvoice found for id: " + deleteQuickInvoiceRequest.user_id);
            return "No invoices found";
        }

        invoices.sort((a, b) -> {
            try {
                return b.getOrderDate().compareTo(a.getOrderDate());
            } catch (Exception e) {
                return 0;
            }
        });

        List<QuickProducts> allProducts = new ArrayList<>();
        int count = 0;
        for (QuickInvoice invoice : invoices) {
            if (count == 15) break;
            if (invoice.getQuickProducts() != null) {
                allProducts.addAll(invoice.getQuickProducts());
            }
            count++;
        }

        if (allProducts.isEmpty()) {
            return "No products found";
        }

        List<QuickProducts> uniqueProducts = new ArrayList<>();
        Map<String, Integer> seenNames = new HashMap<>();
        int index = 0;
        for (QuickProducts product : allProducts) {
            log.info("value:" + product.product_priceperunit);
            if (product.isDelete == false && product.product_priceperunit != null) {
                String key = product.product_name + product.product_priceperunit;
                if (!seenNames.containsKey(key)) {
                    seenNames.put(key, index);
                    uniqueProducts.add(product);
                    index++;
                    if (uniqueProducts.size() == 10) break;
                }
            }
        }

        List<QuickProducts> modifiedProducts = new ArrayList<>();
        for (QuickProductsDeleteDTO item : deleteQuickInvoiceRequest.quickProductsDeleteDTO) {
            String key = item.product_name + item.product_priceperunit;
//
            if (seenNames.containsKey(key)) {
                QuickProducts productToModify = uniqueProducts.get(seenNames.get(key));
                productToModify.isDelete = true;
                modifiedProducts.add(productToModify);
            }
        }
        for (QuickProducts modifiedProduct : modifiedProducts) {
            for (QuickInvoice invoice : invoices) {
                if (invoice.getQuickProducts() != null) {
                    for (QuickProducts qp : invoice.getQuickProducts()) {

                        if (qp.isDelete == false) {
                            if (qp.product_name.equals(modifiedProduct.product_name) && qp.product_priceperunit != null
                                    && qp.product_priceperunit.equals(modifiedProduct.product_priceperunit)) {
                                qp.isDelete = (true);
                            }
                        }
                    }
                }
            }
        }
        quickInvoiceRepo.saveAll(invoices);
        log.info("Products marked as deleted: " + modifiedProducts.size());
        return "Deleted";
    }

    public List<QuickInvoiceResponse> FiveLatestInvoice(FiveLatestInvoiceRequest fiveLatestInvoiceRequest) throws IOException {
        List<QuickInvoice> ans = quickInvoiceRepo.findByCompanyid(new ObjectId(fiveLatestInvoiceRequest.companyid));
        ans.sort((a, b) -> {
            try {
                return b.getOrderDate().compareTo(a.getOrderDate());
            } catch (Exception e) {
                return 0;
            }
        });
        List<QuickInvoiceResponse> res = new ArrayList<QuickInvoiceResponse>();
        for (QuickInvoice i : ans) {
            res.add(SellerAdaptor.ToQuickInvoiceResponse(i, null, null));
            if (res.size() == 5) return res;
        }
        return res;

    }

    private LocalDate parseDate(String date) {
        try {
            // Case 1: Exact format yyyy-MM-dd
            return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {

            try {
                // Case 2: Contains time -> yyyy-MM-ddTHH:mm:ss
                return LocalDate.parse(date.substring(0, 10), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception ex) {
                log.error("Error: " + ex);
                return null;
            }
        }
    }

    public List<SaveOrderResponse> FiveLatestFRInvoice(FiveLatestFRInvoiceRequest req) throws IOException {
        List<SaveOrderResponse> ans = new ArrayList<SaveOrderResponse>();
        List<Save> data = new ArrayList<>();

        log.info("req from date: " + req.fromDate);
        log.info("req to date: " + req.toDate);

        // If date range provided -> query DB directly
        if (req.fromDate != null && req.toDate != null) {

            LocalDate fromLocal = LocalDate.parse(req.fromDate);
            LocalDate toLocal = LocalDate.parse(req.toDate);

            Date from = Date.from(
                    fromLocal.atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
            );

            Date to = Date.from(
                    toLocal.atTime(23, 59, 59, 999_000_000)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
            );

            log.info("From date: " + from + "-----" + "To date: " + to);

            data = saveOrderRepo.getProductsAndServicesBetweenDate(
                    new ObjectId(req.companyid),
                    from,
                    to,
                    req.limit
            );
            log.info("" + data.size());
        } else {
            data = saveOrderRepo.findTop5ByCompanyidOrderByOrderDateDesc(
                    new ObjectId(req.companyid), req.limit
            );
        }
        data = new ArrayList<>(data);

        for (Save i : data) {
            for (Inventory j : i.getSeller_items()) {
                if (j.getBatchid() != null) {
                    log.info(j.getBatchid().getProductname());
                } else {
                    log.info(j.getServicename());
                }
            }
        }

        switch (req.filterType) {
            case "TOP_SELLING": {
                SaveOrderResponse topSelling = getTopSellingFROrders(
                        new ObjectId(req.companyid),
                        req.fromDate,
                        req.toDate,
                        req.seller_type,
                        req.limit
                );
                return List.of(topSelling);   // return single element list
            }

            case "OUT_OF_STOCK":
                return getRecentOutOfStockItems(req.companyid, req.limit);


        }


        return data.stream()
                .map(order -> SellerAdaptor.ToSaveResponse(
                        order.getRefno(),
                        SellerAdaptor.FromInventorytoInventoryDTOAndPreferredSellerType(order.getSeller_items(), req.seller_type),
                        order.get_id(),
                        order.getIsgst(),
                        order.getIspriceInclusive(),
                        String.valueOf(order.getOrderDate()),
                        order
                ))
                .collect(Collectors.toList());


    }

    private SaveOrderResponse getTopSellingFROrders(ObjectId companyId, String fromDate, String toDate, String sellertype, int limit) {


        Date from = null, to = null;

        if (fromDate != null && toDate != null) {
            LocalDate fromLocal = LocalDate.parse(fromDate);
            LocalDate toLocal = LocalDate.parse(toDate);

            from = Date.from(
                    fromLocal.atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
            );

            to = Date.from(
                    toLocal.atTime(23, 59, 59, 999_000_000)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
            );
        } else {
            // Default: use very wide date range
            from = new Date(0); // epoch
            to = new Date();    // now
        }

        // **Call Aggregation Method**
        List<TopSellingProductDTO> topProducts = new ArrayList<>();
        List<TopSellingServiceDTO> topService = new ArrayList<>();
        topProducts = saveOrderRepo.getTopSellingProducts(companyId, from, to, limit);
        topService = saveOrderRepo.getTopSellingService(companyId, from, to, limit);
        SaveOrderResponse a = new SaveOrderResponse();
        if (sellertype.equals("product")) {
            a.Productinventory = topProducts;

        } else if (sellertype.equals("service")) {
            a.Serviceinventory = topService;

        } else {
            a.Productinventory = topProducts;
            a.Serviceinventory = topService;

        }

        log.info("TopProducts: " + topProducts.size());
        log.info("TopService: " + topService.size());

        return a;
    }

    private List<SaveOrderResponse> getRecentOutOfStockItems(String companyId, int limit) {

        List<Inventory> inventories = inventoryRepo.findBycompanyidAndOutOfStock(companyId, limit);
        log.info("getRecentOutOfStockItems for company: " + inventories.size());

        Map<String, Date> map = new HashMap<>();

        for (Inventory i : inventories) {
            if (i.getBatchid() == null) continue;
            if (i.getBatchid().getStock_availability() != 0) continue;
            log.info("Stored in DB : " + i.getBatchid().getUpdatedAt());

            String productName = i.getBatchid().getProductname();
            Date lastUpdateDate;
            String rawDate = i.getBatchid().getUpdatedAt();

            try {

                if (rawDate.contains(".")) {
                    String[] parts = rawDate.split("\\.");
                    log.info(parts[0]);
                    String nanos = parts[1];
                    log.info(nanos);

                    nanos = nanos.length() > 9
                            ? nanos.substring(0, 9)
                            : String.format("%-9s", nanos).replace(' ', '0');
                    log.info(nanos);

                    rawDate = parts[0] + "." + nanos + "Z";
                }
                log.info("" + Instant.parse(rawDate));

                lastUpdateDate = Date.from(Instant.parse(rawDate));

            } catch (Exception e) {

                log.error(" Error parsing date: " + rawDate + " --> " + e.getMessage());
                continue;
            }

            if (productName == null || lastUpdateDate == null) continue;

            if (!map.containsKey(productName) || map.get(productName).before(lastUpdateDate)) {
                map.put(productName, lastUpdateDate);
            }
        }

        return map.entrySet()
                .stream()
                .map(entry -> {
                    SaveOrderResponse res = new SaveOrderResponse();
                    res.setRefno(entry.getKey() + "|" + entry.getValue());
                    return res;
                })
                .collect(Collectors.toList());
    }

    public List<QuickProductsDTO> SecondTimeQI(SecondTimeQIRequest secondTimeQIRequest) {

        // Validate the ObjectId input
        if (secondTimeQIRequest.getId() == null) {
            log.warn("Invalid ObjectId provided: " + secondTimeQIRequest.getId());
            return new ArrayList<>();
        }


        List<QuickInvoice> ans = quickInvoiceRepo.findByCompanyid(new ObjectId(secondTimeQIRequest.getId()));

        if (ans == null || ans.isEmpty()) {
            log.info("Warning: No QuickInvoice found for id: " + secondTimeQIRequest.id);
            return new ArrayList<>();
        }

        ans.sort((a, b) -> {
            try {
                return b.getOrderDate().compareTo(a.getOrderDate());
            } catch (Exception e) {

                return 0; // fallback in case of bad format
            }
        });

        int ind = 0;

        List<QuickProducts> products = new ArrayList<>();
        for (QuickInvoice invoice : ans) {
            if (ind == secondTimeQIRequest.limit * 2) break;
            if (invoice.getQuickProducts() != null) {
                products.addAll(invoice.getQuickProducts());
            }
            ind++;
        }

        if (products.isEmpty()) return new ArrayList<>();

        List<QuickProducts> uniqueProducts = new ArrayList<>();
        Set<String> seenNames = new HashSet<>();

        for (QuickProducts product : products) {
            String key = product.product_name;
            if (!seenNames.contains(key)) {
                seenNames.add(key);
                uniqueProducts.add(product);
                if (uniqueProducts.size() == secondTimeQIRequest.limit) {
                    break;
                }
            }
        }

        return SellerAdaptor.FromQuickProductstoQuickProductsDTO(uniqueProducts);
    }

    public String ReadQuickInvoice(ReadQuickInvoiceRequest readQuickInvoiceRequest) throws Exception {
        Optional<QuickInvoice> optionalQuickInvoice = quickInvoiceRepo.findById(readQuickInvoiceRequest.id);
        Optional<Save> optionalInvoice = saveOrderRepo.findById(String.valueOf(readQuickInvoiceRequest.id));
        Optional<Seller> optionalSeller = null;
        if (optionalQuickInvoice.isPresent()) {
            optionalSeller = sellerRepo.findById(String.valueOf(optionalQuickInvoice.get().getCompanyid()));
        } else {
            optionalSeller = sellerRepo.findById(String.valueOf(optionalInvoice.get().getCompanyid()));
        }
        log.debug("Seller: ", optionalSeller);

        if (optionalQuickInvoice.isPresent()) {
            QuickInvoiceResponse a = SellerAdaptor.ToQuickInvoiceResponse(optionalQuickInvoice.get(), "invoice_template_4", optionalSeller.get());
            return a.invoiceHtml;
        } else if (optionalInvoice.isPresent()) {
            InvoiceResponse a = SellerAdaptor.ToInvoiceResponse(optionalInvoice.get(), readQuickInvoiceRequest.template, optionalSeller.get(), readQuickInvoiceRequest.userfolder);
            return a.invoiceHtml;
        } else {
            throw new FileNotFoundException("QuickInvoice with id " + readQuickInvoiceRequest.id + " not found.");
        }

    }

    public UpdateCompanyInfoResponse UpdateCompanyInfo(UpdateCompanyInfoRequest request) {

        UpdateCompanyInfoResponse response = new UpdateCompanyInfoResponse();

        Optional<User> userOpt = sellerUserRepo.findById(request.user_id);

        Optional<Seller> seller = sellerRepo.findById(request.companyId);

        if (!seller.isPresent()) {
            response.msg = "Seller not found";
            return response;
        }
        if (!userOpt.isPresent()) {
            response.msg = "User not found";
            return response;
        }

        if (request.isLangChange) {
            log.info("" + userOpt.isPresent());
            if (userOpt.isPresent()) {
                log.info(userOpt.get().getMobileNum());
                User user = userOpt.get();
                user.setLanguage_preferred(request.language_preferred);
                if (!request.firstname.isEmpty() || request.firstname != "") {
                    user.setFirstname(request.firstname);
                }
                if (!request.lastname.isEmpty() || request.lastname != "") {
                    user.setLastname(request.lastname);
                }
                sellerUserRepo.save(user);
                response.language_preferred = user.getLanguage_preferred();
            }
        }
        if (!request.userMobileNum.isEmpty() || request.userMobileNum != "") {
            userOpt.get().setMobileNum(request.userMobileNum);
        }

        Seller updatedSeller = SellerAdaptor.ToUpdateCompanyInfo(request, seller.get());
        sellerRepo.save(updatedSeller);

        response.msg = "Successfully changed";
        response.isgst = updatedSeller.isGst_enabled();
        response.isprice = updatedSeller.isPrice_inclusive_gst();

        return response;
    }

    public InitialGetUpdateCompanyInfoResponse InitialGetUpdateCompanyInfo(InitialGetUpdateCompanyInfoRequest initialGetUpdateCompanyInfoRequest) {

        Seller sellerOptional = sellerRepo.findByUseridContaining(initialGetUpdateCompanyInfoRequest.user_id);

        if (sellerOptional == null) {
            log.info("Seller not found for userId: " + initialGetUpdateCompanyInfoRequest.user_id);
            return new InitialGetUpdateCompanyInfoResponse();
        }


        Optional<User> userOpt = sellerUserRepo.findById(initialGetUpdateCompanyInfoRequest.user_id);

        if (userOpt.isEmpty()) {
            log.info("User not found for userid: " + sellerOptional.getUserid());
            return new InitialGetUpdateCompanyInfoResponse();
        }

        User user = userOpt.get();
        log.info("User found: " + user.getMobileNum());

        if (sellerOptional.getPickupPoints() == null && sellerOptional.getAddress() == null) {
            log.info("Pickup points and address both are null");
            return SellerAdaptor.ToinitialGetUpdateCompanyInfoResponse(null, sellerOptional, user);
        }

        List<PickupPointsDTO> pickupPointsDTOs = new ArrayList<>();
        if (sellerOptional.getPickupPoints() != null) {
            log.info("Pickup Points retrieve");
            List<PickupPoints> activePoints = sellerOptional.getPickupPoints().stream()
                    .filter(PickupPoints::isActive)
                    .collect(Collectors.toList());
            pickupPointsDTOs = SellerAdaptor.FromPickupPointstoPickupPointsDTO(activePoints);
        }

        if (sellerOptional.getAddress() == null) {
            log.info("Address is null");
        }
        if (sellerOptional.getProduct_type() != null) {
            log.info("product_type" + sellerOptional.getProduct_type());
        }

        return SellerAdaptor.ToinitialGetUpdateCompanyInfoResponse(pickupPointsDTOs, sellerOptional, userOpt.get());
    }

    public ResponseEntity<String> sendSubscriptionEmail(SubscriptionEmailDTO dto, String message, String mobileNum) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
//            String [] cc = {"yogeshgimmy07@gmail.com"};

            helper.setTo(dto.email);
            helper.setFrom("admin@intellesyde.com");
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
                                "<p>Your OTP for mobile number <b>" + mobileNum + "</b> is:</p>" +
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

    public String UserBehaviour(UserBehaviourRequestDTO userBehaviourRequestDTO) {
        try {
            UserBehaviour insert_UserBehaviour = SellerAdaptor.ToUserBehaviour(userBehaviourRequestDTO);
            userBehaviourRepo.insert(insert_UserBehaviour);
            return "Inserted Data Successfully";
        } catch (Exception e) {
            log.error("Error: " + e);
            e.printStackTrace();
            return "Error inserting data: " + e.getMessage();
        }
    }

    public List<InitialServiceInventoryServiceInventoryResponse> InitialServiceInventory(InitialServiceInventoryServiceInventoryRequest initialServiceInventoryserviceInventoryRequest) {
        List<ServiceInventory> serviceInventory = serviceInventoryRepo.findBySellerid(initialServiceInventoryserviceInventoryRequest.sellerid);
        log.info("serviceInventory size: " + serviceInventory.size());
        if (!serviceInventory.isEmpty() && serviceInventory.size() != 0) {
            return SellerAdaptor.ToInitialServiceInventoryServiceInventory(serviceInventory);
        }
        return new ArrayList<InitialServiceInventoryServiceInventoryResponse>();
    }

    public InitialServiceInventoryServiceInventoryResponse viewServiceInventoryDetails(InitialServiceInventoryServiceInventoryRequest request) {
        if (request == null || request._id == null) return new InitialServiceInventoryServiceInventoryResponse();

        Optional<ServiceInventory> serviceInventory = serviceInventoryRepo.findById(new ObjectId(String.valueOf(request._id)));

        return serviceInventory
                .map(SellerAdaptor::ToViewServiceInventoryServiceInventory)
                .orElseGet(InitialServiceInventoryServiceInventoryResponse::new);
    }

    public ServiceInventoryResponse ServiceInventory(ServiceInventoryRequest serviceInventoryRequest) {
        ServiceInventoryResponse a = new ServiceInventoryResponse();
        if (serviceInventoryRequest.msg != null && !serviceInventoryRequest.msg.equals("") && serviceInventoryRequest.msg.equals("edit")) {
            log.info("IN service: " + serviceInventoryRequest.isDelete);
            ServiceInventory get = serviceInventoryRepo.save(SellerAdaptor.ToServiceInventory(serviceInventoryRequest));
            a._id = get.id;
            a.msg = "Successfully added";
            return a;
        }
        log.info("Service Inventory in ");

        ServiceInventory serviceInventory = SellerAdaptor.ToServiceInventory(serviceInventoryRequest);
        ServiceInventory ans = serviceInventoryRepo.save(serviceInventory);
        a.msg = serviceInventory.id != null ? "Service is edited" : "Successfully added";
        a._id = ans.id;
        return a;
    }

    public String DeleteServiceInventory(@RequestBody DeleteServiceInventoryRequest DeleteserviceInventoryRequest) {
        serviceInventoryRepo.save(SellerAdaptor.ToDeleteServiceInventory(DeleteserviceInventoryRequest));
        return "Successfully Deleted";
    }

    public ResponseEntity<?> uploadServiceImages(Map<String, Object> payload, String serviceBasePath) {
        try {
            String serviceId = String.valueOf(payload.get("id"));
            String userFolder = String.valueOf(payload.get("userfolder"));

            if (serviceId == null || serviceId.isBlank()
                    || userFolder == null || userFolder.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Required fields missing"));
            }

            if (!ObjectId.isValid(serviceId)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid service ID"));
            }

            Object imagesObj = payload.get("images");

            if (!(imagesObj instanceof List<?>)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid images payload"));
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> images =
                    (List<Map<String, Object>>) imagesObj;

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> deleteImages =
                    payload.get("deleteimages") instanceof List<?>
                            ? (List<Map<String, Object>>) payload.get("deleteimages")
                            : Collections.emptyList();

            if (images.isEmpty() && deleteImages.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No images provided"));
            }

            Path basePath = Paths.get("https://inock.intellesyde.com/" + serviceBasePath)
                    .toAbsolutePath()
                    .normalize();

            Path baseFolder = basePath
                    .resolve(userFolder)
                    .resolve(serviceId)
                    .normalize();

            Path archiveFolder = baseFolder
                    .resolve("Archieve")
                    .normalize();

            if (!baseFolder.startsWith(basePath)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid image path"));
            }

            Files.createDirectories(baseFolder);

            if (!deleteImages.isEmpty()) {
                Files.createDirectories(archiveFolder);
            }

            Optional<ServiceInventory> serviceInventory = serviceInventoryRepo.findById(new ObjectId(serviceId));

            if (serviceInventory.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Service inventory not found"));
            }

            for (Map<String, Object> img : deleteImages) {

                if (img == null) continue;

                String base64 = img.get("base64") != null
                        ? String.valueOf(img.get("base64"))
                        : null;

                String fileName = img.get("fileName") != null
                        ? String.valueOf(img.get("fileName"))
                        : null;

                if (base64 == null || base64.isBlank()
                        || fileName == null || fileName.isBlank()) {
                    continue;
                }

                String safeFileName = Paths.get(fileName)
                        .getFileName()
                        .toString();

                String lower = safeFileName.toLowerCase(Locale.ROOT);

                if (!(lower.endsWith(".jpg")
                        || lower.endsWith(".jpeg")
                        || lower.endsWith(".png")
                        || lower.endsWith(".webp"))) {
                    continue;
                }

                String cleaned = base64.contains(",")
                        ? base64.substring(base64.indexOf(",") + 1)
                        : base64;

                byte[] decodedBytes;

                try {
                    decodedBytes = Base64.getDecoder().decode(cleaned);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid base64 image: {}", safeFileName);
                    continue;
                }

                String extension =
                        lower.substring(lower.lastIndexOf("."));

                byte[] safeImage =
                        imageSecurityService.scanAndCompress(
                                decodedBytes,
                                extension
                        );

                Path outputFile = archiveFolder
                        .resolve(safeFileName)
                        .normalize();

                if (!outputFile.startsWith(archiveFolder)) {
                    continue;
                }

                Files.write(
                        outputFile,
                        safeImage,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING
                );

///                NEW: remove the original from the base (active) folder
                Path originalFile = baseFolder.resolve(safeFileName).normalize();
                if (originalFile.startsWith(baseFolder)) {
                    try {
                        Files.deleteIfExists(originalFile);
                    } catch (IOException e) {
                        log.warn("Failed to delete original image after archiving: {}", safeFileName, e);
                    }
                }
            }

            List<Map<String, String>> savedImages =
                    new ArrayList<>();

            String baseUrl = imageBaseUrl
                    .replaceAll("/+$", "");

            for (Map<String, Object> img : images) {

                if (img == null) continue;

                String base64 = img.get("base64") != null
                        ? String.valueOf(img.get("base64"))
                        : null;

                String fileName = img.get("fileName") != null
                        ? String.valueOf(img.get("fileName"))
                        : null;

                boolean isDefault =
                        Boolean.TRUE.equals(img.get("isDefault"));

                if (base64 == null || base64.isBlank()
                        || fileName == null || fileName.isBlank()) {
                    continue;
                }

                String safeFileName = Paths.get(fileName)
                        .getFileName()
                        .toString();

                String lower = safeFileName.toLowerCase(Locale.ROOT);

                if (!(lower.endsWith(".jpg")
                        || lower.endsWith(".jpeg")
                        || lower.endsWith(".png")
                        || lower.endsWith(".webp"))) {
                    continue;
                }

                String cleaned = base64.contains(",")
                        ? base64.substring(base64.indexOf(",") + 1)
                        : base64;

                byte[] decodedBytes;

                try {
                    decodedBytes = Base64.getDecoder().decode(cleaned);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid base64 image: {}", safeFileName);
                    continue;
                }

                String extension =
                        lower.substring(lower.lastIndexOf("."));

                byte[] safeImage =
                        imageSecurityService.scanAndCompress(
                                decodedBytes,
                                extension
                        );

                Path outputFile = baseFolder
                        .resolve(safeFileName)
                        .normalize();

                if (!outputFile.startsWith(baseFolder)) {
                    continue;
                }

                Files.write(
                        outputFile,
                        safeImage,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING
                );

                if (isDefault) {
                    ServiceInventory inventory =
                            serviceInventory.get();

                    inventory.defaultImage = safeFileName;
                    serviceInventoryRepo.save(inventory);
                }

                String imageUrl = baseUrl
                        + "/" + serviceBasePath
                        + userFolder
                        + "/"
                        + serviceId
                        + "/"
                        + safeFileName;

                savedImages.add(
                        Map.of(
                                "fileName", safeFileName,
                                "path", outputFile.toString(),
                                "url", imageUrl,
                                "isDefault", String.valueOf(isDefault)
                        )
                );
            }

            return ResponseEntity.ok(
                    Map.of(
                            "message", "Images uploaded successfully",
                            "images", savedImages
                    )
            );

        } catch (SecurityException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            "Image security validation failed: "
                                    + e.getMessage()
                    ));

        } catch (Exception e) {

            log.error("uploadServiceImages error", e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error",
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Unexpected error"
                    ));
        }
    }

    public ResponseEntity<?> uploadEC2ServiceImages(Map<String, Object> payload, String serviceBasePath) {
        try {
            String serviceId = String.valueOf(payload.get("id"));
            String userFolder = String.valueOf(payload.get("userfolder"));

            if (serviceId == null || serviceId.isBlank()
                    || userFolder == null || userFolder.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Required fields missing"));
            }

            if (!ObjectId.isValid(serviceId)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid service ID"));
            }

            Object imagesObj = payload.get("images");

            if (!(imagesObj instanceof List<?>)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid images payload"));
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> images =
                    (List<Map<String, Object>>) imagesObj;

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> deleteImages =
                    payload.get("deleteimages") instanceof List<?>
                            ? (List<Map<String, Object>>) payload.get("deleteimages")
                            : Collections.emptyList();

            if (images.isEmpty() && deleteImages.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No images provided"));
            }

            // FIX: use a real local/mounted storage root, not the public URL.
            // serviceBasePath here should be a relative folder segment under imageStoragePath,
            // not a full URL.
            Path basePath = Paths.get(imageStoragePath)
                    .toAbsolutePath()
                    .normalize();

            Path baseFolder = basePath
                    .resolve(serviceBasePath)
                    .resolve(userFolder)
                    .resolve(serviceId)
                    .normalize();

            Path archiveFolder = baseFolder
                    .resolve("Archieve")
                    .normalize();

            if (!baseFolder.startsWith(basePath)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid image path"));
            }

            Files.createDirectories(baseFolder);

            if (!deleteImages.isEmpty()) {
                Files.createDirectories(archiveFolder);
            }

            Optional<ServiceInventory> serviceInventory = serviceInventoryRepo.findById(new ObjectId(serviceId));

            if (serviceInventory.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Service inventory not found"));
            }

            for (Map<String, Object> img : deleteImages) {

                if (img == null) continue;

                String base64 = img.get("base64") != null
                        ? String.valueOf(img.get("base64"))
                        : null;

                String fileName = img.get("fileName") != null
                        ? String.valueOf(img.get("fileName"))
                        : null;

                if (base64 == null || base64.isBlank()
                        || fileName == null || fileName.isBlank()) {
                    continue;
                }

                String safeFileName = Paths.get(fileName)
                        .getFileName()
                        .toString();

                String lower = safeFileName.toLowerCase(Locale.ROOT);

                if (!(lower.endsWith(".jpg")
                        || lower.endsWith(".jpeg")
                        || lower.endsWith(".png")
                        || lower.endsWith(".webp"))) {
                    continue;
                }

                String cleaned = base64.contains(",")
                        ? base64.substring(base64.indexOf(",") + 1)
                        : base64;

                byte[] decodedBytes;

                try {
                    decodedBytes = Base64.getDecoder().decode(cleaned);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid base64 image: {}", safeFileName);
                    continue;
                }

                String extension =
                        lower.substring(lower.lastIndexOf("."));

                byte[] safeImage =
                        imageSecurityService.scanAndCompress(
                                decodedBytes,
                                extension
                        );

                Path outputFile = archiveFolder
                        .resolve(safeFileName)
                        .normalize();

                if (!outputFile.startsWith(archiveFolder)) {
                    continue;
                }

                Files.write(
                        outputFile,
                        safeImage,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING
                );

                Path originalFile = baseFolder.resolve(safeFileName).normalize();
                if (originalFile.startsWith(baseFolder)) {
                    try {
                        Files.deleteIfExists(originalFile);
                    } catch (IOException e) {
                        log.warn("Failed to delete original image after archiving: {}", safeFileName, e);
                    }
                }
            }

            List<Map<String, String>> savedImages =
                    new ArrayList<>();

            String baseUrl = imageBaseUrl
                    .replaceAll("/+$", "");

            for (Map<String, Object> img : images) {

                if (img == null) continue;

                String base64 = img.get("base64") != null
                        ? String.valueOf(img.get("base64"))
                        : null;

                String fileName = img.get("fileName") != null
                        ? String.valueOf(img.get("fileName"))
                        : null;

                boolean isDefault =
                        Boolean.TRUE.equals(img.get("isDefault"));

                if (base64 == null || base64.isBlank()
                        || fileName == null || fileName.isBlank()) {
                    continue;
                }

                String safeFileName = Paths.get(fileName)
                        .getFileName()
                        .toString();

                String lower = safeFileName.toLowerCase(Locale.ROOT);

                if (!(lower.endsWith(".jpg")
                        || lower.endsWith(".jpeg")
                        || lower.endsWith(".png")
                        || lower.endsWith(".webp"))) {
                    continue;
                }

                String cleaned = base64.contains(",")
                        ? base64.substring(base64.indexOf(",") + 1)
                        : base64;

                byte[] decodedBytes;

                try {
                    decodedBytes = Base64.getDecoder().decode(cleaned);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid base64 image: {}", safeFileName);
                    continue;
                }

                String extension =
                        lower.substring(lower.lastIndexOf("."));

                byte[] safeImage =
                        imageSecurityService.scanAndCompress(
                                decodedBytes,
                                extension
                        );

                Path outputFile = baseFolder
                        .resolve(safeFileName)
                        .normalize();

                if (!outputFile.startsWith(baseFolder)) {
                    continue;
                }

                Files.write(
                        outputFile,
                        safeImage,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING
                );

                if (isDefault) {
                    ServiceInventory inventory =
                            serviceInventory.get();

                    inventory.defaultImage = safeFileName;
                    serviceInventoryRepo.save(inventory);
                }

                String imageUrl = baseUrl
                        + "/" + serviceBasePath
                        + userFolder
                        + "/"
                        + serviceId
                        + "/"
                        + safeFileName;

                savedImages.add(
                        Map.of(
                                "fileName", safeFileName,
                                "path", outputFile.toString(),
                                "url", imageUrl,
                                "isDefault", String.valueOf(isDefault)
                        )
                );
            }

            return ResponseEntity.ok(
                    Map.of(
                            "message", "Images uploaded successfully",
                            "images", savedImages
                    )
            );

        } catch (SecurityException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            "Image security validation failed: "
                                    + e.getMessage()
                    ));

        } catch (Exception e) {

            log.error("uploadServiceImages error", e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error",
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Unexpected error"
                    ));
        }
    }

    public List<Map<String, Object>> getServiceImages(String userfolder, String servicename, String Service_BASE_PATH) throws IOException {

        String formattedServiceName = servicename.trim();
        log.info("formattedServiceName : " + formattedServiceName);
        log.info("Userfolder: " + userfolder);
        log.info("Service_BASE_PATH: " + Service_BASE_PATH);

        Path serviceDir = Paths.get(Service_BASE_PATH, userfolder, formattedServiceName);
        Path tempDir = Paths.get(Service_BASE_PATH, userfolder, formattedServiceName, "temp");

        log.info("Service path : {}", serviceDir.toAbsolutePath());
        log.info("Service exists : {}", Files.exists(serviceDir));

        log.info("Temp path is exist : " + Files.exists(tempDir));

        if (Files.exists(serviceDir) == false && !Files.exists(tempDir) == false) {
            log.info("Files not found");
            return Collections.emptyList();
        }

        List<Map<String, Object>> images = new ArrayList<>();

        if (Files.exists(tempDir) && Files.isDirectory(tempDir)) {
            long tempCount;
            try (Stream<Path> tempPaths = Files.list(tempDir)) {
                tempCount = tempPaths.filter(Files::isRegularFile).count();
            }

            if (tempCount > 0) {
                Map<String, Object> tempInfo = new HashMap<>();
                tempInfo.put("tempImages", tempCount);
                images.add(tempInfo);
            }
        }
        if (!images.isEmpty()) {
            log.info("Temp folder available ");
            return images;
        }

        try (Stream<Path> paths = Files.list(serviceDir)) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
                    })
                    .forEach(path -> {
                        try {
                            Map<String, Object> img = new HashMap<>();

                            String fileName = path.getFileName().toString();
                            log.info("Filename: " + fileName);
                            fileName = fileName.substring(0, fileName.indexOf('.'));
                            String extension = path.getFileName().toString().substring(path.getFileName().toString().lastIndexOf('.') + 1).toLowerCase();


                            byte[] fileBytes = Files.readAllBytes(path);

                            // 🔹 base64 encode
                            String base64 = Base64.getEncoder().encodeToString(fileBytes);

                            img.put("fileName", fileName);
                            img.put("type", extension);
                            img.put("url", userfolder + "/" + formattedServiceName + "/" + fileName);
                            img.put("base64", "data:image/" + (extension) + ";base64," + base64);
                            img.put("isDefault", fileName.toLowerCase().contains("default"));

                            images.add(img);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        }

        return images;
    }

    public ObjectId QICustomer(QICustomerRequestDTO qiCustomerRequestDTO) {

        log.info("Enterer in QICustomer Service ");
        QICustomers getFromDB = qiCustomersRepo.findByMobileNum(qiCustomerRequestDTO.mobile_num);
        if (getFromDB != null) {
            return getFromDB.get_id();
        }

        QICustomers qiCustomers1 = SellerAdaptor.ToQICustomersDTOtoQICustomers(qiCustomerRequestDTO);
        QICustomers getqiCustomers = qiCustomersRepo.insert(qiCustomers1);
        System.out.println(getqiCustomers.get_id());

        return getqiCustomers.get_id();

    }

    public ResponseEntity<List<Map<String, Object>>> getImages(
            String userfolder,
            String type,
            String inventoryId,
            String companyName,
            String productId) {

        log.info("========== getImages START ==========");
        log.info("Userfolder: {}", userfolder);
        log.info("Type: {}", type);
        log.info("InventoryId: {}", inventoryId);
        log.info("ProductId: {}", productId);
        log.info("Company name: {}", companyName);

        final Path basePath = Paths.get(uploadDir);

        log.info("Upload base path: {}", basePath.toAbsolutePath());

        final Path productImageDir = basePath.resolve("uploadsProductImages")
                .resolve(companyName)
                .resolve(inventoryId);

        final Path serviceImageDir = basePath.resolve("uploadsServiceImages")
                .resolve(userfolder)
                .resolve(inventoryId);

        log.info("Product image path: {}", productImageDir.toAbsolutePath());

        log.info("Service image path: {}", serviceImageDir.toAbsolutePath());

        Products products = null;
        ServiceInventory serviceInventory = null;

        try {
            if ("uploadsServiceImages".equals(type)) {
                log.info("Loading Service Inventory");
                if (inventoryId == null || !ObjectId.isValid(inventoryId)) {
                    log.warn("Invalid service inventory ID: {}", inventoryId);
                    return ResponseEntity.ok(Collections.emptyList());
                }
                Optional<ServiceInventory> serviceOptional = serviceInventoryRepo.findById(new ObjectId(inventoryId));
                if (serviceOptional.isPresent()) {
                    serviceInventory = serviceOptional.get();
                    log.info("Service inventory found: {}", serviceInventory.id);
                } else {
                    log.warn("Service inventory not found: {}", inventoryId);
                    return ResponseEntity.ok(Collections.emptyList());
                }

            } else {

                log.info("Loading Product");

                if (productId == null || productId.isBlank()) {

                    log.warn("Product ID is empty");

                    return ResponseEntity.ok(
                            Collections.emptyList()
                    );
                }

                Optional<Products> productOptional = productRepo.findById(productId);

                if (productOptional.isPresent()) {

                    products = productOptional.get();

                    log.info(
                            "Product found: {}",
                            productId
                    );

                } else {

                    log.warn(
                            "Product not found: {}",
                            productId
                    );

                    return ResponseEntity.ok(
                            Collections.emptyList()
                    );
                }
            }

        } catch (Exception e) {

            log.error(
                    "Error loading product/service data",
                    e
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }

        final String defaultImage =
                "uploadsServiceImages".equals(type)
                        ? serviceInventory.defaultImage
                        : products.getDefaultImage();

        log.info(
                "Default image from DB: {}",
                defaultImage
        );

        /*
         * ---------------------------------------------------------
         * 5. Select directory
         * ---------------------------------------------------------
         */
        final Path imageDir =
                "uploadsServiceImages".equals(type)
                        ? serviceImageDir
                        : productImageDir;

        log.info(
                "Selected image directory: {}",
                imageDir.toAbsolutePath()
        );

        log.info(
                "Directory exists: {}",
                Files.exists(imageDir)
        );

        /*
         * ---------------------------------------------------------
         * 6. Directory validation
         * ---------------------------------------------------------
         */
        if (!Files.exists(imageDir)
                || !Files.isDirectory(imageDir)) {

            log.warn(
                    "Image directory not found: {}",
                    imageDir.toAbsolutePath()
            );

            return ResponseEntity.ok(
                    Collections.emptyList()
            );
        }

        final List<Map<String, Object>> images =
                new ArrayList<>();

        /*
         * ---------------------------------------------------------
         * 7. Read files
         * ---------------------------------------------------------
         */
        try (Stream<Path> paths = Files.list(imageDir)) {

            final List<Path> fileList =
                    paths
                            .filter(Files::isRegularFile)
                            .toList();

            log.info(
                    "Total files found: {}",
                    fileList.size()
            );

            for (Path path : fileList) {

                try {

                    final String originalFileName =
                            path.getFileName().toString();

                    log.info(
                            "Processing file: {}",
                            originalFileName
                    );

                    /*
                     * Get extension
                     */
                    final int dotIndex =
                            originalFileName.lastIndexOf('.');

                    if (dotIndex <= 0
                            || dotIndex == originalFileName.length() - 1) {

                        log.warn(
                                "Skipping file without valid extension: {}",
                                originalFileName
                        );

                        continue;
                    }

                    final String extension =
                            originalFileName
                                    .substring(dotIndex + 1)
                                    .toLowerCase(Locale.ROOT);

                    /*
                     * Only allow image formats
                     */
                    if (!Set.of(
                            "jpg",
                            "jpeg",
                            "png",
                            "gif",
                            "webp"
                    ).contains(extension)) {

                        log.warn(
                                "Skipping unsupported image: {}",
                                originalFileName
                        );

                        continue;
                    }

                    /*
                     * Read file
                     */
                    final byte[] fileBytes =
                            Files.readAllBytes(path);

                    /*
                     * Base64
                     */
                    final String base64 =
                            Base64.getEncoder()
                                    .encodeToString(fileBytes);

                    /*
                     * Default image check
                     */
                    final boolean isDefault =
                            defaultImage != null
                                    && originalFileName.equalsIgnoreCase(
                                    defaultImage
                            );

                    /*
                     * -------------------------------------------------
                     * Build HTTP image URL
                     * -------------------------------------------------
                     */
                    final String relativePath;

                    if ("uploadsServiceImages".equals(type)) {

                        relativePath =
                                Service_BASE_PATH
                                        + userfolder
                                        + "/"
                                        + inventoryId
                                        + "/"
                                        + originalFileName;

                    } else {

                        relativePath =
                                Product_BASE_PATH
                                        + companyName
                                        + "/"
                                        + inventoryId
                                        + "/"
                                        + originalFileName;
                    }

                    /*
                     * Remove trailing slash from base URL
                     */
                    final String baseUrl =
                            imageBaseUrl
                                    .replaceAll("/+$", "");

                    final String imageUrl =
                            baseUrl + "/" + relativePath;

                    log.info(
                            "Image URL: {}",
                            imageUrl
                    );

                    /*
                     * Response object
                     */
                    final Map<String, Object> img =
                            new HashMap<>();

                    img.put(
                            "fileName",
                            originalFileName
                    );

                    img.put(
                            "type",
                            extension
                    );

                    img.put(
                            "url",
                            imageUrl
                    );

                    img.put(
                            "base64",
                            "data:image/"
                                    + extension
                                    + ";base64,"
                                    + base64
                    );

                    img.put(
                            "isDefault",
                            isDefault
                    );

                    images.add(img);

                } catch (Exception e) {

                    log.error(
                            "Error processing image: {}",
                            path,
                            e
                    );
                }
            }

            log.info(
                    "Final image count returned: {}",
                    images.size()
            );

            log.info("========== getImages END ==========");

            return ResponseEntity.ok(images);

        } catch (IOException e) {

            log.error(
                    "Error reading image directory: {}",
                    imageDir.toAbsolutePath(),
                    e
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    public List<QICustomerResponseDTO> QIcustomersuggestion(QIcustomersuggestionRequestDTO qIcustomersuggestionRequestDTO) {
        List<QICustomers> qiCustomers = qiCustomersRepo.findByMobileNumStartingWith(qIcustomersuggestionRequestDTO.mobile_num);
        if (!qiCustomers.isEmpty()) {
            List<QICustomerResponseDTO> qiCustomerResponseDTO = SellerAdaptor.ToQICustomerResponseDTO(qiCustomers);
            return qiCustomerResponseDTO;
        }
        return new ArrayList<QICustomerResponseDTO>();
    }

    public GetUPIResponse GetUPI(GetUPIRequest getUPIRequest) {
        GetUPIResponse getUPIResponse = new GetUPIResponse();
        Seller seller = sellerRepo.findByUseridContaining(getUPIRequest.id);
        if (seller != null) {
            Optional<User> user = sellerUserRepo.findById(getUPIRequest.id);
            if (user.isPresent()) {
                getUPIResponse.name = user.get().getFirstname();
            }

            getUPIResponse.upi = seller.getSeller_Payment_Details() == null ? null : seller.getSeller_Payment_Details().getUpi_Id();
        }
        return getUPIResponse;
    }

    public boolean readOTP(String otpFromUser) {
//        String thirdPartyOtp = getOtpFromThirdParty();
        String thirdPartyOtp = "1234";
        return thirdPartyOtp.equals(otpFromUser);
    }

    private String getOtpFromThirdParty() {
        try {
            String apiUrl = "https://thirdparty.com/api/getOtp?userId=123";

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JSONObject json = new JSONObject(response.getBody());
                return json.getString("otp");
            } else {
                log.warn("Failed to get OTP from third party");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean GetOTP(GetOTPRequest getOTPRequest) {
        String otpFromUser = getOTPRequest.otp;
        return readOTP(otpFromUser);
    }

    public ResponseEntity<Map<String, String>> GenerateOtp(GetOTPRequest request, String directory) {
        try {

            int otp = 100000 + new Random().nextInt(900000);

            File folder = new File(directory);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File[] files = folder.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().startsWith(request.mobileNum + "_")) {
                        f.delete();
                    }
                }
            }

//            ResponseEntity<String> res = sendSubscriptionEmail(
//                    new SubscriptionEmailDTO(request.email),
//                    String.valueOf(otp),
//                    request.mobileNum
//            );
//
//            if (res == null || res.getBody() == null ||
//                    !res.getBody().equals("Email sent successfully")) {
//                return ResponseEntity.badRequest().body(
//                        Map.of("message", "FAILED TO SEND EMAIL")
//                );
//            }


            String fileName = request.mobileNum + "_" + otp + ".txt";
            File file = new File(folder, fileName);

            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Your OTP is: " + otp);
            }

            return ResponseEntity.ok(
                    Map.of("message", "OTP SENT", "OTP", String.valueOf(otp))
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(
                    Map.of("message", "FAILED TO SEND OTP")
            );
        }
    }

    public boolean ValidateOtp(GetOTPRequest getOTPRequest, String Directory, int attempt) {
        try {

            String fileName = getOTPRequest.mobileNum + "_" + getOTPRequest.otp + ".txt";

            File file = new File(Directory + File.separator + fileName);

            if (file.exists()) {
//                boolean deleted = file.delete();
                log.info("File is Exist");
                return true;
//                if (deleted) {
//                    return true;
//                } else {
//                    return false;
//                }
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
            // Validate request
            if (getOTPRequest == null) {
                return false;
            }

            // Validate mobile number
            String mobileNum = getOTPRequest.mobileNum;

            if (mobileNum == null || mobileNum.trim().isEmpty()) {
                return false;
            }

            // Validate directory
            if (directory == null || directory.trim().isEmpty()) {
                return false;
            }

            File folder = new File(directory);

            if (!folder.exists() || !folder.isDirectory()) {
                return false;
            }

            File[] files = folder.listFiles();

            if (files == null) {
                return false;
            }

            for (File file : files) {
                if (file == null || !file.isFile()) {
                    continue;
                }

                String fileName = file.getName();

                if (fileName != null && fileName.contains(mobileNum)) {
                    return file.delete();
                }
            }

            return false; // No matching file found

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public AddProductResponse InsertProducts(InsertProductsRequest request) {
        AddProductResponse response = new AddProductResponse();
        try {
            if (request == null || request.productName == null || request.brand == null) {
                response.message = "Product name, category and brand are required";
                response.success = false;
                return response;
            }


            Optional<ProductCategory> productCategory =
                    productCategoryRepo.findById(
                            String.valueOf(request.categoryId)
                    );

            String defaultImageFileName = "";
            for (ProductImageDTO img : request.defaultImage) {
                if (img.isDefault) {
                    defaultImageFileName = img.fileName;
                    break;
                }
            }
            if (request._id != null) {
                Optional<Products> existProduct = productRepo.findById(String.valueOf(request.productId));
                if (!existProduct.isPresent()) {
                    throw new RuntimeException("Existing Product not found ");
                }
                Products updateProduct = existProduct.get();


                log.info("Updating.....");


                Inventory existInventory =
                        inventoryRepo.findByProductidAndCompanyid(
                                updateProduct.get_id(),
                                String.valueOf(request.companyId)
                        );

                if (existInventory == null) {
                    throw new RuntimeException("Inventory not found");
                }

                if (request.brand != null && !request.brand.trim().isEmpty()) {
                    updateProduct.setBrand(request.brand);
                }
                if (request.productName != null && !request.productName.trim().isEmpty()) {
                    updateProduct.setProductName(request.productName);
                }

                if (request.productDescription != null && !request.productDescription.trim().isEmpty()) {

                    updateProduct.setProductDescription(
                            request.productDescription
                    );
                }

                if (productCategory.isPresent()) {
                    updateProduct.setProductCategory(productCategory.get());
                    existInventory.setProductCategory(productCategory.get());
                }

                updateProduct.setUpdatedAt(String.valueOf(System.currentTimeMillis()));


                updateProduct.setQuickadd(request.quickadd);


                Set<String> requestVariantIds = request.variants.stream()
                        .map(v -> String.valueOf(v._id))
                        .collect(Collectors.toSet());

                for (String i : requestVariantIds) log.info("Requested Variant ID : " + i);

                List<Batches> savedBatches = new ArrayList<>();

                Optional<Batches> last =
                        batchesRepo.findTopByCompanyidOrderByCreatedAtDesc(
                                String.valueOf(request.companyId)
                        );

                String lastBatchId =
                        last.map(Batches::getBatch_Id).orElse(null);

                for (ProductVariantDTO variant : request.variants) {

                    Optional<Batches> getbatch = batchesRepo.findById(String.valueOf(variant._id));

                    if (getbatch.isPresent() && getbatch.get().isActive()) {
                        log.info("Updating batch .....:" + variant._id);
                        Batches batch = getbatch.get();

                        if (variant.variantName != null && batch.getVariantName() != null && !batch.getVariantName().trim().equalsIgnoreCase(variant.variantName.trim())) {
                            String oldVariantName = batch.getVariantName();
                            String newVariantName = variant.variantName.trim();

                            batch.setVariantName(newVariantName);
                            batch.setSeller_price(variant.sellingPrice);
                            batch.setProcurement_price((int) variant.procumentPrice);
                            batch.setOfferPrice(variant.offerPrice);
                            batch.setMargin_percentage((int) variant.margin);
                            batch.setStock_availability(variant.quantity);
                            batch.setDiscount(variant.discount);
                            batch.setProductname(request.productName);
                            batch.setUpdatedAt(String.valueOf(System.currentTimeMillis()));

                            log.info("Variant name changed from {} to {}", oldVariantName, newVariantName);

                            List<Batches> fullBatchesByVariantName =
                                    batchesRepo.findByListProductidAndCompanyidAndVariantName(
                                            batch.getProductid(),
                                            batch.getCompanyid(),
                                            oldVariantName
                                    );

                            if (fullBatchesByVariantName != null && !fullBatchesByVariantName.isEmpty()) {
                                List<Batches> updated = new ArrayList<>();
                                for (Batches item : fullBatchesByVariantName) {
                                    if (!item.get_id().equals(batch.get_id())) {
                                        item.setVariantName(newVariantName);
                                        item.setProductname(request.productName);
                                        item.setUpdatedAt(String.valueOf(System.currentTimeMillis()));
                                        updated.add(item);
                                    }
                                }
                                updated.add(batch);
                                batchesRepo.saveAll(updated);

                                // update in Inventory also
                                List<Batches> needs_To_Update = new ArrayList<>();
                                for (Batches k : updated) {
                                    if (k.isActive()) needs_To_Update.add(k);
                                }
                                savedBatches.addAll(needs_To_Update);
                                log.info("updated {} batches with new variant name",
                                        updated.size());
                            }
                        } else {
                            batch.setSeller_price(variant.sellingPrice);

                            batch.setProcurement_price((int) variant.procumentPrice);

                            batch.setOfferPrice(variant.offerPrice);

                            batch.setMargin_percentage((int) variant.margin);

                            batch.setStock_availability(variant.quantity);

                            batch.setDiscount(variant.discount);

                            batch.setUpdatedAt(String.valueOf(System.currentTimeMillis()));
                            batch.setProductname(request.productName);

                            Batches updatedBatch =
                                    batchesRepo.save(batch);

                            savedBatches.add(updatedBatch);

                        }
                    } else {
                        log.info("Adding batch .....for :" + variant._id);
                        List<ProductVariantDTO> tempVariant = Collections.singletonList(variant);

                        List<Batches> batchList =
                                SellerAdaptor.ToBatches(
                                        request,
                                        tempVariant,
                                        lastBatchId,
                                        updateProduct.get_id()
                                );

                        if (!batchList.isEmpty()) {

                            Batches insertedBatch = batchesRepo.save(batchList.get(0));

                            savedBatches.add(insertedBatch);
                        }
                    }
                }

                List<ObjectId> batchIdsToDelete = Optional
                        .ofNullable(existInventory.getVariantBatches())
                        .orElse(Collections.emptyList())
                        .stream()
                        .filter(batch ->
                                batch.get_id() != null &&
                                        !requestVariantIds.contains(String.valueOf(batch.get_id()))
                        )
                        .map(batch -> new ObjectId(batch.get_id()))
                        .toList();

                for (ObjectId i : batchIdsToDelete) log.info("To be Delete Variant ID : " + i);

                if (!batchIdsToDelete.isEmpty()) {
                    batchesRepo.deleteBy_idIn(batchIdsToDelete);
                }


                if (!savedBatches.isEmpty()) {
                    if (savedBatches.size() > 1) {
                        existInventory.setBatchid(null);
                        existInventory.setSellerprice(savedBatches.get(0).getSeller_price());
                        existInventory.setVariantBatches(savedBatches);


                    } else {
                        existInventory.setBatchid(savedBatches.get(0));
                        existInventory.setSellerprice(savedBatches.get(0).getSeller_price());
                        existInventory.setVariantBatches(null);
                    }
                }
                existInventory.setDeals(request.deals);
                existInventory.setProductName(request.productName);
                existInventory.setNewArrivals(request.newArrivals);
                Inventory updated = inventoryRepo.save(existInventory);

                if (updated == null) {
                    throw new RuntimeException("Failed in Update Inventory ");
                }

                Map<String, Object> savedImages =
                        updateProductImages(
                                request.defaultImage,
                                request.companyName,
                                updated.get_id()
                        );

                if (request.defaultImage != null
                        && !request.defaultImage.isEmpty()
                        && savedImages.isEmpty()) {
                    throw new RuntimeException("Failed in upload product images");
                }

                log.info("SavedImages: {}", savedImages);

                if (!savedImages.isEmpty()) {
                    String defaultFileName = String.valueOf(savedImages.get("fileName"));
                    updateProduct.setDefaultImage(defaultFileName);
                    updateProduct.setUpdatedAt(String.valueOf(System.currentTimeMillis()));

                }
                productRepo.save(updateProduct);
                response.success = true;
                response.productId = updateProduct.get_id().toString();
                response.message = "Product updated successfully";

                return response;
            }


            Optional<Seller> seller = sellerRepo.findById(String.valueOf(request.companyId));


            Products newProduct = new Products();


            newProduct.setProductName(request.productName);
            newProduct.setProductDescription(request.productDescription);

            if (productCategory.isPresent()) {

                log.info("Category found");

                newProduct.setProductCategory(productCategory.get());

            } else {

                log.info("Category Not found");
            }

            if (seller.isPresent()) {

                List<SellerList> newEntry = new ArrayList<>();

                SellerList sellers = new SellerList();

                sellers.setCompanyid(
                        new ObjectId(seller.get().getId())
                );

                sellers.setCompanyname(
                        seller.get().getCompanyname()
                );

                newEntry.add(sellers);

                newProduct.setSellerLists(newEntry);
            }

            newProduct.setQuickadd(true);
            newProduct.setUnit(request.unit);
            newProduct.setProduct_type(request.product_type);
            newProduct.setBrand(request.brand);
            newProduct.setDefaultImage(defaultImageFileName);

            newProduct.setProductfeatureid(request.productfeatureid);
            newProduct.setCreatedAt(String.valueOf(System.currentTimeMillis()));

            newProduct.setUpdatedAt(String.valueOf(System.currentTimeMillis()));

            Products savedProduct = productRepo.save(newProduct);

            log.info("Product saved: " + savedProduct);

            Optional<Batches> last =
                    batchesRepo.findTopByCompanyidOrderByCreatedAtDesc(
                            String.valueOf(request.companyId)
                    );

            String lastBatchId =
                    last.isPresent()
                            ? last.get().getBatch_Id()
                            : null;

            SellerBatchesRequest sellerBatchesRequest = new SellerBatchesRequest();
            sellerBatchesRequest.categoryId = request.categoryId;
            sellerBatchesRequest.setCompanyid(String.valueOf(request.companyId));
            sellerBatchesRequest.setProductid(String.valueOf(savedProduct.get_id()));
            sellerBatchesRequest.setUnit(request.unit);
            sellerBatchesRequest.setBatch_expiryDate(request.batchExpiryDate);
            sellerBatchesRequest.setMinimumorder(request.minimumOrder);
            sellerBatchesRequest.setExpiry(request.expiry);
            sellerBatchesRequest.setBatch_unit(request.batchUnit);
            sellerBatchesRequest.deals = request.deals;
            sellerBatchesRequest.newArrivals = request.newArrivals;
            sellerBatchesRequest.setGst(request.gst);
            sellerBatchesRequest.setProductname(request.productName);

            List<Batches> batchList = SellerAdaptor.ToBatches(request, request.variants, lastBatchId, savedProduct.get_id());
            log.info("BatchList size : " + batchList.size());


            InventoryBatchResponse ans = AddBatchandInventory(batchList, sellerBatchesRequest);
            log.info("Ans: " + ans.getStatus());

            if (!ans.getStatus().contains("Successfully")) {
                throw new RuntimeException(ans.getStatus());
            }

            Inventory currentInventory = ans.getInventory();
            if (currentInventory == null) {
                throw new RuntimeException("Failed in save Inventory");
            }
            Map<String, Object> savedImages = saveProductImages(request.defaultImage, request.companyName, currentInventory.get_id());


            if (request.defaultImage.size() > 0 && savedImages.isEmpty()) {
                throw new RuntimeException("Failed in upload product images");
            }
            log.info("SavedImages: " + savedImages);


            response.success = true;
            response.productId = savedProduct.get_id().toString();
            response.message = "Product added successfully";

            return response;

        } catch (Exception e) {

            log.error("Error in InsertProducts : ", e);

            throw new RuntimeException(e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public InventoryBatchResponse AddBatchandInventory(List<Batches> batches, SellerBatchesRequest request) {

        Inventory inventory =
                inventoryRepo.findByProductidAndCompanyid(
                        new ObjectId(request.getProductid()),
                        request.getCompanyid()
                );

        Optional<ProductCategory> productCategory =
                productCategoryRepo.findById(
                        String.valueOf(request.categoryId)
                );

        log.info("Inventory : {}", inventory);

        Collection<Batches> savedCollection =
                mongoTemplate.insertAll(batches);

        List<Batches> savedBatches =
                new ArrayList<>(savedCollection);

        if (savedBatches.isEmpty()) {

            throw new RuntimeException(
                    "Failed to save batches"
            );
        }

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        String currentDate =
                LocalDateTime.now().format(formatter);


        if (inventory != null) {

            // ✅ optional old batch cleanup
            if (inventory.getVariantBatches() != null
                    && !inventory.getVariantBatches().isEmpty()) {

                List<ObjectId> oldIds =
                        inventory.getVariantBatches()
                                .stream()
                                .map(batch -> new ObjectId(batch.get_id()))
                                .toList();

                mongoTemplate.remove(
                        Query.query(
                                Criteria.where("_id").in(oldIds)
                        ),
                        Batches.class
                );
            }

            if (inventory.getBatchid() != null) {

                mongoTemplate.remove(
                        inventory.getBatchid()
                );
            }

            inventory.setBatchid(null);
            inventory.setVariantBatches(null);

            inventory.setSellerprice(savedBatches.get(0).getSeller_price());

            if (savedBatches.size() > 1) {

                inventory.setVariantBatches(savedBatches);

            } else {

                inventory.setBatchid(savedBatches.get(0));
            }

            if (request.getGst() != null
                    && !request.getGst().isEmpty()) {

                inventory.setGst(request.getGst());
            }

            inventory.setUpdated_at(currentDate);
            inventory.setDeals(request.deals);
            inventory.setNewArrivals(request.newArrivals);
            inventory.setActive(true);

            Inventory savedInventory = mongoTemplate.save(inventory);

            return new InventoryBatchResponse(
                    "Successfully updated inventory batches",
                    savedInventory
            );
        }


        Inventory newInventory = new Inventory();
        newInventory.setActive(true);
        newInventory.setCompanyid(
                request.getCompanyid()
        );

        newInventory.setProductid(
                new ObjectId(request.getProductid())
        );

        newInventory.setSellerprice(savedBatches.get(0).getSeller_price());

        productCategory.ifPresent(
                newInventory::setProductCategory
        );

        newInventory.setProductName(
                request.getProductname()
        );

        if (savedBatches.size() > 1) {

            newInventory.setVariantBatches(
                    savedBatches
            );

        } else {

            newInventory.setBatchid(
                    savedBatches.get(0)
            );
        }

        if (request.getGst() != null
                && !request.getGst().isEmpty()) {

            newInventory.setGst(
                    request.getGst()
            );
        }

        newInventory.setCreated_at(currentDate);
        newInventory.setUpdated_at(currentDate);
        newInventory.setDeals(request.deals);
        newInventory.setNewArrivals(request.newArrivals);


        Inventory savedInventory = mongoTemplate.insert(newInventory);

        return new InventoryBatchResponse(
                "Successfully registered inventory and batches",
                savedInventory
        );
    }

    public Map<String, Object> saveProductImages(List<ProductImageDTO> images, String companyName, String inventoryId) {
        List<Map<String, Object>> savedImages = new ArrayList<>();
        try {
            if (images == null || images.isEmpty()) {
                return new HashMap<>();
            }

            companyName = companyName
                    .trim()
                    .replaceAll("[/()]", "")
                    .replaceAll("\\s+", "-");

            log.info("Formatted product name: " + companyName);

            String baseFolderPath = "uploads"
                    + File.separator + "uploadsProductImages"
                    + File.separator + companyName
                    + File.separator + inventoryId;

            log.info("Base Folder: " + baseFolderPath);

            File baseFolder = new File(baseFolderPath);

            if (!baseFolder.exists()) {
                log.info("Base Folder not found , creating....");
                baseFolder.mkdirs();
            }


            for (ProductImageDTO img : images) {
                String base64 = img.base64;
                String fileName = img.fileName;
                if (base64 == null || fileName == null || base64.isBlank()) {
                    continue;
                }
                String lower = fileName.toLowerCase();

                if (!(lower.endsWith(".jpg")
                        || lower.endsWith(".jpeg")
                        || lower.endsWith(".png"))) {

                    continue;
                }

                // Split filename
                int dotIndex = fileName.lastIndexOf(".");

                if (dotIndex == -1) {
                    continue;
                }

                String nameWithoutExt = fileName.substring(0, dotIndex);
                String extension = fileName.substring(dotIndex);

                String finalName = fileName;
                log.info("FinalName : " + finalName);

                // Remove base64 header
                String cleaned = base64.contains(",")
                        ? base64.substring(base64.indexOf(",") + 1)
                        : base64;

                byte[] imageBytes;

                try {
                    imageBytes = Base64.getDecoder().decode(cleaned);

                    imageBytes = imageSecurityService.scanAndCompress(imageBytes, extension);

                } catch (IllegalArgumentException e) {
                    continue;
                }
                File outputFile = new File(baseFolder, finalName);
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    fos.write(imageBytes);

                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }

                // Save metadata
                Map<String, Object> imageData = new HashMap<>();

                imageData.put("fileName", finalName);
                imageData.put("isDefault", img.isDefault);
                imageData.put("path", outputFile.getAbsolutePath());

                if (img.isDefault) savedImages.add(imageData);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return savedImages.size() > 0 ? savedImages.get(0) : new HashMap<>();
    }

    public Map<String, Object> updateProductImages(List<ProductImageDTO> newImages, String companyName, String inventoryId) {

        Map<String, Object> result = new HashMap<>();
        if (newImages == null) {
            return result;
        }
        companyName = companyName.trim()
                .replaceAll("[/()]", "")
                .replaceAll("\\s+", "-");

        String baseFolderPath = "uploads"
                + File.separator + "uploadsProductImages"
                + File.separator + companyName
                + File.separator + inventoryId;

        File baseFolder = new File(baseFolderPath);

        if (!baseFolder.exists() && !baseFolder.mkdirs()) {
            throw new RuntimeException(
                    "Unable to create product image folder: " + baseFolderPath
            );
        }

        File[] existingFiles = baseFolder.listFiles();

        Set<String> incomingFileNames = new HashSet<>();

        for (ProductImageDTO img : newImages) {
            if (img != null
                    && img.fileName != null
                    && !img.fileName.isBlank()) {

                incomingFileNames.add(img.fileName);
            }
        }

        File archiveFolder =
                new File(baseFolder, "Archive");

        if (!archiveFolder.exists() && !archiveFolder.mkdirs()) {
            throw new RuntimeException(
                    "Unable to create archive folder"
            );
        }

        if (existingFiles != null) {

            for (File existingFile : existingFiles) {

                if (!existingFile.isFile()) {
                    continue;
                }

                String existingName = existingFile.getName();


                if (!incomingFileNames.contains(existingName)) {

                    File archivedFile =
                            getUniqueArchiveFile(
                                    archiveFolder,
                                    existingName
                            );

                    boolean moved =
                            existingFile.renameTo(archivedFile);

                    if (!moved) {
                        throw new RuntimeException(
                                "Failed to archive image: "
                                        + existingName
                        );
                    }

                    log.info(
                            "Archived deleted product image: {} -> {}",
                            existingName,
                            archivedFile.getAbsolutePath()
                    );
                }
            }
        }

        Map<String, Object> defaultImageData = new HashMap<>();

        for (ProductImageDTO img : newImages) {

            if (img == null) {
                continue;
            }

            String base64 = img.base64;
            String fileName = img.fileName;

            if (base64 == null
                    || base64.isBlank()
                    || fileName == null
                    || fileName.isBlank()) {
                continue;
            }

            String lower = fileName.toLowerCase();

            if (!(lower.endsWith(".jpg")
                    || lower.endsWith(".jpeg")
                    || lower.endsWith(".png"))) {

                continue;
            }

            String extension = fileName.substring(
                    fileName.lastIndexOf(".")
            );

            String cleaned = base64.contains(",")
                    ? base64.substring(base64.indexOf(",") + 1) : base64;

            byte[] imageBytes;
            try {
                imageBytes = Base64.getDecoder().decode(cleaned);

                imageBytes = imageSecurityService.scanAndCompress(imageBytes, extension);

            } catch (IllegalArgumentException e) {
                log.error("Invalid base64 image: {}", fileName, e);
                continue;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            File outputFile = new File(baseFolder, fileName);

            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(imageBytes);
            } catch (Exception e) {
                log.error("Failed to save image: {}", fileName, e);
                continue;
            }
            log.info("Saved product image: {}", outputFile.getAbsolutePath());
            if (img.isDefault) {
                defaultImageData.put("fileName", fileName);
                defaultImageData.put("isDefault", true);
                defaultImageData.put("path", outputFile.getAbsolutePath()
                );
            }
        }
        return defaultImageData;
    }

    private File getUniqueArchiveFile(File archiveFolder, String fileName) {

        File archiveFile = new File(archiveFolder, fileName);
        if (!archiveFile.exists()) {
            return archiveFile;
        }
        String name = fileName.substring(0, fileName.lastIndexOf("."));
        String extension = fileName.substring(fileName.lastIndexOf("."));
        int counter = 1;
        do {
            archiveFile = new File(archiveFolder, name + "_" + counter + extension);
            counter++;
        } while (archiveFile.exists());
        return archiveFile;
    }

    public InventoryDTO GetProduct(GetProductRequest getProductRequest) {
        if (getProductRequest.inventoryId == null && getProductRequest.companyId == null) return new InventoryDTO();
        log.info("inventory id :" + getProductRequest.inventoryId);
        log.info("Company id :" + getProductRequest.companyId);
        Optional<Inventory> inventory = inventoryRepo.findById(String.valueOf(getProductRequest.inventoryId));
        if (!inventory.isPresent()) {
            return new InventoryDTO();
        }
        Optional<Products> products = productRepo.findById(String.valueOf(inventory.get().getProductid()));
        if (inventory != null && products.isPresent()) {
            List<Inventory> a = new ArrayList<>();
            a.add(inventory.get());
            List<InventoryDTO> ans = SellerAdaptor.FromInventorytoInventoryDTO(a, new HashMap<>());
            if (ans.size() > 0) {
                InventoryDTO single = ans.get(0);
                single.brand = products.get().getBrand();
                single.productDescription = products.get().getProductDescription();
                single.defaultImage = products.get().getDefaultImage();
                single.quickadd = products.get().isQuickadd();
                return single;
            }
            return new InventoryDTO();
        }
        return new InventoryDTO();
    }

    public List<PickupPointsDTO> GetDeliveryPoints(String user_id) {
        Optional<Seller> seller = sellerRepo.findById(user_id);
        List<PickupPointsDTO> pickupPointsDTO = new ArrayList<PickupPointsDTO>();
        if (seller.isPresent()) {
            pickupPointsDTO = SellerAdaptor.FromPickupPointstoPickupPointsDTO(seller.get().getPickupPoints());
        }
        return pickupPointsDTO;
    }

    public List<PaymentsDTO> PaymentHistory(PaymentHistoryRequest PaymentHistoryRequest) {
        if (PaymentHistoryRequest._id == null) return new ArrayList<PaymentsDTO>();
        PaymentDetails paymentDetails = paymentDetailsRepo.findByOrderId(new ObjectId(PaymentHistoryRequest._id));
        if (paymentDetails != null) {
            log.info("_id: " + paymentDetails.get_id());
            log.info("Order id: " + paymentDetails.getOrderId());
            List<PaymentsDTO> ans = new ArrayList<>();
            for (Payments i : paymentDetails.getPayments()) {
                ans.add(SellerAdaptor.FromPaymenttoPaymentDTO(i));
            }
            return ans;
        }
        return new ArrayList<PaymentsDTO>();
    }

    /// /            Payments getUpdated = save.getPaymentDetails();
    /// /
    /// /            PaymentDetails paymentDetails1 = new PaymentDetails();
    /// /            paymentDetails1.setOrderId(save.get_id());
    /// /            paymentDetails1.setPayments(new ArrayList<>(Collections.singletonList(getUpdated)));
    /// /
    /// /            mongoTemplate.withSession(session).save(paymentDetails1);
//
//            mongoTemplate.withSession(session).save(save);
//
//            log.info("Execute completed successfully");
//
//            session.commitTransaction();
//            return SellerAdaptor.ToSaveResponse(save.getRefno(), checkedItems, save.get_id(), save.getIsgst(), save.getIspriceInclusive(), String.valueOf(save.getOrderDate()), save);
//
//        } catch (DuplicateKeyException e) {
//            log.error("error : " + e);
//            abortSession(session, "DuplicateKeyException");
//            return SellerAdaptor.ToSaveResponse(null, null, null, null, null, null, null);
//        } catch (Exception e) {
//            log.error("Execute error: " + e);
//            session.abortTransaction(); // rollback if using outer session
//            e.printStackTrace();
//            return SellerAdaptor.ToSaveResponse(null, null, null, null, null, null, null);
//        } finally {
//            session.close(); // close only if Execute created session
//        }
//    }
    @Transactional
    public SaveOrderResponse AcceptReviewOrders(ExecuteOrderRequest executeOrderRequest) throws JsonProcessingException {
        ClientSession session = null;
        try {
            session = mongoClient.startSession();
            session.startTransaction();

            Save save = null;

            if (StringUtils.hasText(executeOrderRequest.save_id)) {
                try {
                    ObjectId saveId = new ObjectId(executeOrderRequest.save_id);
                    save = mongoTemplate.withSession(session).findOne(Query.query(Criteria.where("_id").is(saveId)), Save.class);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Invalid save_id format");
                }
            }

            if (save == null) {
                log.error("No Save found for refno: {}", executeOrderRequest.refno);
                session.abortTransaction();
                throw new RuntimeException("Save not found for refno " + executeOrderRequest.refno);
            }

            List<InventoryDTO> inventoryDTOs = Optional.ofNullable(executeOrderRequest.inventory).orElseGet(ArrayList::new);

            List<ObjectId> inventoryIds = inventoryDTOs.stream()
                    .filter(i -> i._id != null)
                    .map(i -> new ObjectId(i._id))
                    .collect(Collectors.toList());

            List<ObjectId> productIds = inventoryDTOs.stream()
                    .filter(i -> i.productid != null)
                    .map(i -> i.productid)
                    .collect(Collectors.toList());

            List<Inventory> inventories = mongoTemplate.withSession(session).find(
                    Query.query(Criteria.where("_id").in(inventoryIds)), Inventory.class);

            log.info("Inventory size: {}", inventories.size());

            if (executeOrderRequest.companyid == null) {
                log.error("companyid is null in ExecuteOrderRequest");
                session.abortTransaction();
                return SellerAdaptor.ToSaveResponse(null, null, null, null, null, null, null);
            }

            List<Batches> batches = mongoTemplate.withSession(session).find(
                    Query.query(Criteria.where("companyid")
                            .is(executeOrderRequest.companyid.toString())
                            .and("productid").in(productIds)), Batches.class);

            log.info("Batches size: {}", batches.size());

            List<InventoryDTO> productInventory = inventoryDTOs.stream()
                    .filter(i -> i.flag == null || !"isService".equals(i.flag))
                    .collect(Collectors.toList());

            List<InventoryDTO> serviceInventory = inventoryDTOs.stream()
                    .filter(i -> "isService".equals(i.flag))
                    .collect(Collectors.toList());

            log.info("Productinventory size: {}", productInventory.size());
            log.info("Serviceinventory size: {}", serviceInventory.size());

            if (save.getSeller_items() != null) {
                save.getSeller_items().removeIf(item -> {
                    if (item.getFlag() == null) {
                        return productInventory.stream().noneMatch(i ->
                                i.flag == null && i.get_id() != null && i.get_id().equals(item.get_id()));
                    }
                    return false;
                });

                save.getSeller_items().removeIf(item -> {
                    if ("isService".equals(item.getFlag())) {
                        return serviceInventory.stream().noneMatch(i ->
                                "isService".equals(i.flag)
                                        && Objects.equals(i.get_id(), item.get_id())
                                        && isSame(i.getPropertyAttributes(), item.getPropertyAttributes()));
                    }
                    return false;
                });
            }

            // QUANTITY CHANGE REASON
            for (InventoryDTO inventoryDTO : inventoryDTOs) {
                if (StringUtils.hasText(inventoryDTO.quantityChangeReason)) {
                    if (save.getSeller_items() == null) continue;

                    for (Inventory item : save.getSeller_items()) {
                        if (inventoryDTO._id != null && inventoryDTO._id.equals(item.get_id())) {
                            item.setReason_for_QtyChanges(inventoryDTO.quantityChangeReason);
                        }
                    }
                }
            }

            // UPDATE QUANTITY
            if (save.getSeller_items() != null) {
                for (Inventory item : save.getSeller_items()) {

                    // PRODUCT
                    if (item.getFlag() == null && item.getUniqueID() != null) {
                        productInventory.stream()
                                .filter(i -> i._id != null && i._id.equals(item.get_id()))
                                .findFirst()
                                .ifPresent(matchingInventory -> {
                                    Integer newQty = matchingInventory.getOrder_quantity();
                                    Integer currentQty = item.getOrder_quantity();

                                    log.info("currentQty: {}", currentQty);
                                    log.info("newQty: {}", newQty);

                                    if (!Objects.equals(currentQty, newQty)) {
                                        item.setOrder_quantity(newQty);
                                    }
                                });
                    }

                    // SERVICE
                    else if ("isService".equals(item.getFlag()) && item.getUniqueID() != null) {
                        serviceInventory.stream()
                                .filter(i -> "isService".equals(i.flag)
                                        && i.uniqueID != null
                                        && i.uniqueID.equals(item.getUniqueID()))
                                .findFirst()
                                .ifPresent(matchingInventory -> {
                                    Integer newQty = matchingInventory.getOrder_quantity();
                                    Integer currentQty = item.getOrder_quantity();

                                    log.info("currentQty: {}", currentQty);
                                    log.info("newQty: {}", newQty);

                                    if (!Objects.equals(currentQty, newQty)) {
                                        item.setOrder_quantity(newQty);
                                    }
                                });
                    }
                }
            }

            // DATE
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

            if (!save.isDelivered() && executeOrderRequest.isDelivered) {
                save.setDeliveryDate(LocalDateTime.now().format(formatter));
            }

            List<InventoryDTO> checkedItems = Precheck(inventories, productInventory, false);
            log.info("CheckedItems size: {}", checkedItems.size());

            List<InventoryDTO> validItems = checkedItems.stream()
                    .filter(item -> item.getMessage() == null || item.getMessage().isEmpty())
                    .collect(Collectors.toList());

            log.info("Validitems size: {}", validItems.size());

            if (validItems.size() != checkedItems.size()) {
                session.abortTransaction();
                return SellerAdaptor.ToSaveResponse(null, checkedItems, null, null, null, null, null);
            }

            // UPDATE STOCK
            for (InventoryDTO dto : validItems) {
                for (Inventory inv : inventories) {
                    if (!Objects.equals(dto.getProductid(), inv.getProductid())) continue;
                    if (!Objects.equals(dto.getCompanyid(), inv.getCompanyid())) continue;

                    Batches matchedBatch = null;

                    // NORMAL BATCH
                    if (inv.getBatchid() != null
                            && inv.getBatchid().get_id() != null
                            && inv.getBatchid().get_id().equals(dto.batch_id)) {
                        matchedBatch = inv.getBatchid();
                    }

                    // VARIANT BATCH
                    if (matchedBatch == null && inv.getVariantBatches() != null && !inv.getVariantBatches().isEmpty()) {
                        matchedBatch = inv.getVariantBatches().stream()
                                .filter(batch -> batch.get_id() != null && batch.get_id().equals(dto.batch_id))
                                .findFirst()
                                .orElse(null);
                    }

                    if (matchedBatch == null) continue;

                    int existingStock = matchedBatch.getStock_availability();
                    int remaining = existingStock - dto.getOrder_quantity();

                    log.info("Existing Stock: {}", existingStock);
                    log.info("Remaining Stock: {}", remaining);

                    if (remaining <= 0) {
                        log.info("Entering Soldout");
                        soldout(matchedBatch, inv, batches);
                        continue;
                    }

                    // UPDATE BATCH
                    if (batches != null && !batches.isEmpty()) {
                        final Batches finalMatchedBatch = matchedBatch;

                        Batches batch = batches.stream()
                                .filter(b -> Boolean.TRUE.equals(b.getActive())
                                        && Objects.equals(b.get_id(), finalMatchedBatch.get_id())
                                        && Objects.equals(b.getCompanyid(), finalMatchedBatch.getCompanyid())
                                        && Objects.equals(b.getProductid(), inv.getProductid()))
                                .findFirst()
                                .orElse(null);

                        if (batch == null) continue;

                        log.info("Batch found: {}", batch.getProductname());

                        batch.setStock_availability(remaining);
                        mongoTemplate.withSession(session).save(batch);

                        if (inv.getBatchid() != null && Objects.equals(inv.getBatchid().get_id(), batch.get_id())) {
                            inv.setBatchid(batch);
                        }

                        if (inv.getVariantBatches() != null && !inv.getVariantBatches().isEmpty()) {
                            List<Batches> updatedVariants = inv.getVariantBatches().stream()
                                    .map(v -> Objects.equals(v.get_id(), batch.get_id()) ? batch : v)
                                    .collect(Collectors.toList());

                            inv.setVariantBatches(updatedVariants);
                        }

                        mongoTemplate.withSession(session).save(inv);
                    }
                }
            }

            save.setDelivered(executeOrderRequest.isDelivered);
            save.setDeliveryDueDate(executeOrderRequest.deliveryDueDate);
            save.setUpdatedAt(LocalDateTime.now().format(formatter));

            Payments newPayment = null;

            if (executeOrderRequest.paymentsDTO != null) {
                newPayment = SellerAdaptor.fromPaymentsDTOtoPayments(executeOrderRequest.paymentsDTO);

                if (executeOrderRequest.payment_mode != null) {
                    newPayment.setPayment_mode(executeOrderRequest.payment_mode);
                }

                newPayment.setPayment_date(LocalDateTime.now().toString());

                log.info("New payment created for orderId={}: {}", save.get_id(), newPayment);
            }

            // PAYMENT MODE
            if (executeOrderRequest.paymentsDTO != null && !"COD".equals(executeOrderRequest.paymentsDTO.payment_mode)) {
                save.setPayment_mode(executeOrderRequest.payment_mode);
            }

            // ORDER VALUE
            save.setTotal_Amount(executeOrderRequest.ordervalue);

            // PAYMENT DISCOUNT
            if (newPayment != null) {
                double discount = newPayment.getDiscount();
                save.setTotal_Discount(save.getTotal_Discount() + discount);

                if (save.getPaymentDetails() == null) {
                    save.setPaymentDetails(newPayment);
                } else {
                    Payments currentPayment = save.getPaymentDetails();
                    currentPayment.setDiscount(discount);
                    currentPayment.setTotal_amount(executeOrderRequest.ordervalue);

                    if (newPayment.getPayment_mode() != null) {
                        currentPayment.setPayment_mode(newPayment.getPayment_mode());
                    }

                    currentPayment.setPayment_date(newPayment.getPayment_date());
                    log.info("Save paymentDetails updated");
                }
            }

            // AMOUNT DUE / REFUND
            double totalAmount = executeOrderRequest.ordervalue;
            double totalPaid = save.getTotal_Paid();
            double totalDiscount = save.getTotal_Discount();

            if (totalPaid < totalAmount) {
                double remaining = totalAmount - totalPaid - totalDiscount;
                remaining = Math.max(remaining, 0);

                save.setTotal_AmountDue(remaining);

                if (save.getPaymentDetails() != null) {
                    save.getPaymentDetails().setAmountDue(remaining);
                }
            } else if (totalPaid > totalAmount) {
                double refund = totalPaid - totalAmount;

                save.setTotal_Refund_amount(refund);
                save.setTotal_AmountDue(0.0);

                if (save.getPaymentDetails() != null) {
                    save.getPaymentDetails().setAmountDue(0.0);
                }
            } else {
                save.setTotal_AmountDue(0.0);
            }

            // MARK AS PAID
            if (executeOrderRequest.isMarkAsPaid) {
                save.setPayment_status("Paid");

                if (save.getPaymentDetails() != null) {
                    save.getPaymentDetails().setPayment_status("Paid");
                    save.getPaymentDetails().setAmountDue(0.0);
                    save.getPaymentDetails().setAdvanceAmt(0.0);
                    save.getPaymentDetails().setCurrent_payment_amount(executeOrderRequest.ordervalue);
                    save.getPaymentDetails().setPaid_amount(executeOrderRequest.ordervalue);
                }
                if (newPayment != null) {
                    newPayment.setCurrent_payment_amount(executeOrderRequest.ordervalue);
                    newPayment.setPaid_amount(executeOrderRequest.ordervalue);
                    newPayment.setAdvanceAmt(0.0);
                    newPayment.setAmountDue(0.0);
                }

                save.setTotal_AmountDue(0.0);
                save.setTotal_Paid(executeOrderRequest.ordervalue);
            } else {
                if (save.getTotal_Amount() != save.getTotal_Paid()) {
                    save.setPayment_status("Partially Paid");
                } else if (save.getTotal_Refund_amount() > 0) {
                    save.setPayment_status("Refund to be processed");
                } else if ("COD".equals(save.getPayment_mode())) {
                    save.setPayment_status("UnPaid");
                } else {
                    save.setPayment_status("Paid");
                }
            }

            // DELIVERY
            save.setDelivery_details(executeOrderRequest.address);
            save.setDelivery_mode(executeOrderRequest.delivery_mode);
            save.setOrderStatus("In-Progress");

            // DELETED INVENTORIES
            if (executeOrderRequest.deletedInventories != null) {
                save.setDeletedInventories(executeOrderRequest.deletedInventories);
            }

            // ACCEPT REVIEW DETAILS
            AcceptReviewInfo acceptReviewInfo = new AcceptReviewInfo();
            double refund = 0.0;
            double needtoPay = 0.0;

            if (save.getTotal_Paid() - executeOrderRequest.ordervalue > 0) {
                refund = save.getTotal_Paid() - executeOrderRequest.ordervalue;
            } else {
                needtoPay = executeOrderRequest.ordervalue - save.getTotal_Paid();
            }

            if (executeOrderRequest.isMarkAsPaid) {
                needtoPay = 0.0;
            }

            acceptReviewInfo.setRefund_amount(refund);
            acceptReviewInfo.setCustomer_need_to_pay(needtoPay);

            ZoneId istZone = ZoneId.of("Asia/Kolkata");
            Date istDate = Date.from(ZonedDateTime.now(istZone).toInstant());
            acceptReviewInfo.setAction_date(istDate);

            if (acceptReviewInfo.getRefund_amount() > 0) {
                acceptReviewInfo.setRefund_status("To be processed");
            }

            AcceptReviewDetails acceptReviewDetails = new AcceptReviewDetails(Collections.singletonList(acceptReviewInfo));
            acceptReviewDetails = acceptReviewDetailsRepo.save(acceptReviewDetails);
            save.setAcceptReviewDetails(acceptReviewDetails);

            // PAYMENT DETAILS HISTORY
            if (newPayment != null) {
                List<PaymentDetails> paymentDetailsList = mongoTemplate.withSession(session).find(
                        Query.query(Criteria.where("orderId").is(save.get_id())), PaymentDetails.class);

                PaymentDetails paymentDetails;

                if (paymentDetailsList == null || paymentDetailsList.isEmpty()) {
                    paymentDetails = new PaymentDetails();
                    paymentDetails.setOrderId(save.get_id());
                    paymentDetails.setPayments(new ArrayList<>());
                } else {
                    paymentDetails = paymentDetailsList.get(0);

                    if (paymentDetails.getPayments() == null) {
                        paymentDetails.setPayments(new ArrayList<>());
                    }

                    if (paymentDetailsList.size() > 1) {
                        log.warn("DUPLICATE PaymentDetails found. orderId={}, count={}", save.get_id(), paymentDetailsList.size());
                    }
                }

                paymentDetails.getPayments().add(newPayment);
                mongoTemplate.withSession(session).save(paymentDetails);

                log.info("PaymentDetails updated. orderId={}, paymentHistoryCount={}", save.get_id(), paymentDetails.getPayments().size());
            }

            // SAVE ORDER
            mongoTemplate.withSession(session).save(save);
            log.info("Execute completed successfully");

            // COMMIT
            session.commitTransaction();

            return SellerAdaptor.ToSaveResponse(
                    save.getRefno(),
                    checkedItems,
                    save.get_id(),
                    save.getIsgst(),
                    save.getIspriceInclusive(),
                    String.valueOf(save.getOrderDate()),
                    save
            );

        } catch (DuplicateKeyException e) {
            log.error("DuplicateKeyException in AcceptReviewOrders", e);
            abortSession(session, "DuplicateKeyException");

            return SellerAdaptor.ToSaveResponse(null, null, null, null, null, null, null);

        } catch (Exception e) {
            log.error("AcceptReviewOrders Execute error", e);

            if (session != null) {
                try {
                    if (session.hasActiveTransaction()) {
                        session.abortTransaction();
                    }
                } catch (Exception abortEx) {
                    log.error("Failed to abort transaction", abortEx);
                }
            }

            return SellerAdaptor.ToSaveResponse(null, null, null, null, null, null, null);

        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public String RejectReviewOrders(RejectReviewOrdersRequest rejectReviewOrdersRequest) {

        Optional<Save> saveDB = saveOrderRepo.findById(String.valueOf(rejectReviewOrdersRequest._id));

        if (saveDB.isEmpty()) {
            return "Order not found";
        }

        Save save = saveDB.get();

        boolean isRefundOnlyFlow =
                rejectReviewOrdersRequest.refund_status != null
                        && rejectReviewOrdersRequest.refund_status.equals("Refunded")
                        && rejectReviewOrdersRequest.reason == null;

        ZoneId istZone = ZoneId.of("Asia/Kolkata");
        Date istDate = Date.from(ZonedDateTime.now(istZone).toInstant());

        AcceptReviewInfo acceptReviewInfo = new AcceptReviewInfo();

        if (isRefundOnlyFlow) {
            save.getPaymentDetails().setAmountDue(0.0);
            AcceptReviewDetails existingDetails = save.getAcceptReviewDetails();
            if (existingDetails == null
                    || existingDetails.getAcceptReviewInfos() == null
                    || existingDetails.getAcceptReviewInfos().isEmpty()) {
                return "No existing review info found to update refund status";
            }

            List<AcceptReviewInfo> existingInfos = existingDetails.getAcceptReviewInfos();
            AcceptReviewInfo lastInfo = existingInfos.get(existingInfos.size() - 1);

            acceptReviewInfo.setReason(lastInfo.getReason());
            acceptReviewInfo.setRefund_amount(lastInfo.getRefund_amount());
            acceptReviewInfo.setAction_date(istDate);
            acceptReviewInfo.setRefund_status(rejectReviewOrdersRequest.refund_status);
            acceptReviewInfo.setRefund_payment_mode(lastInfo.getRefund_payment_mode());

        } else {
            // Genuine "Reject Order" flow
            save.setOrderStatus("Cancelled");
            save.setReason_FullyRejectedOrders(rejectReviewOrdersRequest.reason);

            acceptReviewInfo.setReason(rejectReviewOrdersRequest.reason);
            acceptReviewInfo.setRefund_amount(rejectReviewOrdersRequest.refund_amount);
            acceptReviewInfo.setAction_date(istDate);
            acceptReviewInfo.setRefund_status(rejectReviewOrdersRequest.refund_status);
            acceptReviewInfo.setRefund_payment_mode(rejectReviewOrdersRequest.refund_payment_mode);
        }

        // Append to history instead of overwriting it
        AcceptReviewDetails acceptReviewDetails = save.getAcceptReviewDetails();
        if (acceptReviewDetails == null) {
            acceptReviewDetails = new AcceptReviewDetails(new ArrayList<>());
        }
        if (acceptReviewDetails.getAcceptReviewInfos() == null) {
            acceptReviewDetails.setAcceptReviewInfos(new ArrayList<>());
        }
        acceptReviewDetails.getAcceptReviewInfos().add(acceptReviewInfo);

        acceptReviewDetails = acceptReviewDetailsRepo.save(acceptReviewDetails);

        save.setAcceptReviewDetails(acceptReviewDetails);
        save.setPayment_status(rejectReviewOrdersRequest.refund_status);

        if (rejectReviewOrdersRequest.refund_status != null
                && rejectReviewOrdersRequest.refund_status.equals("Refunded")) {
            save.setTotal_AmountDue(0.0);
        }

        saveOrderRepo.save(save);
        return "Order rejected successfully";
    }

    public List<BuyerDetailsResponse> searchBuyerByMobile(String value, ObjectId sellerid) {
        if (value == null || value.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String searchValue = value.trim();

        List<Buyer> buyerMobile = new ArrayList<>();
        List<Buyer> buyerCustomer = new ArrayList<>();

        boolean isMobileNumber = searchValue.matches("\\d{10}");

        if (isMobileNumber) {
            buyerMobile = buyerRepo.findByMobileNumContainingAndBuyerSiteid(
                    searchValue,
                    sellerid
            );
        } else {
            buyerCustomer = buyerRepo.findByCustomerNameContainingAndBuyerSiteid(
                    searchValue,
                    sellerid
            );
        }

        Map<ObjectId, Buyer> merged = new LinkedHashMap<>();

        for (Buyer buyer : buyerMobile) {
            if (buyer != null && buyer.getId() != null) {
                merged.put(new ObjectId(buyer.getId()), buyer);
            }
        }

        for (Buyer buyer : buyerCustomer) {
            if (buyer != null && buyer.getId() != null) {
                merged.put(new ObjectId(buyer.getId()), buyer);
            }
        }

        List<BuyerDetailsResponse> ans = new ArrayList<>();

        for (Buyer buyer : merged.values()) {

            BuyerDetailsResponse res = new BuyerDetailsResponse();

            res._id = buyer.getId();
            res.firstName = buyer.getFirstName();
            res.mobileNum = buyer.getMobileNum();

            if (buyer.getRewards() != null) {
                res.currentRewards = buyer.getRewards().getCurrentRewards();
                res.lastEarned = buyer.getRewards().getLastEarned();
                res.lastRedeemed = buyer.getRewards().getLastRedeemed();
                res.totalRedeemed = buyer.getRewards().getTotalRedeemed();
                res.totalRewards = buyer.getRewards().getTotalRewards();
            }

            ans.add(res);
        }

        return ans;
    }


    public boolean isValidNameForProductAndService(String name, String companyid, boolean isService) {
        log.info("Name : " + name);
        if (companyid == null || companyid.isBlank()) return false;
        if (isService) {
            ServiceInventory serviceInventory = serviceInventoryRepo.findByServicenameAndSellerid(name, new ObjectId(companyid));
            if (serviceInventory == null) return true;
        } else {
            Inventory inventory = inventoryRepo.findByProductName(name, companyid);
            if (inventory == null) return true;
        }
        return false;
    }

    public void deleteAllData() {
        sellerUserRepo.deleteAll();
        sellerRepo.deleteAll();
    }

    public List<ProductSuggestDTO> searchProducts(String searchValue, String sellerId, boolean isQI) {

        List<Criteria> andCriteria = new ArrayList<>();

        if (StringUtils.hasText(sellerId)) {
            andCriteria.add(Criteria.where("seller_id").is(sellerId));
        }

        if (StringUtils.hasText(searchValue)) {
            // escape regex special chars from user input
            String safe = Pattern.quote(searchValue.trim());

            Criteria textMatch = new Criteria().orOperator(
                    Criteria.where("productName").regex(safe, "i")
//                    Criteria.where("productDescription").regex(safe, "i")
            );
            andCriteria.add(textMatch);
        }

        Criteria finalCriteria = new Criteria();
        if (!andCriteria.isEmpty()) {
            finalCriteria = finalCriteria.andOperator(
                    andCriteria.toArray(new Criteria[0])
            );
        }

        Query query = new Query(finalCriteria)
                .limit(10)
                .with(Sort.by(Sort.Direction.ASC, "productName"));
        System.out.println("Mongo query: " + query.getQueryObject().toJson());

        List<ProductSuggest> docs = mongoTemplate.find(query, ProductSuggest.class, "products_suggest");
        System.out.println("Matched count: " + docs.size());


        return docs.stream()
                .map(p -> new ProductSuggestDTO(
                        p.getId(),
                        p.getProductId(),
                        p.getProductName(),
                        p.getSellerprice(),
                        p.getOfferprice()
                ))
                .collect(Collectors.toList());
    }

    public boolean DeleteByInventoryId(ObjectId inventoryId) {
        if (inventoryId == null) return false;

        Optional<Inventory> inventoryOpt = inventoryRepo.findById(String.valueOf(inventoryId));
        if (!inventoryOpt.isPresent()) return false;

        Inventory inventory = inventoryOpt.get();

        Optional<Products> productsOpt = productRepo.findById(String.valueOf(inventory.getProductid()));
        if (!productsOpt.isPresent()) return false;

        Products products = productsOpt.get();
        ObjectId companyId = new ObjectId(inventory.getCompanyid());

        List<SellerList> updatedSellerLists = products.getSellerLists().stream()
                .filter(sl -> sl.getCompanyid() != null && !sl.getCompanyid().equals(companyId))
                .collect(Collectors.toList());

        products.setSellerLists(updatedSellerLists);
        productRepo.save(products);

        inventory.setActive(false);
        inventoryRepo.save(inventory);

        return true;
    }


    /// /            if (remaining > 0 && request.seeProd == 1) {
    /// /
    /// /                List<AggregationOperation> productPipeline = new ArrayList<>();
    /// /
    /// /                Criteria searchCriteria = Criteria.where("productName")
    /// /                        .regex(".*" + Pattern.quote(request.searchValue.trim()) + ".*", "i");
    /// /
    /// /                Criteria companyCriteria = Criteria.where("sellerLists")
    /// /                        .elemMatch(
    /// /                                Criteria.where("companyid")
    /// /                                        .is(new ObjectId(request.companyId.trim()))
    /// /                        );
    /// /
    /// /                Criteria excludeInventoryCriteria = Criteria.where("_id")
    /// /                        .nin(inventoryProductIds);
    /// /
    /// /                Criteria finalCriteria = new Criteria().andOperator(
    /// /                        searchCriteria,
    /// /                        companyCriteria,
    /// /                        excludeInventoryCriteria
    /// /                );
    /// /
    /// /
    /// /                productPipeline.add(Aggregation.match(finalCriteria));
    /// /
    /// /                productPipeline.add(
    /// /                        project()
    /// /                                .and("_id").as("_id")
    /// /                                .and("_id").as("ProductId")
    /// /                                .and("productCategory").as("productCategoryDTO")
    /// /                                .and("Quickadd").as("Quickadd")
    /// /                                .and("productName").as("ProductName")
    /// /                                .and("defaultImage").as("defaultImage")
    /// /                                .and("created_at").as("CreatedAt")
    /// /                                .and("productCategory._id").as("Category.CategoryId")
    /// /                                .and("productCategory.category_name").as("Category.CategoryName")
    /// /                                .and("unit").as("unit")
    /// /                );
    /// /
    /// /                productPipeline.add(sort(Sort.by("ProductName")));
    /// /                productPipeline.add(skip((long) request.prodTaken));
    /// /                productPipeline.add(limit(remaining));
    /// /
    /// /                if (!productPipeline.isEmpty()) {
    /// /                    List<InitialGetAllProductsResponse.ProductItem> productList =
    /// /                            mongoTemplate.aggregate(
    /// /                                    newAggregation(productPipeline),
    /// /                                    "products",
    /// /                                    InitialGetAllProductsResponse.ProductItem.class
    /// /                            ).getMappedResults();
    /// /
    /// /                    finalResult.addAll(productList);
    /// /                    AlreadyTaken_Prod += productList.size();
    /// /                }
    /// /
    /// /            }
//            if (remaining > 0 && request.seeProd == 1) {
//
//                List<AggregationOperation> productPipeline = new ArrayList<>();
//                List<Criteria> criteriaList = new ArrayList<>();
//
//                // Search
//                criteriaList.add(
//                        Criteria.where("productName")
//                                .regex(".*" + Pattern.quote(request.searchValue.trim()) + ".*", "i")
//                );
//
//                // Company
//                criteriaList.add(
//                        Criteria.where("sellerLists")
//                                .elemMatch(
//                                        Criteria.where("companyid")
//                                                .is(new ObjectId(request.companyId.trim()))
//                                )
//                );
//
//                // Category Filter
//                if (request.categories != null && !request.categories.isEmpty()) {
//                    criteriaList.add(
//                            Criteria.where("productCategory._id")
//                                    .in(request.categories.stream()
//                                            .map(ObjectId::new)
//                                            .toList())
//                    );
//                }
//
//                // Exclude Inventory Products
//                criteriaList.add(
//                        Criteria.where("_id")
//                                .nin(inventoryProductIds)
//                );
//
//                Criteria finalCriteria = new Criteria().andOperator(
//                        criteriaList.toArray(new Criteria[0])
//                );
//
//                productPipeline.add(Aggregation.match(finalCriteria));
//
//                productPipeline.add(
//                        project()
//                                .and("_id").as("_id")
//                                .and("_id").as("ProductId")
//                                .and("productCategory").as("productCategoryDTO")
//                                .and("Quickadd").as("Quickadd")
//                                .and("productName").as("ProductName")
//                                .and("defaultImage").as("defaultImage")
//                                .and("created_at").as("CreatedAt")
//                                .and("productCategory._id").as("Category.CategoryId")
//                                .and("productCategory.category_name").as("Category.CategoryName")
//                                .and("unit").as("unit")
//                );
//
//                productPipeline.add(sort(Sort.by("ProductName")));
//                productPipeline.add(skip((long) request.prodTaken));
//                productPipeline.add(limit(remaining));
//
//                List<InitialGetAllProductsResponse.ProductItem> productList =
//                        mongoTemplate.aggregate(
//                                newAggregation(productPipeline),
//                                "products",
//                                InitialGetAllProductsResponse.ProductItem.class
//                        ).getMappedResults();
//
//                finalResult.addAll(productList);
//                AlreadyTaken_Prod += productList.size();
//            }
//
//
//            resultList = finalResult;
//        } else {
//            resultList = new ArrayList<>();
//            response.setProducts(Collections.emptyList());
//            return response;
//        }
//
//        response.setProducts(resultList);
//        response.invTaken = AlreadyTaken_Inv;
//        response.prodTaken = AlreadyTaken_Prod;
//        response.servTaken = AlreadyTaken_Serv;
//
//        response.seeInv = AlreadyTaken_Inv >= Initial_Inv ? 0 : 1;
//        response.seeProd = AlreadyTaken_Prod >= Initial_Prod ? 0 : 1;
//        response.seeServ = AlreadyTaken_Serv >= Initial_Serv ? 0 : 1;
//        return response;
//    }


    private enum StockFilter {
        NONE, OUT_OF_STOCK, BELOW_STOCK, ABOVE_STOCK;

        static StockFilter parse(String raw) {
            if (raw == null) return NONE;
            String normalized = raw.trim().toUpperCase().replace("_", "").replace(" ", "");
            return switch (normalized) {
                case "OUTOFSTOCK" -> OUT_OF_STOCK;
                case "BELOWSTOCK" -> BELOW_STOCK;
                case "ABOVESTOCK" -> ABOVE_STOCK;
                default -> NONE;
            };
        }
    }


}

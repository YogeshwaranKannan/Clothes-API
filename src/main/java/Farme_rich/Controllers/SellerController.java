package Farme_rich.Controllers;

import Farme_rich.ErrorHandling.ExceptionHandling;
import Farme_rich.Seller.Adaptor.SellerAdaptor;
import Farme_rich.Seller.BL.ProductCategoryService;
import Farme_rich.Seller.BL.SellerService;
import Farme_rich.Seller.DTO.Request.*;
import Farme_rich.Seller.DTO.Response.*;
import Farme_rich.Seller.Model.BackEnd.Batches;
import Farme_rich.Seller.Model.BackEnd.ProductCategory;
import Farme_rich.Seller.Model.FrontEnd.Seller;
import Farme_rich.Seller.Model.FrontEnd.User;
import Farme_rich.Seller.Repo.BackEnd.BatchesRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/seller")
@Configuration
@EnableTransactionManagement
public class SellerController {

    public static final String Product_BASE_PATH = "uploads/uploadsProductImages/";
    public static final String Service_BASE_PATH = "uploads/uploadsServiceImages/";
    private static final Logger log = LoggerFactory.getLogger(SellerController.class);
    private final SellerService sellerService;
    private final BatchesRepo batchesRepo;
    private final ProductCategoryService productCategoryService;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${app.baseUrl}")
    private String baseUrl;
    @Value("${app.upload-dir}")
    private String uploadDir;

    @Autowired
    public SellerController(SellerService sellerService, BatchesRepo batchesRepo, ProductCategoryService productCategoryService) {
        this.sellerService = sellerService;
        this.productCategoryService = productCategoryService;
        this.batchesRepo = batchesRepo;
    }

    @PostMapping("/deviceId")
    public UserSignupResponse HandleDeviceId(@RequestBody UserSignupRequest userSignupRequest) {
        log.info("controller deviceid :", userSignupRequest.getDeviceId());

        UserSignupResponse response = null;
        try {
            User user = SellerAdaptor.ToUserSignup(userSignupRequest);
            return sellerService.HandleDeviceId(user);
        } catch (Exception ex) {
            log.error("Error in HandleDeviceId : ", ex);
            response = new UserSignupResponse();
            response.ResponseMessage = new MessageResponse();
            response.ResponseMessage.setErrorMsg(ExceptionHandling.GetFullExceptionDetails(ex));
        }
        return response;
    }

    @PostMapping("/profile")
    public UserSignupResponse RegisterProfile(@RequestBody UserSignupRequest userSignupRequest) throws JsonProcessingException {
        log.info("Search Product Request : \n{}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(userSignupRequest));


        UserSignupResponse response = null;
        try {
            User user = SellerAdaptor.ToUserSignup(userSignupRequest);
            log.info("User prefer lang: ", user.getLanguage_preferred());
            Seller seller = SellerAdaptor.ToUserLogin(userSignupRequest);
            return sellerService.RegisterProfile(user, seller, userSignupRequest.getValidationCode());
        } catch (Exception ex) {
            log.error("Error in RegisterProfile : ", ex);
            response = new UserSignupResponse();
            response.ResponseMessage = new MessageResponse();
            response.ResponseMessage.setErrorMsg(ExceptionHandling.GetFullExceptionDetails(ex));
        }
        return response;
    }

    @PostMapping("/signupvalidate")
    public UserSignupResponse SignupValidate(@RequestBody UserSignupRequest userSignupRequest) throws JsonProcessingException {
        log.info(" Request : \n{}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(userSignupRequest));

        UserSignupResponse response = null;
        try {
            User user = SellerAdaptor.ToUserSignup(userSignupRequest);
            return sellerService.SignupValidate(user);
        } catch (Exception ex) {
            log.error("Error in SignupValidate : ", ex);
            response = new UserSignupResponse();
            response.ResponseMessage = new MessageResponse();
            response.ResponseMessage.setErrorMsg(ExceptionHandling.GetFullExceptionDetails(ex));
        }
        return response;
    }

    @PostMapping("/checkpasscode")
    public UserSignupResponse CheckPasscode(@RequestBody UserSignupRequest userSignupRequest) throws JsonProcessingException {
        log.info("Search Product Request : \n{}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(userSignupRequest));

        UserSignupResponse response = null;
        try {
            User user = SellerAdaptor.ToUserSignup(userSignupRequest);
            return sellerService.CheckPasscode(user);
        } catch (Exception ex) {
            log.error("Error in CheckPasscode : ", ex);
            response = new UserSignupResponse();
            response.ResponseMessage = new MessageResponse();
            response.ResponseMessage.setErrorMsg(ExceptionHandling.GetFullExceptionDetails(ex));
        }
        return response;
    }

    @PostMapping("/isPaidUser")
    public UserSignupResponse isPaidUser(@RequestBody UserSignupRequest userSignupRequest) {

        UserSignupResponse response = null;
        try {
            User user = SellerAdaptor.ToUserSignup(userSignupRequest);
            return sellerService.HandleDeviceId(user);
        } catch (Exception ex) {
            log.error("Error in isPaidUser : ", ex);
            response = new UserSignupResponse();
            response.ResponseMessage = new MessageResponse();
            response.ResponseMessage.setErrorMsg(ExceptionHandling.GetFullExceptionDetails(ex));
        }
        return response;
    }

    @PostMapping("/search")
    public List<SellerProductResponse> SearchProducts(@RequestBody SellerProductRequest sellerProductRequest) {
        log.info("CompanyId in SearchProducts : ", sellerProductRequest.getCompanynameid());
        try {
            return sellerService.SearchProductsByName(sellerProductRequest);
        } catch (Exception ex) {
            log.error("Error in SearchProducts : ", ex);
        }
        return new ArrayList<SellerProductResponse>();
    }

//    @PostMapping("/getAllProductsForSeller")
//    public InitialGetAllProductsResponse InitialGetAllProducts(@RequestBody getSearchProductsRequest request) {
//        return sellerService.getAllProductsForSeller(request);
//    }

    @PostMapping("/searchServiceInventory")
    public List<InitialServiceInventoryServiceInventoryResponse> searchServiceInventory(@RequestBody SellerProductRequest sellerProductRequest) {
        try {
            return sellerService.searchServiceInventory(sellerProductRequest);
        } catch (Exception ex) {
            log.error("Error in searchServiceInventory : ", ex);
        }
        return new ArrayList<InitialServiceInventoryServiceInventoryResponse>();
    }

    @PostMapping("/BothSearchServiceInventoryAndProductInventory")
    public BothSearchServiceInventoryandProductInventoryResponse BothSearchServiceInventoryandProductInventory(@RequestBody SellerProductRequest sellerProductRequest) {
        try {
            return sellerService.BothSearchServiceInventoryandProductInventory(sellerProductRequest);
        } catch (Exception ex) {
            log.error("Error in BothSearchServiceInventoryandProductInventory : ", ex);
        }
        return new BothSearchServiceInventoryandProductInventoryResponse();
    }

    //    PreAuthorize("hasAnyRole('PAID', 'GUEST')")
    @PostMapping("/suggest")
    public List<ProductSuggestionDTO> SuggestProducts(@RequestBody SellerProductRequest sellerProductRequest) {
        log.info("CompanyId in SuggestProducts: " + sellerProductRequest.getCompanynameid());
        log.info("Categories : " + sellerProductRequest.categories);
        try {
            return sellerService.SuggestProductsByName(sellerProductRequest);
        } catch (Exception ex) {
            log.error("Error in SuggestProducts : ", ex);
        }
        return new ArrayList<ProductSuggestionDTO>();
    }

    @PostMapping("/initialOrderProductLoad")
    public List<SellerProductResponse> SearchInitial(@RequestBody SellerProductRequest sellerProductRequest) {
        System.out.println(sellerProductRequest.getCompanynameid());
        return sellerService.SearchInitial(sellerProductRequest);
    }

    @PostMapping("/searchProductsManageOrderSeller")
    public InitialGetAllProductsResponse searchProductsManageOrderSeller(@RequestBody getSearchProductsRequest request) {
        try {
            log.info("Search Product Request : \n{}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
            return sellerService.searchProductsManageOrderSeller(request);
        } catch (JsonProcessingException e) {
            log.error("Request logging failed", e);
        } catch (Exception ex) {
            log.error("Error in searchProductsManageOrderSeller : ", ex);
        }

        return new InitialGetAllProductsResponse();
    }

    @PostMapping("/searchProductsManageOrderBuyer")
    public InitialGetAllProductsResponse searchProductsManageOrderBuyer(@RequestBody getSearchProductsRequest request) {
        try {
            return sellerService.searchProductsManageOrderBuyer(request);
        } catch (Exception ex) {
            log.error("Error in searchProductsManageOrderBuyer : ", ex);
        }
        return new InitialGetAllProductsResponse();
    }

    @PostMapping("/searchProductsManageInventory")
    public InitialGetAllProductsResponse searchProductsManageInventory(@RequestBody getSearchProductsRequest request) {

        try {
            log.info("Search Product Request : \n{}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
            return sellerService.searchProductsManageInventory(request);
        } catch (Exception ex) {
            log.error("Error in searchProductsManageInventory : ", ex);
        }
        return new InitialGetAllProductsResponse();
    }

    @PostMapping("/GetViewProductDetailsById")
    public GetViewProductDetailsResponse InitialGetAllProducts(@RequestBody GetViewProductDetailsRequest request) {
        try {
            return sellerService.GetViewProductDetails(request);
        } catch (Exception ex) {
            log.error("Error in InitialGetAllProducts : ", ex);
        }
        return new GetViewProductDetailsResponse();
    }

    @PostMapping("/GetSalesFeatures")
    public GetSalesFeatureResponse getSalesFeatures(@RequestBody GetSalesFeatureRequest request) {
        try {
            return sellerService.getSalesFeatures(request);
        } catch (Exception ex) {
            log.error("Error in getSalesFeatures : ", ex);
        }
        return new GetSalesFeatureResponse();

    }

    @PostMapping("/GetCategories")
    public GetCategoriesResponse GetCategories(@RequestBody GetCategoriesRequest request) {
        log.info("Company id : " + request.sellerId);
        try {
            return sellerService.GetCategories(request);
        } catch (Exception ex) {
            log.error("Error in GetCategories : ", ex);
        }
        return new GetCategoriesResponse();
    }

    @PostMapping("/GetPromoMessage")
    public GetPromoMessageResponse GetPromoMessage(@RequestBody GetPromoMessageRequest request) {
        try {
            return sellerService.GetPromoMessage(request);
        } catch (Exception ex) {
            log.error("Error in GetPromoMessage : ", ex);
        }
        return new GetPromoMessageResponse();
    }

    @PostMapping("/initialOrderServiceLoad")
    public List<SellerServiceResponse> initialOrderServiceLoad(@RequestBody SellerServiceRequest sellerserviceRequest) {
        log.info("CompanyId in initialOrderServiceLoad : ", sellerserviceRequest.sellerid);
        try {
            return sellerService.initialOrderServiceLoad(sellerserviceRequest);
        } catch (Exception ex) {
            log.error("Error in initialOrderServiceLoad : ", ex);
        }
        return new ArrayList<SellerServiceResponse>();
    }

    @PostMapping("/Quickadd")
    public String Quickadd(@RequestBody List<SellerBatchesRequest> sellerBatchesRequest) {
        try {
            String json = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(sellerBatchesRequest);

            log.info("Incoming Request Payload:\n{}", json);

        } catch (Exception e) {
            log.error("Error while logging request payload", e);
        }
        SellerBatchesResponse sellerBatchesResponse = new SellerBatchesResponse();
        sellerBatchesResponse.setResponseMessage(new MessageResponse());
        try {
            List<Batches> batchList = new ArrayList<>();
            HashMap<String, ProductCategory> map = new HashMap<>();
            LinkedHashMap<String, String> gstMap = new LinkedHashMap<>();
            // Get last batch reference for numbering
            Batches lastExistingBatch = batchesRepo.findTopByCompanyidOrderByCreatedAtDesc(sellerBatchesRequest.get(0).getCompanyid()).orElse(null);
            // Use batchId, not Mongo _id
            String lastBatchRef = lastExistingBatch != null ? lastExistingBatch.getBatch_Id() : null;
            for (SellerBatchesRequest request : sellerBatchesRequest) {
                // Generate next batch id based on last generated value
                String previousBatchId = batchList.isEmpty()
                        ? lastBatchRef
                        : batchList.get(batchList.size() - 1).get_id();
                Batches newBatch = SellerAdaptor.ToBatches(request, previousBatchId);

                log.debug("New Batch : ", newBatch);
                batchList.add(newBatch);
                map.put(request.getProductid(), SellerAdaptor.FromProductCategoryDTOtoProductCategory(request.productCategory));
                if (request.getGst() != null && !request.getGst().isEmpty()) {
                    gstMap.put(request.getProductid(), request.getGst());
                }
            }
            for (Map.Entry<String, ProductCategory> entry : map.entrySet()) {
                log.info("Key: {}, Value: {}", entry.getKey(), entry.getValue());
            }
//           log.info("After Adaptor size: " + batchList.size());
            return sellerService.Quickadd(batchList, gstMap, map);
        } catch (Exception e) {
            log.error("Error in QuickAdd : ", e);
            sellerBatchesResponse.getResponseMessage().setErrorMsg(
                    ExceptionHandling.GetFullExceptionDetails(e)
            );
            return sellerBatchesResponse.getResponseMessage().getErrorMsg();
        }
    }

    @PostMapping("/AddInventory")
    public String AddInventory(@RequestBody SellerBatchesRequest sellerBatchesRequest) {
        log.info("selling price from DTO: " + sellerBatchesRequest.seller_price);
        log.info("CompanyId in AddInventory : " + sellerBatchesRequest.getCompanyid());
        SellerBatchesResponse sellerBatchesResponse = new SellerBatchesResponse();
        sellerBatchesResponse.setResponseMessage(new MessageResponse());
        Batches last = batchesRepo.findTopByCompanyidOrderByCreatedAtDesc(sellerBatchesRequest.getCompanyid())
                .orElse(null);
        String lastBatchId = last != null ? last.getBatch_Id() : null;
        try {
            Batches batches = SellerAdaptor.ToBatches(sellerBatchesRequest, lastBatchId);
            log.info("In ADD Inventory Batch selling price : " + batches.getSeller_price());
            return sellerService.AddInventory(batches, sellerBatchesRequest);
        } catch (Exception e) {
            log.error("Error in AddInventory : ", e);
            sellerBatchesResponse.getResponseMessage().setErrorMsg(ExceptionHandling.GetFullExceptionDetails(e));
            return sellerBatchesResponse.getResponseMessage().getErrorMsg();
        }
    }

    @PostMapping("/GetBatches")
    public SellerGetBatchesResponse GetBatches(@RequestBody SellerGetBatchesRequest sellerGetBatchesRequest) {
        try {
            return sellerService.GetBatches(sellerGetBatchesRequest);
        } catch (Exception ex) {
            log.error("Error in GetBatches : ", ex);
        }
        return new SellerGetBatchesResponse(null, 0);
    }

    @PostMapping("/Update")
    public String Update(@RequestBody SellerUpdateRequest sellerUpdateRequest) {
        try {
            return sellerService.Update(sellerUpdateRequest);
        } catch (Exception e) {
            log.error("Error in Update : ", e);
            String s = ExceptionHandling.GetFullExceptionDetails(e);
            return s;
        }
    }

    @PreAuthorize("hasAnyRole('REGD','ADMIN')")
    @PostMapping("/Save")
    public SaveOrderResponse Save_Order(@RequestBody SellerOrderRequest sellerOrderRequest) throws JsonProcessingException {
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(sellerOrderRequest);
        log.info(json);
        try {
            return sellerService.Save_Order(sellerOrderRequest);
        } catch (Exception ex) {
            log.error("Error in Save_Order : ", ex);
        }
        return new SaveOrderResponse();

    }

    @PostMapping("/SaveAndExecuteQuickInvoice")
    public SaveAndExecuteQuickInvoiceResponse ToSaveAndExecuteQuickInvoice(@RequestBody SaveAndExecuteQuickInvoiceRequest saveAndExecuteQuickInvoiceRequest) throws JsonProcessingException {

        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(saveAndExecuteQuickInvoiceRequest);
        log.info(json);
        try {
            return sellerService.ToSaveAndExecuteQuickInvoice(saveAndExecuteQuickInvoiceRequest);
        } catch (Exception ex) {
            log.error("Error in SaveAndExecuteQuickInvoice : ", ex);
        }
        return new SaveAndExecuteQuickInvoiceResponse();

    }

    @PostMapping("/RepeatOrder")
    public ViewOrderListResponse RepeatOrder(@RequestBody ViewOrderRequest viewOrderRequest) {
        try {
            return sellerService.RepeatOrder(viewOrderRequest);
        } catch (Exception ex) {
            log.error("Error in View : ", ex);
        }
        return new ViewOrderListResponse();
    }

    @PostMapping("/View")
    public ViewOrderListResponse View(@RequestBody ViewOrderRequest viewOrderRequest) {
        try {
            return sellerService.View(viewOrderRequest);
        } catch (Exception ex) {
            log.error("Error in View : ", ex);
        }
        return new ViewOrderListResponse();
    }

    @PostMapping("/ViewQuickInvoice")
    public QuickInvoiceResponse ViewQuickInvoice(@RequestBody ViewQuickInvoiceRequest viewQuickInvoiceRequest) throws IOException {
        try {
            return sellerService.ViewQuickInvoice(viewQuickInvoiceRequest);
        } catch (Exception ex) {
            log.error("Error in ViewQuickInvoice : ", ex);
        }
        return new QuickInvoiceResponse();
    }

    @PostMapping("/ViewOrderList")
    public List<ViewOrderListResponse> ViewOrderList(@RequestBody ViewOrderListRequest viewOrderListRequest) {
        try {
            return sellerService.ViewOrderList(viewOrderListRequest);
        } catch (Exception ex) {
            log.error("Error in ViewOrderList : ", ex);
        }
        return new ArrayList<ViewOrderListResponse>();
    }

    @PostMapping("/SearchOrders")
    public List<ViewOrderListResponse> SearchOrders(@RequestBody SearchOrdersRequest searchOrdersRequest) {
        try {
            return sellerService.SearchOrders(searchOrdersRequest);
        } catch (Exception ex) {
            log.error("Error in ViewOrderList : ", ex);
        }
        return new ArrayList<ViewOrderListResponse>();
    }

    @PostMapping("/DeleteOrders")
    public String DeleteOrders(@RequestBody SearchOrdersRequest searchOrdersRequest) {
        System.out.println("DELETE CONTROLLER HIT");
        System.out.println("ID: " + searchOrdersRequest._id);
        try {
            return sellerService.DeleteOrders(searchOrdersRequest);
        } catch (Exception ex) {
            log.error("Error in ViewOrderList : ", ex);
        }
        return null;
    }

    @PostMapping("/UpdateOrderStatus")
    public String UpdateOrderStatus(@RequestBody UpdateOrderStatusRequest updateOrderStatusRequest) {
        try {
            return sellerService.UpdateOrderStatus(updateOrderStatusRequest);
        } catch (Exception ex) {
            log.error("Error in ViewOrderList : ", ex);
        }
        return null;
    }

    @PostMapping("/Execute")
    public SaveOrderResponse Execute(@RequestBody ExecuteOrderRequest executeOrderRequest) throws JsonProcessingException {
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(executeOrderRequest);
        log.info(json);
        try {
            SaveOrderResponse saveOrderResponse = sellerService.Execute(executeOrderRequest);

            log.info(objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(saveOrderResponse));
            return saveOrderResponse;
        } catch (JsonProcessingException e) {
            log.error("Error in Execute : ", e);
        }
        return new SaveOrderResponse();
    }
//    @PostMapping("/InvoiceData")
//    public InvoiceResponse InvoiceData(@RequestBody InvoiceRequest invoiceRequest) throws IOException, ParseException {
//        try {
//            return sellerService.InvoiceData(invoiceRequest);
//        } catch (IOException e) {
//            log.error("Error in GenerateInvoice : ",e);
//        } catch (ParseException e) {
//            log.error("Error in GenerateInvoice : ",e);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        return new InvoiceResponse();
//    }

    @PostMapping("/GenerateInvoice")
    public InvoiceResponse GenerateInvoice(@RequestBody InvoiceRequest invoiceRequest) throws IOException, ParseException {
        try {
            return sellerService.GenerateInvoice(invoiceRequest);
        } catch (IOException e) {
            log.error("Error in GenerateInvoice : ", e);
        } catch (ParseException e) {
            log.error("Error in GenerateInvoice : ", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new InvoiceResponse();
    }

    @GetMapping("/CustomerTemplate/{id}")
    public String getHtml(@PathVariable String id) {
        return "redirect:/CustomerTemplate.html?orderId=" + id;
    }

    @GetMapping("/InvoiceData/{id}")
    public InvoiceResponse getInvoice(@PathVariable String id) throws Exception {

        InvoiceRequest req = new InvoiceRequest();
        req.id = new ObjectId(id);

        InvoiceResponse response = sellerService.InvoiceData(req);
        response.baseUrl = baseUrl;


        return response;
    }

    @PreAuthorize("hasAnyRole('PAID', 'GUEST')")
    @PostMapping("/QuickInvoice")
    public ResponseEntity<?> quickInvoice(@RequestBody QuickInvoiceRequest request) {
        try {
            QuickInvoiceResponse response = sellerService.processQuickInvoice(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in quickInvoice : ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/DeleteQuickInvoiceProducts")
    public String DeleteQuickInvoiceProducts(@RequestBody DeleteQuickInvoiceRequest deleteQuickInvoiceRequest) throws IOException {
        try {
            return sellerService.DeleteQuickInvoiceProducts(deleteQuickInvoiceRequest);
        } catch (IOException e) {
            log.error("Error in DeleteQuickInvoiceProducts : ", e);
        }
        return null;
    }

    @PostMapping("/SecondTimeQI")
    public List<QuickProductsDTO> SecondTimeQI(@RequestBody SecondTimeQIRequest secondTimeQIRequest) {
        log.info("SecondTimeQI Request id : " + secondTimeQIRequest.getId());
        try {
            return sellerService.SecondTimeQI(secondTimeQIRequest);
        } catch (Exception e) {
            log.error("Error in SecondTimeQI : ", e);
        }
        return new ArrayList<QuickProductsDTO>();
    }

    @PostMapping("/FiveLatestInvoice")
    public List<QuickInvoiceResponse> FiveLatestInvoice(@RequestBody FiveLatestInvoiceRequest fiveLatestInvoiceRequest) throws IOException {
        log.info("companyid: " + fiveLatestInvoiceRequest.companyid);
        try {
            return sellerService.FiveLatestInvoice(fiveLatestInvoiceRequest);
        } catch (IOException e) {
            log.error("Error in FiveLatestInvoice : ", e);
        }
        return new ArrayList<QuickInvoiceResponse>();
    }

    @PostMapping("/FiveLatestFRInvoice")
    public List<SaveOrderResponse> FiveLatestFRInvoice(@RequestBody FiveLatestFRInvoiceRequest fiveLatestfrInvoiceRequest) throws IOException {
        log.info("Seller Type: " + fiveLatestfrInvoiceRequest.seller_type);
        log.info("companyid: " + fiveLatestfrInvoiceRequest.companyid);

        try {
            return sellerService.FiveLatestFRInvoice(fiveLatestfrInvoiceRequest);
        } catch (IOException e) {
            log.error("Error in Five-Latest-FR-Invoice : ", e);
        }
        return new ArrayList<SaveOrderResponse>();
    }

    @PostMapping("/ReadQuickInvoice")
    public String ReadQuickInvoice(@RequestBody ReadQuickInvoiceRequest readQuickInvoiceRequest) throws IOException, ParseException {
        try {
            return sellerService.ReadQuickInvoice(readQuickInvoiceRequest);
        } catch (IOException e) {
            log.error("Error in ReadQuickInvoice : ", e);
        } catch (ParseException e) {
            log.error("Error in ReadQuickInvoice : ", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @PreAuthorize("GUEST")
    @PostMapping("/UserBehaviour")
    public String UserBehaviour(@RequestBody UserBehaviourRequestDTO userBehaviourRequestDTO) {
        try {
            return sellerService.UserBehaviour(userBehaviourRequestDTO);
        } catch (Exception e) {
            log.error("Error in UserBehaviour : ", e);
        }
        return null;
    }

    @PostMapping("/UpdateCompanyInfo")
    public UpdateCompanyInfoResponse UpdateCompanyInfo(@RequestBody UpdateCompanyInfoRequest updateCompanyInfoRequest) throws IOException {
        log.info("UserId in UpdateCompanyInfo : " + updateCompanyInfoRequest.user_id);
        log.info("CompanyId in UpdateCompanyInfo : " + updateCompanyInfoRequest.companyId);
        try {
            return sellerService.UpdateCompanyInfo(updateCompanyInfoRequest);
        } catch (Exception e) {
            log.error("Error in UpdateCompanyInfo : ", e);
        }
        return new UpdateCompanyInfoResponse();
    }

    @PostMapping("/InitialGetUpdateCompanyInfo")
    public InitialGetUpdateCompanyInfoResponse InitialGetUpdateCompanyInfo(@RequestBody InitialGetUpdateCompanyInfoRequest initialGetUpdateCompanyInfoRequest) throws IOException {
        log.info("InitialGetUpdateCompanyInfo /  UserID: " + initialGetUpdateCompanyInfoRequest.user_id);
        try {
            return sellerService.InitialGetUpdateCompanyInfo(initialGetUpdateCompanyInfoRequest);
        } catch (Exception e) {
            log.error("Error in InitialGetUpdateCompanyInfo : ", e);
        }
        return new InitialGetUpdateCompanyInfoResponse();
    }

    @PostMapping("/ContactUsEmail")
    public ResponseEntity<String> sendSubscriptionEmail(@RequestBody SubscriptionEmailDTO subscriptionEmailDTO) {
        try {
            return sellerService.sendSubscriptionEmail(subscriptionEmailDTO, null, null);
        } catch (Exception e) {
            log.error("Error in sendSubscriptionEmail : ", e);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/readinvoice/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> readInvoice(@PathVariable String id
            , @RequestParam(defaultValue = "farme_rich_template_2") String template
            , @RequestParam(defaultValue = "") String userFolder) {
        ReadQuickInvoiceRequest request = new ReadQuickInvoiceRequest();
        request.id = new ObjectId(id);  // convert string to ObjectId
        request.template = template;
        request.userfolder = userFolder;

        try {
            String response = sellerService.ReadQuickInvoice(request);
            return ResponseEntity.ok(response);
        } catch (FileNotFoundException e) {
            log.error("Error in readInvoice : ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("<h1>Invoice not found</h1>");
        } catch (IOException e) {
            log.error("Error in readInvoice : ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("<h1>Error generating invoice</h1>");
        } catch (ParseException e) {
            log.error("Error in readInvoice : ", e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/InitialServiceInventory")
    public List<InitialServiceInventoryServiceInventoryResponse> InitialServiceInventory(@RequestBody InitialServiceInventoryServiceInventoryRequest InitialServiceInventoryserviceInventoryRequest) {
        log.info("CompanyId in InitialServiceInventory : " + InitialServiceInventoryserviceInventoryRequest.sellerid);
        try {
            return sellerService.InitialServiceInventory(InitialServiceInventoryserviceInventoryRequest);
        } catch (Exception e) {
            log.error("Error in InitialServiceInventory : ", e);
        }
        return new ArrayList<InitialServiceInventoryServiceInventoryResponse>();
    }

    @PostMapping("/viewServiceInventoryDetails")
    public InitialServiceInventoryServiceInventoryResponse viewServiceInventoryDetails(@RequestBody InitialServiceInventoryServiceInventoryRequest InitialServiceInventoryserviceInventoryRequest) {
        try {
            return sellerService.viewServiceInventoryDetails(InitialServiceInventoryserviceInventoryRequest);
        } catch (Exception e) {
            log.error("Error in viewServiceInventoryDetails : ", e);
        }
        return new InitialServiceInventoryServiceInventoryResponse();
    }

    @PostMapping("/ServiceInventory")
    public ServiceInventoryResponse ServiceInventory(@RequestBody ServiceInventoryRequest serviceInventoryRequest) throws JsonProcessingException {
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(serviceInventoryRequest);

        log.info("Incoming Request Payload:\n{}", json);
        try {
            return sellerService.ServiceInventory(serviceInventoryRequest);
        } catch (Exception e) {
            log.error("Error in ServiceInventory : ", e);
        }
        return null;
    }

    @PostMapping("/DeleteServiceInventory")
    public String DeleteServiceInventory(@RequestBody DeleteServiceInventoryRequest DeleteserviceInventoryRequest) {
        try {
            return sellerService.DeleteServiceInventory(DeleteserviceInventoryRequest);
        } catch (Exception e) {
            log.error("Error in DeleteServiceInventory : ", e);
        }
        return null;
    }

    @PostMapping("/UploadImage")
    public ResponseEntity<?> uploadImageForUpdateProfile(@RequestBody Map<String, Object> payload) {
        try {
            String companyId = (String) payload.get("companyid");
            String imgBase64 = (String) payload.get("imgBase64");
            boolean reset = (boolean) payload.get("reset");
            log.info("isReset: " + reset);
            if (reset) {
                // Delete old file if exists
                File oldFile = new File(uploadDir + companyId + "_profile.jpg");

                if (oldFile.exists()) {
                    if (oldFile.delete()) {
                        log.info("Deleted old file: " + oldFile.getName());
                    } else {
                        log.info("Failed to delete old file: " + oldFile.getName());
                    }
                    return ResponseEntity.ok(Map.of(
                            "message", "Reset successful"
                    ));
                }
                return ResponseEntity.ok(Map.of(
                        "message", "No Image exist"
                ));
            }

            if (imgBase64 == null || imgBase64.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No image provided"));
            }

            // Remove "data:image/jpeg;base64," prefix if present
            String base64Data = imgBase64.contains(",") ? imgBase64.split(",")[1] : imgBase64;
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            File dir = new File(uploadDir);

            if (!dir.exists()) {
                dir.mkdirs();
            } else {
                System.out.println(dir.getPath());
                System.out.println(dir);
                log.info("Directory is available");
            }
            // Save new file
            String filePath = uploadDir + companyId + "_profile.jpg";
            System.out.println(filePath);

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(imageBytes);
            }
            return ResponseEntity.ok(Map.of(
                    "message", "Upload successful",
                    "path", filePath
            ));
        } catch (Exception e) {
            log.error("Error in UploadImage : ", e);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Root folder
    @PostMapping("/uploadServiceImages")
    public ResponseEntity<?> uploadServiceImages(@RequestBody Map<String, Object> payload) {

        try {
            return sellerService.uploadServiceImages(payload, Service_BASE_PATH);
        } catch (Exception e) {
            log.error("uploadServiceImages error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/getServiceImages")
    public ResponseEntity<?> getServiceImages(@RequestParam String userfolder, @RequestParam String servicename) {
        try {
            List<Map<String, Object>> images = sellerService.getServiceImages(userfolder, servicename, Service_BASE_PATH);
            return ResponseEntity.ok(Map.of("images", images));
        } catch (Exception e) {
            log.error("Error fetching service images", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch service images"));
        }
    }

    @PostMapping("/QICustomers")
    public ObjectId QICustomers(@RequestBody QICustomerRequestDTO qiCustomerRequestDTO) {
        try {
            return sellerService.QICustomer(qiCustomerRequestDTO);
        } catch (Exception e) {
            log.error("Error in QICustomers : ", e);
        }
        return null;
    }

//    @GetMapping("/FullImages")
//    public ResponseEntity<List<String>> getImages(@RequestParam String userfolder,
//                                                  @RequestParam String type,
//                                                  @RequestParam String productname, @RequestParam String inventorycreated) {
//

    /// /        String formattedProductName = productname.replaceAll("\\s+", "_").trim();
    /// /        String folderName = formattedProductName + "_" + inventorycreated;
//
//        String base = type.equals("uploadsProductImages") ? Product_BASE_PATH : Service_BASE_PATH;
//        Path Product_imageDir = Paths.get(
//                base, productname
//        );
//
//        //$host/uploads/uploadsServiceImages/$<sellername-createdat>/$<ServiceName>/
//        Path Service_imageDir = Paths.get(
//                base, userfolder, productname
//        );
//        Path imageDir = type.equals("uploadsProductImages") ? Product_imageDir : Service_imageDir;
//        if (imageDir != null) {
//            System.out.println("Path is : " + imageDir.toString());
//        }
//
//
//        if (!Files.exists(imageDir)) {
//            return ResponseEntity.ok(Collections.emptyList());
//        }
//
//
//        try (Stream<Path> paths = Files.list(imageDir)) {
//            List<String> imageUrls = paths
//                    .filter(Files::isRegularFile)
//                    .map(path -> base + (
//                            type.equals("uploadsServiceImages") ? userfolder + "/" + productname : productname) + "/"
//                            + path.getFileName().toString())
//                    .collect(Collectors.toList());
//
//            return ResponseEntity.ok(imageUrls);
//
//        } catch (IOException e) {
//            log.error("Error in getImages : ", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Collections.emptyList());
//        }
//    }
    @GetMapping("/FullImages")
    public ResponseEntity<List<Map<String, Object>>> getImages(@RequestParam String userfolder, @RequestParam String type,
                                                               @RequestParam String inventoryId, @RequestParam String companyName,
                                                               @RequestParam String productId) {
        return sellerService.getImages(userfolder, type, inventoryId, companyName, productId);
    }

    @PostMapping("/QIcustomersuggestion")
    public List<QICustomerResponseDTO> QIcustomersuggestion(@RequestBody QIcustomersuggestionRequestDTO qIcustomersuggestionRequestDTO) {
        try {
            return sellerService.QIcustomersuggestion(qIcustomersuggestionRequestDTO);
        } catch (Exception e) {
            log.error("Error in QIcustomersuggestion : ", e);
        }
        return new ArrayList<QICustomerResponseDTO>();
    }


    @PostMapping("/GetUPI")
    public GetUPIResponse GetUPI(@RequestBody GetUPIRequest getUPIRequest) {
        try {
            return sellerService.GetUPI(getUPIRequest);
        } catch (Exception e) {
            log.error("Error in GetUPI : ", e);
        }
        return new GetUPIResponse();
    }

    @PostMapping("/GetOTP")
    public boolean GetOTP(@RequestBody GetOTPRequest getOTPRequest) {
        try {
            return sellerService.GetOTP(getOTPRequest);
        } catch (Exception e) {
            log.error("Error in GetOTP : ", e);
        }
        return false;
    }

    @PostMapping("/GenerateOtp")
    public ResponseEntity<Map<String, String>> GenerateOtp(@RequestBody GetOTPRequest getOTPRequest) {
        try {
            return sellerService.GenerateOtp(getOTPRequest, uploadDir);
        } catch (Exception e) {
            log.error("Error in GetOTP : ", e);
        }
        return ResponseEntity.badRequest().body(Map.of("message", "try again some time"));
    }

    @PostMapping("/ValidateOtp")
    public boolean ValidateOtp(@RequestBody GetOTPRequest getOTPRequest) {
        try {
            return sellerService.ValidateOtp(getOTPRequest, uploadDir, getOTPRequest.attempt);
        } catch (Exception e) {
            log.error("Error in ValidateOtp : ", e);
        }
        return false;
    }

    @PostMapping("/DeleteOtp")
    public boolean DeleteOtp(@RequestBody GetOTPRequest getOTPRequest) {
        try {
            return sellerService.DeleteOtpFile(getOTPRequest, uploadDir);
        } catch (Exception e) {
            log.error("Error in DeleteOtp : ", e);
        }
        return false;
    }

    @PostMapping("/InsertProducts")
    public AddProductResponse InsertProducts(@RequestBody InsertProductsRequest insertProductsRequest) {
        try {
            String json = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(insertProductsRequest);

            log.info("Incoming Request Payload:\n{}", json);

        } catch (Exception e) {
            log.error("Error while logging request payload", e);
        }
        try {
            return sellerService.InsertProducts(insertProductsRequest);
        } catch (Exception e) {
            log.error("Error in InsertProducts : ", e);
        }
        return new AddProductResponse();
    }

    @PostMapping("/ViewProduct")
    public InventoryDTO GetProduct(@RequestBody GetProductRequest getProductRequest) {
        try {
            return sellerService.GetProduct(getProductRequest);
        } catch (Exception e) {
            log.error("Error in InsertProducts : ", e);
        }
        return new InventoryDTO();
    }


    @PostMapping("/GetDeliveryPoints")
    public List<PickupPointsDTO> GetDeliveryPoints(@RequestBody GetDeliveryPointsRequest getDeliveryPointsRequest) {
        log.info("User_id DTO: " + getDeliveryPointsRequest.user_id);
        if (getDeliveryPointsRequest.user_id == null) return new ArrayList<PickupPointsDTO>();
        try {
            return sellerService.GetDeliveryPoints(getDeliveryPointsRequest.user_id);
        } catch (Exception e) {
            log.error("Error in InsertProducts : ", e);
        }
        return new ArrayList<PickupPointsDTO>();
    }

    @PostMapping("/PaymentHistory")
    public List<PaymentsDTO> PaymentHistory(@RequestBody PaymentHistoryRequest PaymentHistoryRequest) {
        log.info("Requested Order ID: " + PaymentHistoryRequest._id);
        try {
            return sellerService.PaymentHistory(PaymentHistoryRequest);
        } catch (Exception e) {
            log.error("Error in InsertProducts : ", e);
        }
        return new ArrayList<PaymentsDTO>();
    }

    @PostMapping("/AcceptReviewOrders")
    public SaveOrderResponse AcceptReviewOrders(@RequestBody ExecuteOrderRequest executeOrderRequest) throws JsonProcessingException {
        log.info("AcceptReviewOrders Request: {}",
                objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(executeOrderRequest));
        try {
            return sellerService.AcceptReviewOrders(executeOrderRequest);
        } catch (JsonProcessingException e) {
            log.error("Error in Execute : ", e);
        }
        return new SaveOrderResponse();
    }

    @PostMapping("/RejectReviewOrders")
    public String RejectReviewOrders(@RequestBody RejectReviewOrdersRequest rejectReviewOrdersRequest) throws JsonProcessingException {
        return sellerService.RejectReviewOrders(rejectReviewOrdersRequest);

    }

    @PostMapping("/searchBuyerByMobileOrName")
    public List<BuyerDetailsResponse> searchBuyerByMobile(@RequestParam String value, @RequestParam ObjectId sellerid) {
        List<BuyerDetailsResponse> res = new ArrayList<>();
        try {
            log.info("Value for dearch is : " + value);
            return sellerService.searchBuyerByMobile(value, sellerid);
        } catch (Exception e) {
            log.error("Error in DeleteOtp : ", e);
        }
        return res;
    }

    @PostMapping("/GetCategoryTree")
    public List<CategoryTreeDTO> getCategoryTree(@RequestBody(required = false) Map<String, String> body) {
        return productCategoryService.getCategoryTree();
    }

    // POST /GetCategoriesFlat  -> flat list, used to populate the "Parent category" dropdown
    @PostMapping("/GetCategoriesFlat")
    public List<CategoryTreeDTO> getCategoriesFlat() {
        return productCategoryService.getAllFlat();
    }

    // POST /SaveCategory  -> add new category (parent or child)
    @PostMapping("/SaveCategory")
    public ResponseEntity<?> saveCategory(@ModelAttribute CategoryRequestDTO request,
                                          @RequestPart(value = "coverImage", required = false) MultipartFile coverImage) {

        try {
            ProductCategory saved = productCategoryService.addCategory(request, coverImage);
            return ResponseEntity.ok(Map.of("msg", "Category saved successfully", "category", saved));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("msg", e.getMessage()));
        }
    }

    //    // POST /UpdateCategory?id=...  -> edit existing category
    @PostMapping("/UpdateCategory")
    public ResponseEntity<?> updateCategory(@RequestParam String id, @ModelAttribute CategoryRequestDTO request,
                                            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage) throws JsonProcessingException {

        log.info("Update category Request: {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
        try {
            ProductCategory updated = productCategoryService.updateCategory(id, request, coverImage);
            return ResponseEntity.ok(Map.of("msg", "Category updated successfully", "category", updated)
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("msg", e.getMessage()));
        }
    }

    // POST /DeleteCategory?id=...  -> matches /DeleteServiceInventory naming style
    @PostMapping("/DeleteCategory")
    public ResponseEntity<?> deleteCategory(@RequestParam String id) {
        try {
            productCategoryService.deleteCategory(id);
            return ResponseEntity.ok("Successfully Deleted");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("msg", e.getMessage()));
        }
    }

    @PostMapping("/isValidName")
    public boolean isValidNameForProductAndService(@RequestParam String name, @RequestParam String companyId, @RequestParam boolean isService) {
        try {
            return sellerService.isValidNameForProductAndService(name, companyId, isService);
        } catch (Exception e) {
            return false;
        }
    }

    @PostMapping("/searchProductsBySearchValue")
    public ResponseEntity<List<ProductSuggestDTO>> searchProducts(
            @RequestBody ProductSearchRequest request) {

        List<ProductSuggestDTO> results = sellerService.searchProducts(
                request.getSearchValue(),
                request.getCompanyId(),
                request.isQI()
        );

        return ResponseEntity.ok(results);
    }

    @PostMapping("/DeleteByInventoryId")
    public boolean DeleteByInventoryId(@RequestParam ObjectId inventoryId) {
        return sellerService.DeleteByInventoryId(inventoryId);
    }


    @DeleteMapping("/deleteAll")
    public ResponseEntity<String> DeleteAllData() {
        sellerService.deleteAllData();
        return ResponseEntity.ok("All data deleted successfully!");
    }

    @GetMapping("/AboutFR")
    public String GetAllAboutFR() throws Exception {
        String Secret_key = "05e9a649244547da" + "intellesydetech";
        String encrypted = Farme_rich.Security.PasswordUtils.encrypt("1234", Secret_key);
        log.info(Secret_key);
        return encrypted + ":" + java.time.LocalDateTime.now() + " Sun 09 Aug : Farm-e-Rich is a comprehensive inventory and billing management system designed to streamline operations for businesses. It offers features such as stegration capabilities, Farm-e-Rich aims to enhance efficiency and accuracy in business processes.";
    }


    @RequestMapping(value = "/{path:[^\\.]*}")
    public String redirect() {
        return "forward:/index.html";
    }

}

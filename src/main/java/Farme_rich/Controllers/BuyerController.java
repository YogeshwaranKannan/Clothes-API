package Farme_rich.Controllers;


import Farme_rich.Buyer.BL.BuyerService;
import Farme_rich.Buyer.DTO.Request.*;
import Farme_rich.Buyer.DTO.Response.*;
import Farme_rich.Security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/buyer")
@Configuration
@EnableTransactionManagement

public class BuyerController {
    private static final Logger log = LoggerFactory.getLogger(BuyerController.class);

    private final BuyerService buyerService;

    @Autowired
    private ObjectMapper objectMapper;
    @Value("${app.upload-dir}")
    private String uploadDir;

    @Autowired
    public BuyerController(BuyerService buyerService) {
        this.buyerService = buyerService;

    }

    @PostMapping("/GetSellerDetails")
    public GetSellerDetailsResponse GetSellerDetails(@RequestBody GetSellerDetailsRequest request) {

        try {
            return buyerService.GetSellerDetails(request);

        } catch (Exception ex) {
            log.error("Error in GetSellerDetails : ", ex);
        }

        return new GetSellerDetailsResponse();
    }

    @PostMapping("/GetCategories")
    public GetCategoriesApiResponse GetCategories(@RequestBody GetCategoriesRequest request) {

        log.info("Company id : " + request.sellerId);

        GetCategoriesApiResponse response = new GetCategoriesApiResponse();

        try {
            List<GetCategoriesResponse> flatList =
                    buyerService.GetCategoriesBySellerID(request);

            List<GetCategoriesResponse> nested = buyerService.buildTree(flatList);

            // Header
            GetCategoriesApiResponse.ResponseHeader header =
                    new GetCategoriesApiResponse.ResponseHeader();
            header.action = "getCategories";
            header.seller_id = 1;
            header.status = "success";

            response.responseHeader = header;
            response.categories = nested;

        } catch (Exception ex) {
            log.error("Error in GetCategories : ", ex);
        }

        return response;
    }

    @PostMapping("/searchProductsManageOrderSeller")
    public InventoryProductsAndServiceResponse searchProductsManageOrderSeller(@RequestBody InventoryProductsAndServiceRequest request) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String prettyRequest = mapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(request);

            log.info("Request : \n{}", prettyRequest);

        } catch (Exception e) {
            log.error("Failed to log request", e);
        }
        try {
            return buyerService.searchProductsManageOrderSeller(request);
        } catch (Exception ex) {
            log.error("Error in searchProductsManageOrderBuyer : ", ex);
        }
        return new InventoryProductsAndServiceResponse();
    }

    @PostMapping("/placeOrderForBuyer")
    public PlaceOrderResponse placeOrderForBuyer(@RequestBody BuyerOrderRequest request) {
        try {
            log.info("Buyer request: {}",
                    objectMapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(request));
        } catch (Exception e) {
            log.error("Error logging request", e);
        }
        try {
            return buyerService.placeOrderForBuyer(request);
        } catch (Exception ex) {
            log.error("Error in placeOrderForBuyer: ", ex);
        }
        return null;
    }

    @PostMapping("/verifyPayment")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> data) {
        return buyerService.verifyPayment(data);
    }

    @PostMapping("/ViewOrderDetails")
    public ViewOrderDetailsResponse ViewOrderDetails(@RequestBody ViewOrderDetailsRequest request) {
        log.info("Buyer request: {}", request.refno);
        log.info("Companyid : " + request.companyid);
        try {
            return buyerService.ViewOrderDetails(request, false, null);
        } catch (Exception ex) {
            log.error("Error in ViewOrderDetails : ", ex);
        }
        return null;
    }

    @PostMapping("/searchProductById")
    public InventoryProductsAndServiceResponse searchProductById(@RequestBody searchProductByIdRequest request) {
        log.info("ProductId : " + request.productId);
        try {
            return buyerService.searchProductById(request);
        } catch (Exception ex) {
            log.error("Error in InitialGetAllProducts : ", ex);
        }
        return new InventoryProductsAndServiceResponse();
    }

    //    neeed to remove
    @PostMapping("/updateOrderStatus")
    public boolean updateOrderStatus(@RequestBody UpdateOrderStatusRequest updateOrderStatusRequest) {
        log.info("Buyer request: {}", updateOrderStatusRequest.id);

        try {
            return buyerService.updateOrderStatus(updateOrderStatusRequest);
        } catch (Exception ex) {
            log.error("Error in updateOrderStatus : ", ex);
        }
        return false;
    }
//

    @GetMapping("/v1/payments/{paymentId}")
    public ResponseEntity<?> getPayment(@PathVariable String paymentId) {
        try {
            RazorpayClient client = new RazorpayClient("rzp_test_SgBCFz4OopLkPs", "52E3FPYrX0WW3p7qyU3RnFP4");

            Payment payment = client.payments.fetch(paymentId);


            return ResponseEntity.ok(payment.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/sendMail")
    public ResponseEntity<Map<String, String>> sendMail(@RequestBody sendMailRequest request) {
        log.info("Buyer request maild id : {}", request.mailId);
        log.info("Buyer request save id : {}", request.save_id);
        return buyerService.sendMail(request);
    }

    @PostMapping("/suggest")
    public List<Farme_rich.Buyer.DTO.Response.ProductSuggestionDTO> SuggestProducts(@RequestBody SellerProductRequest sellerProductRequest) {
        log.info("CompanyId in SuggestProducts: " + sellerProductRequest.getCompanynameid());
        log.info("Categories : " + sellerProductRequest.categories);
        try {
            return buyerService.SuggestProductsByName(sellerProductRequest);
        } catch (Exception ex) {
            log.error("Error in SuggestProducts : ", ex);
        }
        return new ArrayList<ProductSuggestionDTO>();
    }

    @PostMapping("/signup")
    public SignupOrLoginResponse signup(@RequestBody SignupRequest signupRequest) {
        try {
            return buyerService.signup(signupRequest);
        } catch (Exception ex) {
            log.error("Error in SuggestProducts : ", ex);
        }
        return new SignupOrLoginResponse();
    }

    @PostMapping("/login")
    public SignupOrLoginResponse login(@RequestBody LoginRequest loginRequest) {
        log.info("Mobile : " + loginRequest.identifier);
        try {
            return buyerService.login(loginRequest);
        } catch (Exception ex) {
            log.error("Error in SuggestProducts : ", ex);
        }
        return new SignupOrLoginResponse();
    }

    @PostMapping("/updateProfile")
    public boolean updateProfile(@RequestBody UpdateProfileRequest request) {
        return buyerService.updateProfile(request);
    }

    @PostMapping("/getOrGenerateToken")
    public ResponseEntity<?> getOrGenerateToken(@RequestBody TokenRequest request) {

        String mobileNum = request.mobileNum;

        if (mobileNum == null || mobileNum.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Mobile is required"));
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "WEB-GUEST");
        claims.put("type", "buyer");
//        claims.put("token", "dummy");
        claims.put("mobileNum", mobileNum);
        claims.put("platform", "WEB");

        String token = JwtService.generateToken(claims, null, null, mobileNum);

        return ResponseEntity.ok(Map.of(
                "message", "success",
                "token", token,
                "role", claims.get("role")
        ));
    }

    @PostMapping("/getAllOrders")
    public List<GetAllOrdersRes> getAllOrders(@RequestBody GetAllOrdersRequest request) {
        return buyerService.getAllOrders(request);
    }

    @PostMapping("/GenerateOtp")
    public OtpResponse GenerateOtp(@RequestBody GetOTPRequest request) {
        return buyerService.GenerateOtp(request, uploadDir);
    }

    @PostMapping("/ValidateOtp")
    public boolean ValidateOtp(@RequestBody GetOTPRequest getOTPRequest) {
        try {
            return buyerService.ValidateOtp(getOTPRequest, uploadDir, getOTPRequest.attempt);
        } catch (Exception e) {
            log.error("Error in ValidateOtp : ", e);
        }
        return false;
    }

    @PostMapping("/DeleteOtp")
    public boolean DeleteOtp(@RequestBody GetOTPRequest getOTPRequest) {
        try {
            return buyerService.DeleteOtpFile(getOTPRequest, uploadDir);
        } catch (Exception e) {
            log.error("Error in DeleteOtp : ", e);
        }
        return false;
    }

    @PostMapping("/ResetPassword")
    public boolean ResetPassword(@RequestBody GetOTPRequest getOTPRequest) {
        try {
            return buyerService.ResetPassword(getOTPRequest);
        } catch (Exception e) {
            log.error("Error in DeleteOtp : ", e);
        }
        return false;
    }

    @PostMapping("/GetBuyerProfile")
    public SignupOrLoginResponse.UserDTO GetBuyerProfile(@RequestBody GetBuyerProfileRequest getBuyerProfileRequest) {
        try {
            return buyerService.GetBuyerProfile(getBuyerProfileRequest);
        } catch (Exception e) {
            log.error("Error in DeleteOtp : ", e);
        }
        return new SignupOrLoginResponse.UserDTO();
    }

    @GetMapping("/FullImages")
    public ResponseEntity<List<String>> getImages(@RequestParam String userfolder, @RequestParam String type,
                                                  @RequestParam String inventoryId, @RequestParam String companyName) {
        return buyerService.getImages(userfolder, type, inventoryId, companyName);
    }

    @PostMapping("/createDraft")
    public PlaceOrderResponse createDraft(@RequestBody BuyerOrderRequest request) {
        try {
            log.info("Draft request: {}",
                    objectMapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(request));
        } catch (Exception e) {
            log.error("Error logging request", e);
        }
        try {
            return buyerService.createDraft(request);
        } catch (Exception ex) {
            log.error("Error in createDraft: ", ex);
        }
        return null;
    }

    @DeleteMapping("/deleteBuyerDraft")
    public String DeleteDraft(@RequestParam String save_id) {
        log.info("Save id for delete draft :" + save_id);
        try {
            return buyerService.DeleteDraft(save_id);
        } catch (Exception ex) {
            log.error("Error in createDraft: ", ex);
        }
        return null;
    }

    @PostMapping("/SearchBuyerExist")
    public String SearchBuyerExist(@RequestParam String mobileNum, @RequestParam ObjectId sellerid) {

        try {
            return buyerService.SearchBuyerExist(mobileNum, sellerid);
        } catch (Exception ex) {
            log.error("Error in SearchBuyerExist : ", ex);
        }
        return null;
    }


}

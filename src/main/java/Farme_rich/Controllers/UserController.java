//package Farme_rich.Controllers;
//
//import Farme_rich.Buyer.Adaptor.BuyerAdaptor;
//import Farme_rich.Buyer.BL.BuyerService;
//import Farme_rich.Buyer.DTO.Request.BuyerProductRequest;
//import Farme_rich.Buyer.DTO.Request.UserSignupRequest;
//import Farme_rich.Buyer.DTO.Response.BuyerProductResponse;
//import Farme_rich.Buyer.DTO.Response.MessageResponse;
//import Farme_rich.Buyer.DTO.Response.UserSignupResponse;
//import Farme_rich.ErrorHandling.ExceptionHandling;
//import Farme_rich.Buyer.Model.Buyer;
//import Farme_rich.Buyer.Model.User;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.ResponseEntity;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.*;
//@CrossOrigin(origins = "*")
//@RestController
//@RequestMapping("/api/users")
//@Configuration
//@EnableTransactionManagement
//public class UserController {
//
//    private final BuyerService buyerservice;
//
//    @Autowired
//    public UserController(BuyerService buyerservice) {
//        this.buyerservice = buyerservice;
//    }
//
//    @GetMapping("/profile")
//    public List<Buyer> GetAllLogins() {
//        return buyerservice.GetAllLogins();
//    }
//
//    @GetMapping
//    public List<User> GetAllUsers() {
//        return buyerservice.GetAllUsers();
//    }
//
//    @PostMapping("/deviceId")
//    public UserSignupResponse HandleDeviceId(@RequestBody UserSignupRequest userSignupRequest) {
//        UserSignupResponse response = null;
//        try {
//            User user = BuyerAdaptor.ToUserModel(userSignupRequest);
//            return buyerservice.HandleDeviceId(user);
//        } catch (Exception ex) {
//            response = new UserSignupResponse();
//            response.ResponseMessage= new MessageResponse();
//            response.ResponseMessage.setErrorMsg(ExceptionHandling.GetFullExceptionDetails(ex));
//        }
//        return response;
//    }
//
//    @PostMapping("/profile")
//    public UserSignupResponse RegisterProfile(@RequestBody UserSignupRequest userSignupRequest) {
//        System.out.println(userSignupRequest.getMobileNum());
//        System.out.println(userSignupRequest.getValidationCode()+".......");
//        UserSignupResponse response = null;
//        try {
//            User user = BuyerAdaptor.ToUserModel(userSignupRequest);
//            Buyer buyer = BuyerAdaptor.ToUserLogin(userSignupRequest);
//            return buyerservice.RegisterProfile(user, buyer,userSignupRequest.getValidationCode());
//        } catch (Exception ex) {
//            response = new UserSignupResponse();
//            response.ResponseMessage= new MessageResponse();
//            response.ResponseMessage.setErrorMsg(ExceptionHandling.GetFullExceptionDetails(ex));
//        }
//        return response;
//    }
//
//    @PostMapping("/signupvalidate")
//    public UserSignupResponse SignupValidate(@RequestBody UserSignupRequest userSignupRequest) {
//        UserSignupResponse response = null;
//        try {
//            User user = BuyerAdaptor.ToUserModel(userSignupRequest);
//            return buyerservice.SignupValidate(user);
//        } catch (Exception ex) {
//            response = new UserSignupResponse();
//            response.ResponseMessage= new MessageResponse();
//            response.ResponseMessage.setErrorMsg(ExceptionHandling.GetFullExceptionDetails(ex));
//        }
//        return response;
//    }
//
//    @PostMapping("/checkpasscode")
//    public UserSignupResponse CheckPasscode(@RequestBody UserSignupRequest userSignupRequest) {
//        UserSignupResponse response = null;
//        try {
//            User user = BuyerAdaptor.ToUserModel(userSignupRequest);
//            return buyerservice.CheckPasscode(user);
//        } catch (Exception ex) {
//            response = new UserSignupResponse();
//            response.ResponseMessage= new MessageResponse();
//            response.ResponseMessage.setErrorMsg(ExceptionHandling.GetFullExceptionDetails(ex));
//        }
//        return response;
//    }
//
//    @PostMapping("/search")
//    public List<BuyerProductResponse> SearchProducts(@RequestBody BuyerProductRequest buyerProductRequest ) {
//        System.out.println(buyerProductRequest.getSearchvalue());
//        return buyerservice.SearchProductsByName(buyerProductRequest.getSearchvalue());
//    }
//
//    @DeleteMapping("/deleteAll")
//    public ResponseEntity<String> deleteAllData() {
//        buyerservice.DeleteAllData();
//        return ResponseEntity.ok("All data deleted successfully!");
//    }
//
//
//    //    @PostMapping("/login")
////    public ResponseEntity<Map<String, Object>> login(@RequestBody UserCredentials credentials) {
////        // Debugging output to check the received credentials
////        System.out.println("User: " + credentials.getEmail() + ", Password: " + credentials.getPassword());
////
////        // Validate if email is provided
////        if (credentials.getEmail() == null || credentials.getEmail().isEmpty()) {
////            Map<String, Object> errorResponse = new HashMap<>();
////            // {key,value} stored
////            errorResponse.put("msg", "Email cannot be empty");
////            return ResponseEntity.badRequest().body(errorResponse);
////        }
////
////        // Check if user exists with the provided email
////        UserCredentials user = userRepository.findByEmail(credentials.getEmail());
////
////        // If user is not found, return 401 Unauthorized
////        if (user == null) {
////            Map<String, Object> errorResponse = new HashMap<>();
////            errorResponse.put("msg", "User not found");
////            return ResponseEntity.status(401).body(errorResponse);
////        }
////
////        // Check if the password matches
////        if (user != null && user.getPassword().equals(credentials.getPassword())) {
////            // If successful, set a pin code and return success message
////            String pin = "1234";  // You can replace this with actual pin logic
////
////            Map<String, Object> response = new HashMap<>();
////            response.put("msg", "Successfully Registered");
////            response.put("pin", pin);
////            return ResponseEntity.ok(response);
////        } else {
////            // Return 401 Unauthorized if the password is incorrect
////            Map<String, Object> errorResponse = new HashMap<>();
////            errorResponse.put("msg", "Invalid email or password");
////            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
////        }
////    }
//}
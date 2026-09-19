package Farme_rich.Controllers;

import Farme_rich.Security.JwtService;
import Farme_rich.Seller.DTO.Request.AuthRequest;
import Farme_rich.Seller.Model.FrontEnd.User;
import Farme_rich.Seller.Repo.FrontEnd.SellerUserRepo;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static Farme_rich.Security.PasswordUtils.*;

@RestController
@RequestMapping("api/seller/auth")
public class AuthController {

    @Autowired
    private JwtService jwtService;
    private final SellerUserRepo sellerUserRepo;

    public AuthController(SellerUserRepo sellerUserRepo) {
        this.sellerUserRepo = sellerUserRepo;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody AuthRequest request) throws Exception {
        System.out.println("SignIn Request: " + request.getUserType() + ", " + request.getDeviceId());

        String role = request.getUserType();
        String deviceId = request.getDeviceId();
        String pin = request.getPin() == null ? "" : request.getPin();
        if (!pin.equals("")) {
            pin = encrypt(pin, deviceId + "intellesydetech");
        }

        if (deviceId == null || deviceId.isEmpty()) {
            return ResponseEntity.badRequest().body("DeviceId missing");
        }


        HashMap<String, Object> map = new HashMap<>();
//        map.put("deviceId", deviceId);
        map.put("role", role);
        map.put("platform", "MOBILE");

        if ("PAID".equalsIgnoreCase(request.getUserType())) {
            if (!"1234".equals(pin)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid PIN");
            }
//            map.put("role", "PAID");
        } else {
//            map.put("role", "GUEST");
        }

//        map.put("pin", pin); // Store pin in map


        try {
            String token = jwtService.generateToken(map, pin, deviceId, "");
            return ResponseEntity.ok(Collections.singletonMap("token", token));

        } catch (Exception e) {
            System.err.println("JWT generation error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Could not generate token");
        }
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader,
                                          @RequestHeader(value = "x-user-role", required = false) String role,
                                          HttpServletResponse response) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token missing");
            }
            String oldToken = authHeader.substring(7);
            // read subject without expiry validation

            String subject = jwtService.extractSubjectWithoutValidation(oldToken);
            System.out.println("OLD SUBJECT : " + subject);

            String[] parts = subject.split("\\|");

            if (parts.length < 2) {
                return ResponseEntity.status(401).body("Invalid token");
            }

            String deviceId = parts[1];
            String pin = parts.length > 2
                    ? parts[2]
                    : "";
            User user = sellerUserRepo.findBydeviceId(deviceId);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not found");
            }

            // rebuild secret same as validation
            String secret = (pin != null && !pin.isBlank()) ? pin + deviceId : deviceId;

            // verify old token signature

            Claims oldClaims = jwtService.parseClaimsAllowExpired(oldToken, secret);

            HashMap<String, Object> claims = new HashMap<>();
            claims.put("role", role);
            claims.put("platform", "MOBILE");

            String newToken = JwtService.generateToken(claims, pin, deviceId, "");

            response.setHeader("Access-Control-Expose-Headers", "Authorization");
            response.setHeader("Authorization", "Bearer " + newToken);

            return ResponseEntity.ok(Map.of("message", "Token refreshed"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Refresh failed"));
        }
    }
}



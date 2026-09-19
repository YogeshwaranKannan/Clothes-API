package Farme_rich.Notification;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${whatsapp.token}")
    private String TOKEN;
    @Value("${whatsapp.phoneID}")
    private String PHONE_NUMBER_ID;


    @PostConstruct
    public void init() {
        System.out.println("Token loaded = " + (TOKEN != null));
        System.out.println("PhoneNumberId = " + PHONE_NUMBER_ID);
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(TOKEN);
        return headers;
    }

    private ResponseEntity<String> errorResponse(String message) {
        return ResponseEntity.badRequest().body(message);
    }

    private String formatPhoneNumber(String mobile) {

        if (mobile == null) {
            return null;
        }

        mobile = mobile.replaceAll("\\s+", "")
                .replace("-", "")
                .replace("+", "");

        if (mobile.startsWith("91")) {
            return mobile;
        }

        return "91" + mobile;
    }

    private ResponseEntity<String> sendMessage(Map<String, Object> payload) {

        String apiUrl =
                "https://graph.facebook.com/v20.0/"
                        + PHONE_NUMBER_ID
                        + "/messages";

        log.info("API_URL: {}", apiUrl);
        log.info("Payload: {}", payload);

        try {
            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(payload, getHeaders());

            ResponseEntity<String> response = restTemplate.postForEntity(
                    apiUrl,
                    request,
                    String.class
            );
            log.info("WhatsApp Status: {}", response.getStatusCode());
            log.info("WhatsApp Response: {}", response.getBody());
            return response;

        } catch (HttpClientErrorException ex) {

            String errorBody = ex.getResponseBodyAsString();

            log.error("WhatsApp API Error: {}", errorBody, ex);

            return ResponseEntity
                    .status(ex.getStatusCode())
                    .body("WhatsApp API Error : " + errorBody);
        } catch (Exception ex) {

            log.error("Unexpected WhatsApp Error", ex);

            return ResponseEntity
                    .internalServerError()
                    .body("Unexpected WhatsApp Error : " + ex.getMessage());
        }
    }

    public ResponseEntity<String> sendOtp(String to, String customerNumber, String otp) {

        if (to == null || to.trim().isEmpty()) {
            return errorResponse("Mobile number is required");
        }

        if (otp == null || otp.trim().isEmpty()) {
            return errorResponse("OTP is required");
        }

        to = formatPhoneNumber(to);

        if (!to.matches("^91\\d{10}$")) {
            return errorResponse("Invalid mobile number: " + to);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("to", to);
//        payload.put("to", "916383111439");
        payload.put("type", "template");

        Map<String, Object> template = new HashMap<>();
        template.put("name", "otp_authentication");
        template.put("language", Map.of("code", "en"));

        List<Map<String, Object>> components = new ArrayList<>();
        components.add(Map.of(
                "type", "body",
                "parameters", List.of(
//                        Map.of("type", "text", "text", customerNumber),
                        Map.of("type", "text", "text", otp)
                )
        ));
        components.add(Map.of(
                "type", "button",
                "sub_type", "url",
                "index", "0",
                "parameters", List.of(
                        Map.of("type", "text", "text", otp)
                )
        ));

        template.put("components", components);
        payload.put("template", template);

        return sendMessage(payload);
    }

    //    public ResponseEntity<String> sendOrderConfirmation(String to, String customerName, String companyName, String orderRef, String link, String save_id) {
//        if (to == null || to.trim().isEmpty()) {
//            return errorResponse("Mobile number missing");
//        }
//
//        if (orderRef == null || orderRef.trim().isEmpty()) {
//            return errorResponse("Order reference missing");
//        }
//
//        to = formatPhoneNumber(to);
//
//        if (!to.matches("^91\\d{10}$")) {
//            return errorResponse("Invalid mobile number: " + to);
//        }
//
//        customerName = customerName == null ? "" : customerName;
//        companyName = companyName == null ? "" : companyName;
//        link = link == null ? "" : link;
//
//        Map<String, Object> payload = new HashMap<>();
//        payload.put("messaging_product", "whatsapp");
//        payload.put("to", to);
//
//        payload.put("type", "template");
//
//        Map<String, Object> template = new HashMap<>();
//        template.put("name", "order_confrimation");
//        template.put("language", Map.of("code", "en"));
//
//        List<Map<String, Object>> components = new ArrayList<>();
//
//
//        components.add(Map.of(
//                "type", "header",
//                "parameters", List.of(
//                        Map.of("type", "text", "text", orderRef)
//                )
//        ));
//
//
//        components.add(Map.of(
//                "type", "body",
//                "parameters", List.of(
//                        Map.of("type", "text", "text", customerName),
//                        Map.of("type", "text", "text", companyName),
//                        Map.of("type", "text", "text", orderRef),
//                        Map.of("type", "text", "text", link)
//                )
//        ));
//
//        String viewDetailsLink = "CustomerTemplate.html?orderid=" + save_id;
//        components.add(Map.of(
//                "type", "button",
//                "sub_type", "url",
//                "index", "0",
//                "parameters", List.of(
//                        Map.of("type", "text", "text", viewDetailsLink)
//                )
//        ));
//
//        template.put("components", components);
//        payload.put("template", template);
//
//        return sendMessage(payload);
//    }
    public ResponseEntity<String> sendInvoiceDetails(
            String to,
            String customerName,
            String companyName,
            double orderValue,
            String companyDisplayName,
            String supportNumber,
            String saveId) {

        if (to == null || to.trim().isEmpty()) {
            return errorResponse("Mobile number missing");
        }

        if (saveId == null || saveId.trim().isEmpty()) {
            return errorResponse("Order ID missing");
        }

        to = formatPhoneNumber(to);

        if (!to.matches("^91\\d{10}$")) {
            return errorResponse("Invalid mobile number: " + to);
        }

        customerName = customerName == null ? "" : customerName;
        companyName = companyName == null ? "" : companyName;
        orderValue = orderValue;
        companyDisplayName = companyDisplayName == null ? "" : companyDisplayName;
        supportNumber = supportNumber == null ? "" : supportNumber;

        Map<String, Object> payload = new HashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("to", to);
        payload.put("type", "template");

        Map<String, Object> template = new HashMap<>();
        template.put("name", "invoice_details");
        template.put("language", Map.of("code", "en"));

        List<Map<String, Object>> components = new ArrayList<>();

        // BODY PARAMETERS
        components.add(Map.of(
                "type", "body",
                "parameters", List.of(
                        Map.of("type", "text", "text", customerName),       // {{1}}
                        Map.of("type", "text", "text", companyName),        // {{2}}
                        Map.of("type", "text", "text", orderValue),         // {{3}}
                        Map.of("type", "text", "text", companyDisplayName), // {{4}}
                        Map.of("type", "text", "text", supportNumber)       // {{5}}
                )
        ));

        // URL BUTTON PARAMETER
        components.add(Map.of(
                "type", "button",
                "sub_type", "url",
                "index", "0",
                "parameters", List.of(
                        Map.of(
                                "type", "text",
                                "text", saveId // replaces {{1}} in button URL
                        )
                )
        ));

        template.put("components", components);
        payload.put("template", template);

        return sendMessage(payload);
    }

    // -------------------------
    // 3. Order Update
    // -------------------------
    public ResponseEntity<String> sendOrderStatus(
            String to,
            String customerName,
            String orderRef,
            String sellerName,
            String status,
            String expectedDelivery,
            String companyName,
            String contactNumber) {

        if (to == null || to.trim().isEmpty()) {
            return errorResponse("Mobile number missing");
        }

        if (orderRef == null || orderRef.trim().isEmpty()) {
            return errorResponse("Order reference missing");
        }

        if (status == null || status.trim().isEmpty()) {
            return errorResponse("Order status missing");
        }

        to = formatPhoneNumber(to);

        if (!to.matches("^91\\d{10}$")) {
            return errorResponse("Invalid mobile number: " + to);
        }

        customerName = customerName == null ? "" : customerName;
        sellerName = sellerName == null ? "" : sellerName;
        expectedDelivery = expectedDelivery == null ? "" : expectedDelivery;
        companyName = companyName == null ? "" : companyName;
        contactNumber = contactNumber == null ? "" : contactNumber;

        Map<String, Object> payload = new HashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("to", to);
        payload.put("type", "template");

        Map<String, Object> template = new HashMap<>();
        template.put("name", "order_status");
        template.put("language", Map.of("code", "en"));

        List<Map<String, Object>> components = new ArrayList<>();

        components.add(Map.of(
                "type", "body",
                "parameters", List.of(
                        Map.of("type", "text", "text", customerName),      // {{1}}
                        Map.of("type", "text", "text", orderRef),          // {{2}}
                        Map.of("type", "text", "text", sellerName),        // {{3}}
                        Map.of("type", "text", "text", status),            // {{4}}
                        Map.of("type", "text", "text", expectedDelivery),  // {{5}}
                        Map.of("type", "text", "text", companyName),       // {{6}}
                        Map.of("type", "text", "text", contactNumber)      // {{7}}
                )
        ));

        template.put("components", components);
        payload.put("template", template);

        return sendMessage(payload);
    }


}

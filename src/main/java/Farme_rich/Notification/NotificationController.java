package Farme_rich.Notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({
        "/api/seller/notifications",
        "/api/buyer/notifications"
})
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);
    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @PostMapping("/otp")
    public ResponseEntity<String> sendOtp(@RequestParam String to, @RequestParam String customerNumber,
                                          @RequestParam String otp) {
        return service.sendOtp(to, customerNumber, otp);
    }

    @PostMapping("/order-confirmation")
    public ResponseEntity<String> sendOrderConfirmation(
            @RequestParam String to,
            @RequestParam String customerName,
            @RequestParam String companyName,
            @RequestParam double orderValue,
            @RequestParam String companyDisplayName,
            @RequestParam String save_id,
            @RequestParam String supportNumber) {


        log.info("to={}", to);
        log.info("customerName={}", customerName);
        log.info("companyName={}", companyName);
        log.info("orderValue={}", orderValue);
        log.info("companyDisplayName={}", companyDisplayName);
        log.info("SupportNumber={}", supportNumber);
        log.info("OrderId={}", save_id);

        return service.sendInvoiceDetails(to, customerName, companyName, orderValue, companyDisplayName, supportNumber, save_id);


    }


    @PostMapping("/order-update")
    public ResponseEntity<String> sendOrderUpdate(
            @RequestParam String to,
            @RequestParam String customerName,
            @RequestParam String orderRef,
            @RequestParam String sellerName,
            @RequestParam String status,
            @RequestParam String expectedDelivery,
            @RequestParam String companyName,
            @RequestParam String contactNumber
    ) {
        try {

            log.info(
                    "Order Update WhatsApp -> to={}, orderRef={}, status={}",
                    to,
                    orderRef,
                    status
            );

            return service.sendOrderStatus(
                    to,
                    customerName,
                    orderRef,
                    sellerName,
                    status,
                    expectedDelivery,
                    companyName,
                    contactNumber
            );

        } catch (Exception ex) {

            log.error("Order Update WhatsApp Failed", ex);

            return ResponseEntity
                    .internalServerError()
                    .body("Failed to send WhatsApp notification");
        }
    }


}

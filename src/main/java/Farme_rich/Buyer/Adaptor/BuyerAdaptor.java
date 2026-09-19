package Farme_rich.Buyer.Adaptor;

import Farme_rich.Buyer.DTO.Request.BuyerOrderRequest;
import Farme_rich.Buyer.DTO.Request.RazorPaymentDetailsDTO;
import Farme_rich.Buyer.DTO.Response.*;
import Farme_rich.Seller.DTO.Request.CustomerInformationDTO;
import Farme_rich.Seller.DTO.Request.PaymentsDTO;
import Farme_rich.Seller.Model.BackEnd.*;
import Farme_rich.Seller.Model.FrontEnd.Seller;
import com.razorpay.Payment;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class BuyerAdaptor {


    private static String SavecurrentDate = getTodayDate();
    private static char SavecurrentAlphabet = 'A';
    private static int Savecount = 1;

    public static BuyerProductResponse ToBuyerProductResponse(Products products, Inventory inventory, MessageResponse msgResponse) {
        BuyerProductResponse buyerProductResponse = new BuyerProductResponse();
        if (inventory != null) {
            buyerProductResponse.inventoryData = inventory;
        }
        if (products != null) {
            buyerProductResponse.setProductName(products.getProductName());
            buyerProductResponse.setProductDescription(products.getProductDescription());

            buyerProductResponse.setImgurl(products.getImage_url());
        }
        buyerProductResponse.ResponseMessage = msgResponse;
        return buyerProductResponse;
    }

    public static List<GetCategoriesResponse> FromProductCategoriesToGetCategories(List<ProductCategory> productCategories) {
        List<GetCategoriesResponse> ans = new ArrayList<GetCategoriesResponse>();
        for (ProductCategory i : productCategories) {
            GetCategoriesResponse getCategoriesResponse = new GetCategoriesResponse();
            getCategoriesResponse.id = String.valueOf(i.get_id());
            getCategoriesResponse.category = i.getCategory_name();
            getCategoriesResponse.coverImage = i.getCoverImagePath();
            getCategoriesResponse.parent = String.valueOf(i.getParent_id());
            getCategoriesResponse.isDisplayOnHome = i.isDisplayOnHomePage();
            getCategoriesResponse.Description = i.getDescription();
//            getCategoriesResponse.sellerid=i.getSellerid();
            ans.add(getCategoriesResponse);
        }
        return ans;
    }

    private static String getTodayDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return LocalDate.now().format(formatter);
    }

    public static synchronized String TosaveReferenceNo(Save save) {
        String today = getTodayDate();
        if (save != null && save.getRefno() != null) {
            String lastRef = save.getRefno().trim();
            if (!lastRef.isEmpty() && lastRef.contains("B") && lastRef.length() >= 15) { //20260331-BA0001
                try {
                    String lastDate = lastRef.substring(0, 8);   // yyyyMMdd

                    char lastAlphabet = lastRef.charAt(10);      // A
                    int lastCount = Integer.parseInt(lastRef.substring(12)); // 0001

                    if (!lastDate.equals(today)) {
                        // New day reset
                        SavecurrentDate = today;
                        SavecurrentAlphabet = 'A';
                        Savecount = 1;
                    } else {
                        // Same day increment
                        SavecurrentDate = lastDate;
                        SavecurrentAlphabet = lastAlphabet;
                        Savecount = lastCount + 1;

                        if (Savecount > 9999) {
                            Savecount = 1;
                            SavecurrentAlphabet = (SavecurrentAlphabet < 'Z') ? (char) (SavecurrentAlphabet + 1) : 'A';
                        }
                    }

                } catch (Exception e) {
                    //  Corrupt refno → reset safely
                    reset(today);
                }

            } else {
                // Invalid / empty refno → reset
                reset(today);
            }

        } else {
            // First record
            reset(today);
        }

        return SavecurrentDate + "-" + "B" + SavecurrentAlphabet + String.format("%04d", Savecount);
    }

    private static void reset(String today) {
        SavecurrentDate = today;
        SavecurrentAlphabet = 'A';
        Savecount = 1;
    }


    public static Payments fromPaymentsDTOtoPayments(PaymentsDTO paymentsDTO) {

        Payments payments = new Payments();
        payments.setOrder_created_At(LocalDateTime.now().toString());

        payments.setPayment_mode(paymentsDTO.payment_mode);
        payments.setCurrent_payment_amount(paymentsDTO.isAdvance ? paymentsDTO.advanceAmt : paymentsDTO.payment_amount);
        payments.setEMI(paymentsDTO.isEMI);
        payments.setNo_of_installments(paymentsDTO.no_of_installments);
        payments.setTotal_amount(paymentsDTO.total_amount);
        payments.setAdvance(paymentsDTO.isAdvance);
        if (payments.isAdvance()) payments.setAdvanceAmt(paymentsDTO.advanceAmt);

        payments.setPayment_date(LocalDateTime.now().toString());
        payments.setDiscount(paymentsDTO.discount);

        payments.setNext_payment_due(paymentsDTO.next_payment_due);
        return payments;

    }


    public static Save TosaveModel(BuyerOrderRequest buyerOrderRequest, List<Inventory> finalItems, String now, String refRequest, Save latest, CustomerInformationDTO customerInformationDTO, PaymentsDTO paymentsDTO) {
        Save save = new Save();

        save.setOrderStatus("Draft");
        if (buyerOrderRequest.orderThreshold != 0 && buyerOrderRequest.deliveryFee != 0) {
            save.setDeliveryFeeDetails(new Seller.DeliveryFeeDetails(buyerOrderRequest.deliveryFee, buyerOrderRequest.orderThreshold));
        }
        save.setCompanyid(buyerOrderRequest.companyid);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");


        save.setUpdatedAt(LocalDateTime.now().format(formatter));
        save.setOrderDate(new Date());
        save.setRefno((refRequest == null || refRequest == "") ? TosaveReferenceNo(latest) : refRequest);
        save.setTotal_Discount(paymentsDTO.discount);
        save.setTotal_Paid(paymentsDTO.payment_amount);
        save.setTotal_Amount(paymentsDTO.total_amount);


        save.setSeller_items(finalItems);
        save.setIsgst(buyerOrderRequest.isgst);
        save.setIspriceInclusive(buyerOrderRequest.ispriceInclusive);

        return save;
    }

    public static List<ItemsDTO> FromInventorytoInventoryDTO(List<Inventory> inventories) {
        List<ItemsDTO> items = new ArrayList<ItemsDTO>();
        for (Inventory i : inventories) {
            ItemsDTO itemsDTO = new ItemsDTO();
            itemsDTO.item_name = i.getProductName();
            if (i.getVariantBatches() != null && i.getVariantBatches().size() > 0) {
                Batches getOrderedVariant = i.getVariantBatches().get(0);
                itemsDTO.item_name += "-" + getOrderedVariant.getVariantName();
            }
            itemsDTO.sellerPrice = (int) i.getSellerprice();
            itemsDTO.offerPrice = i.getOfferprice();
            itemsDTO.qty = i.Order_quantity;
            itemsDTO.inv_id = i.get_id();
            itemsDTO.productId = String.valueOf(i.getProductid());
            itemsDTO.image_url = i.getDefaultImage();
            itemsDTO.isService = (i.getFlag() != null && i.getFlag().equals("isService")) ? true : false;
            items.add(itemsDTO);
        }
        return items;
    }

    public static PaymentDTO FromPaymenttoPaymentDTO(Payments payments) {
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.date = payments.getPayment_date();
        paymentDTO.status = payments.getPayment_status();
        return paymentDTO;
    }

    public static String maskValue(String input) {
        if (input == null || input.length() <= 6) {
            return "XXX";
        }
        String first = input.substring(0, 3);
        String last = input.substring(input.length() - 3);
        return first + "XXX" + last;
    }

    public static CustomerInfoDTO FromCustomerInformationtoCustomerInformationDTO(CustomerInformation customerInformation) {
        CustomerInfoDTO customerInfoDTO = new CustomerInfoDTO();
        customerInfoDTO.name = customerInformation.getCustomerName();
        customerInfoDTO.email = maskValue(customerInformation.getEmail());
        customerInfoDTO.phone = maskValue(customerInformation.getCustomerMobileNum());
        return customerInfoDTO;
    }

    public static ViewOrderDetailsResponse ToViewOrderDetailsResponse(Save save, HashMap<String, Integer> map) {
        ViewOrderDetailsResponse viewOrderListResponse = new ViewOrderDetailsResponse();
        if (save != null) {
            viewOrderListResponse.order_id = save.getRefno();

            viewOrderListResponse.order_status = save.getOrderStatus();

            viewOrderListResponse.tot_order_value = String.valueOf(save.getTotal_Amount());

            viewOrderListResponse.rewardsDTO = new GetSellerDetailsResponse.RewardsDTO();
            viewOrderListResponse.rewardsDTO = FromRewardsToRewardsDTO(save.getRewards());

            viewOrderListResponse.items = FromInventorytoInventoryDTO(save.getSeller_items());
            viewOrderListResponse.payment = FromPaymenttoPaymentDTO(save.getPaymentDetails());
            viewOrderListResponse.payment.paymentID = save.getRazorpaymentdetails() == null ? "" : save.getRazorpaymentdetails().getPayment_id();
            viewOrderListResponse.customer = FromCustomerInformationtoCustomerInformationDTO(save.getCustomerInformation());

            viewOrderListResponse.delivery = new DeliveryDTO();
            viewOrderListResponse.delivery.address = maskValue(save.getCustomerInformation().getDeliveryAddress());
            viewOrderListResponse.delivery.city = save.getCustomerInformation().getCity();
            viewOrderListResponse.delivery.expected_date = save.getDeliveryDueDate();


        }
        return viewOrderListResponse;
    }

    public static GetSellerDetailsResponse.RewardsDTO FromRewardsToRewardsDTO(Save.Rewards rewards) {
        GetSellerDetailsResponse.RewardsDTO ans = new GetSellerDetailsResponse.RewardsDTO();
        if (rewards == null) return ans;
        ans.RewardPoints = rewards.getRewardsEarned();
        ans.RedeemPoints = rewards.getRewardsRedeemed();
        return ans;
    }

    public static RazorPaymentDetails ToRazorPaymentModel(JSONObject details) {
        RazorPaymentDetails razorPaymentDetails = new RazorPaymentDetails();

        razorPaymentDetails.setId(details.getString("id"));


        razorPaymentDetails.setReceipt(details.getString("receipt"));
        razorPaymentDetails.setAmount_paid(0);
        razorPaymentDetails.setAttempts(details.getInt("attempts"));
        razorPaymentDetails.setCurrency(details.getString("currency"));
        razorPaymentDetails.setAmount(details.getInt("amount"));
        razorPaymentDetails.setAmount_due(details.getInt("amount"));
        razorPaymentDetails.setCreated_at(details.getInt("created_at"));
        razorPaymentDetails.setEntity("order");
        razorPaymentDetails.setStatus(details.getBoolean("success") ? "created" : "failed");

        return razorPaymentDetails;
    }

    public static RazorPaymentDetails ToRazorPaymentDetailsDTOtoRazorPaymentDetails(RazorPaymentDetails exist, RazorPaymentDetailsDTO dto) {

        RazorPaymentDetails r = new RazorPaymentDetails();

        if (exist != null) {
            r.setId(exist.getId());
            r.setReceipt(exist.getReceipt());
            r.setAttempts(exist.getAttempts());
            r.setAmount(exist.getAmount());
            r.setAmount_due(exist.getAmount_due());
            r.setAmount_paid(exist.getAmount());
        }

        r.setPayment_id(dto.id);
        r.setEntity(dto.entity);
        r.setStatus(dto.status);
        r.setCurrency(dto.currency);
        r.setAmount_refunded(dto.amount_refunded);
        r.setRefund_status(dto.refund_status);
        r.setMethod(dto.method);
        r.setDescription(dto.description);
        r.setEmail(dto.email);
        r.setContact(dto.contact);
        r.setFee(dto.fee);
        r.setTax(dto.tax);
        r.setInternational(dto.international);
        r.setInvoice_id(dto.invoice_id);
        r.setCreated_at(dto.created_at);

        // ✅ Error fields
        r.setError_code(dto.error_code);
        r.setError_description(dto.error_description);
        r.setError_reason(dto.error_reason);

        // ✅ Acquirer data (null-safe)
        if (dto.acquirer_data != null) {
            AcquirerData a = new AcquirerData();
            a.setBank_transaction_id(dto.acquirer_data.getBank_transaction_id());
            r.setAcquirer_data(a);
        }

        return r;
    }

    public static RazorPaymentDetailsDTO ToRazorPaymentDetailsDTO(Payment payment) {

        RazorPaymentDetailsDTO dto = new RazorPaymentDetailsDTO();
        if (payment == null) return dto;

        JSONObject json = payment.toJson();

        dto.id = json.optString("id");
        dto.paymentId = json.optString("id");
        dto.receipt = json.optString("receipt");

        dto.amount = json.optInt("amount");
        dto.amount_paid = json.optInt("amount_paid");
        dto.amount_due = json.optInt("amount_due");
        dto.created_at = json.optInt("created_at");

        dto.currency = json.optString("currency");
        dto.status = json.optString("status");
        dto.attempts = json.optInt("attempts");
        dto.entity = json.optString("entity");

        // 🔹 Acquirer Data
        JSONObject acquirerJson = json.optJSONObject("acquirer_data");
        if (acquirerJson != null) {
            AcquirerData acquirer = new AcquirerData();
            acquirer.setBank_transaction_id(acquirerJson.optString("bank_transaction_id"));
//            acquirer.rrn = acquirerJson.optString("rrn");
//            acquirer.upi_transaction_id = acquirerJson.optString("upi_transaction_id");
            dto.acquirer_data = acquirer;
        }

        dto.amount_refunded = json.optInt("amount_refunded");
        dto.bank = json.optString("bank");
        dto.contact = json.optString("contact");
        dto.description = json.optString("description");
        dto.email = json.optString("email");

        dto.error_code = json.optString("error_code");
        dto.error_description = json.optString("error_description");
        dto.error_reason = json.optString("error_reason");

        dto.fee = json.optInt("fee");
        dto.international = json.optBoolean("international");

        // 🔹 invoice_id (String → ObjectId)
        String invoiceIdStr = json.optString("invoice_id");
        if (invoiceIdStr != null && !invoiceIdStr.isEmpty()) {
            try {
                dto.invoice_id = new ObjectId(invoiceIdStr);
            } catch (Exception e) {
                dto.invoice_id = null;
            }
        }

        dto.method = json.optString("method");
        dto.order_id = json.optString("order_id");
        dto.refund_status = json.optString("refund_status");

        dto.captured = String.valueOf(json.optBoolean("captured"));
        dto.tax = json.optInt("tax");

        return dto;
    }

    public static List<ItemsDTO> FromInventorytoItemsDTO(List<Inventory> inventory) {
        List<ItemsDTO> ans = new ArrayList<>();
        for (Inventory inv : inventory) {
            ItemsDTO itemsDTO = new ItemsDTO();
            if (inv.getFlag() == null) {
                itemsDTO.item_name = inv.getProductName();
                itemsDTO.inv_id = inv.get_id();
                itemsDTO.productId = String.valueOf(inv.getProductid());
                String.valueOf(itemsDTO.qty = inv.Order_quantity);
                itemsDTO.batch_id = inv.getBatch_Id();
                itemsDTO.sellerPrice = inv.getSellerprice();
                itemsDTO.offerPrice = inv.getOfferprice();
                itemsDTO.image_url = inv.getDefaultImage();
                itemsDTO.isService = false;
            } else {
                itemsDTO.item_name = inv.getServicename();
                itemsDTO.productId = String.valueOf(inv.getProductid());
                itemsDTO.qty = inv.Order_quantity;
                itemsDTO.inv_id = inv.get_id();
                itemsDTO.sellerPrice = inv.getSellerprice();
                itemsDTO.offerPrice = inv.getOfferprice();
                itemsDTO.image_url = inv.getDefaultImage();
                itemsDTO.isService = true;
            }
            ans.add(itemsDTO);
        }
        return ans;
    }


}

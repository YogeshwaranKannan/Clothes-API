package Farme_rich.Seller.Adaptor;


import Farme_rich.Buyer.DTO.Request.RazorPaymentDetailsDTO;
import Farme_rich.Controllers.SellerController;
import Farme_rich.Seller.DTO.Request.*;
import Farme_rich.Seller.DTO.Response.*;
import Farme_rich.Seller.Model.BackEnd.*;
import Farme_rich.Seller.Model.FrontEnd.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class SellerAdaptor {
    private static final Logger log = LoggerFactory.getLogger(SellerController.class);
    public static String baseUrl = "https://dev.fr.api.intellesyde.com";
    private static String currentBatchDate = getTodayDate();
    private static char batchAlphabet = 'B';
    private static int batchCount = 1;
    private static String currentDate = getTodayDate();
    private static char currentAlphabet = 'A';
    private static int count = Integer.numberOfLeadingZeros(3);
    private static String SavecurrentDate = getTodayDate();
    private static char SavecurrentAlphabet = 'A';
    private static int Savecount = 1;

    public static Seller ToUserLogin(UserSignupRequest dto) {
        Seller seller = new Seller();
        seller.setCompanyname(dto.getCompanyname());
        seller.setSeller_type("PRODUCTS");
        seller.setCompanyMobile(dto.getMobileNum());
        if (dto.getUserid() != null) {
            List<String> ids = new ArrayList<>();
            ids.add(dto.getUserid());
            seller.setUserid(ids);
        }
        seller.setEmail(dto.getEmail());

        return seller;
    }

    public static User ToUserSignup(UserSignupRequest dto) {
        User user = new User();
        user.setMobileNum(dto.getMobileNum());
        user.setDeviceId(dto.getDeviceId());
        user.setCreateAT(dto.created_at);
        user.setFirstname(dto.getFirstname());
        user.setLastname(dto.getLastname());
        user.setEmail(dto.getEmail());
        user.setLanguage_preferred(dto.language_preferred);
        if (dto.getFirstPiN() != null && !dto.getFirstPiN().isEmpty()) {
            user.setFirstPiN(dto.getFirstPiN());
        }
        if (dto.getRole() != null && !dto.getRole().isEmpty()) {
            user.setRole(List.of(dto.getRole()));
        }


        return user;
    }

    public static synchronized String generateBatchId(String lastBatchId) {
        String today = getTodayDate();

        if (lastBatchId != null && lastBatchId.startsWith("BATCH-")) {

            String lastDate = lastBatchId.substring(6, 14); // yyyyMMdd
            char lastAlphabet = lastBatchId.charAt(15);
            int lastCount = Integer.parseInt(lastBatchId.substring(16));

            if (!lastDate.equals(today)) {
                // New day → reset
                currentBatchDate = today;
                batchAlphabet = 'B';
                batchCount = 1;
            } else {
                // Same day → continue
                currentBatchDate = lastDate;
                batchAlphabet = lastAlphabet;
                batchCount = lastCount + 1;

                if (batchCount > 1000) {
                    batchCount = 1;
                    if (batchAlphabet < 'Z') {
                        batchAlphabet++;
                    } else {
                        batchAlphabet = 'B'; // Restart after Z
                    }
                }
            }
        } else {
            // No previous batch → start fresh
            currentBatchDate = today;
            batchAlphabet = 'B';
            batchCount = 1;
        }

        return "BATCH-" + currentBatchDate + "-" + batchAlphabet + String.format("%04d", batchCount);
    }

    public static Batches ToBatches(SellerBatchesRequest dto, String lastbatchID) {
        LocalDateTime now = LocalDateTime.now();
        double a = dto.getProcurement_price();

        Batches batches = new Batches();
        batches.setBatch_Id(generateBatchId(lastbatchID));
        batches.setProductname(dto.getProductname());
        batches.setBatch_unit(dto.getBatch_unit() == null ? "piece" : dto.getBatch_unit());
        batches.setBatch_expiryDate(dto.getBatch_expiryDate());

        batches.setSeller_price(dto.getSeller_price());
        batches.setCompanyid(dto.getCompanyid());
        batches.setUnit(dto.getUnit());
        batches.setCreatedAt(now.toString());
        batches.setUpdatedAt(now.toString());
        batches.setActive(true);
        batches.setMinimum_order(dto.getMinimumorder());
        batches.setExpiry_date(dto.getExpiry());
        batches.setMargin_percentage(dto.getMargin());
        batches.setProductname(dto.getProductname());
        batches.setVariantName(dto.variantName);
        batches.setOfferPrice(dto.offerPrice);
        batches.setDiscount(dto.discount);
        batches.setProcurement_price(dto.getProcurement_price());
        log.info(dto.getProductid());
        batches.setProductid(new ObjectId(dto.getProductid()));
        batches.setStock_availability(dto.getStock_availability());
        return batches;
    }

    public static Batches ToResponseBatch(Batches batch) {
        Batches batches = new Batches();
        batches.set_id(batch.get_id());
        batches.setBatch_unit(batch.getBatch_unit());
        batches.setBatch_expiryDate(batch.getBatch_expiryDate());
        batches.setSeller_price(batch.getSeller_price());
        batches.setCompanyid(batch.getCompanyid());
        batches.setUnit(batch.getUnit());
        batches.setCreatedAt(batches.getCreatedAt());
        batches.setUpdatedAt(batches.getUpdatedAt());
        batches.setActive(batch.getActive());
        batches.setMinimum_order(batch.getMinimum_order());
        batches.setExpiry_date(batch.getExpiry_date());
        batches.setMargin_percentage(batch.getMargin_percentage());
        batches.setProcurement_price(batch.getProcurement_price());
        batches.setProductid(batch.getProductid());
        batches.setStock_availability(batch.getStock_availability());
        batches.setVariantName(batch.getVariantName());
        batches.setOfferPrice(batch.getOfferPrice());
        batches.setDiscount(batch.getDiscount());
        batches.setVersion(batch.getVersion());
        return batches;
    }

    public static List<Inventory> FromInventoryDTOtoInventory(List<InventoryDTO> dto) {
        List<Inventory> ans = new ArrayList<>();
        for (InventoryDTO i : dto) {
            Inventory inventory = new Inventory();
            inventory.set_id(i.get_id());
            inventory.setUniqueID(i.uniqueID);
            inventory.setCompanyid(i.getCompanyid());
            if (i.variantName != null && !i.variantName.isBlank()) {
                inventory.setProductName(i.productname + "-" + i.variantName);
            } else inventory.setProductName(i.productname);
            inventory.setCreated_at(i.created_at);
            inventory.setDefaultImage(i.defaultImage);
            inventory.setUnique_batch_ID(i.batch_StringID);
            inventory.setBatch_Id(i.batch_id);
            inventory.setSellerprice(i.sellerprice);
            inventory.setOfferprice(i.offerPrice);
            if (i.getProductid() != null) {
                inventory.setProductid(new ObjectId(String.valueOf(i.getProductid())));
            } else {
                log.info("Skipping invalid productid: " + i.getProductid());
            }
            if (i.flag != null && !i.flag.isEmpty()) inventory.setFlag(i.flag);

            inventory.setDiscount_percentage(i.getDiscount_percentage());
            inventory.setCreated_at(i.getCreated_at());

            inventory.setGst(i.gst);
            inventory.setUpdated_at(i.getUpdated_at());
            inventory.setAvailable_properties(i.available_properties);

            List<Batches> variantBatchList = new ArrayList<>();
            if (i.variantBatches != null && !i.variantBatches.isEmpty()) {
                for (BatchesDTO batchDTO : i.variantBatches) {
                    Batches batches = new Batches();
//                   batches.setStock_availability(batchDTO.stock_availability);
                    batches.set_id(batchDTO._id);
//                   batches.setProcurement_price(batchDTO.procurement_price);
                    batches.setSeller_price(batchDTO.getSeller_price());
                    inventory.setSellerprice(batchDTO.getSeller_price());
                    inventory.setOfferprice(batchDTO.offerPrice);
//                   inventory.setProductName(batchDTO.productname+"-"+batchDTO.variantName);
                    inventory.setDiscount_percentage(String.valueOf(batchDTO.discount));
//                   batches.setBatch_unit(batchDTO.getBatch_unit());
//                   batches.setActive(batchDTO.active);
//                   batches.setTo_be_deleted(batchDTO.to_be_deleted);
//                   batches.setMaufactured_date(batchDTO.maufactured_date);

                    // expiry_date safe parse
                    if (batchDTO.expiry_date != null &&
                            !batchDTO.expiry_date.isEmpty()) {

                        try {
                            batches.setExpiry_date(batchDTO.expiry_date);
                        } catch (NumberFormatException e) {
                            log.info("Invalid expiry_date format: "
                                    + batchDTO.expiry_date);

                            batches.setExpiry_date(null);
                        }

                    } else {
                        batches.setExpiry_date(null);
                    }

//                   batches.setMinimum_order(batchDTO.minimum_order);
//                   batches.setShelf_life(batchDTO.shelf_life);
//                   batches.setCreatedAt(batchDTO.created_at);
//                   batches.setUpdatedAt(batchDTO.updated_at);

//                   if (batchDTO.productid != null) {
//                       batches.setProductid(
//                               new ObjectId(batchDTO.productid)
//                       );
//                   }

//                   batches.setCompanyid(batchDTO.getCompanyid());
//                   batches.setProductname(batchDTO.getProductname());
                    batches.setUnit(batchDTO.unit);
//                   batches.setVersion(batchDTO.version);
                    batches.setMargin_percentage(
                            batchDTO.getMargin_percentage()
                    );

                    // Variant fields
                    batches.setVariantName(batchDTO.variantName);
                    batches.setOfferPrice(batchDTO.offerPrice);
                    batches.setDiscount(batchDTO.discount);

                    variantBatchList.add(batches);
                }

                inventory.setVariantBatches(variantBatchList);
            } else if (i.getBatchid() != null) {
                log.info("//FromInventoryDTOtoInventory/batchid inside");
                Batches batches = new Batches();
                batches.setStock_availability(i.batchid.stock_availability);
                batches.set_id(i.batchid._id);
                batches.setProcurement_price(i.batchid.procurement_price);
                batches.setSeller_price(i.batchid.getSeller_price());
                batches.setBatch_unit(i.batchid.getBatch_unit());
                batches.setActive(i.batchid.active);
                batches.setTo_be_deleted(i.batchid.to_be_deleted);
                batches.setMaufactured_date(i.batchid.maufactured_date);
                // ✅ Add null and empty check before parsing
                if (i.batchid.expiry_date != null && !i.batchid.expiry_date.isEmpty()) {
                    try {
                        batches.setExpiry_date(i.batchid.expiry_date);
                    } catch (NumberFormatException e) {
                        log.info("Invalid expiry_date format: " + i.batchid.expiry_date);
                        batches.setExpiry_date(null); // or some default value
                    }
                } else {
                    log.info("Expiry date is null or empty");
                    batches.setExpiry_date(null); // or a fallback default
                }
                batches.setMinimum_order(i.batchid.minimum_order);
                batches.setShelf_life(i.batchid.shelf_life);
                batches.setCreatedAt(i.batchid.created_at);
                batches.setUpdatedAt(i.batchid.updated_at);
                batches.setProductid(new ObjectId(i.batchid.productid));
                batches.setCompanyid(i.batchid.getCompanyid());
                batches.setProductname(i.batchid.getProductname());
                batches.setUnit(i.batchid.unit);
                batches.setVersion(i.batchid.version);
                batches.setMargin_percentage(i.batchid.getMargin_percentage());
                batches.setVariantName(i.batchid.variantName);
                batches.setOfferPrice(i.batchid.offerPrice);
                batches.setDiscount(i.batchid.discount);
                inventory.setBatchid(batches);
            }

            inventory.setSellerstock((int) i.sellerstock);
            if (i.getPropertyAttributes() != null) {
                inventory.setPropertyAttributes(i.getPropertyAttributes());
            }
            if (i.getServicename() != null) {
                inventory.setServicename(i.getServicename());
            }
            inventory.Order_quantity = i.Order_quantity;
            if ("isService".equals(i.flag)) {
                try {
                    inventory.setServicecost(i.servicecost);
                    inventory.setSelling_value(i.servicecost * i.Order_quantity);
                } catch (Exception e) {
                    log.info("Error calculating selling_value for service: " + e.getMessage());
                    inventory.setSelling_value(0);
                }
            } else {
                inventory.setSelling_value(i.Order_quantity * i.procurement_price);
            }

            inventory.setVersion(i.version);

            ans.add(inventory);
        }
        return ans;
    }

    public static List<InventoryDTO> FromInventorytoInventoryDTOAndPreferredSellerType(List<Inventory> dto, String seller_type) {
        List<InventoryDTO> ans = new ArrayList<>();
        for (Inventory i : dto) {
            if ((i.getFlag() == null || i.getFlag() == "" && seller_type.equals("product"))
                    || (i.getFlag().equals("isService") && seller_type.equals("service"))
                    || seller_type.equals("both")) {
                InventoryDTO inventory = new InventoryDTO();
                inventory.set_id(i.get_id());
                inventory.setCompanyid(i.getCompanyid());
                inventory.flag = i.getFlag();
                inventory.gst = i.getGst();

                inventory.setCGST(1000);
                inventory.setSGST(4000);
                inventory.setProductid(i.getProductid());
                inventory.setDiscount_percentage(i.getDiscount_percentage());
                inventory.setCreated_at(i.getCreated_at());
                inventory.setUpdated_at(i.getUpdated_at());
                inventory.available_properties = i.getAvailable_properties();
                if (i.getBatchid() != null) {
                    BatchesDTO batches = new BatchesDTO();
                    batches.setStock_availability(i.getBatchid().getStock_availability());
                    batches.set_id(i.getBatchid().get_id());
                    batches.setProcurement_price(i.getBatchid().getProcurement_price());
                    batches.setSeller_price(i.getBatchid().getSeller_price());
                    batches.setBatch_unit(i.getBatchid().getBatch_unit());
                    batches.setActive(i.getBatchid().getActive());
                    batches.setTo_be_deleted(i.getBatchid().getTo_be_deleted());
                    batches.setMaufactured_date(i.getBatchid().getMaufactured_date());
                    batches.setMinimum_order(i.getBatchid().getMinimum_order());
                    batches.setShelf_life(i.getBatchid().getShelf_life());
                    batches.setCreated_at(i.getBatchid().getCreatedAt());
                    batches.setUpdated_at(i.getBatchid().getUpdatedAt());
                    batches.setProductid(String.valueOf(i.getBatchid().getProductid()));
                    batches.setCompanyid(i.getBatchid().getCompanyid());
                    batches.setProductname(i.getBatchid().getProductname());
                    batches.setUnit(i.getBatchid().getUnit());
                    batches.setVersion(i.getBatchid().getVersion());
                    batches.setMargin_percentage(i.getBatchid().getMargin_percentage());

                    inventory.setBatchid(batches);
                }
                if (i.getPropertyAttributes() != null) {
                    inventory.setPropertyAttributes(i.getPropertyAttributes());
                }
                if (i.getFlag() != null && i.getFlag().equals("isService")) {
                    log.info("Is service is used");
                    inventory.servicename = i.getServicename();
                    inventory.servicecost = i.getServicecost();
                    inventory.sellerprice = (i.getOrder_quantity() * Integer.parseInt(String.valueOf(i.getServicecost())));
                } else {
                    log.info("Is Product is used");
                    inventory.sellerprice = (i.getOrder_quantity() * i.getSelling_value());
                }

                inventory.Order_quantity = i.Order_quantity;
                ans.add(inventory);
            }
        }
        return ans;
    }

    public static List<InventoryDTO> FromInventorytoInventoryDTO(List<Inventory> dto, HashMap<String, Integer> map) {

        List<InventoryDTO> ans = new ArrayList<>();
        for (Inventory i : dto) {
            InventoryDTO inventory = new InventoryDTO();
            inventory.set_id(i.get_id());
            inventory.setCompanyid(i.getCompanyid());
            inventory.flag = i.getFlag();
            inventory.productname = i.getProductName();
            inventory.uniqueID = i.getUniqueID();
            inventory.batch_StringID = i.getUnique_batch_ID();
            inventory.defaultImage = i.getDefaultImage();
            inventory.deals = i.isDeals();
            inventory.newArrivals = i.isNewArrivals();

            if (i.getProductCategory() != null) inventory.categoryId = i.getProductCategory().get_id();


            inventory.batch_id = i.getBatch_Id();
            inventory.gst = i.getGst();
            inventory.created_at = i.getCreated_at();

            inventory.setCGST(1000);
            inventory.setSGST(4000);
            inventory.setProductid(i.getProductid());
            inventory.setDiscount_percentage(i.getDiscount_percentage());
            inventory.setCreated_at(i.getCreated_at());
            inventory.setUpdated_at(i.getUpdated_at());
            log.info("Offer price for " + i.getProductName() + ": " + i.getOfferprice());
            inventory.offerPrice = i.getOfferprice();

            inventory.sellerstock =
                    map.containsKey(String.valueOf(i.getProductid()))
                            ? map.get(String.valueOf(i.getProductid()))
                            : i.getSellerstock();

            List<BatchesDTO> batchesDTOS = new ArrayList<>();

            if (i.getVariantBatches() != null && !i.getVariantBatches().isEmpty()) {

                for (Batches k : i.getVariantBatches()) {

                    BatchesDTO batches = new BatchesDTO();

                    batches.setStock_availability(k.getStock_availability());
                    batches.set_id(k.get_id());
                    batches.setProcurement_price(k.getProcurement_price());
                    batches.setSeller_price(k.getSeller_price());
                    batches.setBatch_unit(k.getBatch_unit());
                    batches.setActive(k.getActive());
                    batches.setTo_be_deleted(k.getTo_be_deleted());
                    batches.setMaufactured_date(k.getMaufactured_date());
                    batches.setMinimum_order(k.getMinimum_order());
                    batches.setShelf_life(k.getShelf_life());
                    batches.setCreated_at(k.getCreatedAt());
                    batches.setUpdated_at(k.getUpdatedAt());
                    batches.setProductid(String.valueOf(k.getProductid()));
                    batches.setCompanyid(k.getCompanyid());
                    batches.setProductname(k.getProductname());
                    batches.setUnit(k.getUnit());
                    if (k.getVersion() != null) batches.setVersion(k.getVersion());
                    batches.setMargin_percentage(k.getMargin_percentage());
                    batches.variantName = k.getVariantName();
                    batches.offerPrice = k.getOfferPrice();
                    batches.discount = k.getDiscount();

                    inventory.unit = k.getUnit();
                    inventory.expiry = k.getExpiry_date();
                    inventory.minimum_order = k.getMinimum_order();

                    batchesDTOS.add(batches);
                }
            } else if (i.getBatchid() != null) {

                Batches k = i.getBatchid();

                BatchesDTO batches = new BatchesDTO();

                batches.setStock_availability(k.getStock_availability());
                batches.set_id(k.get_id());
                batches.setProcurement_price(k.getProcurement_price());
                batches.setSeller_price(k.getSeller_price());
                batches.setBatch_unit(k.getBatch_unit());
                batches.setActive(k.getActive());
                batches.setTo_be_deleted(k.getTo_be_deleted());
                batches.setMaufactured_date(k.getMaufactured_date());
                batches.setMinimum_order(k.getMinimum_order());
                batches.setShelf_life(k.getShelf_life());
                batches.setCreated_at(k.getCreatedAt());
                batches.setUpdated_at(k.getUpdatedAt());
                batches.setProductid(String.valueOf(k.getProductid()));
                batches.setCompanyid(k.getCompanyid());
                batches.setProductname(k.getProductname());
                batches.setUnit(k.getUnit());
                batches.setVersion(k.getVersion());
                batches.setMargin_percentage(k.getMargin_percentage());
                batches.variantName = k.getVariantName();
                batches.offerPrice = k.getOfferPrice();
                batches.discount = k.getDiscount();

                inventory.unit = k.getUnit();
                inventory.expiry = k.getExpiry_date();
                inventory.minimum_order = k.getMinimum_order();

                batchesDTOS.add(batches);
            }

            inventory.variantBatches = batchesDTOS;

            if (i.getFlag() != null && i.getFlag().equals("isService")) {
                if (i.getPropertyAttributes() != null) {

                    inventory.setPropertyAttributes(FrompropertyAttributestopropertyAttributesDTO(i.getPropertyAttributes()));
                    if (inventory.available_properties == null) {
                        inventory.available_properties = new ArrayList<>();
                    }
                    if (i.getAvailable_properties() != null) {
                        inventory.available_properties.addAll(i.getAvailable_properties());
                    }
                }
                inventory.servicename = i.getServicename() == null || i.getServicename().equals("") ? i.getProductName() : i.getServicename();
                inventory.servicecost = i.getServicecost() == 0 ? i.getSellerprice() : i.getServicecost();
                inventory.sellerprice = (i.getOrder_quantity() * i.getServicecost());
                inventory.offerPrice = i.getOfferprice() == 0 ? i.getOfferprice() : i.getOfferprice();
            } else {
                inventory.sellerprice = (i.getSellerprice());
            }

            inventory.Order_quantity = i.Order_quantity;
            ans.add(inventory);
        }
        return ans;
    }

    public static List<InventoryDTO> FromInventorytoInventoryDTOForProductView(List<Inventory> dto) {
        List<InventoryDTO> ans = new ArrayList<>();
        for (Inventory i : dto) {
            InventoryDTO inventory = new InventoryDTO();
            inventory.set_id(i.get_id());
            inventory.setCompanyid(i.getCompanyid());
            inventory.flag = i.getFlag();
            inventory.productname = i.getProductName();


            inventory.gst = i.getGst();
            inventory.created_at = i.getCreated_at();
            inventory.defaultImage = i.getDefaultImage();
            log.info("DefaultImage:" + i.getDefaultImage());
            inventory.setCGST(1000);
            inventory.setSGST(4000);
            inventory.setProductid(i.getProductid());
            inventory.setDiscount_percentage(i.getDiscount_percentage());
            inventory.setCreated_at(i.getCreated_at());
            inventory.setUpdated_at(i.getUpdated_at());
            inventory.available_properties = i.getAvailable_properties();
            inventory.sellerstock = i.getSellerstock();

            if (i.getBatchid() != null) {
                BatchesDTO batches = new BatchesDTO();
                batches.setStock_availability(i.getBatchid().getStock_availability());
                batches.set_id(i.getBatchid().get_id());
                batches.setProcurement_price(i.getBatchid().getProcurement_price());
                batches.setSeller_price(i.getBatchid().getSeller_price());
                batches.offerPrice = i.getBatchid().getOfferPrice();
                batches.setBatch_unit(i.getBatchid().getBatch_unit());
                batches.setActive(i.getBatchid().getActive());
                batches.setTo_be_deleted(i.getBatchid().getTo_be_deleted());
                batches.setMaufactured_date(i.getBatchid().getMaufactured_date());
                batches.setMinimum_order(i.getBatchid().getMinimum_order());
                batches.setShelf_life(i.getBatchid().getShelf_life());
                batches.setCreated_at(i.getBatchid().getCreatedAt());
                batches.setUpdated_at(i.getBatchid().getUpdatedAt());
                batches.setProductid(String.valueOf(i.getBatchid().getProductid()));
                batches.setCompanyid(i.getBatchid().getCompanyid());
                batches.setProductname(i.getBatchid().getProductname());
                batches.setUnit(i.getBatchid().getUnit());
                batches.setVersion(i.getBatchid().getVersion());
                batches.setMargin_percentage(i.getBatchid().getMargin_percentage());

                inventory.batch_StringID = i.getBatchid().getBatch_Id();

                inventory.batch_id = i.getBatchid().get_id();
                inventory.setBatchid(batches);
            }

            if (i.getPropertyAttributes() != null) {
                inventory.setPropertyAttributes(i.getPropertyAttributes());
            }
            if (i.getFlag() != null && i.getFlag().equals("isService")) {
                log.info("Is service is used");
                inventory.servicename = i.getServicename();
                inventory.servicecost = i.getServicecost();
                inventory.sellerprice = (i.getOrder_quantity() * Integer.parseInt(String.valueOf(i.getServicecost())));
            } else {
                log.info("Is Product is used");
//                 inventory.setSellerprice(i.getOrder_quantity() * Integer.parseInt(i.getBatchid().getSeller_price()));
                inventory.sellerprice = (i.getSellerprice());
            }

            inventory.Order_quantity = i.Order_quantity;
            ans.add(inventory);
        }
        return ans;
    }

    public static UserSignupResponse ToSighUpResponseDTO(User userModel, User existUser, MessageResponse msgResponse) {
        UserSignupResponse userSignupResponse = new UserSignupResponse();
        if (userModel != null) {
            userSignupResponse.user_id = userModel.getId();
            userSignupResponse.firstName = userModel.getFirstname();
            userSignupResponse.lastName = userModel.getLastname();
            userSignupResponse.companyid = userModel.getCompanyid();
            userSignupResponse.deviceId = userModel.getDeviceId();
            userSignupResponse.userMobileNum = userModel.getMobileNum();
            userSignupResponse.isActive = userModel.isActive();
            userSignupResponse.email = userModel.getEmail();
            userSignupResponse.language_preferred = userModel.getLanguage_preferred();
            userSignupResponse.role = userModel.getRole() != null
                    ? List.copyOf(userModel.getRole())
                    : List.of();
            userSignupResponse.RegdUser = userModel.isPaidUser();
        }
        if (existUser != null) {
            userSignupResponse.deviceId = existUser.getDeviceId();
            userSignupResponse.userMobileNum = existUser.getMobileNum();
            userSignupResponse.firstName = existUser.getFirstname();
            userSignupResponse.lastName = existUser.getLastname();
            userSignupResponse.companyid = existUser.getCompanyid();
            userSignupResponse.language_preferred = existUser.getLanguage_preferred();
            userSignupResponse.isActive = existUser.isActive();
            userSignupResponse.role = existUser.getRole() != null
                    ? List.copyOf(existUser.getRole())
                    : List.of();
            userSignupResponse.RegdUser = existUser.isPaidUser();
        }
        userSignupResponse.ResponseMessage = msgResponse;
        return userSignupResponse;
    }

    public static UserSignupResponse ToLoginResponseDTO(Seller seller, User user, MessageResponse msgResponse) {
        UserSignupResponse userSignupResponse = new UserSignupResponse();
        if (seller != null) {
            List<String> ids = new ArrayList<>();
            ids.addAll(seller.getUserid());
            userSignupResponse.userid = new ArrayList<>(ids);
            userSignupResponse.companyname = seller.getCompanyname();
            userSignupResponse.companyMobileNum = seller.getCompanyMobile();
//            userSignupResponse.upi = seller.getUpi_id();
            userSignupResponse.isgst = seller.isGst_enabled();
            userSignupResponse.ispriceinclusive = seller.isPrice_inclusive_gst();
        }
        if (user != null) {
            userSignupResponse.user_id = user.getId();
            userSignupResponse.userMobileNum = user.getMobileNum();
            userSignupResponse.deviceId = user.getDeviceId();
            userSignupResponse.firstPiN = user.getFirstPiN();
            userSignupResponse.firstName = user.getFirstname();

            userSignupResponse.setRole(
                    new ArrayList<>(user.getRole())
            );
            userSignupResponse.lastName = user.getLastname();
            userSignupResponse.companyid = user.getCompanyid();
            userSignupResponse.isActive = user.isActive();
            userSignupResponse.createAT = user.getCreateAT();
            userSignupResponse.updatedAT = user.getUpdateAT();
            userSignupResponse.language_preferred = user.getLanguage_preferred();
            userSignupResponse.RegdUser = user.isPaidUser();
//            userSignupResponse.role = user.getRole() != null
//                    ? List.copyOf(user.getRole())
//                    : List.of();

        }

        userSignupResponse.ResponseMessage = msgResponse;
        return userSignupResponse;
    }

    public static UserSignupResponse ToUserRegisterResponseDTO(User userModel, Seller seller, MessageResponse msgResponse) {
        UserSignupResponse userSignupResponse = new UserSignupResponse();
        if (userModel != null) {
            userSignupResponse.user_id = userModel.getId();
            userSignupResponse.firstName = userModel.getFirstname();
            userSignupResponse.lastName = userModel.getLastname();
            userSignupResponse.companyid = userModel.getCompanyid();
            userSignupResponse.userMobileNum = userModel.getMobileNum();
            userSignupResponse.deviceId = userModel.getDeviceId();
            userSignupResponse.firstPiN = userModel.getFirstPiN();
            userSignupResponse.isActive = userModel.isActive();
            userSignupResponse.createAT = userModel.getCreateAT();
            userSignupResponse.language_preferred = userModel.getLanguage_preferred();
            userSignupResponse.updatedAT = userModel.getUpdateAT();
            userSignupResponse.RegdUser = userModel.isPaidUser();
            userSignupResponse.role = userModel.getRole() != null
                    ? List.copyOf(userModel.getRole())
                    : List.of();

        }
        if (seller != null) {
            userSignupResponse.id = seller.getId();
            userSignupResponse.userid = seller.getUserid();
            userSignupResponse.createAT = seller.getUpdateAT();
            userSignupResponse.createAT = seller.getCreateAT();
            userSignupResponse.companyname = seller.getCompanyname();
            userSignupResponse.companyMobileNum = seller.getCompanyMobile();
            if (seller.getRewards() != null) {
                userSignupResponse.rewardsDTO = new UpdateCompanyInfoRequest.RewardsDTO(seller.getRewards().getRewardAmount(),
                        seller.getRewards().getRewardPoints(), seller.getRewards().getRedeeemAmount(),
                        seller.getRewards().getRedeemPoints());
            }
        }
        userSignupResponse.ResponseMessage = msgResponse;
        log.info("adapted settled");
        return userSignupResponse;
    }

    public static SellerProductResponse ToSellerProductResponse(Products products, Inventory inventory, MessageResponse msgResponse) {
        SellerProductResponse sellerProductResponse = new SellerProductResponse();

        if (products != null) {
            sellerProductResponse.setProductName(products.getProductName());
            sellerProductResponse.setBrand(products.getBrand());
            sellerProductResponse.setProductDescription(products.getProductDescription());
//            sellerProductResponse.procumentPrice=products.getProcumentPrice();
            sellerProductResponse.setImgurl(products.getImage_url());
        }
        sellerProductResponse.ResponseMessage = msgResponse;
        return sellerProductResponse;
    }

    public static SellerBatchesResponse ToSellerBatchResponse(MessageResponse msg) {
        SellerBatchesResponse sellerBatchesResponse = new SellerBatchesResponse();
        sellerBatchesResponse.ResponseMessage = msg;
        return sellerBatchesResponse;
    }

    public static SellerGetBatchesResponse ToSellerGetBatchesResponse(List<Batches> a, int inventory_version) {
        return new SellerGetBatchesResponse(a, inventory_version);
    }

    //    public static SaveOrderResponse ToSaveResponse(String refno, List<InventoryDTO> a, ObjectId id, String isgst, String ispriceInclusive, String orderdate, Save newsave) {
//        SaveOrderResponse saveOrderResponse = new SaveOrderResponse();
//        if (a == null || a.isEmpty()) {
//            saveOrderResponse.save = newsave;
//            saveOrderResponse.refno = refno;
//            return saveOrderResponse;
//        }
//        List<String> msg = new ArrayList<>();
//        a.stream().map(i -> i.getMessage() != null && !i.getMessage().isEmpty() && msg.add(i.getMessage()));
//        //saveOrderResponse.setCompanyid(new ObjectId(a.getFirst().companyid));
//        if (!a.isEmpty() && a.size() > 0 && a.get(0).companyid != null) {
//            saveOrderResponse.setCompanyid(new ObjectId(a.get(0).companyid));
//        }
//        saveOrderResponse.order_date = orderdate;
//        saveOrderResponse.save = newsave;
//        saveOrderResponse.refno = refno;
//        saveOrderResponse.setInventory(a);
//        if (!msg.isEmpty() && msg != null) saveOrderResponse.messages.addAll(msg);
//        saveOrderResponse.ispriceInclusive = ispriceInclusive;
//        saveOrderResponse.isgst = isgst;
//        if (id != null) saveOrderResponse.save_id = id;
//        return saveOrderResponse;
//    }
    public static SaveOrderResponse ToSaveResponse(String refno, List<InventoryDTO> a, ObjectId id, String isgst, String ispriceInclusive, String orderdate, Save newsave) {
        SaveOrderResponse saveOrderResponse = new SaveOrderResponse();
        if (a == null || a.isEmpty()) {
            saveOrderResponse.save = newsave;
            saveOrderResponse.refno = refno;
            return saveOrderResponse;
        }

        List<String> msg = a.stream()
                .map(InventoryDTO::getMessage)
                .filter(m -> m != null && !m.isEmpty())
                .collect(Collectors.toList());

        if (!a.isEmpty() && a.get(0).companyid != null) {
            saveOrderResponse.setCompanyid(new ObjectId(a.get(0).companyid));
        }
        saveOrderResponse.order_date = orderdate;
        saveOrderResponse.save = newsave;
        saveOrderResponse.refno = refno;
        saveOrderResponse.setInventory(a);
        if (!msg.isEmpty()) {
            saveOrderResponse.messages.addAll(msg);
        }
        saveOrderResponse.ispriceInclusive = ispriceInclusive;
        saveOrderResponse.isgst = isgst;
        if (id != null) saveOrderResponse.save_id = id;
        return saveOrderResponse;
    }

    public static RazorPaymentDetailsDTO ToRazorPaymentDetailsDTOtoRazorPaymentDetails(RazorPaymentDetails razorPaymentDetails) {

        if (razorPaymentDetails == null) return null;

        RazorPaymentDetailsDTO r = new RazorPaymentDetailsDTO();

        r.id = razorPaymentDetails.getId();
        r.paymentId = razorPaymentDetails.getPayment_id();
        r.receipt = razorPaymentDetails.getReceipt();
        r.amount = razorPaymentDetails.getAmount();
        r.amount_paid = razorPaymentDetails.getAmount_paid();
        r.amount_due = razorPaymentDetails.getAmount_due();
        r.created_at = razorPaymentDetails.getCreated_at();
        r.currency = razorPaymentDetails.getCurrency();
        r.status = razorPaymentDetails.getStatus();
        r.attempts = razorPaymentDetails.getAttempts();
        r.entity = razorPaymentDetails.getEntity();

        r.acquirer_data = razorPaymentDetails.getAcquirer_data();
        r.amount_refunded = razorPaymentDetails.getAmount_refunded();
        r.bank = razorPaymentDetails.getBank();
        r.contact = razorPaymentDetails.getContact();
        r.description = razorPaymentDetails.getDescription();
        r.email = razorPaymentDetails.getEmail();

        r.error_code = razorPaymentDetails.getError_code();
        r.error_description = razorPaymentDetails.getError_description();
        r.error_reason = razorPaymentDetails.getError_reason();

        r.fee = razorPaymentDetails.getFee();
        r.international = razorPaymentDetails.isInternational();
        r.invoice_id = razorPaymentDetails.getInvoice_id();
        r.method = razorPaymentDetails.getMethod();
        r.order_id = razorPaymentDetails.getOrder_id();
        r.refund_status = razorPaymentDetails.getRefund_status();
        r.captured = razorPaymentDetails.getCaptured();
        r.tax = razorPaymentDetails.getTax();

        return r;
    }

    public static ViewOrderListResponse ToViewOrderResponse(Save save, HashMap<String, Integer> map) {

        ViewOrderListResponse viewOrderListResponse = new ViewOrderListResponse();
        if (save != null) {
            viewOrderListResponse.refno = save.getRefno();
            viewOrderListResponse.companyid = save.getCompanyid();
            viewOrderListResponse.buyerid = save.getBuyerid();
            viewOrderListResponse.orderSource = save.getOrderSource();
            viewOrderListResponse.order_date = String.valueOf(save.getOrderDate());
            viewOrderListResponse.order_status = save.getOrderStatus();
            viewOrderListResponse.payment_status = save.getPayment_status();
            viewOrderListResponse.delivery_details = save.getDelivery_details();
            viewOrderListResponse.delivery_mode = save.getDelivery_mode();
//            viewOrderListResponse.draft_active = save.isDraft_active();
            viewOrderListResponse.total_Advance = save.getTotal_Advance();
            viewOrderListResponse.total_AmountDue = save.getTotal_AmountDue();
            viewOrderListResponse.total_Discount = save.getTotal_Discount();
            viewOrderListResponse.total_Paid = save.getTotal_Paid();
            viewOrderListResponse.total_Amount = save.getTotal_Amount();

            viewOrderListResponse.razorPaymentDetailsDTO = SellerAdaptor.ToRazorPaymentDetailsDTOtoRazorPaymentDetails(save.getRazorpaymentdetails());

            viewOrderListResponse.isDelivered = save.isDelivered();
            viewOrderListResponse.deliveryDueDate = save.getDeliveryDueDate() == null ? "Nil" : save.getDeliveryDueDate();
            viewOrderListResponse.deliveredDate = save.getDeliveryDate();

            viewOrderListResponse.inventory = FromInventorytoInventoryDTO(save.getSeller_items(), map);
            viewOrderListResponse.paymentsDTO = FromPaymenttoPaymentDTO(save.getPaymentDetails());
            viewOrderListResponse.customerInformationDTO = FromCustomerInformationtoCustomerInformationDTO(save.getCustomerInformation());
            viewOrderListResponse.payment_mode = save.getPayment_mode();
            viewOrderListResponse.save_orderid = save.get_id();
            viewOrderListResponse.tracking_details = save.getTracking_details();
            viewOrderListResponse.isgst = save.getIsgst();
            viewOrderListResponse.ispriceInclusive = save.getIspriceInclusive();
            viewOrderListResponse.acceptReviewDetails = save.getAcceptReviewDetails();
        }
        return viewOrderListResponse;
    }

    private static double parseDoubleSafe(String value) {
        try {
            if (value == null || value.trim().isEmpty()) return 0.0;
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
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
        if (!paymentsDTO.payment_mode.equals("RAZORPAY")) {
            payments.setAmountDue(paymentsDTO.total_amount);
        }

        payments.setNext_payment_due(paymentsDTO.next_payment_due);
        return payments;

    }

    public static PaymentsDTO FromPaymenttoPaymentDTO(Payments payments) {

        PaymentsDTO payment = new PaymentsDTO();
        if (payments != null) {
            payment.paid_amount = payments.getPaid_amount();
            payment.payment_date = payments.getPayment_date();
            payment.next_payment_due = payments.getNext_payment_due();
            payment.current_installments = payments.getOngoing_installment();
            payment.isEMI = payments.isEMI();
            payment.current_amount = payments.getCurrent_payment_amount();
            payment.isAdvance = payments.isAdvance();
            payment.advanceAmt = payments.getAdvanceAmt();
            payment.paymentMode = payments.getPayment_mode();
            payment.amountDue = payments.getAmountDue();
            payment.discount = payments.getDiscount();
            payment.total_amount = payments.getTotal_amount();
            payment.no_of_installments = payments.getNo_of_installments();
            payment.outsanding_amount = payments.getOutstanding_amount();
        }
        return payment;
    }

    public static ProductsDTO FromProducttoProductDTO(Products products) {
        ProductsDTO productsDTO = new ProductsDTO();
        productsDTO.product_type = products.getProduct_type();
        //category details
        if (products.getProductCategory() != null) {

            productsDTO.productCategory = new ProductCategoryDTO();

            productsDTO.productCategory.setCategory_name(products.getProductCategory().getCategory_name());
            productsDTO.productCategory.set_id(String.valueOf(products.getProductCategory().get_id()));
            productsDTO.productCategory.setCreatedAt(products.getProductCategory().getCreatedAt());
            productsDTO.productCategory.setUpdatedAt(products.getProductCategory().getUpdatedAt());
            productsDTO.productCategory.setDescription(products.getProductCategory().getDescription());
            productsDTO.productCategory.coverImagePath = products.getProductCategory().getCoverImagePath();
            productsDTO.productCategory.displayOnHomePage = products.getProductCategory().isDisplayOnHomePage();
            productsDTO.productCategory.setParent(String.valueOf(products.getProductCategory().getParent_id()));
        }

        productsDTO.productDescription = products.getProductDescription();
        productsDTO.productName = products.getProductName();
        productsDTO.brand = products.getBrand();
        productsDTO._id = products.get_id();
        productsDTO.createdAt = products.getCreatedAt();
        productsDTO.updatedAt = products.getUpdatedAt();
        productsDTO.image_url = products.getImage_url();
        productsDTO.defaultImage = products.getDefaultImage();
        productsDTO.productfeatureid = products.getProductfeatureid();
        productsDTO.Quickadd = products.isQuickadd();
        productsDTO.unit = products.getUnit();
//        productsDTO.procumentPrice =products.getProcumentPrice();
        return productsDTO;


    }

    //    public static String baseUrl = "http://192.168.1.6:8080";
    public static String buildSimpleInvoiceTemplate(Save save, Seller seller, String htmlTemplate, String userfolder) throws Exception {

        List<InventoryDTO> items = FromInventorytoInventoryDTO(save.getSeller_items(), new HashMap<>());

        StringBuilder productRows = new StringBuilder();

        double subTotal = 0.0;
        ObjectMapper mapper = new ObjectMapper();


        for (InventoryDTO item : items) {
            try {
                System.out.println("Full JSON: " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(item));
            } catch (Exception e) {
                log.error("error: " + e);
                e.printStackTrace();
            }
            boolean isService = "isService".equals(item.flag);

            String name = isService
                    ? safeString(item.servicename)
                    : safeString(item.productname);

            if (isService) {
                name += " (Service)";
            }

            int qty = item.getOrder_quantity();

            double price = isService
                    ? parseDoubleSafe(String.valueOf(item.servicecost))
                    : parseDoubleSafe(String.valueOf(item.sellerprice));

            double amount = qty * price;
            subTotal += amount;

            String batch = isService
                    ? "-"
                    : (item.getBatchid() != null
                    ? item.getBatchid()._id
                    : (item.batch_StringID != null ? item.batch_StringID : "-"));

                /* =====================================================
                   IMAGE LOGIC
                ===================================================== */

            String imageUrl1 = "-";

            try {

                String joinName = (isService ? item.servicename : item.productname)
                        .trim()
                        .replaceAll("[/()]", "")     // remove / ( )
                        .replaceAll("\\s+", "-")     // spaces → -
                        .trim();

                String relativePath;

                if (item.defaultImage == null || item.defaultImage.trim().isEmpty()) {

                    relativePath = "uploads/noImage.jpg";

                } else if (isService) {

                    String datePart = "000000";
                    String timePart = "0000";

                    if (seller.getCreateAT() != null && seller.getCreateAT().contains("T")) {

                        String[] parts = seller.getCreateAT().split("T");

                        String[] d = parts[0].split("-");
                        datePart = d[0] + d[1] + d[2];

                        String[] t = parts[1].split(":");
                        timePart = t[0] + t[1];
                    }

                    relativePath =
                            "uploads/uploadsServiceImages/"
                                    + seller.getCompanyname() + "-"
                                    + datePart + timePart + "/"
                                    + joinName + "/"
                                    + item.defaultImage;

                } else {

                    relativePath =
                            "uploads/uploadsProductImages/"
                                    + joinName + "/"
                                    + item.defaultImage;
                }

                // Normalize slashes
                relativePath = relativePath.replace("\\", "/");

                // Build full URL
                imageUrl1 = baseUrl.endsWith("/")
                        ? baseUrl + relativePath
                        : baseUrl + "/" + relativePath;

                log.info("Image URL: {}", imageUrl1);

            } catch (Exception e) {
                log.error("Image path build error: ", e);
            }

            String imageHtml =
                    imageUrl1.equals("-")
                            ? "-"
                            : "<img src='" + imageUrl1 +
                            "' style='width:60px;height:60px;object-fit:cover;border-radius:6px;' />";


            StringBuilder measurementTableHtml = new StringBuilder();
            StringBuilder otherAttributesHtml = new StringBuilder();

            if ("isService".equals(item.flag)) {

                name = (item.servicename != null && !item.servicename.trim().isEmpty()
                        ? item.servicename.trim()
                        : "Unnamed Service") + " (Service)";

                String tick = "&#10004;";
                String green = "style='color:green;font-weight:800;'";
                String black = "style='color:black;font-weight:800;'";
                String normal = "style='color:#444;font-weight:500;'";

                String checkboxsimplewithTick = "style='font-size:16px;color:green;font-weight:600;'";
                String checkboxsimplewithNotTick = "style='font-size:16px;color:#444;'";
                String radiobuttonwithmark = "style='color:green;font-weight:600;'";
                String radiobuttonwithUnmark = "style='color:#444;'";

                // ---------- LINE 2 : MEASUREMENT TABLE ----------
                // ---------- TABLE TYPE ATTRIBUTES ----------
                for (Map.Entry<String, String> entry : item.propertyAttributes.entrySet()) {

                    String propertyName = entry.getKey();
                    String value = entry.getValue();

                    if (value == null || value.trim().isEmpty()) continue;

                    // find property definition
                    PropertyAttributesDTO matchedProperty = null;
                    if (item.available_properties != null) {
                        for (PropertyAttributesDTO ap : item.available_properties) {
                            if (propertyName.equalsIgnoreCase(ap.name)) {
                                matchedProperty = ap;
                                break;
                            }
                        }
                    }

                    // only TABLE type
                    if (matchedProperty == null || !"Table".equalsIgnoreCase(matchedProperty.type)) {
                        continue;
                    }

                    // ---------- BUILD TABLE ----------
//                        measurementTableHtml.append(
//                                "<table border='1' width='90%' cellspacing='0' cellpadding='4' " +
//                                        "style='border-collapse:collapse;text-align:center;margin:4px'>"
//                        );
                    measurementTableHtml.append(
                            "<table class='measurement-table' border='1' width='98%' cellspacing='0' cellpadding='5' " +
                                    "style='border-collapse:collapse;text-align:center;" +
                                    "margin:5px auto;" +
                                    "border:3px solid #b33;'>"
                    );


                    // HEADER ROW
                    measurementTableHtml.append("<tr>");
                    for (String pair : value.split(",")) {
                        String[] kv = pair.split("=");
                        if (kv.length == 2) {
                            measurementTableHtml.append("<th>")
                                    .append(kv[0].trim())
                                    .append("</th>");
                        }
                    }
                    measurementTableHtml.append("</tr>");

                    // VALUE ROW
                    measurementTableHtml.append("<tr>");
                    for (String pair : value.split(",")) {
                        String[] kv = pair.split("=");
                        if (kv.length == 2) {
                            measurementTableHtml.append("<td><span ")
                                    .append(black)
                                    .append(">")
                                    .append(kv[1].trim())
                                    .append("</span></td>");
                        }
                    }
                    measurementTableHtml.append("</tr></table>");
                }


                // ---------- LINE 3 : OTHER ATTRIBUTES ----------
                otherAttributesHtml.append(
                        "<table class='attributes-table' width='100%' cellspacing='10' cellpadding='0'><tr>"
                );

                for (Map.Entry<String, String> entry : item.propertyAttributes.entrySet()) {

                    String propertyName = entry.getKey();
                    String selectedValue = entry.getValue();
                    if (selectedValue == null || selectedValue.trim().isEmpty()) continue;

                    log.info("Raw selectedValue: " + selectedValue);


                    // only TABLE type

                    PropertyAttributesDTO matchedProperty = null;
                    if (item.available_properties != null) {
                        for (PropertyAttributesDTO ap : item.available_properties) {
                            if (propertyName.equals(ap.name)) {
                                matchedProperty = ap;
                                break;
                            }
                        }
                    }
                    String propertyType = matchedProperty != null ? matchedProperty.type : "Text";


                    if ("Checkbox".equalsIgnoreCase(propertyType)
                            || "Radio".equalsIgnoreCase(propertyType)
                            || "Dropdown".equalsIgnoreCase(propertyType)) {
                        otherAttributesHtml.append("<td valign='top'>")
                                .append("<b style='font-size:13px;font-weight:700;'>")
                                .append(propertyName + " :")
                                .append("</b><br>");

                    }

//                        // checkbox / radio / dropdown
//                        if ("checkbox".equalsIgnoreCase(propertyType) || "radio".equalsIgnoreCase(propertyType) || "dropdown".equalsIgnoreCase(propertyType)) {
//
//                            Set<String> selectedSet = Arrays.stream(selectedValue.split(","))
//                                    .map(String::trim)
//                                    .map(String::toLowerCase)
//                                    .collect(Collectors.toSet());
//
//                            if (matchedProperty != null && matchedProperty.value != null) {
//                                for (String option : matchedProperty.value.split(",")) {
//                                    String opt = option.trim();
//                                    boolean isSelected = selectedSet.contains(opt.toLowerCase());
//
//                                    otherAttributesHtml.append(
//                                            isSelected
//                                                    ? "<div style='margin-left:10px;'><span " + green + ">" + tick + " " + opt + "</span></div>"
//                                                    : "<div style='margin-left:25px;'><span " + normal + ">" + opt + "</span></div>"
//                                    );
//
//                                }
//                            }
//                        }
                    // checkbox / radio / dropdown
                    if ("Checkbox".equalsIgnoreCase(propertyType)
                            || "Radio".equalsIgnoreCase(propertyType)
                            || "Dropdown".equalsIgnoreCase(propertyType)) {

                        Set<String> selectedSet = Arrays.stream(selectedValue.split(","))
                                .map(String::trim)
                                .map(String::toLowerCase)
                                .collect(Collectors.toSet());

                        if (matchedProperty != null && matchedProperty.value != null) {

                            for (String option : matchedProperty.value.split(",")) {

                                String opt = option.trim();
                                boolean isSelected = selectedSet.contains(opt.toLowerCase());

                                String icon = "";
                                String style = "";

                                // ✅ Checkbox / Dropdown
                                if ("Checkbox".equalsIgnoreCase(propertyType)
                                        || "Dropdown".equalsIgnoreCase(propertyType)) {

                                    icon = isSelected ? "☑" : "☐";
                                    style = isSelected ? green : normal;
                                }

                                // ✅ Radio
                                else if ("Radio".equalsIgnoreCase(propertyType)) {

                                    if (isSelected) {

                                        icon = "<span style='display:inline-block; width:10px; height:10px;"
                                                + "background-color:green;"
                                                + "border:2px solid black;"
                                                + "border-radius:50%;'></span>";

                                        style = radiobuttonwithmark;

                                    } else {

                                        icon = "<span style='display:inline-block;width:10px;height:10px;"
                                                + "border:2px solid black;"
                                                + "border-radius:50%;'></span>";

                                        style = radiobuttonwithUnmark;
                                    }
                                }


                                otherAttributesHtml.append(
                                        "<div >"
                                                + "<span " + style + " style='display:inline-flex; align-items:center; gap:3px;'>"
                                                + icon + " " + opt
                                                + "</span></div>"
                                );
                            }
                        }
                    } else if ("Image".equalsIgnoreCase(propertyType)) {

                        otherAttributesHtml.append("<div style='display:flex;align-items:center;gap:10px;'>");

                        otherAttributesHtml.append("<b>")
                                .append(propertyName)
                                .append(":</b>");

                        try {

                            if (selectedValue != null && !selectedValue.isBlank()) {

                                // 🔥 Convert Windows backslash to forward slash
                                String normalizedPath = selectedValue.replace("\\", "/");

//                                String imageUrl = "http://192.168.1.2:8080"+ "/" + normalizedPath;
                                String imageUrl = baseUrl + "/" + normalizedPath;


                                log.info("ImagePath : " + imageUrl);

                                otherAttributesHtml.append("<img src='")
                                        .append(imageUrl)
                                        .append("' style='width:70px;height:70px;object-fit:cover;border-radius:6px;'/>");
                            } else {
                                otherAttributesHtml.append("-");
                            }

                        } catch (Exception e) {
                            log.error("Service property image load error: ", e);
                            otherAttributesHtml.append("-");
                        }

                        otherAttributesHtml.append("</div><br>");
                    } else if ("Text".equalsIgnoreCase(propertyType)) {
//                        otherAttributesHtml.append("<span ")
//                                .append(green)
//                                .append(" style='margin-right:10px;'>")
//                                .append(selectedValue.trim())
//                                .append("</span>");
                        otherAttributesHtml.
                                append("<td valign='top'>")
                                .append("<span style='font-size:12px;font-weight:700;'>")
                                .append(propertyName + " : ")
                                .append("<span ")
                                .append(green)
                                .append(">")
                                .append(selectedValue.trim())
                                .append("</span>")
                                .append("</span>");
                    }

                    otherAttributesHtml.append("</td>");
                }

                otherAttributesHtml.append("</tr></table>");


//                String attributesRowHtml =
//                        "<div class='product-row attributes-row'>" +
//                                "<div class='product-col' style='flex:100%; text-align:left;'>" +
//                                measurementTableHtml +
//                                otherAttributesHtml +
//                                "</div>" +
//                                "</div>";
                String attributesRowHtml =
                        "<div class='product-row attributes-row' style='border:1px solid black; padding:2px; box-sizing:border-box;'>" +
                                "<div class='product-col' style='flex:100%; text-align:left;'>" +
                                measurementTableHtml +
                                otherAttributesHtml +
                                "</div>" +
                                "</div>";


            }

            productRows.append(String.format(
                    "<tr>" +
                            "<td style='text-align:center;'>%s</td>" +
                            "<td>%s</td>" +
                            "<td style='text-align:center;'>%d</td>" +
                            "<td style='text-align:center;'>₹%.2f</td>" +
                            "<td style='text-align:center;'>₹%.2f</td>" +
                            "</tr>",
                    imageHtml,
                    name,
                    qty,
                    price,
                    amount
            ));
            if ("isService".equals(item.flag)) {

                String attributesRowHtml =
                        "<tr>" +
                                "<td colspan='4' style='padding:15px 25px; background:#f4f4f4;'>" +
                                "<div style='width:100%;'>" +
                                measurementTableHtml.toString() +
                                otherAttributesHtml.toString() +
                                "</div>" +
                                "</td>" +
                                "</tr>";

                productRows.append(attributesRowHtml);
            }
        }

        // ✅ GST Calculation
        double gstTotal = 0.0;
        double cgst = 0.0;
        double sgst = 0.0;

        if ("true".equalsIgnoreCase(save.getIsgst())
                || "true".equalsIgnoreCase(save.getIspriceInclusive())) {

            gstTotal = subTotal * 0.18;  // adjust if dynamic GST needed
            cgst = gstTotal / 2;
            sgst = gstTotal / 2;
        }

        double finalTotal = subTotal + gstTotal;

        // ✅ Format Order Date
        String formattedDate = "";
        if (save.getOrderDate() != null) {

            SimpleDateFormat outputFormat =
                    new SimpleDateFormat("dd/MM/yyyy");

            formattedDate = outputFormat.format(save.getOrderDate());
        }

        // ✅ Customer Info
        CustomerInformation customer =
                save.getCustomerInformation();

        String populatedHtml = htmlTemplate
                .replace("{{company_name}}",
                        seller != null ? safeString(seller.getCompanyname()) : "Company")

                .replace("{{refno}}", safeString(save.getRefno()))
                .replace("{{date}}", formattedDate)
                .replace("{{buyerid}}", safeString(save.getOrderSource()))

                .replace("{{product_rows}}", productRows.toString())
                .replace("{{cartlength}}", String.valueOf(items.size()))

                .replace("{{order_total}}",
                        String.format("%.2f", subTotal))

                .replace("{{cgst}}",
                        String.format("%.2f", cgst))
                .replace("{{sgst}}",
                        String.format("%.2f", sgst))
                .replace("{{gst_total}}",
                        String.format("%.2f", gstTotal))

                // Customer Info
                .replace("{{CustomerName}}",
                        safeString(customer != null ? customer.getCustomerName() : null))

                .replace("{{CustomerMobileNum}}",
                        safeString(customer != null ? customer.getCustomerMobileNum() : null))

                .replace("{{delivery_mode}}",
                        safeString(save.getDelivery_mode()))

                .replace("{{CustomerDeliveryAddress}}",
                        safeString(customer != null ? customer.getDeliveryAddress() : null))

                .replace("{{CustomerPickupAddress}}",
                        safeString(save.getDelivery_details()))

                .replace("{{DeliveryDate}}",
                        formatDate(save.getDeliveryDate()))

                .replace("{{EstimatedDeliveryDate}}",
                        formatDate(save.getDeliveryDueDate()))

                // Payment Info
                .replace("{{payment}}",
                        safeString(save.getPayment_mode()))

                .replace("{{Total_Amount}}",
                        String.format("%.2f", finalTotal))

                .replace("{{Total_Advance}}",
                        safeString(save.getTotal_Advance()))

                .replace("{{Total_Discount}}",
                        safeString(save.getTotal_Discount()))

                .replace("{{Total_Paid}}",
                        safeString(save.getTotal_Paid()))

                .replace("{{Total_AmountDue}}",
                        safeString(save.getTotal_AmountDue()));


        String logoPath = "uploads/" + save.getCompanyid() + "_profile.jpg";
        File logoFile = new File(logoPath);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (logoFile.exists()) {
            try (FileInputStream fis = new FileInputStream(logoFile)) {
                byte[] logoBytes = fis.readAllBytes();
                String base64Image = Base64.getEncoder().encodeToString(logoBytes);
                populatedHtml = populatedHtml.replace("{{logo_base64}}", base64Image);
            }
        } else {
            // fallback to default AppImg.png from resources
            try (InputStream logoStream = classLoader.getResourceAsStream("Invoice_Logo/AppImg.png")) {
                if (logoStream != null) {
                    String base64Image = Base64.getEncoder().encodeToString(logoStream.readAllBytes());
                    populatedHtml = populatedHtml.replace("{{logo_base64}}", base64Image);
                } else {
                    populatedHtml = populatedHtml.replace("{{logo_base64}}", ""); // empty if no logo
                }
            }
        }

        return populatedHtml;
    }

    private static String formatDate(Object dateObj) {

        if (dateObj == null) return "";

        try {

            SimpleDateFormat outputFormat =
                    new SimpleDateFormat("dd/MM/yyyy");

            // If already Date
            if (dateObj instanceof Date) {
                return outputFormat.format((Date) dateObj);
            }

            // If String like: 2026-03-31T08:37:00.000Z
            String dateStr = dateObj.toString();

            if (dateStr.contains("T")) {

                SimpleDateFormat isoFormat =
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");

                Date parsedDate = isoFormat.parse(dateStr);

                return outputFormat.format(parsedDate);
            }

            // If format like: 2026-03-03 15:09:31.878
            if (dateStr.contains(" ")) {

                SimpleDateFormat dbFormat =
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

                Date parsedDate = dbFormat.parse(dateStr);

                return outputFormat.format(parsedDate);
            }

            return dateStr;

        } catch (Exception e) {
            return dateObj.toString();
        }
    }

    public static InvoiceResponse ToInvoiceResponse(Save save, String templatePath, Seller seller, String userfolder) throws Exception {
        InvoiceResponse response = new InvoiceResponse();

        if (save != null) {
            response.refno = save.getRefno();
            response.companyid = save.getCompanyid();
            response.buyerid = save.getBuyerid();
            response.orderSource = save.getOrderSource();
            response.order_date = String.valueOf(save.getOrderDate());
            response.order_status = save.getOrderStatus();
            response.delivery_details = save.getDelivery_details();
            response.delivery_mode = save.getDelivery_mode();
//            response.draft_active = save.isDraft_active();
            response.inventory = FromInventorytoInventoryDTO(save.getSeller_items(), new HashMap<>());

            response.total_Advance = save.getTotal_Advance();
            response.total_AmountDue = save.getTotal_AmountDue();
            response.total_Discount = save.getTotal_Discount();
            response.total_Paid = save.getTotal_Paid();
            response.total_Amount = save.getTotal_Amount();

            response.customerInformationDTO = FromCustomerInformationtoCustomerInformationDTO(save.getCustomerInformation());

            response.payment_status = save.getPayment_status();
            response.payment_mode = save.getPayment_mode();
            response.paymentsDTO = FromPaymenttoPaymentDTO(save.getPaymentDetails());
            response.isDelivered = save.isDelivered();
            response.deliveredDate = save.getDeliveryDate();
            response.deliveryDueDate = save.getDeliveryDueDate();

            response.payment_details = save.getPayment_mode();
            response.save_orderid = save.get_id();
            response.tracking_details = save.getTracking_details();
            response.isgst = save.getIsgst() == null ? "false" : save.getIsgst();
            response.ispriceInclusive = save.getIspriceInclusive() == null ? "false" : save.getIspriceInclusive();
        }


        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("templates/" + templatePath + ".html");
        if (inputStream == null) throw new FileNotFoundException("Template not found: " + templatePath);
        String htmlTemplate = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

        log.info("Loading template: templates/" + templatePath + ".html");

        List<InventoryDTO> items = response.inventory;
        StringBuilder productRows = new StringBuilder();
        double total = 0.0, totalCGST = 0.0, totalSGST = 0.0, only_products = 0.0;

        boolean isFarmeRich = templatePath.toLowerCase().contains("farme_rich");
        boolean isThermal = templatePath.toLowerCase().contains("invoice_template_3");
        boolean isTailor = templatePath.toLowerCase().contains("tailor_template_5");
        boolean isSwagathStyle = htmlTemplate.contains("item-title") && htmlTemplate.contains("item-desc");

        boolean isPurest = templatePath.equalsIgnoreCase("PurestTemplate");


        ObjectMapper mapper = new ObjectMapper();

        if (isFarmeRich) {
            String html = buildSimpleInvoiceTemplate(
                    save,
                    seller,
                    htmlTemplate,
                    userfolder
            );

            response.invoiceHtml = html;
            return response;
        }


        for (int i = 0; i < items.size(); i++) {
            InventoryDTO item = items.get(i);
            try {
                System.out.println("Full JSON: " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(item));
            } catch (Exception e) {
                log.error("error: " + e);
                e.printStackTrace();
            }

            double price = 0.0;
            int qty = item.getOrder_quantity();
            String name;
            String property_attribute = "";
            String Tailor_property_attribute = "";

            if ("isService".equals(item.flag)) {

                price = parseDoubleSafe(String.valueOf(item.servicecost));

                name = item.servicename != null && !item.servicename.trim().isEmpty()
                        ? item.servicename.trim()
                        : "Unnamed Service";
                name += " (Service)";

                if (item.propertyAttributes != null && !item.propertyAttributes.isEmpty()) {

                    StringBuilder attributesHtml = new StringBuilder();

                    for (Map.Entry<String, String> entry : item.propertyAttributes.entrySet()) {

                        String propertyName = entry.getKey();
                        String valueString = entry.getValue();

                        log.info("value :" + valueString);

                        if (valueString == null || valueString.trim().isEmpty()) continue;

                        // Find property type from available_properties
                        String propertyType = "Text";
                        if (item.available_properties != null) {
                            for (PropertyAttributesDTO ap : item.available_properties) {
                                if (propertyName.equals(ap.name)) {
                                    propertyType = ap.type;
                                    break;
                                }
                            }
                        }

                        if ("Table".equalsIgnoreCase(propertyType)) {

                            attributesHtml.append("<b>").append(propertyName).append("</b>");
                            attributesHtml.append("<table border='1' cellspacing='0' cellpadding='2' " +
                                    "style='border-collapse:collapse;width:50%;font-size:12px;color:#777;'>");

                            String[] pairs = valueString.split(",");
                            for (String pair : pairs) {
                                String[] kv = pair.split("=");
                                if (kv.length == 2) {
                                    attributesHtml.append("<tr>")
                                            .append("<td>").append(kv[0].trim()).append("</td>")
                                            .append("<td>").append(kv[1].trim()).append("</td>")
                                            .append("</tr>");
                                }
                            }

                            attributesHtml.append("</table><br>");

                        } else if ("Image".equalsIgnoreCase(propertyType)) {

                            attributesHtml.append("<div style='display:flex; flex-direction:row; align-items:center; gap:10px;'>");

                            attributesHtml.append("<b>")
                                    .append(propertyName)
                                    .append(":</b>");

                            try {
                                // Extract base64 value
                                String base64Prefix = "\"base64\":\"";
                                int start = valueString.indexOf(base64Prefix);

                                if (start != -1) {
                                    start += base64Prefix.length();
                                    int end = valueString.indexOf("\"", start);

                                    if (end != -1) {
                                        String base64Value = valueString.substring(start, end);

                                        attributesHtml.append("<img src='")
                                                .append(base64Value)  // Already contains data:image/jpeg;base64,...
                                                .append("' style='max-width:70px; height:70px;'/>");
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            attributesHtml.append("</div><br>");
                        } else {
                            attributesHtml.append("<b>")
                                    .append(propertyName)
                                    .append(":</b> ")
                                    .append(valueString.trim())
                                    .append("<br>");
                        }
                    }

                    if (attributesHtml.length() > 0) {
                        property_attribute = "<br><small>" + attributesHtml + "</small>";
                        log.info("Service attributes : " + attributesHtml);
                    }
                }
            } else {
                if (item.variantBatches != null && item.variantBatches.size() > 0) {
                    name = item.productname == "" || item.productname == null ? "Unnamed Product" : item.productname;
                    name += item.variantBatches.get(0).variantName == "" || item.variantBatches.get(0).variantName == null ? "Unnamed Variant Name" : item.variantBatches.get(0).variantName;
                } else {
                    name = item.productname == "" || item.productname == null ? "Unnamed Product" : item.productname;
                }

                if (item.offerPrice > 0.0) {
                    price = parseDoubleSafe(String.valueOf(item.offerPrice));
                } else {
                    price = item.sellerprice != 0 ? parseDoubleSafe(String.valueOf(item.sellerprice)) : 0.0;
                }

            }

            double subtotal = qty * price;
            double gst_percentage = 0;

            if (item.gst != null && !item.gst.isEmpty()) {
                int idx = item.gst.indexOf('%');
                if (idx > 0) {
                    gst_percentage = Double.parseDouble(item.gst.substring(0, idx));
                } else {
                    // case when gst doesn't have "%"
                    gst_percentage = Double.parseDouble(item.gst);
                }
            }

            double c = item.sellerprice + (item.sellerprice * gst_percentage / 100);

            log.info("each gst: " + c);
            only_products += subtotal;
            total += response.ispriceInclusive.equals("true") || response.isgst.equals("true") ? c : subtotal;
            if (isPurest) {

                String purestItemName =
                        name == null || name.trim().isEmpty()
                                ? "Unnamed Product"
                                : name.trim();

                productRows.append(
                        String.format(
                                "<tr>" +

                                        // #
                                        "<td class=\"center\">%d</td>" +

                                        // Item Name
                                        "<td>" +
                                        "<span class=\"item-name-text\">%s</span>" +
                                        "</td>" +

                                        // HSN/SAC - intentionally empty
                                        "<td></td>" +

                                        // Quantity
                                        "<td class=\"num\">%s</td>" +

                                        // Unit - intentionally empty
                                        "<td></td>" +

                                        // Price / Unit
                                        "<td class=\"num\">Rs. %.2f</td>" +

                                        // Amount
                                        "<td class=\"num\">Rs. %.2f</td>" +

                                        "</tr>",

                                i + 1,
                                purestItemName,
                                String.valueOf(qty),
                                price,
                                subtotal
                        )
                );

                // Do not allow the generic 5-column invoice logic
                // to generate another row.
                continue;
            }

            if (isFarmeRich) {
                String joinService_Name = "";

                if ("isService".equals(item.flag) && item.servicename != null && !item.servicename.trim().isEmpty()) {

                    String[] serviceNameParts = item.servicename.trim().split("\\s+");
                    StringBuilder sb = new StringBuilder();

                    for (int k = 0; k < serviceNameParts.length; k++) {
                        if (k > 0) sb.append("-");
                        sb.append(serviceNameParts[k]);
                    }

                    joinService_Name = sb.toString();
                } else {
                    String[] serviceNameParts = item.productname.trim().split("\\s+");
                    StringBuilder sb = new StringBuilder();

                    for (int k = 0; k < serviceNameParts.length; k++) {
                        if (k > 0) sb.append("-");
                        sb.append(serviceNameParts[k]);
                    }

                    joinService_Name = sb.toString();
                }


                String date = item.created_at;
                log.info("Initial date : " + date);
                String time = "";

                if (seller.getCreateAT() != null && seller.getCreateAT().contains("T")) {
                    String[] parts = seller.getCreateAT().split("T");
                    String[] d1 = parts[0].split("-");

                    date = d1[0] + d1[1] + d1[2];
                    String[] t1 = parts[1].split(":");
                    String actualTime = t1[0] + t1[1];

                    time = parts.length > 1 ? t1[0] + t1[1] : "";
                }


                log.info("date: " + date);

                log.info("Time: " + time);
                String isServiceorProduct = item.flag != null ? item.flag.equals("isService") ? "Services" : "Products" : "Products";
                SimpleDateFormat inputFormat =
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

                SimpleDateFormat sdf =
                        new SimpleDateFormat("yyMMddHHmmss");

                Date createdAtDate = null;

                if (item.created_at != null && item.created_at.contains("T")) {

                    String trimmedDate = item.created_at.split("\\.")[0];
                    createdAtDate = inputFormat.parse(trimmedDate);
                }

                String formattedCreatedAt = createdAtDate != null ? sdf.format(createdAtDate) : "000000000000";


                log.info("After Join name :" + joinService_Name);
                Path imgPath = isServiceorProduct.equals("Services") ? Paths.get(
                        "uploads/uploadsServiceImages/"
                                + seller.getCompanyname() + "-" + date + time + "/"
                                + joinService_Name + "/" + item.defaultImage
                ) : Paths.get(
                        "uploads/uploadsProductImages/"
                                + joinService_Name + "/" + item.defaultImage
                );

                String base64Image = "";
                if (Files.exists(imgPath)) {
                    byte[] bytes = Files.readAllBytes(imgPath);
                    base64Image = Base64.getEncoder().encodeToString(bytes);
                }

                log.info("img path: " + imgPath);

                if ("isService".equals(item.flag)) {
//                    productRows.append(String.format(
//                            "<div class=\"product-row\">" +
//                                    "<div class=\"product-col name\">" +
//                                    "<img class=\"img\" src=\"data:image/jpeg;base64,%s\" /> %s" +
//                                    "</div>" +
//                                    "<div class=\"product-col\">%d</div>" +
//                                    "<div class=\"product-col\">Rs. %.2f</div>" +
//                                    "<div class=\"product-col batch\">%s</div>" +
//                                    "<div class=\"product-col\">Rs. %.2f</div>" +
//                                    "</div>",
//                            base64Image,
//                            name + property_attribute,
//                            qty,
//                            price,
//                            "-",
//                            subtotal
//                    ));
                    productRows.append(String.format(
                            "<div class=\"product-row\">" +
                                    "<div class=\"product-col name\">" +
                                    "<img class=\"img\" src=\"data:image/jpeg;base64,%s\" />" +
                                    "<div class=\"product-name-wrapper\">" +
                                    "<div class=\"product-title\">%s</div>" +
                                    "<div class=\"product-attribute\">%s</div>" +
                                    "</div>" +
                                    "</div>" +
                                    "<div class=\"product-col\">%d</div>" +
                                    "<div class=\"product-col\">Rs. %.2f</div>" +
                                    "<div class=\"product-col batch\">%s</div>" +
                                    "<div class=\"product-col\">Rs. %.2f</div>" +
                                    "</div>",
                            base64Image,
                            name,
                            property_attribute,
                            qty,
                            price,
                            "-",
                            subtotal
                    ));
//                    productRows.append(String.format(
//                            "<div class='product-row'>" +
//
//                                    "<div class='product-col name' style='display:flex; flex-direction:column; align-items:flex-start;'>" +
//
//                                    "<img class='img' style='width:60px;height:auto;margin-bottom:8px;' " +
//                                    "src='data:image/jpeg;base64,%s' />" +
//
//                                    "<div class='product-name-wrapper'>" +
//                                    "<div class='product-title' style='font-weight:bold;'>%s</div>" +
//                                    "<div class='product-attribute'>%s</div>" +
//                                    "</div>" +
//
//                                    "</div>" +
//
//                                    "<div class='product-col'>%d</div>" +
//                                    "<div class='product-col'>Rs. %.2f</div>" +
//                                    "<div class='product-col batch'>%s</div>" +
//                                    "<div class='product-col'>Rs. %.2f</div>" +
//
//                                    "</div>",
//                            base64Image,
//                            name,
//                            property_attribute,
//                            qty,
//                            price,
//                            "-",
//                            subtotal
//                    ));

                } else {
                    // Product layout (with Batch)
                    productRows.append(String.format(
                            "<div class=\"product-row\">" +

                                    // 1️⃣ Product column (Image + Name)
                                    "<div class=\"product-col name\">" +
                                    "<img class=\"img\" src=\"data:image/jpeg;base64,%s\" />" +
                                    "<div class=\"product-name\">%s</div>" +
                                    "</div>" +

                                    // 2️⃣ Quantity
                                    "<div class=\"product-col\">%d</div>" +

                                    // 3️⃣ Rate
                                    "<div class=\"product-col\">Rs. %.2f</div>" +

                                    // 4️⃣ Batch
                                    "<div class=\"product-col batch\">%s</div>" +

                                    // 5️⃣ Amount
                                    "<div class=\"product-col\">Rs. %.2f</div>" +

                                    "</div>",
                            base64Image,
                            name,
                            qty,
                            price,
                            item.getBatchid() != null ? item.getBatchid() : item.batch_StringID != null ? item.batch_StringID : "-",
                            subtotal
                    ));


                }
            } else if (isTailor) {
                StringBuilder measurementTableHtml = new StringBuilder();
                StringBuilder otherAttributesHtml = new StringBuilder();

                if ("isService".equals(item.flag)) {

                    name = (item.servicename != null && !item.servicename.trim().isEmpty()
                            ? item.servicename.trim()
                            : "Unnamed Service") + " (Service)";

                    String tick = "&#10004;";
                    String green = "style='color:green;font-weight:800;'";
                    String black = "style='color:black;font-weight:800;'";
                    String normal = "style='color:#444;font-weight:500;'";

                    String checkboxsimplewithTick = "style='font-size:16px;color:green;font-weight:600;'";
                    String checkboxsimplewithNotTick = "style='font-size:16px;color:#444;'";
                    String radiobuttonwithmark = "style='color:green;font-weight:600;'";
                    String radiobuttonwithUnmark = "style='color:#444;'";

                    // ---------- LINE 2 : MEASUREMENT TABLE ----------
                    // ---------- TABLE TYPE ATTRIBUTES ----------
                    for (Map.Entry<String, String> entry : item.propertyAttributes.entrySet()) {

                        String propertyName = entry.getKey();
                        String value = entry.getValue();

                        if (value == null || value.trim().isEmpty()) continue;

                        // find property definition
                        PropertyAttributesDTO matchedProperty = null;
                        if (item.available_properties != null) {
                            for (PropertyAttributesDTO ap : item.available_properties) {
                                if (propertyName.equalsIgnoreCase(ap.name)) {
                                    matchedProperty = ap;
                                    break;
                                }
                            }
                        }

                        // only TABLE type
                        if (matchedProperty == null || !"Table".equalsIgnoreCase(matchedProperty.type)) {
                            continue;
                        }

                        // ---------- BUILD TABLE ----------
//                        measurementTableHtml.append(
//                                "<table border='1' width='90%' cellspacing='0' cellpadding='4' " +
//                                        "style='border-collapse:collapse;text-align:center;margin:4px'>"
//                        );
                        measurementTableHtml.append(
                                "<table border='1' width='98%' cellspacing='0' cellpadding='5' " +
                                        "style='border-collapse:collapse;text-align:center;" +
                                        "margin:5px auto;" +   // centers the table
                                        "border:3px solid #b33;'>"
                        );


                        // HEADER ROW
                        measurementTableHtml.append("<tr>");
                        for (String pair : value.split(",")) {
                            String[] kv = pair.split("=");
                            if (kv.length == 2) {
                                measurementTableHtml.append("<th>")
                                        .append(kv[0].trim())
                                        .append("</th>");
                            }
                        }
                        measurementTableHtml.append("</tr>");

                        // VALUE ROW
                        measurementTableHtml.append("<tr>");
                        for (String pair : value.split(",")) {
                            String[] kv = pair.split("=");
                            if (kv.length == 2) {
                                measurementTableHtml.append("<td><span ")
                                        .append(black)
                                        .append(">")
                                        .append(kv[1].trim())
                                        .append("</span></td>");
                            }
                        }
                        measurementTableHtml.append("</tr></table>");
                    }


                    // ---------- LINE 3 : OTHER ATTRIBUTES ----------
                    otherAttributesHtml.append(
                            "<table width='100%' cellspacing='10' cellpadding='0' style='margin:6px'><tr>"
                    );

                    for (Map.Entry<String, String> entry : item.propertyAttributes.entrySet()) {

                        String propertyName = entry.getKey();
                        String selectedValue = entry.getValue();
                        if (selectedValue == null || selectedValue.trim().isEmpty()) continue;

                        // only TABLE type

                        PropertyAttributesDTO matchedProperty = null;
                        if (item.available_properties != null) {
                            for (PropertyAttributesDTO ap : item.available_properties) {
                                if (propertyName.equals(ap.name)) {
                                    matchedProperty = ap;
                                    break;
                                }
                            }
                        }


                        String propertyType = matchedProperty != null ? matchedProperty.type : "Text";


                        if ("Checkbox".equalsIgnoreCase(propertyType)
                                || "Radio".equalsIgnoreCase(propertyType)
                                || "Dropdown".equalsIgnoreCase(propertyType)) {
                            otherAttributesHtml.append("<td valign='top'>")
                                    .append("<b style='font-size:18px;font-weight:700;'>")
                                    .append(propertyName + " :")
                                    .append("</b><br>");

                        }

//                        // checkbox / radio / dropdown
//                        if ("checkbox".equalsIgnoreCase(propertyType) || "radio".equalsIgnoreCase(propertyType) || "dropdown".equalsIgnoreCase(propertyType)) {
//
//                            Set<String> selectedSet = Arrays.stream(selectedValue.split(","))
//                                    .map(String::trim)
//                                    .map(String::toLowerCase)
//                                    .collect(Collectors.toSet());
//
//                            if (matchedProperty != null && matchedProperty.value != null) {
//                                for (String option : matchedProperty.value.split(",")) {
//                                    String opt = option.trim();
//                                    boolean isSelected = selectedSet.contains(opt.toLowerCase());
//
//                                    otherAttributesHtml.append(
//                                            isSelected
//                                                    ? "<div style='margin-left:10px;'><span " + green + ">" + tick + " " + opt + "</span></div>"
//                                                    : "<div style='margin-left:25px;'><span " + normal + ">" + opt + "</span></div>"
//                                    );
//
//                                }
//                            }
//                        }
                        // checkbox / radio / dropdown
                        if ("Checkbox".equalsIgnoreCase(propertyType)
                                || "Radio".equalsIgnoreCase(propertyType)
                                || "Dropdown".equalsIgnoreCase(propertyType)) {

                            Set<String> selectedSet = Arrays.stream(selectedValue.split(","))
                                    .map(String::trim)
                                    .map(String::toLowerCase)
                                    .collect(Collectors.toSet());

                            if (matchedProperty != null && matchedProperty.value != null) {

                                for (String option : matchedProperty.value.split(",")) {

                                    String opt = option.trim();
                                    boolean isSelected = selectedSet.contains(opt.toLowerCase());

                                    String icon = "";
                                    String style = "";

                                    // ✅ Checkbox / Dropdown
                                    if ("checkbox".equalsIgnoreCase(propertyType)
                                            || "dropdown".equalsIgnoreCase(propertyType)) {

                                        icon = isSelected ? "☑" : "☐";
                                        style = isSelected ? green : normal;
                                    }

                                    // ✅ Radio
                                    else if ("Radio".equalsIgnoreCase(propertyType)) {

                                        if (isSelected) {

                                            icon = "<span style='display:inline-block; width:14px; height:14px;"
                                                    + "background-color:green;"
                                                    + "border:2px solid black;"
                                                    + "border-radius:50%;'></span>";

                                            style = radiobuttonwithmark;

                                        } else {

                                            icon = "<span style='display:inline-block;width:14px;height:14px;"
                                                    + "border:2px solid black;"
                                                    + "border-radius:50%;'></span>";

                                            style = radiobuttonwithUnmark;
                                        }
                                    }


                                    otherAttributesHtml.append(
                                            "<div style='margin-left:20px;'>"
                                                    + "<span " + style + " style='display:inline-flex; align-items:center; gap:6px;'>"
                                                    + icon + " " + opt
                                                    + "</span></div>"
                                    );
                                }
                            }
                        } else if ("Text".equalsIgnoreCase(propertyType)) {
//                        otherAttributesHtml.append("<span ")
//                                .append(green)
//                                .append(" style='margin-right:10px;'>")
//                                .append(selectedValue.trim())
//                                .append("</span>");
                            otherAttributesHtml.
                                    append("<td valign='top'>")
                                    .append("<span style='font-size:18px;font-weight:700;'>")
                                    .append(propertyName + " : ")
                                    .append("<span ")
                                    .append(green)
                                    .append(">")
                                    .append(selectedValue.trim())
                                    .append("</span>")
                                    .append("</span>");
                        }

                        otherAttributesHtml.append("</td>");
                    }

                    otherAttributesHtml.append("</tr></table>");


//                String attributesRowHtml =
//                        "<div class='product-row attributes-row'>" +
//                                "<div class='product-col' style='flex:100%; text-align:left;'>" +
//                                measurementTableHtml +
//                                otherAttributesHtml +
//                                "</div>" +
//                                "</div>";
                    String attributesRowHtml =
                            "<div class='product-row attributes-row' style='border:1px solid black; padding:8px; box-sizing:border-box;'>" +
                                    "<div class='product-col' style='flex:100%; text-align:left;'>" +
                                    measurementTableHtml +
                                    otherAttributesHtml +
                                    "</div>" +
                                    "</div>";


                } else {
                    name = item.productname == "" || item.productname == null ? "Unnamed Product" : item.productname;
                    price = item.sellerprice != 0 ? parseDoubleSafe(String.valueOf(item.sellerprice)) : 0.0;
                }

                String joinService_Name = "";
                if ("isService".equals(item.flag) && item.servicename != null && !item.servicename.trim().isEmpty()) {

                    String[] serviceNameParts = item.servicename.trim().split("\\s+");
                    StringBuilder sb = new StringBuilder();

                    for (int k = 0; k < serviceNameParts.length; k++) {
                        if (k > 0) sb.append("-");
                        sb.append(serviceNameParts[k]);
                    }

                    joinService_Name = sb.toString();
                } else {
                    String[] serviceNameParts = item.productname.trim().split("\\s+");
                    StringBuilder sb = new StringBuilder();

                    for (int k = 0; k < serviceNameParts.length; k++) {
                        if (k > 0) sb.append("-");
                        sb.append(serviceNameParts[k]);
                    }

                    joinService_Name = sb.toString();
                }
                String date = item.created_at;
                String time = "";
                if (seller.getCreateAT() != null && seller.getCreateAT().contains("T")) {
                    String[] parts = seller.getCreateAT().split("T");
                    String[] d1 = parts[0].split("-");

                    date = d1[0] + d1[1] + d1[2];
                    String[] t1 = parts[1].split(":");
                    String actualTime = t1[0] + t1[1];

                    time = parts.length > 1 ? t1[0] + t1[1] : "";
                }
                log.info("date: " + date);
                log.info("Time: " + time);
                String isServiceorProduct = item.flag != null ? item.flag.equals("isService") ? "Services" : "Products" : "Products";
                SimpleDateFormat inputFormat =
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

                SimpleDateFormat sdf =
                        new SimpleDateFormat("yyMMddHHmmss");

                Date createdAtDate = null;

                if (item.created_at != null && item.created_at.contains("T")) {

                    String trimmedDate = item.created_at.split("\\.")[0];
                    createdAtDate = inputFormat.parse(trimmedDate);
                }

                String formattedCreatedAt = createdAtDate != null ? sdf.format(createdAtDate) : "000000000000";


                log.info("After Join name :" + joinService_Name);
                Path imgPath = isServiceorProduct.equals("Services") ? Paths.get(
                        "uploads/uploadsServiceImages/"
                                + seller.getCompanyname() + "-" + date + time + "/"
                                + joinService_Name + "/" + item.defaultImage
                ) : Paths.get(
                        "uploads/uploadsProductImages/"
                                + joinService_Name + "/" + item.defaultImage
                );

                String base64Image = "";
                if (Files.exists(imgPath)) {
                    byte[] bytes = Files.readAllBytes(imgPath);
                    base64Image = Base64.getEncoder().encodeToString(bytes);
                }

                log.info("img path: " + imgPath);

                if ("isService".equals(item.flag)) {
                    productRows.append(String.format(
                            "<div class=\"product-row\">" +
                                    "<div class=\"product-col name\">" +
                                    "<img class=\"img\" src=\"data:image/jpeg;base64,%s\" />" +
                                    "<div class=\"product-name\">%s</div>" +
                                    "</div>" +
                                    "<div class=\"product-col\">%d</div>" +
                                    "<div class=\"product-col\">Rs. %.2f</div>" +
                                    "<div class=\"product-col batch\">%s</div>" +
                                    "<div class=\"product-col\">Rs. %.2f</div>" +
                                    "</div>",
                            base64Image,
                            name,
                            qty,
                            price,
                            "-",
                            subtotal
                    ));

                    String attributesRowHtml =
                            "<div class='product-row attributes-row'>" +
                                    "<div class='product-col' style='flex:100%; text-align:left;'>" +
                                    measurementTableHtml +
                                    otherAttributesHtml +
                                    "</div>" +
                                    "</div>";
                    productRows.append(attributesRowHtml);
                } else {
                    // Product layout (with Batch)
                    productRows.append(String.format(
                            "<div class=\"product-row\">" +

                                    // 1️⃣ Product column (Image + Name)
                                    "<div class=\"product-col name\">" +
                                    "<img class=\"img\" src=\"data:image/jpeg;base64,%s\" />" +
                                    "<div class=\"product-name\">%s</div>" +
                                    "</div>" +

                                    // 2️⃣ Quantity
                                    "<div class=\"product-col\">%d</div>" +

                                    // 3️⃣ Rate
                                    "<div class=\"product-col\">Rs. %.2f</div>" +

                                    // 4️⃣ Batch
                                    "<div class=\"product-col batch\">%s</div>" +

                                    // 5️⃣ Amount
                                    "<div class=\"product-col\">Rs. %.2f</div>" +

                                    "</div>",
                            base64Image,
                            name,
                            qty,
                            price,
                            item.getBatchid() != null ? item.getBatchid() : item.batch_StringID != null ? item.batch_StringID : "-",
                            subtotal
                    ));


                }
            } else if (isSwagathStyle) {
                if ("isService".equals(item.flag)) {
                    // Service layout
                    productRows.append(String.format(
                            "<div class=\"item\">" +
                                    "<div class=\"item-col-top\">" +
                                    "<div class=\"item-title\"><span>%s x %d</span></div>" +   // Service name * qty
                                    "<div class=\"item-property\">%s</div>" +                 // attributes neatly under
                                    "</div>" +
                                    "<div class=\"item-col-bottom\">" +
                                    "<div class=\"item-title\"><span>Rs.%.2f</span></div>" +  // total cost
                                    "</div>" +
                                    "</div>",
                            name, qty, property_attribute, subtotal
                    ));
                } else {
                    // Product layout
                    productRows.append(String.format(
                            "<div class=\"item\">" +
                                    "<div class=\"item-col-left\">" +
                                    "<div class=\"item-title\"><span>%s x %d</span></div>" +
                                    "<div class=\"item-property\">%s</div>" +
                                    "<div class=\"item-desc\">(Rs.%.2f ea.)</div>" +
                                    "</div>" +
                                    "<div class=\"item-col-right\">" +
                                    "<div class=\"item-title\"><span>Rs.%.2f</span></div>" +
                                    "</div>" +
                                    "</div>",
                            name, qty, property_attribute, price, subtotal
                    ));
                }
            } else if (isThermal) {
                productRows.append(String.format(
                        "<tr>" +
                                "<td>%d</td>" +
                                "<td>%s<br><span style='font-size:9px;color:#555;'>%s</span></td>" +
                                "<td>%d</td>" +
                                "<td>Rs.%d</td>" +
                                "<td>Rs.%.2f</td>" +
                                "</tr>",
                        i + 1, name + property_attribute, qty, price, subtotal
                ));
            } else {
                productRows.append(String.format(
                        "<tr><td>%d</td><td>%s</td><td>%d</td><td>Rs.%.2f</td><td>Rs.%.2f</td></tr>",
                        i + 1, name + property_attribute, qty, price, subtotal
                ));
            }
        }

        String formattedDate = "";
        Object rawDate = response.order_date;

        if (rawDate instanceof Date) {

            formattedDate = new SimpleDateFormat("dd/MM/yyyy")
                    .format((Date) rawDate);

        } else if (rawDate instanceof String) {

            String dateStr = (String) rawDate;
            formattedDate = dateStr; // fallback if all parsing fails

            try {
                Date parsedDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                        .parse(dateStr);
                formattedDate = new SimpleDateFormat("dd/MM/yyyy").format(parsedDate);
            } catch (ParseException e1) {
                try {
                    Date parsedDate = new SimpleDateFormat(
                            "EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH
                    ).parse(dateStr);
                    formattedDate = new SimpleDateFormat("dd/MM/yyyy").format(parsedDate);
                } catch (ParseException e2) {
                    // give up, keep raw string
                }
            }
        }

        log.info("ProductRows content: {}", productRows);


        if (templatePath.equalsIgnoreCase("PurestTemplate")) {

            CustomerInformation customer =
                    save != null
                            ? save.getCustomerInformation()
                            : null;

            // ------------------------------------------------------------
            // COMPANY INFORMATION
            // ------------------------------------------------------------

            String companyName =
                    seller != null
                            ? safeString(seller.getCompanyname())
                            : "";

            String companyAddress =
                    seller != null
                            ? safeString(seller.getAddress())
                            : "";

            String companyPhone =
                    seller != null
                            ? safeString(seller.getCompanyMobile())
                            : "";

            String companyEmail =
                    seller != null
                            ? safeString(seller.getEmail())
                            : "";

            String companyState = "27-Maharashtra";


            // ------------------------------------------------------------
            // CUSTOMER INFORMATION
            // ------------------------------------------------------------

            String customerName =
                    customer != null
                            ? safeString(customer.getCustomerName())
                            : "";

            String customerMobile =
                    customer != null
                            ? safeString(customer.getCustomerMobileNum())
                            : "";

            String customerAddress =
                    customer != null
                            ? safeString(customer.getDeliveryAddress())
                            : "";


            // ------------------------------------------------------------
            // INVOICE INFORMATION
            // ------------------------------------------------------------

            String refno =
                    save != null
                            ? safeString(save.getRefno())
                            : "";

            String invoiceDate = formattedDate;


            // ------------------------------------------------------------
            // TOTALS
            // ------------------------------------------------------------

            double discountValue = response.total_Discount;
            double totalAmountValue = response.total_Amount;
            double advanceValue = response.total_Advance;
            double paidValue = response.total_Paid;
            double amountDueValue = response.total_AmountDue;


            // ------------------------------------------------------------
            // ITEM TOTALS
            // ------------------------------------------------------------

            int totalQty =
                    items.stream()
                            .mapToInt(InventoryDTO::getOrder_quantity)
                            .sum();

            String totalQuantity =
                    String.valueOf(totalQty);

            String totalItemValue =
                    String.format("%.2f", total);

            String subTotal =
                    String.format("%.2f", total);


            // ------------------------------------------------------------
            // SUMMARY VALUES
            // ------------------------------------------------------------

            String discountRowHtml = discountValue > 0
                    ? "<tr class=\"summary-row\">"
                    + "<td class=\"label\">Discount</td>"
                    + "<td class=\"num summary-amount\" colspan=\"2\">- Rs. "
                    + String.format("%.2f", discountValue) + "</td>"
                    + "</tr>"
                    : "";

            String advanceRowHtml = advanceValue > 0
                    ? "<tr class=\"summary-row\">"
                    + "<td class=\"label\">Advance</td>"
                    + "<td class=\"num summary-amount\" colspan=\"2\">Rs. "
                    + String.format("%.2f", advanceValue) + "</td>"
                    + "</tr>"
                    : "";

            String totalAmount =
                    String.format("%.2f", totalAmountValue);


            String paid =
                    String.format("%.2f", paidValue);

            String balance =
                    String.format("%.2f", amountDueValue);


            String amountInWords = numberToWords(totalAmountValue);


            String paymentStatus =
                    save != null
                            ? safeString(save.getPayment_status())
                            : "";

            String deliveryStatus =
                    save != null && save.isDelivered()
                            ? "Delivered"
                            : "Not delivered yet";


            // ------------------------------------------------------------
            // LOGO
            // ------------------------------------------------------------

            String logoBase64 = "";
            String signBase64 = "";

            if (response.companyid != null) {

                String logoPath =
                        "uploads/" + response.companyid + "_profile.jpg";
                String signPath =
                        "uploads/" + response.companyid + "_Signature.jpg";

                File logoFile = new File(logoPath);

                if (logoFile.exists()) {

                    try (FileInputStream fis =
                                 new FileInputStream(logoFile)) {

                        logoBase64 = Base64.getEncoder()
                                .encodeToString(fis.readAllBytes());

                    } catch (Exception e) {

                        log.error(
                                "Unable to load Purest company logo",
                                e
                        );
                    }
                }

                File signFile = new File(signPath);

                if (signFile.exists()) {

                    try (FileInputStream fis =
                                 new FileInputStream(signFile)) {

                        signBase64 = Base64.getEncoder()
                                .encodeToString(fis.readAllBytes());

                    } catch (Exception e) {

                        log.error(
                                "Unable to load Purest company logo",
                                e
                        );
                    }
                }
            }


            // ------------------------------------------------------------
            // TERMS
            // ------------------------------------------------------------

            String termsAndConditions =
                    "Thank you for doing business with us.";


            // ------------------------------------------------------------
            // SUMMARY ROW COUNT
            //
            // Sub Total
            // Discount
            // Total
            // Invoice Amount In Words label
            // Invoice Amount In Words value
            // Advance
            // Received
            // Balance
            //
            // = 8 rows
            // ------------------------------------------------------------


            int summaryRowCount = 6
                    + (discountValue > 0 ? 1 : 0)
                    + (advanceValue > 0 ? 1 : 0);


            // ------------------------------------------------------------
            // POPULATE PUREST TEMPLATE
            // ------------------------------------------------------------

            String populatedHtml = htmlTemplate

                    // --------------------------------------------------------
                    // TITLE
                    // --------------------------------------------------------

                    .replace(
                            "{{invoiceTitle}}",
                            "Tax Invoice"
                    )


                    // --------------------------------------------------------
                    // INVOICE DETAILS
                    // --------------------------------------------------------

                    .replace(
                            "{{refno}}",
                            refno
                    )

                    .replace(
                            "{{orderDate}}",
                            invoiceDate
                    )


                    // --------------------------------------------------------
                    // COMPANY
                    // --------------------------------------------------------

                    .replace(
                            "{{companyName}}",
                            "Purest Wood Cold Pressed Oil"
                    )

                    .replace(
                            "{{companyAddress}}",
                            "sector 44A Seawood, Nerul W"
                    )

                    .replace(
                            "{{companyPhone}}",
                            "8149922662"
                    )

                    .replace(
                            "{{companyEmail}}",
                            "purestwoodpressedoil@gmail.com"
                    )

                    .replace(
                            "{{companyState}}",
                            "27-Maharashtra"
                    )


                    // --------------------------------------------------------
                    // CUSTOMER
                    // --------------------------------------------------------

                    .replace(
                            "{{customerName}}",
                            customerName
                    )

                    .replace(
                            "{{customerMobile}}",
                            customerMobile
                    )

                    .replace(
                            "{{customerAddressLine}}",
                            customerAddress
                    )


                    // --------------------------------------------------------
                    // ITEMS
                    // --------------------------------------------------------

                    .replace(
                            "{{itemRows}}",
                            productRows.toString()
                    )

                    .replace(
                            "{{totalQuantity}}",
                            totalQuantity
                    )

                    .replace(
                            "{{totalItemValue}}",
                            totalItemValue
                    )


                    // --------------------------------------------------------
                    // SUMMARY
                    // --------------------------------------------------------

                    .replace(
                            "{{subTotal}}",
                            subTotal
                    )

                    .replace(
                            "{{discountRowHtml}}",
                            discountRowHtml
                    )
                    .replace(
                            "{{advanceRowHtml}}",
                            advanceRowHtml
                    )

                    .replace(
                            "{{totalAmount}}",
                            totalAmount
                    )

                    .replace(
                            "{{amountInWords}}",
                            amountInWords
                    )


                    .replace(
                            "{{totalPaid}}",
                            paid
                    )

                    .replace(
                            "{{totalAmountDue}}",
                            balance
                    )


                    // --------------------------------------------------------
                    // PAYMENT / DELIVERY
                    // --------------------------------------------------------

                    .replace(
                            "{{paymentStatus}}",
                            paymentStatus
                    )

                    .replace(
                            "{{deliveryStatus}}",
                            deliveryStatus
                    )


                    // --------------------------------------------------------
                    // TERMS
                    // --------------------------------------------------------

                    .replace(
                            "{{termsAndConditions}}",
                            termsAndConditions
                    )

                    .replace(
                            "{{summaryRowCount}}",
                            String.valueOf(summaryRowCount)
                    )


                    // --------------------------------------------------------
                    // IMAGES
                    // --------------------------------------------------------

                    .replace(
                            "{{companyLogo}}",
                            logoBase64.isEmpty()
                                    ? ""
                                    : "data:image/jpeg;base64," + logoBase64
                    )

                    .replace(
                            "{{signatureImage}}",
                            signBase64.isEmpty()
                                    ? ""
                                    : "data:image/jpeg;base64," + signBase64
                    );


            response.invoiceHtml = populatedHtml;

            return response;
        }
        String populatedHtml = htmlTemplate
                .replace("{{refno}}", safeString(save.getRefno()))
                .replace("{{buyerid}}", safeString(save.getOrderSource()))
                .replace("{{date}}", formattedDate)
                .replace("{{product_rows}}", productRows.toString())
                .replace("{{order_total}}", String.format("%.2f", total))
                .replace("{{discount}}", String.format("%.2f", response.total_Discount));

        // Try to load company logo dynamically
        String logoPath = "uploads/" + response.companyid + "_profile.jpg";
        File logoFile = new File(logoPath);

        if (logoFile.exists()) {
            try (FileInputStream fis = new FileInputStream(logoFile)) {
                byte[] logoBytes = fis.readAllBytes();
                String base64Image = Base64.getEncoder().encodeToString(logoBytes);
                populatedHtml = populatedHtml.replace("{{logo_base64}}", base64Image);
            }
        } else {
            // fallback to default AppImg.png from resources
            try (InputStream logoStream = classLoader.getResourceAsStream("Invoice_Logo/AppImg.png")) {
                if (logoStream != null) {
                    String base64Image = Base64.getEncoder().encodeToString(logoStream.readAllBytes());
                    populatedHtml = populatedHtml.replace("{{logo_base64}}", base64Image);
                } else {
                    populatedHtml = populatedHtml.replace("{{logo_base64}}", ""); // empty if no logo
                }
            }
        }

        if (response.isgst != null && response.ispriceInclusive != null && (response.isgst.equals("true") || response.ispriceInclusive.equals("true"))) {
            populatedHtml = replaceOrRemove(populatedHtml, "{{cgst}}", String.format("%.2f", (total - only_products) / 2));
            populatedHtml = replaceOrRemove(populatedHtml, "{{sgst}}", String.format("%.2f", (total - only_products) / 2));
            populatedHtml = replaceOrRemove(populatedHtml, "{{gst_total}}", String.format("%.2f", (total - only_products)));
        } else {
            populatedHtml = replaceOrRemove(populatedHtml, "{{cgst}}", null);
            populatedHtml = replaceOrRemove(populatedHtml, "{{sgst}}", null);
            populatedHtml = replaceOrRemove(populatedHtml, "{{gst_total}}", null);
        }
        CustomerInformation customer = (save != null) ? save.getCustomerInformation() : null;

        populatedHtml = replaceOrRemove(populatedHtml, "{{CustomerName}}", safeString(customer != null ? customer.getCustomerName() : null));

        populatedHtml = replaceOrRemove(populatedHtml, "{{CustomerMobileNum}}", safeString(customer != null ? customer.getCustomerMobileNum() : null));

        populatedHtml = replaceOrRemove(populatedHtml, "{{delivery_mode}}", safeString(save != null ? save.getDelivery_mode() : null));

        populatedHtml = replaceOrRemove(populatedHtml, "{{CustomerDeliveryAddress}}", safeString(customer != null ? customer.getDeliveryAddress() : null));

        populatedHtml = replaceOrRemove(populatedHtml, "{{CustomerPickupAddress}}", safeString(save != null ? save.getDelivery_details() : null));

        populatedHtml = replaceOrRemove(populatedHtml, "{{DeliveryDate}}",
                safeString(save != null && save.getDeliveryDate() != null
                        ? ConstructDeliveryDate(save.getDeliveryDate())
                        : null));

        populatedHtml = replaceOrRemove(populatedHtml, "{{EstimatedDeliveryDate}}",
                safeString(save != null && save.getDeliveryDueDate() != null
                        ? ConstructDeliveryDueDate(save.getDeliveryDueDate())
                        : null));

        populatedHtml = replaceOrRemove(populatedHtml, "{{payment}}", safeString(save != null ? save.getPayment_mode() : null));
        populatedHtml = replaceOrRemove(populatedHtml, "{{Total_Amount}}", safeString(save != null ? save.getTotal_Amount() : null));
        populatedHtml = replaceOrRemove(populatedHtml, "{{Total_Advance}}", safeString(save != null ? save.getTotal_Advance() : null));
        populatedHtml = replaceOrRemove(populatedHtml, "{{Total_Discount}}", safeString(save != null ? save.getTotal_Discount() : null));
        populatedHtml = replaceOrRemove(populatedHtml, "{{Total_AmountDue}}", safeString(save != null ? save.getTotal_AmountDue() : null));
        populatedHtml = replaceOrRemove(populatedHtml, "{{Total_Paid}}", safeString(save != null ? save.getTotal_Paid() : null));


        if (populatedHtml.contains("{{cartlength}}")) {
            populatedHtml = populatedHtml.replace("{{cartlength}}", String.valueOf(items.size()));
        }

        InputStream logoStream = classLoader.getResourceAsStream("Invoice_Logo/AppImg.png");
        if (logoStream != null) {
            String base64Image = Base64.getEncoder().encodeToString(logoStream.readAllBytes());
            populatedHtml = populatedHtml.replace("{{logo_base64}}", base64Image);
        }

        populatedHtml = populatedHtml
                .replace("{{company.name}}", seller == null ? "IntelleSyde" : safeString(seller.getCompanyname()))
                .replace("{{company_name}}", seller == null ? "IntelleSyde" : safeString(seller.getCompanyname()))
                .replace("{{company_address}}", "Trichy")
                .replace("{{company_phone}}", "9876543210")
                .replace("{{company_abn}}", "ABN123456")
                .replace("{{footer_note}}", "Thank you for shopping with us!")
                .replace("{{payment_details}}", safeString(save.getPayment_mode()));

        response.invoiceHtml = populatedHtml;
        return response;
    }

    public static String ConstructDeliveryDate(String date) {
        if (date == null || date.isEmpty() || date.equals("")) return "";
        if (date.length() <= 16) return "";
        return date.substring(0, 10) + " " + "( " + date.substring(11, 16) + " )";
    }

    public static String ConstructDeliveryDueDate(String date) {
        if (date == null || date.isEmpty() || date.equals("")) return "";
        String[] a = date.split("T");
        if (a.length == 0) return "";
        String ans = a[0] + " " + "( " + a[1].substring(0, 5) + " )";
        return ans;
    }

    private static String safe(Object obj) {
        return obj == null ? "Nil" : String.valueOf(obj);
    }

    private static String formatAttributes(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) return "";

        return attributes.entrySet()
                .stream()
                .map(e -> "<div>" + e.getKey() + ": " + e.getValue() + "</div>")
                .collect(Collectors.joining());
    }

    public static String formatDate(String inputDate) {
        try {
            // Input format
            SimpleDateFormat inputFormat = new SimpleDateFormat(
                    "EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);

            // Output format
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");

            Date date = inputFormat.parse(inputDate);
            return outputFormat.format(date);

        } catch (Exception e) {
            e.printStackTrace();
            return inputDate; // fallback
        }
    }

    public static String formatSellerFolder(String createAT) {

        LocalDateTime dateTime = LocalDateTime.parse(createAT);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

        return dateTime.format(formatter);
    }

    public static InvoiceResponse ToInvoiceDataResponse(Save save, String templatePath, Seller seller, String userfolder) throws Exception {
        InvoiceResponse response = new InvoiceResponse();

        LocalDateTime dateTime = LocalDateTime.parse(seller.getCreateAT());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

        if (save != null) {
            response.refno = save.getRefno();
            response.companyid = save.getCompanyid();

            response.companyName = seller.getCompanyname();
            response.companyFolder = seller.getCompanyname() + "-" + dateTime.format(formatter);
            response.order_status = save.getOrderStatus();
            response.payment_status = save.getPayment_status();
            response.payment_mode = save.getPayment_mode();
            response.paymentsDTO = FromPaymenttoPaymentDTO(save.getPaymentDetails());
            response.delivery_details = save.getDelivery_details();
            response.deliveryDueDate = save.getDeliveryDueDate();
            response.deliveredDate = save.getDeliveryDate();
            response.isDelivered = save.isDelivered();

            response.companyLogo = "/uploads/" + save.getCompanyid() + "_" + "profile.jpg";
            response.signatureLogo = "/uploads/" + save.getCompanyid() + "_" + "Signature.jpg";

            response.order_date = String.valueOf(save.getOrderDate());
            response.delivery_mode = save.getDelivery_mode();

            response.inventory = FromInventorytoInventoryDTO(
                    save.getSeller_items(),
                    new HashMap<>()
            );

            response.total_Paid = save.getTotal_Paid();
            response.total_Amount = save.getTotal_Amount();
            response.total_Discount = save.getTotal_Discount();
            response.total_Advance = save.getTotal_Advance();
            response.total_AmountDue = save.getTotal_AmountDue();

            response.customerInformationDTO =
                    FromCustomerInformationtoCustomerInformationDTO(
                            save.getCustomerInformation()
                    );

            response.payment_mode = save.getPayment_mode();
        }

        return response;
    }

    public static QuickInvoiceResponse ToQuickInvoiceResponse(QuickInvoice quickInvoice, String templatePath, Seller seller) throws IOException {
        QuickInvoiceResponse response = new QuickInvoiceResponse();

        if (quickInvoice != null) {
            response.qiCustomer_id = quickInvoice.getQiCustomer_id();
            response._id = quickInvoice.get_id();
            response.refno = String.valueOf(quickInvoice.get_id());
            response.companyid = quickInvoice.getCompanyid();
            response.quickProductsDTO = FromQuickProductstoQuickProductsDTO(quickInvoice.getQuickProducts());
            response.ordervalue = quickInvoice.getOrdervalue();
            response.totalitem = quickInvoice.getTotalitems();
            response.delivery_mobileNum = quickInvoice.getDelivery_mobileNum();
            response.order_date = quickInvoice.getOrderDate();
            response.updatedAt = quickInvoice.getUpdatedAt();
            response.QuickInvoiceID = quickInvoice.get_id();
            response.AmountDue = quickInvoice.getAmountdue();
            response.AmountPaid = quickInvoice.getAmountpaid();
            response.paymentmode = quickInvoice.getPaymentmode();
            response.Discount = quickInvoice.getDiscount();
            response.Balance = quickInvoice.getBalance();
            response.note = quickInvoice.getNote();
            response.quickinvoicereference = quickInvoice.getQuickinvoice_reference();

        }

        if (templatePath == null) return response;
        // Load template
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("templates/" + templatePath + ".html");
        if (inputStream == null) throw new FileNotFoundException("Template not found: " + templatePath);
        String htmlTemplate = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

        // Format product rows
        StringBuilder productRows = new StringBuilder();
        List<QuickProductsDTO> items = response.quickProductsDTO;
        double total = 0.0;

        boolean isFarmeRich = templatePath.toLowerCase().contains("farme_rich");
        boolean isHtmlTable = htmlTemplate.contains("<table") && htmlTemplate.contains("<tr>");
        boolean isSwagathStyle = htmlTemplate.contains("item-title") && htmlTemplate.contains("item-desc");

        for (int i = 0; i < items.size(); i++) {
            QuickProductsDTO item = items.get(i);
            double price = 0.0;
            int qty = 0;
            try {
                price = Double.parseDouble(item.product_value);
                qty = Integer.parseInt(item.product_qty);
            } catch (NumberFormatException e) {
                // fallback to 0
            }

            double lineTotal = price;
            total += lineTotal;


            if (isFarmeRich) {
                productRows.append(String.format(
                        "<div class=\"product-row\">" +
                                "<div class=\"product-col name\">%s</div>" +
                                "<div class=\"product-col\">%d</div>" +
                                "<div class=\"product-col\">Rs.%.2f</div>" +
                                "<div class=\"product-col\">Batch</div>" +
                                "<div class=\"product-col\">Rs.%.2f</div>" +
                                "</div>",
                        item.product_name, qty, price, lineTotal
                ));
            } else if (isSwagathStyle) {
                productRows.append(String.format(
                        "<div class=\"item\">" +
                                "<div class=\"item-title\">" +
                                "<span>%s x %d</span>" +
                                "<span>₹%.2f</span>" +
                                "</div>" +
                                "<div class=\"item-desc\"> (₹%.2f)  </div>" +
                                "</div>",
                        item.product_name, qty, price, price / qty
                ));


            } else if (isHtmlTable) {
                productRows.append(String.format(
                        "<tr><td>%d</td><td>%s</td><td>%d</td><td>Rs.%.2f</td><td>Rs.%.2f</td></tr>",
                        i + 1, item.product_name, qty, price, lineTotal
                ));
            }
        }

        // Format date
        String formattedDate = "";
        Object rawDate = quickInvoice.getOrderDate();
        if (rawDate instanceof Date) {
            formattedDate = new SimpleDateFormat("dd/MM/yyyy").format((Date) rawDate);
        } else if (rawDate instanceof String) {
            try {
                Date parsedDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse((String) rawDate);
                formattedDate = new SimpleDateFormat("dd/MM/yyyy").format(parsedDate);
            } catch (ParseException e) {
                formattedDate = (String) rawDate;
            }
        }
        NumberFormat indianFormat = NumberFormat.getNumberInstance(new Locale("en", "IN"));


        DecimalFormat df = (DecimalFormat) indianFormat;
        df.applyPattern("#,##,##0.00");

        String formattedAmount = df.format(quickInvoice.getAmountdue());
        log.info("formattedAmount : " + formattedAmount);

        String populatedHtml = htmlTemplate
                .replace("{{refno}}", response.refno)
                .replace("{{Reference_Number}}", response.quickinvoicereference.substring(12))
                .replace("{{orderdate}}", formattedDate)
                .replace("{{date}}", formattedDate)
                .replace("{{product_rows}}", productRows.toString())
                .replace("{{order_total}}", formattedAmount)
                .replace("{{company.name}}", seller == null ? "IntelleSyde.." : safeString(seller.getCompanyname()));

        // Try to load company logo dynamically
        String logoPath = "uploads/" + response.companyid + "_profile.jpg";
        File logoFile = new File(logoPath);

        if (logoFile.exists()) {
            try (FileInputStream fis = new FileInputStream(logoFile)) {
                byte[] logoBytes = fis.readAllBytes();
                String base64Image = Base64.getEncoder().encodeToString(logoBytes);
                populatedHtml = populatedHtml.replace("{{logo_base64}}", base64Image);
            }
        } else {
            // fallback to default AppImg.png from resources
            try (InputStream logoStream = classLoader.getResourceAsStream("Invoice_Logo/AppImg.png")) {
                if (logoStream != null) {
                    String base64Image = Base64.getEncoder().encodeToString(logoStream.readAllBytes());
                    populatedHtml = populatedHtml.replace("{{logo_base64}}", base64Image);
                } else {
                    populatedHtml = populatedHtml.replace("{{logo_base64}}", ""); // empty if no logo
                }
            }
        }


        populatedHtml = replaceOrRemove(populatedHtml, "{{buyerid}}", safeString("OTC"));

        populatedHtml = replaceOrRemove(populatedHtml, "{{cgst}}", safeString(quickInvoice.getCgst()));
        populatedHtml = replaceOrRemove(populatedHtml, "{{sgst}}", safeString(quickInvoice.getSgst()));
        populatedHtml = replaceOrRemove(populatedHtml, "{{gst_total}}", safeString(quickInvoice.getGstTotal()));
        populatedHtml = replaceOrRemove(populatedHtml, "{{payment}}", safeString(quickInvoice.getPayment()));
        populatedHtml = replaceOrRemove(populatedHtml, "{{delivery}}", safeString(quickInvoice.getDelivery()));
        populatedHtml = replaceOrRemove(populatedHtml, "{{note}}", safeString(quickInvoice.getNote()));
        populatedHtml = replaceOrRemove(populatedHtml, "{{paymentmode}}", safeString(quickInvoice.getPaymentmode()));
        populatedHtml = replaceOrRemove(populatedHtml, "{{AmountDue}}", safeString(df.format(quickInvoice.getAmountdue())));
        populatedHtml = replaceOrRemove(populatedHtml, "{{AmountPaid}}", safeString(df.format(quickInvoice.getAmountpaid())));
        populatedHtml = replaceOrRemove(populatedHtml, "{{Discount}}", quickInvoice.getDiscount() != 0 ? safeString(df.format(quickInvoice.getDiscount())) : safeString("0"));
        populatedHtml = replaceOrRemove(populatedHtml, "{{Balance}}", quickInvoice.getBalance() != 0 ? safeString(df.format(quickInvoice.getBalance())) : safeString("0"));


        if (populatedHtml.contains("{{cartlength}}")) {
            populatedHtml = populatedHtml.replace("{{cartlength}}", String.valueOf(items.size()));
        }

//        InputStream logoStream = classLoader.getResourceAsStream("Invoice_Logo/AppImg.png");
//        if (logoStream != null) {
//            String base64Image = Base64.getEncoder().encodeToString(logoStream.readAllBytes());
//            populatedHtml = populatedHtml.replace("{{logo_base64}}", base64Image);
//        }

        String companyAddress = "Trichy";
        String companyAbn = "1233444";

        if (seller != null && seller.getAddress() != null) {
            companyAddress = safeString(seller.getAddress().getAddress_line1()) + "..." + safeString(seller.getAddress().getCity());
            companyAbn = safeString(seller.getAddress().getPincode());
        }

        populatedHtml = populatedHtml
                .replace("{{company.name}}", seller == null ? "IntelleSyde.." : safeString(seller.getCompanyname()))
                .replace("{{company_name}}", seller == null ? "IntelleSyde.." : safeString(seller.getCompanyname()))
                .replace("{{company_address}}", companyAddress)
                .replace("{{company_phone}}", "1111111111")
                .replace("{{company_abn}}", companyAbn)
                .replace("{{footer_note}}", "Thank you for shopping with us!")
                .replace("{{status}}", "Paid ✔️")
                .replace("{{payment_details}}", safeString(quickInvoice.getPayment()));


        response.invoiceHtml = populatedHtml;
        return response;


    }

    private static String safeString(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    public static String replaceOrRemove(String html, String placeholder, String value) {
        if (value == null || value.trim().isEmpty()) {
            // Remove full <p>...</p> or <div>...</div> containing placeholder
            String regexBlock = "(?i)<(p|div)[^>]*>[^<]*" + Pattern.quote(placeholder) + "[^<]*</(p|div)>\\s*";
            html = html.replaceAll(regexBlock, "");

            // Remove full <tr>...</tr> containing placeholder (handles multi-line rows too)
            String regexRow = "(?is)<tr[^>]*>.*?" + Pattern.quote(placeholder) + ".*?</tr>\\s*";
            html = html.replaceAll(regexRow, "");

            // If not inside <p>, <div>, or <tr>, remove the entire line containing placeholder
            String regexLine = "(?m)^.*" + Pattern.quote(placeholder) + ".*(?:\\r?\\n)?";
            html = html.replaceAll(regexLine, "");

            return html;
        }

        // at this point, value is guaranteed non-null & non-empty
        return html.replace(placeholder, value);
    }

    public static List<QuickProductsDTO> FromQuickProductstoQuickProductsDTO(List<QuickProducts> quickProducts) {
        List<QuickProductsDTO> a = new ArrayList<QuickProductsDTO>();
        for (QuickProducts i : quickProducts) {
            QuickProductsDTO b = new QuickProductsDTO();
            b.product_name = i.product_name;
            b.product_value = i.product_value;
            b.product_priceperunit = i.product_priceperunit;
            b.product_qty = i.product_qty;
            b.isDelete = i.isDelete;
            a.add(b);
        }
        return a;
    }

    public static List<QuickProducts> FromQuickProductsDTOtoQuickProducts(List<QuickProductsDTO> quickProductsDTOS) {
        List<QuickProducts> a = new ArrayList<QuickProducts>();
        for (QuickProductsDTO i : quickProductsDTOS) {
            QuickProducts b = new QuickProducts();
            b.product_name = i.product_name;
            b.product_value = i.product_value;
            b.product_priceperunit = i.product_priceperunit;
            b.product_qty = i.product_qty;
            b.isDelete = false;

            a.add(b);
        }
        return a;
    }

    public static synchronized QuickInvoice ToQuickInvoiceModel(QuickInvoiceRequest quickInvoiceRequest, QuickInvoice lastInvoice) {

        String today = getTodayDate();

        String lastref_if_userid_present = "";
        if (lastInvoice != null) {
            String lastRef = lastInvoice.getQuickinvoice_reference(); // Example: QI-20251006-A23
            lastref_if_userid_present = lastRef;
            String lastDate = lastRef.substring(3, 11); // yyyyMMdd
            char lastAlphabet = lastRef.charAt(12);     // 'A'
            int lastCount = Integer.parseInt(lastRef.substring(13)); // 23


            if (!lastDate.equals(today)) {
                // New day → reset everything
                currentDate = today;
                currentAlphabet = 'A';
                count = 1;
            } else {
                // Same day → continue sequence
                currentDate = lastDate;
                currentAlphabet = lastAlphabet;
                count = lastCount + 1;

                if (count > 100) {
                    count = 1;
                    if (currentAlphabet < 'Z') {
                        currentAlphabet++;
                    } else {
                        // Wrap back to A after Z
                        currentAlphabet = 'A';
                    }
                }
            }
        } else {
            // First invoice of the day
            currentDate = today;
            currentAlphabet = 'A';
            count = 1;
        }
        String reference = "";
        if (quickInvoiceRequest._id != null) {
            reference = lastref_if_userid_present;
        } else {
            reference = "QI-" + currentDate + "-" + currentAlphabet + String.format("%04d", count);  // leading
        }

        QuickInvoice quickInvoice1 = new QuickInvoice();
        if (quickInvoiceRequest._id != null) {
            quickInvoice1.set_id(quickInvoiceRequest._id);
        }
        quickInvoice1.setQiCustomer_id(quickInvoiceRequest.qiCustomer_id == null ? null : quickInvoiceRequest.qiCustomer_id);
        quickInvoice1.setQuickinvoice_reference(reference);
        quickInvoice1.setCompanyid(quickInvoiceRequest.companyid);
        quickInvoice1.setQuickProducts(FromQuickProductsDTOtoQuickProducts(quickInvoiceRequest.quickProductsDTO));
        quickInvoice1.setOrdervalue(quickInvoiceRequest.ordervalue);
        quickInvoice1.setTotalitems(quickInvoiceRequest.totalitem);
        quickInvoice1.setOrderDate(LocalDateTime.now().toString());
        quickInvoice1.setUpdatedAt(LocalDateTime.now().toString());
        quickInvoice1.setDelivery_mobileNum(quickInvoiceRequest.delivery_mobileNum);
        quickInvoice1.setCustomer_name(quickInvoiceRequest.customerName);
        quickInvoice1.setNote(quickInvoiceRequest.note);
        quickInvoice1.setAmountdue(quickInvoiceRequest.AmountDue);
        quickInvoice1.setAmountpaid(quickInvoiceRequest.AmountPaid);
        quickInvoice1.setPaymentmode(quickInvoiceRequest.paymentmode);
        quickInvoice1.setDiscount(quickInvoiceRequest.ordervalue - quickInvoiceRequest.AmountDue);
        if (quickInvoiceRequest.AmountPaid > quickInvoiceRequest.AmountDue) {
            quickInvoice1.setBalance(quickInvoiceRequest.AmountPaid - quickInvoiceRequest.AmountDue);
        }

        return quickInvoice1;
    }

    private static String getTodayDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return LocalDate.now().format(formatter);
    }

    public static synchronized String TosaveReferenceNo(Save save) {
        log.info("Generating Refno");

        String today = getTodayDate();
        if (save != null && save.getRefno() != null) {
            String lastRef = save.getRefno().trim();
            if (!lastRef.isEmpty() && lastRef.startsWith("ORD-") && lastRef.length() >= 18) {
                try {
                    String lastDate = lastRef.substring(4, 12);   // yyyyMMdd
                    char lastAlphabet = lastRef.charAt(13);       // A
                    int lastCount = Integer.parseInt(lastRef.substring(14)); // 0001

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

        return "ORD-" + SavecurrentDate + "-" + SavecurrentAlphabet + String.format("%04d", Savecount);
    }

    private static void reset(String today) {
        SavecurrentDate = today;
        SavecurrentAlphabet = 'A';
        Savecount = 1;
    }

    public static Save TosaveModel(SellerOrderRequest sellerOrderRequest, List<Inventory> finalItems, String now, String refRequest, Save latest) {
        Save save = new Save();

        double totalAmount = finalItems.stream()
                .mapToDouble(e -> e.getOfferprice() > 0 ? e.getOrder_quantity() * e.getOfferprice() : e.getOrder_quantity() * e.getSellerprice())
                .sum();
        save.setOrderStatus("Draft");
        save.setCompanyid(sellerOrderRequest.getCompanyid());
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        // Save order details
        save.setUpdatedAt(LocalDateTime.now().format(formatter));
        save.setOrderDate(new Date());
        save.setOrderSource("INOCK");
        save.setRefno((refRequest == null || refRequest.trim().isEmpty())
                ? TosaveReferenceNo(latest)
                : refRequest);
        log.info("completed refno ");
        save.setSeller_items(finalItems);
        save.setIsgst(sellerOrderRequest.isgst);
        save.setIspriceInclusive(sellerOrderRequest.ispriceInclusive);
        save.setTotal_Amount(totalAmount);
        return save;
    }

    public static Address FromAddressDtotoAddress(AddressDTO addressDTO) {
        Address address = new Address();
        if (addressDTO.PickupPointname != null) address.PickupPointname = addressDTO.PickupPointname;
        if (addressDTO.address_line1 != null) address.address_line1 = addressDTO.address_line1;
        if (addressDTO.address_line2 != null) address.address_line2 = addressDTO.address_line2;
        if (addressDTO.city != null) address.city = addressDTO.city;
        if (addressDTO.pincode != null) address.pincode = addressDTO.pincode;
        return address;
    }

    public static AddressDTO FromAddresstoAddressDTO(Address address) {
        AddressDTO addressdto = new AddressDTO();

        if (address == null) return addressdto;

        if (address.PickupPointname != null && !address.PickupPointname.isEmpty()) {
            addressdto.PickupPointname = address.PickupPointname;
        }

        if (address.getAddress_line1() != null && !address.getAddress_line1().isEmpty())
            addressdto.address_line1 = address.getAddress_line1();

        if (address.getAddress_line2() != null && !address.getAddress_line2().isEmpty())
            addressdto.address_line2 = address.getAddress_line2();

        if (address.getCity() != null && !address.getCity().isEmpty())
            addressdto.city = address.getCity();

        if (address.getPincode() != null && !address.getPincode().isEmpty())
            addressdto.pincode = address.getPincode();

        return addressdto;
    }


//    public static Save TosaveModel(SellerOrderRequest sellerOrderRequest, List<Inventory> finalItems, String now, String refRequest, Save latest) {
//        Save save = new Save();
//
//        save.setOrderStatus("Draft");
//        save.setCompanyid(sellerOrderRequest.getCompanyid());
//        DateTimeFormatter formatter =
//                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
//        // Save order details
//        save.setUpdatedAt(LocalDateTime.now().format(formatter));
//        save.setOrderDate(new Date());
//        save.setOrderSource("INOCK");
//        save.setRefno((refRequest == null || refRequest == "") ? TosaveReferenceNo(latest) : refRequest);
//        log.info("completed refno ");
//        save.setSeller_items(finalItems);
//        save.setIsgst(sellerOrderRequest.isgst);
//        save.setIspriceInclusive(sellerOrderRequest.ispriceInclusive);
//        return save;
//    }

    public static List<PickupPoints> FromPickupPointsDTOtoPickupPoints(List<PickupPointsDTO> pickupPointsDTOList, List<PickupPoints> earlier) {
        List<PickupPoints> pickupPointsList = new ArrayList<>();

        if (earlier != null) {
            pickupPointsList.addAll(earlier);
        }

        for (PickupPointsDTO dto : pickupPointsDTOList) {
            PickupPoints newPickup = new PickupPoints();
            newPickup.setAddress(FromAddressDtotoAddress(dto.address));
            newPickup.setActive(dto.active);
            newPickup.setTime_slot(dto.time_slot);

            boolean updated = false;

            for (PickupPoints existing : pickupPointsList) {
                Address a1 = existing.getAddress();
                Address a2 = newPickup.getAddress();

                log.info(a1 + "" + a2);

                boolean isSameAddress = Objects.equals(a1.getPickupPointname(), a2.getPickupPointname()) &&
                        Objects.equals(a1.getAddress_line1(), a2.getAddress_line1()) &&
                        Objects.equals(a1.getAddress_line2(), a2.getAddress_line2()) &&
                        Objects.equals(a1.getCity(), a2.getCity()) &&
                        Objects.equals(a1.getPincode(), a2.getPincode());

                if (isSameAddress) {
                    // Update active status instead of adding a new duplicate
                    existing.setActive(dto.active);
                    existing.setTime_slot(dto.time_slot);
                    updated = true;
                    break;
                }
            }

            if (!updated) {
                pickupPointsList.add(newPickup);
            }
//            pickupPointsList.add(newPickup);
        }

        for (PickupPoints i : pickupPointsList) {
            log.info("" + i.address.PickupPointname);
        }

        return pickupPointsList;
    }

    public static List<PickupPointsDTO> FromPickupPointstoPickupPointsDTO(List<PickupPoints> pickupPointsList) {
        List<PickupPointsDTO> pickupPointsListDTO = new ArrayList<>();

        for (PickupPoints dto : pickupPointsList) {
            PickupPointsDTO pickupPointsDTO = new PickupPointsDTO();
            pickupPointsDTO.address = (FromAddresstoAddressDTO(dto.address));
            pickupPointsDTO.active = dto.active;
            pickupPointsDTO.time_slot = dto.time_slot;
            pickupPointsListDTO.add(pickupPointsDTO);
        }

        return pickupPointsListDTO;
    }

    public static ProductCategory FromProductCategoryDTOtoProductCategory(ProductCategoryDTO dto) {

        if (dto == null) return null;

        ProductCategory productCategory = new ProductCategory();

        productCategory.set_id(
                (dto.get_id() != null && !dto.get_id().isBlank())
                        ? new ObjectId(dto.get_id())
                        : null
        );

        productCategory.setCategory_name(dto.getCategory_name());

        return productCategory;
    }

    public static Seller ToUpdateCompanyInfo(UpdateCompanyInfoRequest updateCompanyDeliveryInfoRequest, Seller seller) {
        if (!updateCompanyDeliveryInfoRequest.companyname.isEmpty() || updateCompanyDeliveryInfoRequest.companyname != "") {
            seller.setCompanyname(updateCompanyDeliveryInfoRequest.companyname);
        }
        if (!updateCompanyDeliveryInfoRequest.companyMobileNum.isEmpty() || updateCompanyDeliveryInfoRequest.companyMobileNum != "") {
            seller.setCompanyMobile(updateCompanyDeliveryInfoRequest.companyMobileNum);
        }
        if (!updateCompanyDeliveryInfoRequest.gst_enabled.isEmpty() || updateCompanyDeliveryInfoRequest.gst_enabled != "") {
            seller.setGst_enabled(updateCompanyDeliveryInfoRequest.gst_enabled == "true" ? true : false);
        }
        if (!updateCompanyDeliveryInfoRequest.price_inclusive_gst.isEmpty() || updateCompanyDeliveryInfoRequest.price_inclusive_gst != "") {
            seller.setPrice_inclusive_gst(updateCompanyDeliveryInfoRequest.price_inclusive_gst == "true" ? true : false);
        }
        if (!updateCompanyDeliveryInfoRequest.product_type.isEmpty() && updateCompanyDeliveryInfoRequest.product_type.size() != 0) {
            seller.setProduct_type(List.copyOf(updateCompanyDeliveryInfoRequest.product_type));
        }

        if (updateCompanyDeliveryInfoRequest.rewardsDTO != null) {
            seller.setRewards(new Seller.Rewards(
                    updateCompanyDeliveryInfoRequest.rewardsDTO.RewardAmount,
                    updateCompanyDeliveryInfoRequest.rewardsDTO.RewardPoints,
                    updateCompanyDeliveryInfoRequest.rewardsDTO.RedeeemAmount,
                    updateCompanyDeliveryInfoRequest.rewardsDTO.RedeemPoints
            ));
        }


        log.info("Before Updated: " + updateCompanyDeliveryInfoRequest.addressDTO.address_line1);
        Address address = null;
        if (updateCompanyDeliveryInfoRequest.addressDTO != null) {
            address = FromAddressDtotoAddress(updateCompanyDeliveryInfoRequest.addressDTO);
            seller.setAddress(address);
        }
        log.info("After updated: " + seller.getAddress().address_line1);
        if (!updateCompanyDeliveryInfoRequest.deliverymode.isEmpty() && updateCompanyDeliveryInfoRequest.deliverymode.size() != 0) {
            seller.setDeliverymode(updateCompanyDeliveryInfoRequest.deliverymode);
        }
        if (updateCompanyDeliveryInfoRequest.pickupPointsDTO != null) {
            seller.setPickupPoints(FromPickupPointsDTOtoPickupPoints(updateCompanyDeliveryInfoRequest.pickupPointsDTO, seller.getPickupPoints()));
        }
        if (updateCompanyDeliveryInfoRequest.deliveryAreasDTO != null) {
            seller.setDeliveryAreas(FromDeliveryAreasDTOtoFromDeliveryAreas(updateCompanyDeliveryInfoRequest.deliveryAreasDTO, seller.getDeliveryAreas() == null ? null : seller.getDeliveryAreas()));
        }
        if ((seller.getPickupPoints() == null || seller.getPickupPoints().isEmpty()) && seller.getAddress() != null) {

            log.info("Put pickup point as Address");

            PickupPoints newPickup = new PickupPoints();
            newPickup.setAddress(address);
            newPickup.setActive(true);

            seller.setPickupPoints(new ArrayList<>(List.of(newPickup)));
        }
        if (updateCompanyDeliveryInfoRequest.deliveryFee != 0 && updateCompanyDeliveryInfoRequest.orderThreshold != 0) {
            Seller.DeliveryFeeDetails a = new Seller.DeliveryFeeDetails();
            a.setDeliveryFee(updateCompanyDeliveryInfoRequest.deliveryFee);
            a.setOrderThreshold(updateCompanyDeliveryInfoRequest.orderThreshold);
            seller.setDeliveryFeeDetails(a);
        }
        return seller;

    }

    public static List<Seller.DeliveryAreas> FromDeliveryAreasDTOtoFromDeliveryAreas(List<DeliveryAreasDTO> deliveryAreasDTOS, List<Seller.DeliveryAreas> exist) {
        List<String> existCity = new ArrayList<>();
        if (exist != null) {
            for (Seller.DeliveryAreas i : exist) existCity.add(i.city);
        }
        List<Seller.DeliveryAreas> ans = new ArrayList<>();
        for (DeliveryAreasDTO i : deliveryAreasDTOS) {
            if (!existCity.contains(i.city)) ans.add(new Seller.DeliveryAreas(i.city, i.pincode));
        }
        return ans;
    }

    public static List<DeliveryAreasDTO> FromDeliveryAreastoFromDeliveryAreasDTO(List<Seller.DeliveryAreas> exist) {
        List<DeliveryAreasDTO> ans = new ArrayList<>();
        for (Seller.DeliveryAreas i : exist) {
            ans.add(new DeliveryAreasDTO(i.city, i.pincode));
        }
        return ans;
    }

    public static InitialGetUpdateCompanyInfoResponse ToinitialGetUpdateCompanyInfoResponse(List<PickupPointsDTO> pickupPointsDTOS, Seller sellerOptional, User user) {
        InitialGetUpdateCompanyInfoResponse initialGetUpdateCompanyInfoResponse = new InitialGetUpdateCompanyInfoResponse();
        if (sellerOptional.getAddress() != null)
            initialGetUpdateCompanyInfoResponse.addressDTO = FromAddresstoAddressDTO(sellerOptional.getAddress());
        initialGetUpdateCompanyInfoResponse.pickupPointsDTO = pickupPointsDTOS;

        if (sellerOptional.getPaymentModes() != null) {
            initialGetUpdateCompanyInfoResponse.paymentModes = sellerOptional.getPaymentModes();
        }
        if (sellerOptional.getDeliveryFeeDetails() != null) {
            initialGetUpdateCompanyInfoResponse.deliveryFee = sellerOptional.getDeliveryFeeDetails().getDeliveryFee();
            initialGetUpdateCompanyInfoResponse.orderThreshold = sellerOptional.getDeliveryFeeDetails().getOrderThreshold();
        }

        if (sellerOptional.getCompanyMobile() != null) {
            initialGetUpdateCompanyInfoResponse.companyMobile = sellerOptional.getCompanyMobile();
        }
        if (sellerOptional != null && sellerOptional.getDeliveryAreas() != null) {
            initialGetUpdateCompanyInfoResponse.deliveryAreasDTOS = FromDeliveryAreastoFromDeliveryAreasDTO(sellerOptional.getDeliveryAreas());
        }

        if (user != null) {
            initialGetUpdateCompanyInfoResponse.userMobileNum = user.getMobileNum();
            initialGetUpdateCompanyInfoResponse.email = user.getEmail();
            initialGetUpdateCompanyInfoResponse.language_preferred = user.getLanguage_preferred();
            initialGetUpdateCompanyInfoResponse.firstname = user.getFirstname();
            initialGetUpdateCompanyInfoResponse.lastname = user.getLastname();
        }
        if (sellerOptional.getRewards() != null) {
            initialGetUpdateCompanyInfoResponse.rewardsDTO = new UpdateCompanyInfoRequest.RewardsDTO(
                    sellerOptional.getRewards().getRewardAmount(),
                    sellerOptional.getRewards().getRewardPoints(),
                    sellerOptional.getRewards().getRedeeemAmount(),
                    sellerOptional.getRewards().getRedeemPoints()
            );
        }
        initialGetUpdateCompanyInfoResponse.companyname = sellerOptional.getCompanyname();
        initialGetUpdateCompanyInfoResponse.gst_enabled = sellerOptional.isGst_enabled();
        initialGetUpdateCompanyInfoResponse.price_inclusive_gst = sellerOptional.isPrice_inclusive_gst();
        initialGetUpdateCompanyInfoResponse.product_type =
                sellerOptional != null && sellerOptional.getProduct_type() != null && !sellerOptional.getProduct_type().isEmpty()
                        ? List.copyOf(sellerOptional.getProduct_type())
                        : List.of();
        return initialGetUpdateCompanyInfoResponse;
    }

    public static UserBehaviour ToUserBehaviour(UserBehaviourRequestDTO userBehaviourRequestDTO) {
        UserBehaviour userBehaviour = new UserBehaviour();
        userBehaviour.userid = userBehaviourRequestDTO.userid;
        userBehaviour.input_text = userBehaviourRequestDTO.input_text;
        userBehaviour.selected_text = userBehaviourRequestDTO.selected_text;
        userBehaviour.insert_timestamp = LocalDateTime.now().toString();
        return userBehaviour;
    }

    public static Map<String, String> FrompropertyAttributestopropertyAttributesDTO(Map<String, String> propertyAttributes) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String key : propertyAttributes.keySet()) {
            if (key != null && !key.isEmpty() && !propertyAttributes.get(key).isEmpty() && propertyAttributes.get(key) != null) {
                map.put(key, propertyAttributes.get(key));
            }
        }
        return map;

    }

    public static List<InitialServiceInventoryServiceInventoryResponse> ToInitialServiceInventoryServiceInventory(List<ServiceInventory> serviceInventory) {
        List<InitialServiceInventoryServiceInventoryResponse> a = new ArrayList<InitialServiceInventoryServiceInventoryResponse>();

        for (ServiceInventory i : serviceInventory) {
            InitialServiceInventoryServiceInventoryResponse b = new InitialServiceInventoryServiceInventoryResponse();
            b.id = i.id;
            b.sellerid = i.sellerid;
            b.servicename = i.servicename;
            b.servicecost = i.servicecost;
            b.created_at = i.created_at;
            b.isActive = i.isActive;
            b.propertyAttributesDTO = FrompropertyAttributestopropertyAttributesDTO(i.propertyAttributes);
            a.add(b);
        }
        return a;
    }

    public static InitialServiceInventoryServiceInventoryResponse ToViewServiceInventoryServiceInventory(ServiceInventory serviceInventory) {
        InitialServiceInventoryServiceInventoryResponse b = new InitialServiceInventoryServiceInventoryResponse();
        ServiceInventory i = serviceInventory;
        b.id = i.id;
        b.sellerid = i.sellerid;
        b.servicename = i.servicename;
        b.servicecost = i.servicecost;
        b.created_at = i.created_at;
        b.isActive = i.isActive;
        b.propertyAttributesDTO = FrompropertyAttributestopropertyAttributesDTO(i.propertyAttributes);

        return b;
    }

    public static List<PropertyAttributesDTO> FrompropertyAttributestopropertyAttributesDTO(List<PropertyAttributes> propertyAttributes) {
        List<PropertyAttributesDTO> ans = new ArrayList<>();
        for (PropertyAttributes i : propertyAttributes) {
            PropertyAttributesDTO b = new PropertyAttributesDTO();
            b.name = i.name;
            b.type = i.type;
            b.value = i.value;
            ans.add(b);
        }
        return ans;

    }

    public static List<PropertyAttributes> FrompropertyAttributesDTOtopropertyAttributes(List<PropertyAttributesDTO> propertyAttributesDTO) {
        List<PropertyAttributes> ans = new ArrayList<>();
        for (PropertyAttributesDTO i : propertyAttributesDTO) {
            PropertyAttributes b = new PropertyAttributes();
            b.name = i.name;
            b.type = i.type;
            b.value = i.value;
            ans.add(b);
        }
        return ans;

    }

    public static ServiceInventory ToDeleteServiceInventory(DeleteServiceInventoryRequest serviceInventoryRequest) {
        ServiceInventory a = new ServiceInventory();
        if (serviceInventoryRequest.id != null && !serviceInventoryRequest.id.equals("")) {
            a.id = serviceInventoryRequest.id;
        }
        a.isActive = false;
        return a;
    }

    /// /        if (serviceInventoryRequest.isDelete==false || !serviceInventoryRequest.isDelete){
    /// /            a.isActive=false;
    /// /        }
    /// /        else a.isActive=true;
//        if (!serviceInventoryRequest.isActive){
//            a.isActive=false;
//        }
//        else a.isActive=true;
//        a.propertyAttributes=FrompropertyAttributesDTOtopropertyAttributes(serviceInventoryRequest.propertyAttributesDTO);
//        a.addOnIds = serviceInventoryRequest.addOnIds != null
//                ? new ArrayList<>(serviceInventoryRequest.addOnIds)
//                : new ArrayList<>();
//        return a;
//    }
    public static ServiceInventory ToServiceInventory(ServiceInventoryRequest serviceInventoryRequest) {

        ServiceInventory a = new ServiceInventory();

        if (serviceInventoryRequest.id != null && !serviceInventoryRequest.id.equals("")) {
            a.id = serviceInventoryRequest.id;
        }

        if (serviceInventoryRequest.sellerid != null) {
            a.sellerid = serviceInventoryRequest.sellerid;
        }

        if (serviceInventoryRequest.category != null) {
            a.category = FromProductCategoryDTOtoProductCategory(serviceInventoryRequest.category);
        }

        if (serviceInventoryRequest.servicename != null) {
            a.servicename = serviceInventoryRequest.servicename;
        }

        if (serviceInventoryRequest.serviceDesc != null) {
            a.serviceDesc = serviceInventoryRequest.serviceDesc;
        }

        if (serviceInventoryRequest.serviceNotes != null) {
            a.serviceNotes = serviceInventoryRequest.serviceNotes;
        }

        if (serviceInventoryRequest.servicecost != null) {
            a.servicecost = serviceInventoryRequest.servicecost;
        }

        // Boolean check
        if (serviceInventoryRequest.isActive) {
            a.isActive = serviceInventoryRequest.isActive;
        }

        if (serviceInventoryRequest.propertyAttributesDTO != null) {
            a.propertyAttributes =
                    FrompropertyAttributesDTOtopropertyAttributes(
                            serviceInventoryRequest.propertyAttributesDTO
                    );
        }

        if (serviceInventoryRequest.addOnIds != null) {
            a.addOnIds = new ArrayList<>(serviceInventoryRequest.addOnIds);
        }

        return a;
    }

    public static List<ObjectId> formatStringToObjectIds(List<String> addonids) {
        List<ObjectId> ans = new ArrayList<>();
        for (String i : addonids) {
            ans.add(new ObjectId(i));
        }
        return ans;
    }

    public static List<SellerServiceResponse> ToSellerServiceResponse(List<ServiceInventory> serviceInventory) {
        List<SellerServiceResponse> sellerServiceResponse = new ArrayList<SellerServiceResponse>();
        for (ServiceInventory i : serviceInventory) {
            SellerServiceResponse a = new SellerServiceResponse();
            a.servicename = i.servicename;
            a.servicecost = i.servicecost;
            a.defaultImage = i.defaultImage;
            a.created_at = i.created_at;
            a.propertyAttributes = FrompropertyAttributestopropertyAttributesDTO(i.propertyAttributes);
            a.isActive = i.isActive;
            a.id = i.id;
            a.sellerid = i.sellerid;
            sellerServiceResponse.add(a);
        }
        return sellerServiceResponse;

    }

//    public static  ServiceInventory ToServiceInventory (ServiceInventoryRequest serviceInventoryRequest){
//        ServiceInventory a = new ServiceInventory();
//        if (serviceInventoryRequest.id!=null && !serviceInventoryRequest.id.equals("")){
//            a.id= serviceInventoryRequest.id;
//        }
//        a.sellerid =serviceInventoryRequest.sellerid;
//        SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddHHmmss");
//        a.created_at = sdf.format(new Date());
//        a.category=FromProductCategoryDTOtoProductCategory(serviceInventoryRequest.category);
//        a.servicename=serviceInventoryRequest.servicename;
//        a.serviceDesc=serviceInventoryRequest.serviceDesc;
//        a.serviceNotes=serviceInventoryRequest.serviceNotes;
//        a.servicecost =serviceInventoryRequest.servicecost;

    public static QICustomers ToQICustomersDTOtoQICustomers(QICustomerRequestDTO qiCustomerRequestDTO) {
        QICustomers qiCustomers = new QICustomers();

        qiCustomers.setAddress(qiCustomerRequestDTO.address);
        qiCustomers.setMobileNum(qiCustomerRequestDTO.mobile_num);
        qiCustomers.setName(qiCustomerRequestDTO.name);
        return qiCustomers;
    }

    public static List<QICustomerResponseDTO> ToQICustomerResponseDTO(List<QICustomers> qiCustomers) {
        List<QICustomerResponseDTO> qiCustomerResponseDTO = new ArrayList<QICustomerResponseDTO>();

        for (QICustomers i : qiCustomers) {
            QICustomerResponseDTO qiCustomerResponseDTO1 = new QICustomerResponseDTO();
            qiCustomerResponseDTO1._id = i.get_id();
            qiCustomerResponseDTO1.mobile_num = i.getMobileNum();
            qiCustomerResponseDTO1.address = i.getAddress();
            qiCustomerResponseDTO1.name = i.getName();
            qiCustomerResponseDTO.add(qiCustomerResponseDTO1);
        }
        return qiCustomerResponseDTO;
    }

    public static GetPromoMessageResponse ToGetPromoMessageResponse(Promo_messages promoMessages) {
        GetPromoMessageResponse a = new GetPromoMessageResponse();
        a._id = promoMessages.get_id();
        a.sellerId = promoMessages.getSellerid();
        a.promo_Msg = promoMessages.getPromo_msg();
        a.created_By = promoMessages.getCreated_by();
        a.updated_By = promoMessages.getUpdated_by();
        a.created_At = promoMessages.getCreated_At();
        a.updated_At = promoMessages.getUpdated_At();
        return a;
    }

    public static InitialGetAllProductsResponse adaptProductResponse(List<InitialGetAllProductsResponse.ProductItem> mongoResults) {
        InitialGetAllProductsResponse response = new InitialGetAllProductsResponse();
        List<InitialGetAllProductsResponse.ProductItem> products = new ArrayList<>();

        for (InitialGetAllProductsResponse.ProductItem src : mongoResults) {
            InitialGetAllProductsResponse.ProductItem item = new InitialGetAllProductsResponse.ProductItem();

            item.setProductId(src.getProductId());
            item.setProductName(src.getProductName());
            item.setSellingPrice(src.getSellingPrice());
            item.setOfferPrice(src.getOfferPrice());
            item.setDiscountPercent(src.getDiscountPercent());
            item.setDefaultImage(src.getDefaultImage());
            item.setCategory(src.getCategory());
            item.propertyAttributes = src.propertyAttributes;

            products.add(item);
        }

        response.setProducts(products);
        return response;
    }

    public static SellerOrderRequest toSellerOrderRequest(SaveAndExecuteQuickInvoiceRequest saveAndExecuteQuickInvoiceRequest) {
        SellerOrderRequest sellerOrderRequest = new SellerOrderRequest();
        sellerOrderRequest.setInventory(saveAndExecuteQuickInvoiceRequest.inventory);
        sellerOrderRequest.setCompanyid(saveAndExecuteQuickInvoiceRequest.companyid);
        sellerOrderRequest.setRefno(saveAndExecuteQuickInvoiceRequest.refno);
        sellerOrderRequest.isgst = saveAndExecuteQuickInvoiceRequest.isgst;
        sellerOrderRequest.ispriceInclusive = saveAndExecuteQuickInvoiceRequest.ispriceInclusive;
        return sellerOrderRequest;
    }

    public static ExecuteOrderRequest toExecuteOrderRequest(SaveAndExecuteQuickInvoiceRequest saveAndExecuteQuickInvoiceRequest) {
        ExecuteOrderRequest executeOrderRequest = new ExecuteOrderRequest();
        executeOrderRequest.inventory = (saveAndExecuteQuickInvoiceRequest.inventory);
        executeOrderRequest.companyid = (saveAndExecuteQuickInvoiceRequest.companyid);
        executeOrderRequest.rewards = saveAndExecuteQuickInvoiceRequest.rewards;
        executeOrderRequest.refno = (saveAndExecuteQuickInvoiceRequest.refno);
        executeOrderRequest.paymentsDTO = saveAndExecuteQuickInvoiceRequest.paymentsDTO;
        executeOrderRequest.customerInformationDTO = saveAndExecuteQuickInvoiceRequest.customerInformationDTO;
        executeOrderRequest.payment_mode = saveAndExecuteQuickInvoiceRequest.payment_mode;
        executeOrderRequest.address = saveAndExecuteQuickInvoiceRequest.address;
        executeOrderRequest.draft_active = saveAndExecuteQuickInvoiceRequest.draft_active;
        return executeOrderRequest;
    }

    public static CustomerInformation FromCustomerInformationDTOtoCustomerInformation(CustomerInformationDTO customerInformationDTO) {
        CustomerInformation customerInformation = new CustomerInformation();
        customerInformation.setCustomerName(customerInformationDTO.customerName);
        customerInformation.setCustomerMobileNum(customerInformationDTO.customerMobileNum);
        if (customerInformationDTO.deliveryAddress != null) {
            customerInformation.setDeliveryAddress(customerInformationDTO.deliveryAddress);
        }
        return customerInformation;
    }

    public static CustomerInformationDTO FromCustomerInformationtoCustomerInformationDTO(CustomerInformation customerInformation) {
        if (customerInformation == null) return new CustomerInformationDTO();
        CustomerInformationDTO customerInformationdto = new CustomerInformationDTO();
        customerInformationdto.customerName = (customerInformation.getCustomerName());
        customerInformationdto.customerMobileNum = (customerInformation.getCustomerMobileNum());
        customerInformationdto.postalCode = (customerInformation.getPostalCode());
        customerInformationdto.city = customerInformation.getCity();
        customerInformationdto.state = customerInformation.getState();
        customerInformationdto.email = customerInformation.getEmail();
        if (customerInformation.getDeliveryAddress() != null) {
            customerInformationdto.deliveryAddress = customerInformation.getDeliveryAddress();
        }
        return customerInformationdto;
    }

    public static List<Batches> ToBatches(InsertProductsRequest request, List<ProductVariantDTO> productVariantDTO, String lastbatchID, ObjectId productId) {
        LocalDateTime now = LocalDateTime.now();
        List<Batches> ans = new ArrayList<>();
        String last = lastbatchID;
        for (ProductVariantDTO dto : productVariantDTO) {
            last = generateBatchId(last);
            int a = (int) dto.procumentPrice;

            Batches batches = new Batches();
            batches.setBatch_Id(last);
            batches.setProductname(request.productName);


            batches.setSeller_price(dto.sellingPrice);
            batches.setCompanyid(String.valueOf(request.companyId));
            batches.setDiscount(dto.discount);
            batches.setOfferPrice(dto.offerPrice);
            batches.setVariantName(dto.variantName);

            batches.setCreatedAt(now.toString());
            batches.setUpdatedAt(now.toString());
            batches.setActive(true);

            batches.setMargin_percentage((int) dto.margin);
            batches.setProcurement_price(a);

            batches.setProductid(productId);
            batches.setStock_availability(dto.quantity);
            ans.add(batches);
        }
        return ans;
    }

    private static String numberToWords(double amount) {

        long number = Math.round(amount);

        if (number == 0) {
            return "Zero Rupees only";
        }

        String[] ones = {
                "", "One", "Two", "Three", "Four", "Five",
                "Six", "Seven", "Eight", "Nine", "Ten",
                "Eleven", "Twelve", "Thirteen", "Fourteen",
                "Fifteen", "Sixteen", "Seventeen", "Eighteen",
                "Nineteen"
        };

        String[] tens = {
                "", "", "Twenty", "Thirty", "Forty",
                "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
        };

        StringBuilder result = new StringBuilder();

        long crore = number / 10_000_000;
        number %= 10_000_000;

        long lakh = number / 100_000;
        number %= 100_000;

        long thousand = number / 1_000;
        number %= 1_000;

        long hundred = number / 100;
        number %= 100;

        if (crore > 0) {
            result.append(convertBelowThousand(crore, ones, tens))
                    .append(" Crore ");
        }

        if (lakh > 0) {
            result.append(convertBelowThousand(lakh, ones, tens))
                    .append(" Lakh ");
        }

        if (thousand > 0) {
            result.append(convertBelowThousand(thousand, ones, tens))
                    .append(" Thousand ");
        }

        if (hundred > 0) {
            result.append(ones[(int) hundred])
                    .append(" Hundred ");
        }

        if (number > 0) {
            result.append(convertBelowHundred(number, ones, tens));
        }

        return result.toString().trim() + " Rupees only";
    }

    private static String convertBelowThousand(
            long number,
            String[] ones,
            String[] tens) {

        StringBuilder result = new StringBuilder();

        if (number >= 100) {
            result.append(ones[(int) (number / 100)])
                    .append(" Hundred ");

            number %= 100;
        }

        if (number > 0) {
            result.append(convertBelowHundred(number, ones, tens));
        }

        return result.toString().trim();
    }

    private static String convertBelowHundred(
            long number,
            String[] ones,
            String[] tens) {

        if (number < 20) {
            return ones[(int) number];
        }

        return tens[(int) (number / 10)]
                + (number % 10 != 0
                ? " " + ones[(int) (number % 10)]
                : "");
    }
}

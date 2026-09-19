package Farme_rich.Revolt.Services;

import Farme_rich.Revolt.Adaptor.StationAdaptor;
import Farme_rich.Revolt.DTO.Request.*;
import Farme_rich.Revolt.DTO.Response.MessageResponse;
import Farme_rich.Revolt.DTO.Response.StationMapResponse;
import Farme_rich.Revolt.DTO.Response.deviceIdResponse;
import Farme_rich.Revolt.DTO.Response.registerResponse;
import Farme_rich.Revolt.Model.Charger;
import Farme_rich.Revolt.Model.RevoltUser;
import Farme_rich.Revolt.Model.Stations;
import Farme_rich.Revolt.Repo.ChargerRepo;
import Farme_rich.Revolt.Repo.RevoltUserRepo;
import Farme_rich.Revolt.Repo.StationRepo;
import Farme_rich.Security.JwtService;
import Farme_rich.Seller.DTO.Request.SubscriptionEmailDTO;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.mongodb.DuplicateKeyException;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import jakarta.mail.internet.MimeMessage;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import static Farme_rich.Security.PasswordUtils.decrypt;
import static Farme_rich.Security.PasswordUtils.encrypt;

@Service
public class StationService {

    private static final Logger log = LoggerFactory.getLogger(StationService.class);
    @Autowired
    private StationRepo stationRepo;
    @Autowired
    private ChargerRepo chargerRepo;
    @Autowired
    private RevoltUserRepo revoltUserRepo;
    @Autowired
    private StationAdaptor stationAdaptor;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private JavaMailSender mailSender;


//    public List<StationMapResponse> getStations(StationRequest request) {
//        List<Stations> stations;
//        if (request.searchSimilarityByAddress != null && !request.searchSimilarityByAddress.trim().isEmpty()) {
//            log.info("Search by Address : " + request.searchSimilarityByAddress);
//            stations = stationRepo.findByStationAddress(request.searchSimilarityByAddress.trim());
//        } else {
//            stations = stationRepo.findAll();
//        }
//        return stationAdaptor.getAllStationMapper(stations);
//    }

    public deviceIdResponse HandleDeviceId(String deviceId) throws Exception {
        MessageResponse msgResponse = new MessageResponse();
        RevoltUser existingUser = revoltUserRepo.findBydeviceId(deviceId);

        if (existingUser == null) {
            log.info("No existing user found for deviceId: " + deviceId);
            msgResponse.ValidationMessage = "User not found";
            return stationAdaptor.TodeviceIdResponseDTO(existingUser, msgResponse, null);
        }

        log.info("Found existingUser with ID: " + existingUser.get_id());


        msgResponse.pin = String.valueOf(existingUser.getPin());
        msgResponse.mobileNum = existingUser.getMobileNum();
        msgResponse.setFirstname(existingUser.getFirstName());

        msgResponse.setLastname(existingUser.getLastName());
        msgResponse.userid = existingUser.get_id();

        msgResponse.ValidationCode = "UpdatePin";
        msgResponse.ValidationMessage = "success";

        // JWT Token Generation
        HashMap<String, Object> map = new HashMap<>();
        map.put("platform", "MOBILE");

        String encryptedPin = existingUser.getPin();
        String deviceKey = existingUser.getDeviceId() + "intellesydetech";
        String decryptedPin = "";

        if (encryptedPin == null || encryptedPin.trim().isEmpty()) {
            log.info("Encrypted PIN is null or empty, cannot decrypt.");
        } else {
            decryptedPin = decrypt(encryptedPin, deviceKey);
            log.info("Decrypted PIN: " + decryptedPin);
        }

//        String token = jwtService.generateToken(
//                map,
//                encryptedPin,
//                existingUser.getDeviceId(),
//                ""
//        );

        deviceIdResponse response = stationAdaptor.TodeviceIdResponseDTO(existingUser, msgResponse, null);
//        response.token = (token);
//        response.seller_type = existingSeller.getSeller_type();
//        response.companyname = existingSeller.getCompanyname();
//        response.role = (roles);
//        response.companyid = existingSeller.getId();
//        response.upi = existingSeller.getUpi_id();
//        response.paidUser = (existingUser.isPaidUser());
//        response.language_preferred = existingUser.getLanguage_preferred();
//        response.createAT = existingSeller.getCreateAT();

        return response;
    }

    @Transactional
    public registerResponse RegisterProfile(userSignupRequest request) throws Exception {

        MessageResponse msgResponse = new MessageResponse();

        String pin = request.getPin();
        String validationCode = request.getValidationCode();

        log.info("Validation Code : {}", validationCode);

        String encryptedPin = null;

        if (pin != null && !pin.trim().isEmpty()
                && request.getDeviceId() != null) {

            String secretKey = request.getDeviceId() + "intellesydetech";
            encryptedPin = encrypt(pin, secretKey);

            log.info("Encrypted PIN : {}", encryptedPin);
        }

        RevoltUser savedUser = null;

        /*
         * NEW USER REGISTRATION
         */
        if ("NewUser".equals(validationCode)) {

            try (ClientSession session = mongoClient.startSession()) {

                session.startTransaction();

                try {

                    LocalDateTime now = LocalDateTime.now();

                    RevoltUser user = new RevoltUser();
                    user.setFirstName(request.getFirstname());
                    user.setLastName(request.getLastname());
                    user.setPin(encryptedPin);
                    user.setDeviceId(request.getDeviceId());
                    user.setMobileNum(request.getMobileNum());
                    user.setCreated_At(now);
                    user.setUpdated_At(now);

                    user.setStatus(true);

                    savedUser = mongoTemplate
                            .withSession(session)
                            .insert(user);

                    session.commitTransaction();

                    msgResponse.ValidationMessage = "Successfully Registered";

                } catch (DuplicateKeyException e) {

                    session.abortTransaction();
                    msgResponse.ErrorMsg =
                            "Duplicate Key Error : " + e.getMessage();

                    return stationAdaptor.ToregisterResponseDTO(
                            null,
                            msgResponse,
                            null
                    );

                } catch (Exception e) {

                    session.abortTransaction();
                    msgResponse.ErrorMsg =
                            "Transaction Failed : " + e.getMessage();

                    return stationAdaptor.ToregisterResponseDTO(
                            null,
                            msgResponse,
                            null
                    );
                }
            }

        }

        /*
         * UPDATE EXISTING USER
         */
        else {

            RevoltUser existingUser =
                    revoltUserRepo.findBydeviceId(request.getDeviceId());

            if (existingUser == null) {

                msgResponse.ErrorMsg = "Revolt User Not Found";

                return stationAdaptor.ToregisterResponseDTO(
                        null,
                        msgResponse,
                        null
                );
            }

            Query query =
                    new Query(Criteria.where("id").is(existingUser.get_id()));

            Update update = new Update();

            switch (validationCode) {

                case "UpdatePin":

                    update.set("firstPiN", encryptedPin);
                    break;

                case "UpdateMobile":

                    update.set("mobileNum", request.getMobileNum());
                    update.set("firstPiN", encryptedPin);
                    break;

                case "UpdateDevice":

                    update.set("deviceId", request.getDeviceId());
                    update.set("firstPiN", encryptedPin);
                    break;

                default:

                    msgResponse.ErrorMsg = "Invalid Validation Code";

                    return stationAdaptor.ToregisterResponseDTO(
                            null,
                            msgResponse,
                            null
                    );
            }

            mongoTemplate.updateFirst(
                    query,
                    update,
                    RevoltUser.class
            );

            savedUser = revoltUserRepo.findById(existingUser.get_id())
                    .orElse(existingUser);

            msgResponse.ValidationMessage = "Successfully Updated";
        }


        /*
         * TOKEN GENERATION
         */
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("platform", "MOBILE");

        String token = jwtService.generateToken(
                claims,
                encryptedPin,
                request.getDeviceId(),
                ""
        );

        msgResponse.setUserid(savedUser.get_id());
        msgResponse.setFirstname(savedUser.getFirstName());

        return stationAdaptor.ToregisterResponseDTO(
                savedUser,
                msgResponse,
                token
        );
    }

    public registerResponse SignupValidate(userSignupRequest user) {
        MessageResponse msgResponse = new MessageResponse();
        RevoltUser existingUser = null;
        log.info(user.getDeviceId() + "/////" + user.getMobileNum());
        try {
            existingUser = revoltUserRepo.findBydeviceId(user.getDeviceId());
            if (existingUser != null) {
                if (existingUser.getMobileNum().equals(user.getMobileNum())) {
                    msgResponse.ValidationMessage = "Duplicate User";
                    msgResponse.userid = existingUser.get_id();
                    return stationAdaptor.ToregisterResponseDTO(existingUser, msgResponse, null);
                } else {
                    msgResponse.ValidationMessage = "deviceID is exist";
                    msgResponse.pin = String.valueOf(existingUser.getPin());
                    msgResponse.mobileNum = user.getMobileNum();
                    msgResponse.userid = existingUser.get_id();
                    msgResponse.ValidationCode = "UpdateMobile";
                    return stationAdaptor.ToregisterResponseDTO(existingUser, msgResponse, null);
                }
            } else {
                existingUser = revoltUserRepo.findBymobileNum(user.getMobileNum());
//                log.info("Exist mobile: "+existingUser!=null);
                if (existingUser != null) {
                    msgResponse.ValidationMessage = "Mobile exist but not found deviceID";
                    msgResponse.userid = existingUser.get_id();
                    msgResponse.ValidationCode = "UpdateDevice";

                    return stationAdaptor.ToregisterResponseDTO(existingUser, msgResponse, null);

                } else {
                    msgResponse.ValidationMessage = "Successfully Registered";
                    msgResponse.ValidationCode = "NewUser";
                    return stationAdaptor.ToregisterResponseDTO(existingUser, msgResponse, null);
                }

            }
        } catch (Exception e) {
            log.error("error : " + e);
            msgResponse.ErrorMsg = e.getMessage();
        }
        return stationAdaptor.ToregisterResponseDTO(null, msgResponse, null);
    }

    public registerResponse CheckPasscode(userSignupRequest user) throws Exception {
        MessageResponse msgResponse = new MessageResponse();
        log.info("Inside checkpasscode deviceid: " + user.getDeviceId() + " ");

        String secretKey = user.getDeviceId() + "intellesydetech";
        RevoltUser a = revoltUserRepo.findBydeviceId(user.getDeviceId());


        if (a == null) {
            log.info("a is null");
            msgResponse.ValidationMessage = "incorrect";
            return stationAdaptor.ToregisterResponseDTO(null, msgResponse, null);
        }


        String encryptedInputPin = encrypt(user.getPin(), secretKey);

        String token = "";
        List<String> role = new ArrayList<>();

        if (a.getPin().equals(user.getPin()) || encryptedInputPin.equals(a.getPin())) {
            msgResponse.ValidationMessage = "correct";

            // JWT Token Generation
            HashMap<String, Object> map = new HashMap<>();

            map.put("deviceId", a.getDeviceId());
            map.put("platform", "MOBILE");

            String encryptedPin = a.getPin();
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
            msgResponse.userid = a.get_id();
        } else {
            msgResponse.ValidationMessage = "pass incorrect";
        }

        return stationAdaptor.ToregisterResponseDTO(a, msgResponse, token);

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

            return false; // no matching file found

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public List<StationMapResponse> getStations(StationRequest request) {

        List<Stations> stations;

        if (request.searchSimilarityByAddress != null && !request.searchSimilarityByAddress.trim().isEmpty()) {

            String address = request.searchSimilarityByAddress.trim();

            log.info("Search by Address : {}", address);

            // Split by comma and spaces
            String[] words = address.split("[,\\s]+");

            List<Criteria> criteriaList = new ArrayList<>();

            for (String word : words) {
                word = word.trim();
                // Ignore small
                if (word.length() < 3) {
                    continue;
                }
                criteriaList.add(Criteria.where("station_Address").regex(word, "i"));
            }

            Query query = new Query();

            if (!criteriaList.isEmpty()) {
                query.addCriteria(
                        new Criteria().orOperator(
                                criteriaList.toArray(new Criteria[0])
                        )
                );

                stations = mongoTemplate.find(query, Stations.class);
            } else {
                stations = stationRepo.findAll();
            }

        } else {
            log.info("Find All stations");
            stations = stationRepo.findAll();
        }

        return stationAdaptor.getAllStationMapper(stations);

    }

    public List<StationMapResponse.ChargerDetailsDTO> getStationChargers(ObjectId stationId) {

        List<Charger> chargers = chargerRepo.findByStationId(stationId);

        List<StationMapResponse.ChargerDetailsDTO> response = new ArrayList<>();

        for (Charger charger : chargers) {
            Charger.ChargerDetails details = charger.getChargerDetails();
            response.add(new StationMapResponse.ChargerDetailsDTO(
                    details.getCharger_type(),
                    details.getMax_power(),
                    details.getMax_voltage(),
                    details.getMax_current(),
                    details.getStatus()
            ));
        }
        return response;
    }

    public List<StationMapResponse> SuggestByStationName(SearchStationRequest request) {

        final String rawSearch = request.searchText;
        if (rawSearch == null || rawSearch.isBlank()) {
            return Collections.emptyList();
        }

        final Path path = Paths.get(
                "uploads",
                "FilterProducts",
                "Stations.json"
        );

        if (!Files.exists(path)) {
            return Collections.emptyList();
        }

        final String searchValue = rawSearch.trim().toLowerCase(Locale.ROOT);

        final List<StationMapResponse> result = new ArrayList<>(10);
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectReader reader = mapper.readerFor(ReadStationsFromJSON.class);

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path.toFile()));
             MappingIterator<ReadStationsFromJSON> it = reader.readValues(bis)) {

            while (it.hasNext() && result.size() < 10) {

                ReadStationsFromJSON station = it.next();

                if (station == null) {
                    continue;
                }

                String stationName = station.station_Name;
                String stationAddress = station.station_Address;

                boolean matchesName = stationName != null &&
                        stationName.toLowerCase(Locale.ROOT).contains(searchValue);

                boolean matchesAddress = stationAddress != null &&
                        stationAddress.toLowerCase(Locale.ROOT).contains(searchValue);

                // Search in both station name and address
                if (!matchesName && !matchesAddress) {
                    continue;
                }

                StationMapResponse response = new StationMapResponse();

                response.stationId = station._id != null
                        ? station._id.toHexString()
                        : null;

                response.stationName = station.station_Name;
                response.stationAddress = station.station_Address;

//                if (station.locationPoints != null) {
//                    response.latitude = station.locationPoints.latitude;
//                    response.longitude = station.locationPoints.longitude;
//                }
//
//                // No charger details in JSON currently
//                response.chargers = new ArrayList<>();

                result.add(response);
            }

        } catch (IOException e) {
            log.error("Error streaming JSON file: {}", path, e);
            return Collections.emptyList();
        }

        return result;
    }
}

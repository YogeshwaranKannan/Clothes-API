package Farme_rich.Revolt.Controllers;


import Farme_rich.ErrorHandling.ExceptionHandling;
import Farme_rich.Revolt.DTO.Request.GetOTPRequest;
import Farme_rich.Revolt.DTO.Request.SearchStationRequest;
import Farme_rich.Revolt.DTO.Request.StationRequest;
import Farme_rich.Revolt.DTO.Request.userSignupRequest;
import Farme_rich.Revolt.DTO.Response.StationMapResponse;
import Farme_rich.Revolt.DTO.Response.deviceIdResponse;
import Farme_rich.Revolt.DTO.Response.registerResponse;
import Farme_rich.Revolt.Services.StationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/revolt")
@Configuration
@EnableTransactionManagement
public class ChargeController {
    private static final Logger log = LoggerFactory.getLogger(ChargeController.class);
    @Autowired
    private StationService stationService;
    @Value("${app.upload-dir}")
    private String uploadDir;

    @PostMapping("/deviceId")
    public deviceIdResponse HandleDeviceId(@RequestParam String deviceId) {
        log.info("controller deviceid :", deviceId);

        deviceIdResponse response = null;
        try {
            return stationService.HandleDeviceId(deviceId);
        } catch (Exception ex) {
            log.error("Error in HandleDeviceId : ", ex);
            response = new deviceIdResponse();

            response.ResponseMessage.setErrorMsg(ExceptionHandling.GetFullExceptionDetails(ex));
        }
        return response;
    }

    @PostMapping("/profile")
    public registerResponse RegisterProfile(@RequestBody userSignupRequest userSignupRequest) {

        log.info("validation code  in RegisterProfile : ", userSignupRequest.getValidationCode());

        registerResponse response = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            log.info("SignupValidate Request : {}",
                    objectMapper.writeValueAsString(userSignupRequest));
            return stationService.RegisterProfile(userSignupRequest);
        } catch (Exception ex) {
            log.error("Error in RegisterProfile : ", ex);
            response = new registerResponse();
            response.ResponseMessage.setErrorMsg(ExceptionHandling.GetFullExceptionDetails(ex));
        }
        return response;
    }

    @PostMapping("/signupvalidate")
    public registerResponse SignupValidate(@RequestBody userSignupRequest userSignupRequest) {
        registerResponse response = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            log.info("SignupValidate Request : {}",
                    objectMapper.writeValueAsString(userSignupRequest));
            return stationService.SignupValidate(userSignupRequest);
        } catch (Exception ex) {
            log.error("Error in SignupValidate : ", ex);
            response = new registerResponse();
            response.ResponseMessage.setErrorMsg(ExceptionHandling.GetFullExceptionDetails(ex));
        }
        return response;
    }

    @PostMapping("/checkpasscode")
    public registerResponse CheckPasscode(@RequestBody userSignupRequest userSignupRequest) {
        registerResponse response = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            log.info("SignupValidate Request : {}",
                    objectMapper.writeValueAsString(userSignupRequest));
            return stationService.CheckPasscode(userSignupRequest);
        } catch (Exception ex) {
            log.error("Error in CheckPasscode : ", ex);
            response = new registerResponse();
            response.ResponseMessage.setErrorMsg(ExceptionHandling.GetFullExceptionDetails(ex));
        }
        return response;
    }

    @PostMapping("/GenerateOtp")
    public ResponseEntity<Map<String, String>> GenerateOtp(@RequestBody GetOTPRequest getOTPRequest) {
        try {
            return stationService.GenerateOtp(getOTPRequest, uploadDir);
        } catch (Exception e) {
            log.error("Error in GetOTP : ", e);
        }
        return ResponseEntity.badRequest().body(Map.of("message", "try again some time"));
    }

    @PostMapping("/ValidateOtp")
    public boolean ValidateOtp(@RequestBody GetOTPRequest getOTPRequest) {
        try {
            return stationService.ValidateOtp(getOTPRequest, uploadDir, getOTPRequest.attempt);
        } catch (Exception e) {
            log.error("Error in ValidateOtp : ", e);
        }
        return false;
    }

    @PostMapping("/DeleteOtp")
    public boolean DeleteOtp(@RequestBody GetOTPRequest getOTPRequest) {
        try {
            return stationService.DeleteOtpFile(getOTPRequest, uploadDir);
        } catch (Exception e) {
            log.error("Error in DeleteOtp : ", e);
        }
        return false;
    }

    @PostMapping("/getAllStations")
    public List<StationMapResponse> getMapStations(@RequestBody StationRequest request) {
        return stationService.getStations(request);
    }

    @PostMapping("/charger-details")
    public ResponseEntity<List<StationMapResponse.ChargerDetailsDTO>> getStationChargers(@RequestBody StationRequest request) {
        return ResponseEntity.ok(stationService.getStationChargers(request.stationId));
    }

    @PostMapping("/suggest")
    public List<StationMapResponse> SuggestStations(@RequestBody SearchStationRequest searchStationRequest) {

        try {
            return stationService.SuggestByStationName(searchStationRequest);
        } catch (Exception ex) {
            log.error("Error in SuggestStations : ", ex);
        }
        return new ArrayList<StationMapResponse>();
    }
}

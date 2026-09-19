package Farme_rich.Revolt.Adaptor;

import Farme_rich.Revolt.DTO.Response.MessageResponse;
import Farme_rich.Revolt.DTO.Response.StationMapResponse;
import Farme_rich.Revolt.DTO.Response.deviceIdResponse;
import Farme_rich.Revolt.DTO.Response.registerResponse;
import Farme_rich.Revolt.Model.Charger;
import Farme_rich.Revolt.Model.RevoltUser;
import Farme_rich.Revolt.Model.Stations;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StationAdaptor {


    public List<StationMapResponse.ChargerDetailsDTO> FromchargerDetailsTochargerDetailsDTO(List<Charger.ChargerDetails> chargerDetailsList) {
        List<StationMapResponse.ChargerDetailsDTO> ans = new ArrayList<StationMapResponse.ChargerDetailsDTO>();
        for (Charger.ChargerDetails i : chargerDetailsList) {
            ans.add(new StationMapResponse.ChargerDetailsDTO(i.getCharger_type(), i.getMax_power(), i.getMax_voltage(), i.getMax_current(), i.getStatus()));
        }
        return ans;
    }

    public List<StationMapResponse> getAllStationMapper(List<Stations> stations) {

        List<StationMapResponse> response = new ArrayList<>();

        for (Stations station : stations) {

            StationMapResponse dto = new StationMapResponse();

            dto.stationId = (station.get_id().toHexString());
            dto.stationName = (station.getStation_Name());
            dto.stationAddress = station.getStation_Address();

            dto.latitude = station.getLocationPoints().getLatitude();

            dto.longitude = station.getLocationPoints().getLongitude();

//            List<Charger> chargers = chargerRepo.findByStationId(station.get_id());
//
//            List<Charger.ChargerDetails> chargerList = chargers.stream()
//                    .map(Charger::getChargerDetails)
//                    .toList();
//
//            List<StationMapResponse.ChargerDetailsDTO> chargerDetailsDTO = FromchargerDetailsTochargerDetailsDTO(chargerList);
//            dto.chargers = new ArrayList<>();
//            dto.chargers.addAll(chargerDetailsDTO);
            response.add(dto);
        }
        return response;
    }


    public deviceIdResponse TodeviceIdResponseDTO(RevoltUser revoltUser, MessageResponse msg, String token) {
        deviceIdResponse response = new deviceIdResponse();
        if (revoltUser != null) {
            response.id = revoltUser.get_id();
            response.firstName = revoltUser.getFirstName();
            response.lastName = revoltUser.getLastName();
            response.pin = revoltUser.getPin();
            response.deviceId = revoltUser.getDeviceId();
            response.mobileNum = revoltUser.getMobileNum();
            response.token = token;
            response.status = revoltUser.isStatus();
        }
        response.ResponseMessage = msg;
        return response;
    }

    public registerResponse ToregisterResponseDTO(RevoltUser revoltUser, MessageResponse msg, String token) {
        registerResponse response = new registerResponse();
        if (revoltUser != null) {
            response.id = revoltUser.get_id();
            response.firstName = revoltUser.getFirstName();
            response.lastName = revoltUser.getLastName();
            response.pin = revoltUser.getPin();
            response.deviceId = revoltUser.getDeviceId();
            response.mobileNum = revoltUser.getMobileNum();
            response.token = token;
            response.status = revoltUser.isStatus();
        }
        response.ResponseMessage = msg;
        return response;
    }

}

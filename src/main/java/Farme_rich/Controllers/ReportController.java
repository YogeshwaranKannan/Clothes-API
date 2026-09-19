package Farme_rich.Controllers;


import Farme_rich.Seller.BL.ReportService;
import Farme_rich.Seller.DTO.Response.OrdersSummaryResponse;
import Farme_rich.Seller.Model.BackEnd.Save;
import Farme_rich.Seller.Model.ReportsModel.ReportData;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/seller/reports")
public class ReportController {

    private static final SimpleDateFormat PARAM_FMT = new SimpleDateFormat("yyyy-MM-dd");
    @Autowired
    private ReportService reportService;

    @GetMapping("/getAllOrders")
    public ResponseEntity<List<Save>> getAllOrders(@RequestParam String companyId, @RequestParam String lastdate) throws ParseException {
        Date fromDate = PARAM_FMT.parse(lastdate);
        List<Save> orders = reportService.getAllOrders(new ObjectId(companyId), fromDate);
        return ResponseEntity.ok(orders);
    }

    // Makes the "to" date inclusive of the whole day (23:59:59), same helper
    // used by /orders-summary below, so a range of e.g. today-to-today
    // actually includes today's orders instead of cutting off at midnight.
    private Date endOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTime();
    }

    @GetMapping("/data")
    public ResponseEntity<ReportData> getReportData(
            @RequestParam String companyId,
            @RequestParam String lastdate,
            // toDate is optional so any existing caller that only sends
            // lastdate keeps working exactly as before (defaults to "now").
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false, defaultValue = "Your Store") String companyName) throws ParseException {

        Date fromDate = PARAM_FMT.parse(lastdate);
        Date to = (toDate != null && !toDate.trim().isEmpty())
                ? endOfDay(PARAM_FMT.parse(toDate))
                : new Date();

        ReportData reportData = reportService.buildReport(new ObjectId(companyId), fromDate, to, companyName);
        return ResponseEntity.ok(reportData);
    }

    @GetMapping("/orders-summary")
    public ResponseEntity<OrdersSummaryResponse> getOrdersSummary(
            @RequestParam String companyId,
            @RequestParam String fromDate,
            @RequestParam String toDate) throws ParseException {

        Date from = PARAM_FMT.parse(fromDate);
        Date to = PARAM_FMT.parse(toDate);
        Date toInclusive = endOfDay(to);

        System.out.println("From: " + from + " & To: " + toInclusive);

        OrdersSummaryResponse response = reportService.buildOrdersSummary(new ObjectId(companyId), from, toInclusive);
        return ResponseEntity.ok(response);
    }
}
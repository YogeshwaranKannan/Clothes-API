package Farme_rich.Controllers;

import Farme_rich.Seller.BL.DashboardService;
import Farme_rich.Seller.DTO.Request.*;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seller/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;


    @GetMapping("/products")
    public DashboardProductDTO getProducts(@RequestParam String companyId) {
        return dashboardService.getTopProductsLast3Days(new ObjectId(companyId));
    }


    @GetMapping("/sales-trend")
    public List<SalesTrendDTO> getSalesTrend(@RequestParam String companyId) {
        return dashboardService.getSalesTrendLast5Days(new ObjectId(companyId));
    }

    @GetMapping("/invoices")
    public List<InvoiceDTO> getInvoices(@RequestParam String companyId) {
        return dashboardService.getInvoicesLast3Days(new ObjectId(companyId));
    }

    @GetMapping("/stock-summary")
    public StockSummaryDTO getStockSummary(@RequestParam String companyId) {
        return dashboardService.getStockSummary(companyId);
    }

    @GetMapping("/stock-products")
    public List<StockProductDTO> getStockProducts(@RequestParam String companyId,
                                                  @RequestParam String category) {
        return dashboardService.getStockProductsByCategory(companyId, category);
    }
}

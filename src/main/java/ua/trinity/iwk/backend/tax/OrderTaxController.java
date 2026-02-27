package ua.trinity.iwk.backend.tax;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@RestController
public class OrderTaxController {
    private TaxService taxService;

    @Autowired
    public OrderTaxController(TaxService taxService) {
        this.taxService = taxService;
    }

    public record ImportResponse(
            int importedCount,
            int unsupportedCount,
            List<TaxService.UnsupportedOrder> unsupportedOrders,
            String resultCsv
    ) {}

    @PostMapping("/orders/import")
    public ResponseEntity<ImportResponse> importOrders(@RequestParam MultipartFile file) throws IOException {
        TaxService.ImportResult result = taxService.process(file.getInputStream());

        ImportResponse response = new ImportResponse(
                result.importedCount(),
                result.unsupportedOrders().size(),
                result.unsupportedOrders(),
                Base64.getEncoder().encodeToString(result.resultCsv())
        );

        return ResponseEntity.ok(response);
    }
}

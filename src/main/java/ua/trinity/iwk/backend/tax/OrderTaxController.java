package ua.trinity.iwk.backend.tax;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class OrderTaxController {
    private TaxService taxService;

    @Autowired
    public OrderTaxController(TaxService taxService) {
        this.taxService = taxService;
    }

    @PostMapping("/orders/import")
    public ResponseEntity<byte[]> importOrders(@RequestParam MultipartFile file) throws IOException {
        byte[] result = taxService.process(file.getInputStream());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=result.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(result);
    }
}

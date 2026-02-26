package ua.trinity.iwk.backend.tax.jurisdictions;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ua.trinity.iwk.backend.tax.TaxImportService;

@RestController
@RequestMapping("/api/jurisdictions")
@RequiredArgsConstructor
public class JurisdictionImportController {

    private final TaxImportService importService;

    @PostMapping("/import")
    public ResponseEntity<String> uploadCsv(@RequestParam("file") MultipartFile file) {
        // 1. Validate the file isn't empty
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please upload a valid CSV file.");
        }

        // 2. Pass the file to your service
        try {
            importService.importFromCsv(file);
            return ResponseEntity.ok("Successfully imported jurisdictions from CSV.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process the CSV file: " + e.getMessage());
        }
    }
}
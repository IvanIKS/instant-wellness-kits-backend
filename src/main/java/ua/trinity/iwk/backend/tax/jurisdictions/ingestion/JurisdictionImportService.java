package ua.trinity.iwk.backend.tax.jurisdictions.ingestion;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ua.trinity.iwk.backend.tax.jurisdictions.JurisdictionRepository;
import ua.trinity.iwk.backend.tax.jurisdictions.entity.Breakdown;
import ua.trinity.iwk.backend.tax.jurisdictions.entity.Jurisdiction;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JurisdictionImportService {
    private final JurisdictionRepository repository;

    public void importFromCsv(MultipartFile file) {
        List<Jurisdiction> jurisdictions = new ArrayList<>();

        try (BufferedReader br =
                     new BufferedReader(
                             new InputStreamReader(
                                     file.getInputStream(),
                                     StandardCharsets.UTF_8))) {
            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                if (values.length < 3) continue;

                String countyName = values[0];
                BigDecimal stateRate = parsePercent(values[1]);
                BigDecimal countyRate = parsePercent(values[2]);

                Breakdown breakdown = new Breakdown(
                        stateRate,
                        countyRate,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                );

                Jurisdiction jurisdiction = new Jurisdiction();
                jurisdiction.setName(countyName);
                jurisdiction.setCode(countyName.toUpperCase().replace(" ", "_"));
                jurisdiction.setBreakdown(breakdown);

                jurisdictions.add(jurisdiction);
            }

            repository.saveAll(jurisdictions);
            System.out.println("Imported " + jurisdictions.size() + " jurisdictions to MongoDB.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BigDecimal parsePercent(String percentStr) {
        String clean = percentStr.replace("%", "").trim();
        return new BigDecimal(clean).divide(BigDecimal.valueOf(100));
    }
}


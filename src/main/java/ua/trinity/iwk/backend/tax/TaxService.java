package ua.trinity.iwk.backend.tax;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import ua.trinity.iwk.backend.tax.jurisdictions.JurisdictionNotFoundException;
import ua.trinity.iwk.backend.tax.order.Order;
import ua.trinity.iwk.backend.tax.order.TaxDetails;
import ua.trinity.iwk.backend.tax.jurisdictions.util.JurisdictionUtil;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TaxService {
    private final JurisdictionUtil jurisdictionUtil;
    private static final int BATCH_SIZE = 1000;
    private final MongoTemplate mongoTemplate;
    private static final Logger log = LoggerFactory.getLogger(TaxService.class);

    public record UnsupportedOrder(long id, double longitude, double latitude, String timestamp, double subtotal, String reason) {}

    public record ImportResult(byte[] resultCsv, List<UnsupportedOrder> unsupportedOrders, int importedCount) {}

    @Autowired
    public TaxService(JurisdictionUtil jurisdictionUtil, MongoTemplate mongoTemplate) {
        this.jurisdictionUtil = jurisdictionUtil;
        this.mongoTemplate = mongoTemplate;
    }

    public void findTax(Order order) throws JurisdictionNotFoundException {
        var taxJurisdiction = jurisdictionUtil
                .getJurisdiction(order.getLatitude(), order.getLongitude());
        var breakdown = taxJurisdiction.getBreakdown();

        var compositeTaxRate = breakdown.getCompositeTaxRate();
        var taxAmount = compositeTaxRate.multiply(
                BigDecimal.valueOf(order.getSubtotal()));
        var total = taxAmount.add(BigDecimal.valueOf(order.getSubtotal()));
        var taxDetails = new TaxDetails(
                compositeTaxRate,
                taxAmount,
                total,
                taxJurisdiction);

        order.setTaxDetails(taxDetails);
    }

    public ImportResult process(InputStream inputStream) throws IOException {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringWriter stringWriter = new StringWriter()
        ) {
            CSVParser parser = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .parse(reader);

            List<Order> batch = new ArrayList<>(BATCH_SIZE);
            List<UnsupportedOrder> unsupported = new ArrayList<>();

            stringWriter.write("id,longitude,latitude,timestamp,subtotal,tax,total\n");

            for (CSVRecord record : parser) {
                Order order = mapRecordToOrder(record);

                try {
                    findTax(order);
                    batch.add(order);

                    stringWriter.write(order.getId() + "," +
                            order.getLongitude() + "," +
                            order.getLatitude() + "," +
                            order.getTimestamp() + "," +
                            order.getSubtotal() + "," +
                            order.getTaxDetails().getTaxAmount() + "," +
                            order.getTotal() + "\n");

                    if (batch.size() >= BATCH_SIZE) {
                        bulkInsert(batch);
                        batch.clear();
                    }
                } catch (JurisdictionNotFoundException e) {
                    log.warn("Jurisdiction not supported for order id={} lat={} lon={}",
                            order.getId(), order.getLatitude(), order.getLongitude());
                    unsupported.add(new UnsupportedOrder(
                            order.getId(),
                            order.getLongitude(),
                            order.getLatitude(),
                            order.getTimestamp(),
                            order.getSubtotal(),
                            e.getMessage()
                    ));
                }
            }

            if (!batch.isEmpty()) {
                bulkInsert(batch);
            }

            int importedCount = (int) (parser.getRecordNumber() - unsupported.size());
            return new ImportResult(stringWriter.toString().getBytes(), unsupported, importedCount);
        }
    }

    private Order mapRecordToOrder(CSVRecord record) {
        Order order = new Order();
        order.setId(Long.parseLong(record.get("id")));
        order.setLongitude(Double.parseDouble(record.get("longitude")));
        order.setLatitude(Double.parseDouble(record.get("latitude")));
        order.setTimestamp(record.get("timestamp"));
        order.setSubtotal(Double.parseDouble(record.get("subtotal")));
        return order;
    }

    private void bulkInsert(List<Order> batch) {
        try {
            BulkOperations bulkOps =
                    mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Order.class);
            bulkOps.insert(new ArrayList<>(batch));
            bulkOps.execute();
            log.info("Inserted batch of {} orders", batch.size());
        } catch (Exception e) {
            log.error("Failed to bulk insert batch of {} orders: {}", batch.size(), e.getMessage(), e);
            throw e;
        }
    }

}
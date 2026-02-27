package ua.trinity.iwk.backend.tax;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ua.trinity.iwk.backend.tax.jurisdictions.JurisdictionNotFoundException;
import ua.trinity.iwk.backend.tax.jurisdictions.entity.Jurisdiction;
import ua.trinity.iwk.backend.tax.order.Order;
import ua.trinity.iwk.backend.tax.order.TaxDetails;
import ua.trinity.iwk.backend.tax.jurisdictions.util.JurisdictionUtil;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TaxService {
    private final JurisdictionUtil jurisdictionUtil;
    private static final int BATCH_SIZE = 1000;
    private final MongoTemplate mongoTemplate;
    private static final Logger log = LoggerFactory.getLogger(TaxService.class);

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

    public @Nullable StreamingResponseBody process(InputStream inputStream) {
        return outputStream -> {

            try (
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))
            ) {

                CSVParser parser = CSVFormat.DEFAULT
                        .withFirstRecordAsHeader()
                        .parse(reader);

                List<Order> batch = new ArrayList<>(BATCH_SIZE);
                BulkOperations bulkOps =
                        mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Order.class);

                writer.write("id,longitude,latitude,timestamp,subtotal,tax,total\n");

                for (CSVRecord record : parser) {

                    Order order = mapRecordToOrder(record);

                    boolean taxFound
                            = tryFindTax(order);

                    if (taxFound) {
                        batch.add(order);

                        writer.write(order.getId() + "," +
                                order.getLongitude() + "," +
                                order.getLatitude() + "," +
                                order.getTimestamp() + "," +
                                order.getSubtotal() + "," +
                                order.getTaxDetails().getTaxAmount() + "," +
                                order.getTotal() + "\n");

                        if (batch.size() >= BATCH_SIZE) {
                            bulkInsert(batch, bulkOps);
                            batch.clear();
                        }
                    }
                }

                if (!batch.isEmpty()) {
                    bulkInsert(batch, bulkOps);
                }

                writer.flush();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Order mapRecordToOrder(CSVRecord record) {

        Order order = new Order();

        order.setId(Long.parseLong(record.get("id")));
        order.setLongitude(Double.parseDouble(record.get("longitude")));
        order.setLatitude(Double.parseDouble(record.get("latitude")));
        order.setTimestamp(
                Long.parseLong((record.get("timestamp"))));
        order.setSubtotal(
                Double.parseDouble(record.get("subtotal")));

        return order;
    }

    private void bulkInsert(List<Order> batch, BulkOperations bulkOps) {
        bulkOps.insert(batch);
        bulkOps.execute();
    }

    private boolean tryFindTax(Order order) {
        try {
            findTax(order);
            return true;
        } catch (JurisdictionNotFoundException e) {
            log.warn("Jurisdiction not supported for order with lat {} lon {}",
                    order.getLatitude(),
                    order.getLongitude());
            return false;
        }
    }

}
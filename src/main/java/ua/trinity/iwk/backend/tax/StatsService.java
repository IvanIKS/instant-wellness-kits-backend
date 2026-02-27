package ua.trinity.iwk.backend.tax;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.stereotype.Service;
import ua.trinity.iwk.backend.tax.order.Order;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class StatsService {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public StatsService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public record OrderStats(long totalOrders, BigDecimal totalRevenue, BigDecimal totalTax) {}

    public OrderStats getStats() {
        long totalOrders = mongoTemplate.getCollection("orders").countDocuments();

        GroupOperation group = Aggregation.group()
                .sum("subtotal").as("totalRevenue")
                .sum("taxDetails.taxAmount").as("totalTax");

        Aggregation aggregation = Aggregation.newAggregation(group);

        AggregationResults<Map> results =
                mongoTemplate.aggregate(aggregation, "orders", Map.class);

        Map result = results.getUniqueMappedResult();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        if (result != null) {
            Object rev = result.get("totalRevenue");
            Object tax = result.get("totalTax");
            if (rev != null) totalRevenue = new BigDecimal(rev.toString());
            if (tax != null) totalTax = new BigDecimal(tax.toString());
        }

        return new OrderStats(totalOrders, totalRevenue, totalTax);
    }
}


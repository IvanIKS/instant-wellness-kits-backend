package ua.trinity.instantwellnesskits.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import ua.trinity.instantwellnesskits.backend.entity.tax.TaxDetails;

@AllArgsConstructor
@Getter
@Setter
@Document("orders")
public class Order {
    @Id
    private long id;
    private double longitude;
    private double latitude;
    private double subtotal;
    private String wellnessType;
    /**
     * Unix timestamp (milliseconds)
     */
    private long timestamp;

    private TaxDetails taxDetails;
}

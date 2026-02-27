package ua.trinity.iwk.backend.tax.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
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
     * Timestamp from the CSV (e.g. "2025-11-04 10:17:04.915257248")
     */
    private String timestamp;

    private TaxDetails taxDetails;

    public BigDecimal getTotal(){
        return taxDetails.getTaxAmount().add(BigDecimal.valueOf(subtotal));
    }

}

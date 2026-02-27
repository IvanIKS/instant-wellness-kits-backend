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
     * Unix timestamp (milliseconds)
     */
    private long timestamp;

    private TaxDetails taxDetails;

    public BigDecimal getTotal(){
        return taxDetails.getTaxAmount().add(BigDecimal.valueOf(subtotal));
    }

}

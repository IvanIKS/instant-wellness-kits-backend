package ua.trinity.instantwellnesskits.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@Getter
@Setter
@Document("orders")
public class Order {
    @Id
    private int id;
    private double longitude;
    private double latitude;
    private double subtotal;
    /**
     * Unix timestamp (milliseconds)
     */
    private long timestamp;
}

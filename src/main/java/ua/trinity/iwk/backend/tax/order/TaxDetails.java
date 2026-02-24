package ua.trinity.iwk.backend.tax.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.trinity.iwk.backend.tax.jurisdictions.entity.Jurisdiction;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaxDetails {

    private BigDecimal compositeTaxRate;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;

    private Jurisdiction jurisdiction;
}
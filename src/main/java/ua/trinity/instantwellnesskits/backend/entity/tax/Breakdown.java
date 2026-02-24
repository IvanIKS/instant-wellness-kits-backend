package ua.trinity.instantwellnesskits.backend.entity.tax;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Breakdown {

    private BigDecimal stateRate;
    private BigDecimal countyRate;
    private BigDecimal cityRate;
    private BigDecimal specialRate;

    public BigDecimal getCompositeTaxRate() {
        return stateRate
                .add(countyRate)
                .add(cityRate)
                .add(specialRate);
    }
}
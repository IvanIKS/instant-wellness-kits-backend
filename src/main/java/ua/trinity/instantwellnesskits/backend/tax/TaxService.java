package ua.trinity.instantwellnesskits.backend.tax;

import org.springframework.stereotype.Service;
import ua.trinity.instantwellnesskits.backend.entity.Order;
import ua.trinity.instantwellnesskits.backend.entity.tax.TaxDetails;

import java.math.BigDecimal;

//@Service
public class TaxService {
    private final JurisdictionUtil jurisdictionUtil;

    //@Autowired
    public TaxService(JurisdictionUtil jurisdictionUtil) {
        this.jurisdictionUtil = jurisdictionUtil;
    }

    public Order calculateTax(Order order) {
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
        return order;
    }

}

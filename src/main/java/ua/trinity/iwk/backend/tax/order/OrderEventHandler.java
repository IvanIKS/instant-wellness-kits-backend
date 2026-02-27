package ua.trinity.iwk.backend.tax.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import ua.trinity.iwk.backend.tax.TaxService;
import ua.trinity.iwk.backend.tax.jurisdictions.JurisdictionNotFoundException;

@Component
@RepositoryEventHandler
public class OrderEventHandler {

    private final TaxService taxService;

    @Autowired
    public OrderEventHandler(TaxService taxService) {
        this.taxService = taxService;
    }

    @HandleBeforeCreate
    public void handleBeforeCreate(Order order) throws JurisdictionNotFoundException {
        taxService.findTax(order);
    }

    @HandleBeforeSave
    public void handleBeforeSave(Order order) throws JurisdictionNotFoundException {
        taxService.findTax(order);
    }
}
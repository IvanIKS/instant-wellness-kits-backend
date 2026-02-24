package ua.trinity.instantwellnesskits.backend.tax;

import ua.trinity.iwk.backend.tax.entity.Jurisdiction;

public interface JurisdictionUtil {

    Jurisdiction getJurisdiction(double latitude, double longitude);
}

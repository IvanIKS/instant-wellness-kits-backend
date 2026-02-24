package ua.trinity.instantwellnesskits.backend.tax;

import ua.trinity.instantwellnesskits.backend.entity.tax.Jurisdiction;

public interface JurisdictionUtil {

    Jurisdiction getJurisdiction(double latitude, double longitude);
}

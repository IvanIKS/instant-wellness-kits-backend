package ua.trinity.iwk.backend.tax.jurisdictions;

import ua.trinity.iwk.backend.tax.jurisdictions.entity.Jurisdiction;

public interface JurisdictionUtil {

    Jurisdiction getJurisdiction(double latitude, double longitude) throws JurisdictionNotFoundException;
}

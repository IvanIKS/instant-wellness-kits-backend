package ua.trinity.iwk.backend.tax.jurisdictions.util;

import ua.trinity.iwk.backend.tax.jurisdictions.JurisdictionNotFoundException;
import ua.trinity.iwk.backend.tax.jurisdictions.entity.Jurisdiction;

public interface JurisdictionUtil {

    Jurisdiction getJurisdiction(double latitude, double longitude) throws JurisdictionNotFoundException;
}

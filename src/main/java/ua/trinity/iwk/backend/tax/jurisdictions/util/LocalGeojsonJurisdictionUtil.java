package ua.trinity.iwk.backend.tax.jurisdictions.util;

import jakarta.annotation.PostConstruct;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ua.trinity.iwk.backend.tax.jurisdictions.JurisdictionNotFoundException;
import ua.trinity.iwk.backend.tax.jurisdictions.JurisdictionRepository;
import ua.trinity.iwk.backend.tax.jurisdictions.entity.Jurisdiction;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class LocalGeojsonJurisdictionUtil implements JurisdictionUtil {

    /**
     * Maps GeoJSON county NAME values to the names used in New_York_Taxes.csv.
     * GeoJSON uses plain county names; the CSV uses "County (Borough)" format for NYC boroughs.
     */
    private static final Map<String, String> GEOJSON_TO_CSV_NAME = Map.of(
            "Kings",     "Kings (Brooklyn)",
            "New York",  "New York (Manhattan)",
            "Richmond",  "Richmond (Staten Island)"
    );

    private final List<Geometry> zones = new ArrayList<>();

    @Value("${geo.db.location}")
    private String databaseLocation;

    private final GeometryFactory factory = new GeometryFactory();
    private JurisdictionRepository jurisdictionRepository;

    @Autowired
    public LocalGeojsonJurisdictionUtil(JurisdictionRepository jurisdictionRepository) {
        this.jurisdictionRepository = jurisdictionRepository;
    }

    @PostConstruct
    private void initializeZones() throws Exception {
        String content = new String(Files.readAllBytes(Paths.get(databaseLocation)));

        JSONParser parser = new JSONParser();
        JSONObject root = (JSONObject) parser.parse(content);

        JSONArray features = (JSONArray) root.get("features");
        GeoJsonReader reader = new GeoJsonReader();

        for (Object obj : features) {
            JSONObject feature = (JSONObject) obj;
            JSONObject geometryJson = (JSONObject) feature.get("geometry");
            JSONObject properties  = (JSONObject) feature.get("properties");
            Geometry geometry = reader.read(geometryJson.toJSONString());
            geometry.setUserData(properties);
            zones.add(geometry);
        }
    }

    @Override
    public Jurisdiction getJurisdiction(double latitude, double longitude) throws JurisdictionNotFoundException {
        JSONObject jurisdictionRaw = getJurisdictionFromZones(latitude, longitude);

        // Fallback: find the nearest county polygon centroid when the point
        // falls in a gap between polygons (e.g. border areas, water bodies).
        if (jurisdictionRaw == null) {
            jurisdictionRaw = getNearestJurisdiction(latitude, longitude);
        }

        if (jurisdictionRaw == null) {
            throw new JurisdictionNotFoundException(
                    String.format("No jurisdiction found for lat=%s, lon=%s", latitude, longitude));
        }

        String geoName = jurisdictionRaw.get("NAME").toString();
        // Translate GeoJSON name → CSV/DB name where they differ
        String dbName = GEOJSON_TO_CSV_NAME.getOrDefault(geoName, geoName);

        Optional<Jurisdiction> taxJurisdiction = jurisdictionRepository.findByName(dbName);
        if (taxJurisdiction.isEmpty()) {
            throw new JurisdictionNotFoundException(
                    String.format("No tax data for jurisdiction '%s' (lat=%s, lon=%s)", dbName, latitude, longitude));
        }
        return taxJurisdiction.get();
    }

    private JSONObject getJurisdictionFromZones(double latitude, double longitude) {
        Point point = factory.createPoint(new Coordinate(longitude, latitude));
        for (Geometry zone : zones) {
            if (zone.contains(point)) {
                return (JSONObject) zone.getUserData();
            }
        }
        return null;
    }

    /**
     * Returns the properties of the geographically nearest county polygon
     * (by distance from its boundary) when the point falls outside all polygons.
     */
    private JSONObject getNearestJurisdiction(double latitude, double longitude) {
        Point point = factory.createPoint(new Coordinate(longitude, latitude));
        Geometry nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Geometry zone : zones) {
            double distance = zone.distance(point);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = zone;
            }
        }

        return nearest != null ? (JSONObject) nearest.getUserData() : null;
    }
}



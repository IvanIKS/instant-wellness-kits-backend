package ua.trinity.iwk.backend.tax.jurisdictions;

import jakarta.annotation.PostConstruct;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ua.trinity.iwk.backend.tax.JurisdictionRepository;
import ua.trinity.iwk.backend.tax.jurisdictions.entity.Jurisdiction;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class LocalGeojsonJurisdictionUtil implements JurisdictionUtil {
    private final List<Geometry> zones = new ArrayList<>();

    @Value("${geo.db.location}")
    private String databaseLocation;

    private final GeometryFactory factory = new GeometryFactory();
    private JurisdictionRepository jurisdictionRepository;

    @Autowired
    public LocalGeojsonJurisdictionUtil(JurisdictionRepository jurisdictionRepository) throws Exception {
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
            JSONObject properties = (JSONObject) feature.get("properties");

            Geometry geometry = reader.read(geometryJson.toJSONString());

            geometry.setUserData(properties);

            zones.add(geometry);
        }
    }

    @Override
    public Jurisdiction getJurisdiction(double latitude, double longitude) throws JurisdictionNotFoundException {
        JSONObject jurisdictionRaw = getJurisdictionFromZones(latitude, longitude);
        if (jurisdictionRaw == null) {
            throw new JurisdictionNotFoundException(String.format("Our program doesn't currently have a jurisdiction %s, %s"));
        }

        String jurisdictionName = jurisdictionRaw.get("NAME").toString();

        Optional<Jurisdiction> taxJurisdiction = jurisdictionRepository.findByName(jurisdictionName);
        if (taxJurisdiction.isEmpty()) {
            throw new JurisdictionNotFoundException(String.format("Our database doesn't currently have a jurisdiction %s, %s"));
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
}

package ua.trinity.iwk.backend.tax;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ua.trinity.iwk.backend.tax.jurisdictions.entity.Jurisdiction;

import java.util.Optional;

@Repository
public interface JurisdictionRepository extends MongoRepository<Jurisdiction, String> {
    Optional<Jurisdiction> findByName(String name);
}

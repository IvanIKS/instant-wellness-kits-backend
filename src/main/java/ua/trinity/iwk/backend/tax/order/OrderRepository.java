package ua.trinity.iwk.backend.tax.order;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource(path = "orders")
public interface OrderRepository extends PagingAndSortingRepository<Order, String>, MongoRepository<Order, String> {
    Optional<Order> findById(String id);

    Optional<Order> findByTaxDetailsJurisdictionName(String jurisdictionName);
}

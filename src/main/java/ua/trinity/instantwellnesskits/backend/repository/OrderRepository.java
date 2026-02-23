package ua.trinity.instantwellnesskits.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ua.trinity.instantwellnesskits.backend.entity.Order;

@RepositoryRestResource(path = "orders")
public interface OrderRepository extends PagingAndSortingRepository<Order, Long>, MongoRepository<Order, Long> {
}

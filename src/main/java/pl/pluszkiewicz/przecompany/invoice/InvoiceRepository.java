package pl.pluszkiewicz.przecompany.invoice;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface InvoiceRepository extends ReactiveMongoRepository<Invoice, String> {
}

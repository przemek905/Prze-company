package pl.pluszkiewicz.przecompany.invoice;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

public interface InvoiceRepository extends ReactiveMongoRepository<Invoice, String> {
    Flux<Invoice> findInvoicesBySettlementDateBetween(LocalDate from, LocalDate to);
}

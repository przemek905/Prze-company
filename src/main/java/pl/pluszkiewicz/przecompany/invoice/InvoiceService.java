package pl.pluszkiewicz.przecompany.invoice;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.naming.directory.InvalidAttributeIdentifierException;
import java.time.LocalDate;
import java.time.YearMonth;

@Service
public class InvoiceService {
    public static final int FIRST_DAY_OF_MONTH = 1;
    private final InvoiceRepository invoiceRepository;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public Flux<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public Mono<Invoice> getInvoiceById(String id) {
        return invoiceRepository.findById(id)
                .doOnError(e -> {throw new ResourceNotFoundException("No invoice with given id.");});
    }

    public Flux<Invoice> getInvoicesBySettlementDateBetween(LocalDate from, LocalDate to) {
        return invoiceRepository.findInvoicesBySettlementDateBetween(from, to);
    }

    public Flux<Invoice> getInvoiceForMonthAndYear(int month, int year) {
        LocalDate lastDayOfMonth = YearMonth.of(year,month).atEndOfMonth();

        return invoiceRepository.findInvoicesBySettlementDateBetween(LocalDate.of(year, month, FIRST_DAY_OF_MONTH), lastDayOfMonth);
    }

    public Mono<Invoice> addInvoice(Invoice invoice) {
        return invoiceRepository.save(invoice);
    }

    public Mono<Invoice> editInvoice(String id, Invoice invoice) throws InvalidAttributeIdentifierException {
        if (!id.equalsIgnoreCase(invoice.getId())) {
            throw new InvalidAttributeIdentifierException("Id of request and given object is not equal.");
        }
        return invoiceRepository
                .findById(id)
                .map(p -> new Invoice(invoice))
                .flatMap(this.invoiceRepository::save)
                .onErrorResume(e -> Mono.error(new ResourceNotFoundException("Error on invoice update", e)));
    }

    public Mono<Invoice> removeInvoice(String id) {
        return this.invoiceRepository
                .findById(id)
                .flatMap(invoice -> this.invoiceRepository.deleteById(invoice.getId()).thenReturn(invoice))
                .onErrorResume(e -> Mono.error(new ResourceNotFoundException("Cannot delete invoice", e)));
    }
}

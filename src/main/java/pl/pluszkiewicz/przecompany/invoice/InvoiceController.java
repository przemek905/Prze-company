package pl.pluszkiewicz.przecompany.invoice;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.naming.directory.InvalidAttributeIdentifierException;
import java.time.LocalDate;

import static java.util.Objects.nonNull;

@RestController
@RequestMapping(value = "invoices")
public class InvoiceController {
    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public Flux<Invoice> getInvoices(@RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "month", required = false) Integer month, @RequestParam(value = "year", required = false) Integer year) {
        if (nonNull(from) && nonNull(to)) {
            return invoiceService.getInvoicesBySettlementDateBetween(from, to);
        }

        if (nonNull(year) && nonNull(month)) {
            return invoiceService.getInvoiceForMonthAndYear(month, year);
        }
        return invoiceService.getAllInvoices();
    }

    @GetMapping("/{id}")
    public Mono<Invoice> getInvoiceById(@PathVariable(value = "id") String id) {
        return invoiceService.getInvoiceById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Invoice> addInvoice(@RequestBody Invoice invoice) {
        return invoiceService.addInvoice(invoice);
    }

    @PutMapping("/{id}")
    public Mono<Invoice> editInvoice(@PathVariable(value = "id") String id, @RequestBody Invoice invoice) throws InvalidAttributeIdentifierException {
        return invoiceService.editInvoice(id, invoice);

    }

    @DeleteMapping("/{id}")
    public Mono<Invoice> deleteInvoice(@PathVariable(value = "id") String id) {
        return invoiceService.removeInvoice(id);
    }
}

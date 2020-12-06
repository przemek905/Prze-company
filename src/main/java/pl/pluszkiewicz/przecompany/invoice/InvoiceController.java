package pl.pluszkiewicz.przecompany.invoice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class InvoiceController {
    private final InvoiceRepository invoiceRepository;

    public InvoiceController(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @GetMapping("/invoices")
    public Flux<Invoice> getInvoices() {
        return invoiceRepository.findAll();
    }

    @GetMapping("/add")
    public void add() {
        invoiceRepository.save(new Invoice("qqq", "moja")).subscribe();
    }
}

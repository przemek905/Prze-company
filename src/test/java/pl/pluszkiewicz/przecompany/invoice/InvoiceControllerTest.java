package pl.pluszkiewicz.przecompany.invoice;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class InvoiceControllerTest {
    public static final String INVOICES_URL = "/invoices";

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @BeforeEach
    public void cleanUp() {
        invoiceRepository.deleteAll().subscribe();
    }

    @Test
    public void shouldGetInvoiceById() {
        //when
        Invoice invoice1 = Invoice.builder().title("INVOICE_1").id("1").settlementDate(LocalDate.of(2020, 12, 13)).build();
        Invoice invoice2 = Invoice.builder().title("INVOICE_2").id("2").settlementDate(LocalDate.of(2020, 12, 13)).build();
        invoiceRepository.saveAll(Lists.newArrayList(invoice1, invoice2)).subscribe();

        //then
        webClient.get().uri(INVOICES_URL + "/1").exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.title").isEqualTo("INVOICE_1");

        webClient.get().uri(INVOICES_URL + "/2").exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.title").isEqualTo("INVOICE_2");

        webClient.get().uri(INVOICES_URL + "/3").exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();
    }

    @Test
    public void shouldSucceedOnCrudOperations() {
        //given
        Invoice newInvoice = Invoice.builder().title("NEW_INVOICE").settlementDate(LocalDate.of(2020, 12, 13)).build();
        webClient.get().uri(INVOICES_URL).exchange().expectStatus().isOk()
                .expectBodyList(Invoice.class).hasSize(0);

        //when add
        webClient.post().uri(INVOICES_URL).body(BodyInserters.fromPublisher(Mono.just(newInvoice), Invoice.class))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().jsonPath("$.title").isEqualTo("NEW_INVOICE");

        //then
        Flux<Invoice> addedInvoice = webClient.get().uri(INVOICES_URL).exchange()
                .expectStatus().isOk()
                .returnResult(Invoice.class).getResponseBody();

        webClient.get().uri(INVOICES_URL).exchange()
                .expectBodyList(Invoice.class).hasSize(1);

        String id = addedInvoice.blockFirst().getId();
        newInvoice.setId(id);

        //when edit
        newInvoice.setTitle("Edited Invoice");
        webClient.put().uri(INVOICES_URL + "/" + id).body(BodyInserters.fromPublisher(Mono.just(newInvoice), Invoice.class))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Edited Invoice");

        //then
        webClient.get().uri(INVOICES_URL).exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].title").isEqualTo("Edited Invoice")
                .jsonPath("$[0].settlementDate").isEqualTo("2020-12-13");

        webClient.get().uri(INVOICES_URL).exchange()
                .expectBodyList(Invoice.class).hasSize(1).contains(newInvoice);

        //when delete
        webClient.delete().uri(INVOICES_URL + "/" + id).exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Edited Invoice")
                .jsonPath("$.settlementDate").isEqualTo("2020-12-13");

        //then
        webClient.get().uri(INVOICES_URL).exchange()
                .expectStatus().isOk()
                .expectBodyList(Invoice.class).hasSize(0);
    }

    @Test
    public void shouldGetInvoicesBySettlementDates() {
        //given
        webClient.get().uri(uriBuilder -> uriBuilder.path(INVOICES_URL)
                .queryParam("from","2020-01-01")
                .queryParam("to", "2020-01-31").build())
                .exchange().expectStatus().isOk()
                .expectBodyList(Invoice.class).hasSize(0);

        Invoice januaryInvoice = Invoice.builder().id("JAN").title("JANUARY_INVOICE").settlementDate(LocalDate.of(2020, 1, 24)).build();
        Invoice aprilInvoice = Invoice.builder().id("APR").title("APRIL_INVOICE").settlementDate(LocalDate.of(2020, 4, 13)).build();
        Invoice maiInvoice = Invoice.builder().id("MAI").title("MAI_INVOICE").settlementDate(LocalDate.of(2020, 5, 2)).build();

        //when
        invoiceRepository.saveAll(Lists.newArrayList(januaryInvoice, aprilInvoice, maiInvoice)).subscribe();

        //then
        webClient.get().uri(uriBuilder -> uriBuilder.path(INVOICES_URL)
                .queryParam("from","2020-01-01")
                .queryParam("to", "2020-01-31").build())
                .exchange().expectStatus().isOk()
                .expectBodyList(Invoice.class).hasSize(1).contains(januaryInvoice);

        webClient.get().uri(uriBuilder -> uriBuilder.path(INVOICES_URL)
                .queryParam("from","2020-04-01")
                .queryParam("to", "2020-04-30").build())
                .exchange().expectStatus().isOk()
                .expectBodyList(Invoice.class).hasSize(1).contains(aprilInvoice);

        webClient.get().uri(uriBuilder -> uriBuilder.path(INVOICES_URL)
                .queryParam("from","2020-05-01")
                .queryParam("to", "2020-05-30").build())
                .exchange().expectStatus().isOk()
                .expectBodyList(Invoice.class).hasSize(1).contains(maiInvoice);
    }

    @Test
    public void shouldGetInvoicesBySettlementDatesWithMonthAndYear() {
        //given
        webClient.get().uri(INVOICES_URL)
                .exchange().expectStatus().isOk()
                .expectBodyList(Invoice.class).hasSize(0);

        Invoice januaryInvoice = Invoice.builder().id("JAN").title("JANUARY_INVOICE").settlementDate(LocalDate.of(2020, 1, 24)).build();
        Invoice aprilInvoice = Invoice.builder().id("APR").title("APRIL_INVOICE").settlementDate(LocalDate.of(2020, 4, 13)).build();
        Invoice maiInvoice = Invoice.builder().id("MAI").title("MAI_INVOICE").settlementDate(LocalDate.of(2020, 5, 2)).build();

        //when
        invoiceRepository.saveAll(Lists.newArrayList(januaryInvoice, aprilInvoice, maiInvoice)).subscribe();

        //then
        webClient.get().uri(uriBuilder -> uriBuilder.path(INVOICES_URL)
                .queryParam("month","1")
                .queryParam("year", "2020").build())
                .exchange().expectStatus().isOk()
                .expectBodyList(Invoice.class).hasSize(1).contains(januaryInvoice);

        webClient.get().uri(uriBuilder -> uriBuilder.path(INVOICES_URL)
                .queryParam("month","4")
                .queryParam("year", "2020").build())
                .exchange().expectStatus().isOk()
                .expectBodyList(Invoice.class).hasSize(1).contains(aprilInvoice);

        webClient.get().uri(uriBuilder -> uriBuilder.path(INVOICES_URL)
                .queryParam("month","5")
                .queryParam("year", "2020").build())
                .exchange().expectStatus().isOk()
                .expectBodyList(Invoice.class).hasSize(1).contains(maiInvoice);

        webClient.get().uri(uriBuilder -> uriBuilder.path(INVOICES_URL)
                .queryParam("month","5")
                .queryParam("year", "2019").build())
                .exchange().expectStatus().isOk()
                .expectBodyList(Invoice.class).hasSize(0);
    }

}

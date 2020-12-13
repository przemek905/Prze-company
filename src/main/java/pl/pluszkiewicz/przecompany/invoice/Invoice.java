package pl.pluszkiewicz.przecompany.invoice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import pl.pluszkiewicz.przecompany.company.Company;

import java.math.BigDecimal;
import java.sql.Blob;
import java.time.LocalDate;
import java.util.List;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Invoice {
    @Id
    private String id;
    private String title;
    private LocalDate settlementDate;
    private BigDecimal amountNetto;
    private BigDecimal amountBrutto;
    private BigDecimal vat;
    private Blob image;
    private Company seller;
    private Company buyer;
    private List<Position> positions;

    public Invoice(Invoice invoice) {
        this(invoice.getId(), invoice.getTitle(), invoice.getSettlementDate(), invoice.getAmountNetto(),
                invoice.getAmountBrutto(), invoice.getVat(), invoice.getImage(), invoice.getSeller(), invoice.getBuyer(),
                invoice.getPositions());
    }
}

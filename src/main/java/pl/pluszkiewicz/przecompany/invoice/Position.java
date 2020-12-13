package pl.pluszkiewicz.przecompany.invoice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Position {
    @Id
    private String id;
    private String name;
    private Integer amount;
    private String unitType;
    private BigDecimal unitPriceNetto;
    private BigDecimal totalAmountNetto;
    private BigDecimal totalAmountBrutto;
    private BigDecimal totalVat;
    private Integer vatPercent;
    private String gtuCode;

}

package pl.pluszkiewicz.przecompany.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address {
    @Id
    private String id;
    private String street;
    private String houseNumber;
    private String apartmentNumber;
    private String zipCode;
    private String city;
}

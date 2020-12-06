package pl.pluszkiewicz.przecompany;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.pluszkiewicz.przecompany.pdf.PdfReadService;

@SpringBootTest
class PrzeCompanyApplicationTests {

    @Autowired
    PdfReadService pdfReadService;

    @Test
    void contextLoads() {
    }

}

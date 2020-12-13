package pl.pluszkiewicz.przecompany.pdf;

import org.apache.commons.compress.utils.Lists;
import org.apache.logging.log4j.util.Strings;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.springframework.stereotype.Service;
import pl.pluszkiewicz.przecompany.company.Address;
import pl.pluszkiewicz.przecompany.company.Company;
import pl.pluszkiewicz.przecompany.invoice.Invoice;
import pl.pluszkiewicz.przecompany.invoice.Position;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@Service
public class PdfReadService {
    public static final String SETTLEMENT_DATE = "Data wystawienia";
    public static final String SUMMARY = "Razem";
    public static final String PDF_WHITE_SPACE = "\u00A0";
    public static final String POSITIONS_SECTION = "Lp Nazwa";
    public static final String SELLER_SECTION = "Sprzedawca";
    public static final String NIP_PREFIX = "NIP: ";
    public static final String INVOICE_TITLE_SECTION = "Faktura: ";

    public Invoice mapToInvoice(String[] pdfText) throws ParseException {
        Invoice invoice = new Invoice();
        mapTitle(pdfText, invoice);
        mapAmounts(pdfText, invoice);
        mapSettlementDate(pdfText, invoice);
        mapCompanies(pdfText, invoice);
        mapPositions(pdfText, invoice);

        return invoice;
    }

    private void mapTitle(String[] pdfText, Invoice invoice) {
        String invoiceTitle = Arrays.stream(pdfText).filter(line -> line.contains(INVOICE_TITLE_SECTION)).findFirst().orElse(Strings.EMPTY);
        invoice.setTitle(invoiceTitle.substring(INVOICE_TITLE_SECTION.length()));
    }

    public String[] readPdf(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            if (!document.isEncrypted()) {
                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                stripper.setSortByPosition(true);
                PDFTextStripper tStripper = new PDFTextStripper();
                String pdfFileInText = tStripper.getText(document);
                return pdfFileInText.split("\\r?\\n");
            }
        }
        return new String[0];
    }

    private void mapPositions(String[] pdfText, Invoice invoice) throws ParseException {
        OptionalInt positionsStart = IntStream.range(0, pdfText.length).filter(index -> pdfText[index].contains(POSITIONS_SECTION)).findFirst();
        if (positionsStart.isPresent()) {
            int index = positionsStart.getAsInt() + 5;
            List<Position> positions = Lists.newArrayList();
            while (!trimPdfString(pdfText[index]).isEmpty()) {
                positions.add(createPosition(pdfText[index]));
                index++;
            }
            invoice.setPositions(positions);
        }
    }

    private Position createPosition(String positionLine) throws ParseException {
        String positionRegex = "(\\d+)(\\s)(\\D+)(\\s)(\\d+,{1}\\d+)(\\s)(\\w+.)(\\s)(\\d+,{1}\\d+)(\\s)(\\d{1,2})(\\s)(%)(\\s*)(\\w*)(\\s)([0-9 ]+,\\d{2})(\\s)([0-9 ]+,\\d{2})(\\s)([0-9 ]+,\\d{2})";
        Matcher matcher = compilePattern(trimPdfString(positionLine), positionRegex);
        if (matcher.matches()) {
            return Position.builder()
                    .name(matcher.group(3))
                    .amount((int) parseToDouble(matcher.group(5)))
                    .unitType(matcher.group(7))
                    .unitPriceNetto(BigDecimal.valueOf(parseToDouble(matcher.group(9))))
                    .vatPercent(Integer.parseInt(matcher.group(11)))
                    .gtuCode(matcher.group(15))
                    .totalAmountNetto(BigDecimal.valueOf(parseToDouble(matcher.group(17))))
                    .totalVat(BigDecimal.valueOf(parseToDouble(matcher.group(19))))
                    .totalAmountBrutto(BigDecimal.valueOf(parseToDouble(matcher.group(21))))
                    .build();
        }

        return null;
    }

    private void mapCompanies(String[] pdfText, Invoice invoice) {
        int sellerStart = 0;
        int buyerStart = 0;
        int sectionEnd = 0;

        for (int i = 0; i < pdfText.length; i++) {
            if (pdfText[i].contains(SELLER_SECTION)) {
                sellerStart = i + 1;
            }

            if (trimPdfString(pdfText[i]).isEmpty() && sellerStart != 0) {
                if (buyerStart == 0) {
                    buyerStart = i;
                } else if (sectionEnd == 0) {
                    sectionEnd = i - 1;
                }
            }
        }

        Company seller = getCompanyData(pdfText, sellerStart);
        Company buyer = getCompanyData(pdfText, buyerStart + 1);

        invoice.setSeller(seller);
        invoice.setBuyer(buyer);
    }

    private Company getCompanyData(String[] pdfText, int startReadIndex) {
        String[] city = pdfText[startReadIndex + 2].split(" ");
        Address sellerAddress = Address.builder().zipCode(city[0]).city(city[1]).build();

        String street = pdfText[startReadIndex + 1];
        String streetRegex = "(^\\S*)(\\s)(\\S*)(\\s*)(\\w+)(\\/{0,})(\\w{0,})";
        Matcher matcher = compilePattern(street, streetRegex);
        if (matcher.matches()) {
            sellerAddress.setStreet(matcher.group(1) + matcher.group(2) + matcher.group(3));
            sellerAddress.setHouseNumber(matcher.group(5));
            sellerAddress.setApartmentNumber(matcher.group(7));
        }

        return Company.builder().name(pdfText[startReadIndex]).country(pdfText[startReadIndex + 3]).nip(getNip(pdfText[startReadIndex + 4]))
                .address(sellerAddress).build();
    }

    private Long getNip(String nipString) {
        return Long.parseLong(nipString.replaceAll(NIP_PREFIX, ""));
    }

    private void mapSettlementDate(String[] pdfText, Invoice invoice) {
        String settlementDateLine = Arrays.stream(pdfText).filter(text -> text.contains(SETTLEMENT_DATE)).findFirst().orElse(Strings.EMPTY);
        String settlementDatePattern = "([" + SETTLEMENT_DATE + ":]+\\s)(\\d+)(.{1})(\\d+)(.{1})(\\d+)([^\\\\W]+)";
        Matcher matcher = compilePattern(settlementDateLine, settlementDatePattern);
        if (matcher.matches()) {
            invoice.setSettlementDate(LocalDate.of(Integer.parseInt(matcher.group(6)), Integer.parseInt(matcher.group(4)),
                    Integer.parseInt(matcher.group(2))));
        }
    }

    private void mapAmounts(String[] pdfText, Invoice invoice) throws ParseException {
        String totalAmountsLine = Arrays.stream(pdfText).filter(text -> text.contains(SUMMARY)).findFirst().orElse(Strings.EMPTY);
        String amountsPattern = "([" + SUMMARY + ":]+\\s)(\\d+[^\\s]?\\d+,\\d{2})(\\s{1})(\\d+[^\\s]?\\d+,\\d{2})(\\s{1})(\\d+[^\\s]?\\d+,\\d{2})";
        Matcher matcher = compilePattern(totalAmountsLine, amountsPattern);
        if (matcher.matches()) {
            invoice.setAmountNetto(BigDecimal.valueOf(parseToDouble(matcher.group(2).trim())));
            invoice.setVat(BigDecimal.valueOf(parseToDouble(matcher.group(4).trim())));
            invoice.setAmountBrutto(BigDecimal.valueOf(parseToDouble(matcher.group(6).trim())));
        }
    }

    private Matcher compilePattern(String line, String regex) {
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(line);
    }

    private double parseToDouble(String number) throws ParseException {
        DecimalFormat df = new DecimalFormat();
        DecimalFormatSymbols sfs = new DecimalFormatSymbols();
        sfs.setDecimalSeparator(',');
        df.setDecimalFormatSymbols(sfs);
        return df.parse(number).doubleValue();
    }

    private String trimPdfString(String input) {
        return input.replaceAll(PDF_WHITE_SPACE, Strings.EMPTY);
    }
}

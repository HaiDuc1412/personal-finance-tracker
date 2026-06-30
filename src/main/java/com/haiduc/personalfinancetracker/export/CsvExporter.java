package com.haiduc.personalfinancetracker.export;

import com.haiduc.personalfinancetracker.transaction.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class CsvExporter {

    private static final String[] HEADERS = {
            "#", "Date", "Type", "Category", "Amount", "Description"
    };

    /**
     * Export danh sách transaction thành CSV bytes (UTF-8 với BOM để Excel mở đúng).
     */
    public byte[] export(List<Transaction> transactions) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // UTF-8 BOM — giúp Excel (Windows) tự nhận diện encoding
        out.writeBytes(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader(HEADERS)
                .build();

        try (CSVPrinter printer = new CSVPrinter(
                new OutputStreamWriter(out, StandardCharsets.UTF_8), format)) {

            int index = 1;
            for (Transaction t : transactions) {
                printer.printRecord(
                        index++,
                        t.getTransactionDate().toString(),
                        t.getType().name(),
                        t.getCategory().getName(),
                        t.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString(),
                        t.getDescription() != null ? t.getDescription() : ""
                );
            }
            printer.flush();

        } catch (IOException e) {
            log.error("Failed to generate CSV export", e);
            throw new RuntimeException("Failed to generate CSV", e);
        }

        return out.toByteArray();
    }
}

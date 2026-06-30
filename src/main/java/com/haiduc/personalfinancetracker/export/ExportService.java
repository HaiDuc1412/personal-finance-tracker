package com.haiduc.personalfinancetracker.export;

import com.haiduc.personalfinancetracker.transaction.Transaction;
import com.haiduc.personalfinancetracker.transaction.TransactionRepository;
import com.haiduc.personalfinancetracker.transaction.TransactionSpecification;
import com.haiduc.personalfinancetracker.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final TransactionRepository transactionRepository;
    private final CsvExporter csvExporter;
    private final PdfExporter pdfExporter;

    @Transactional(readOnly = true)
    public byte[] exportCsv(User user, ExportRequest filter) {
        List<Transaction> transactions = fetchTransactions(user, filter);
        return csvExporter.export(transactions);
    }

    @Transactional(readOnly = true)
    public byte[] exportPdf(User user, ExportRequest filter) {
        List<Transaction> transactions = fetchTransactions(user, filter);
        return pdfExporter.export(transactions, user, filter);
    }

    private List<Transaction> fetchTransactions(User user, ExportRequest filter) {
        Specification<Transaction> spec = Specification
                .where(TransactionSpecification.ofUser(user))
                .and(TransactionSpecification.notDeleted())
                .and(TransactionSpecification.hasType(filter.getType()))
                .and(TransactionSpecification.hasCategory(filter.getCategoryId()))
                .and(TransactionSpecification.inMonth(filter.getMonth() != null ? filter.getMonth().intValue() : null))
                .and(TransactionSpecification.inYear(filter.getYear() != null ? filter.getYear().intValue() : null));

        return transactionRepository.findAll(spec,
                Sort.by(Sort.Direction.DESC, "transactionDate", "createdAt"));
    }

    /**
     * Tạo tên file dựa theo filter hiện tại.
     * Ví dụ: "transactions_2025-06.csv", "transactions_all.csv"
     */
    public String buildFilename(ExportRequest filter, String extension) {
        String suffix;
        if (filter.getMonth() != null && filter.getYear() != null) {
            suffix = String.format("%d-%02d", filter.getYear(), filter.getMonth());
        } else if (filter.getYear() != null) {
            suffix = String.valueOf(filter.getYear());
        } else {
            suffix = LocalDate.now().toString(); // fallback: ngày export
        }
        return "transactions_" + suffix + "." + extension;
    }
}

package com.haiduc.personalfinancetracker.export;

import com.lowagie.text.*;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.haiduc.personalfinancetracker.common.enums.TransactionType;
import com.haiduc.personalfinancetracker.transaction.Transaction;
import com.haiduc.personalfinancetracker.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class PdfExporter {

    // ─── Colors ───────────────────────────────────────────────────────────────
    private static final Color COLOR_HEADER_BG = new Color(30, 58, 138); // indigo-900
    private static final Color COLOR_INCOME = new Color(21, 128, 61); // green-700
    private static final Color COLOR_EXPENSE = new Color(185, 28, 28); // red-700
    private static final Color COLOR_ROW_ALT = new Color(243, 244, 246); // gray-100
    private static final Color COLOR_FOOTER_BG = new Color(249, 250, 251); // gray-50
    private static final Color COLOR_TEXT_MUTED = new Color(107, 114, 128); // gray-500

    // ─── Fonts ────────────────────────────────────────────────────────────────
    private static final Font FONT_TITLE = new Font(Font.HELVETICA, 18, Font.BOLD, Color.WHITE);
    private static final Font FONT_SUB = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(199, 210, 254));
    private static final Font FONT_HEADER = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
    private static final Font FONT_CELL = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);
    private static final Font FONT_INCOME = new Font(Font.HELVETICA, 9, Font.BOLD, COLOR_INCOME);
    private static final Font FONT_EXPENSE = new Font(Font.HELVETICA, 9, Font.BOLD, COLOR_EXPENSE);
    private static final Font FONT_FOOTER = new Font(Font.HELVETICA, 8, Font.NORMAL, COLOR_TEXT_MUTED);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] export(List<Transaction> transactions, User user, ExportRequest filter) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            addHeader(document, user, filter);
            addTable(document, transactions);
            addSummary(document, transactions);
            addFooter(document);

            document.close();

        } catch (Exception e) {
            log.error("Failed to generate PDF export", e);
            throw new RuntimeException("Failed to generate PDF", e);
        }

        return out.toByteArray();
    }

    // ─── Header Section ───────────────────────────────────────────────────────

    private void addHeader(Document doc, User user, ExportRequest filter) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[] { 3f, 1f });
        headerTable.setSpacingAfter(20f);

        // Left cell — title + email
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBackgroundColor(COLOR_HEADER_BG);
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setPadding(16f);

        Paragraph title = new Paragraph("Personal Finance Tracker", FONT_TITLE);
        title.setSpacingAfter(4f);
        leftCell.addElement(title);
        leftCell.addElement(new Paragraph(user.getEmail(), FONT_SUB));

        // Right cell — period info
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBackgroundColor(COLOR_HEADER_BG);
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setPadding(16f);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        rightCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        String period = buildPeriodLabel(filter);
        Paragraph periodPara = new Paragraph("Period: " + period, FONT_SUB);
        periodPara.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(periodPara);

        Paragraph typePara = new Paragraph(
                "Type: " + (filter.getType() != null ? filter.getType().name() : "ALL"), FONT_SUB);
        typePara.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(typePara);

        headerTable.addCell(leftCell);
        headerTable.addCell(rightCell);
        doc.add(headerTable);
    }

    // ─── Table Section ────────────────────────────────────────────────────────

    private void addTable(Document doc, List<Transaction> transactions) throws DocumentException {
        if (transactions.isEmpty()) {
            Paragraph empty = new Paragraph("No transactions found for the selected filters.", FONT_CELL);
            empty.setSpacingBefore(10f);
            doc.add(empty);
            return;
        }

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 0.5f, 1.5f, 1.2f, 2f, 1.8f, 3f });
        table.setHeaderRows(1);

        // Column headers
        String[] headers = { "#", "Date", "Type", "Category", "Amount", "Description" };
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, FONT_HEADER));
            cell.setBackgroundColor(COLOR_HEADER_BG);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPadding(8f);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        // Data rows
        int index = 1;
        for (Transaction t : transactions) {
            boolean isAlt = (index % 2 == 0);
            Color rowBg = isAlt ? COLOR_ROW_ALT : Color.WHITE;

            addCell(table, String.valueOf(index++), rowBg, Element.ALIGN_CENTER, FONT_CELL);
            addCell(table, t.getTransactionDate().format(DATE_FMT), rowBg, Element.ALIGN_CENTER, FONT_CELL);

            // Type cell — màu riêng
            boolean isIncome = t.getType() == TransactionType.INCOME;
            Font typeFont = isIncome ? FONT_INCOME : FONT_EXPENSE;
            addCell(table, t.getType().name(), rowBg, Element.ALIGN_CENTER, typeFont);

            addCell(table, t.getCategory().getName(), rowBg, Element.ALIGN_LEFT, FONT_CELL);

            // Amount — income dương, expense âm
            String amountStr = (isIncome ? "+" : "-")
                    + formatAmount(t.getAmount());
            addCell(table, amountStr, rowBg, Element.ALIGN_RIGHT, isIncome ? FONT_INCOME : FONT_EXPENSE);

            addCell(table,
                    t.getDescription() != null ? t.getDescription() : "—",
                    rowBg, Element.ALIGN_LEFT, FONT_CELL);
        }

        doc.add(table);
    }

    // ─── Summary Section ──────────────────────────────────────────────────────

    private void addSummary(Document doc, List<Transaction> transactions) throws DocumentException {
        if (transactions.isEmpty())
            return;

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netBalance = totalIncome.subtract(totalExpense);
        boolean isPositive = netBalance.compareTo(BigDecimal.ZERO) >= 0;

        PdfPTable summaryTable = new PdfPTable(3);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingBefore(12f);

        // Income cell
        addSummaryCell(summaryTable, "Total Income",
                "+ " + formatAmount(totalIncome), COLOR_INCOME);

        // Expense cell
        addSummaryCell(summaryTable, "Total Expense",
                "- " + formatAmount(totalExpense), COLOR_EXPENSE);

        // Net Balance cell
        Color netColor = isPositive ? COLOR_INCOME : COLOR_EXPENSE;
        addSummaryCell(summaryTable, "Net Balance",
                (isPositive ? "+ " : "- ") + formatAmount(netBalance.abs()), netColor);

        doc.add(summaryTable);
    }

    // ─── Footer ───────────────────────────────────────────────────────────────

    private void addFooter(Document doc) throws DocumentException {
        Paragraph footer = new Paragraph(
                "Generated by Personal Finance Tracker — Do not share this document.",
                FONT_FOOTER);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(16f);
        doc.add(footer);
    }

    // ─── Helper Methods ───────────────────────────────────────────────────────

    private void addCell(PdfPTable table, String text, Color bg, int align, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBorderWidthBottom(0.5f);
        cell.setBorderColorBottom(new Color(229, 231, 235));
        cell.setPadding(7f);
        cell.setHorizontalAlignment(align);
        table.addCell(cell);
    }

    private void addSummaryCell(PdfPTable table, String label, String value, Color valueColor) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COLOR_FOOTER_BG);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(new Color(229, 231, 235));
        cell.setPadding(10f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph lbl = new Paragraph(label, FONT_FOOTER);
        lbl.setAlignment(Element.ALIGN_CENTER);
        lbl.setSpacingAfter(4f);
        cell.addElement(lbl);

        Font valFont = new Font(Font.HELVETICA, 13, Font.BOLD, valueColor);
        Paragraph val = new Paragraph(value, valFont);
        val.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(val);

        table.addCell(cell);
    }

    private String buildPeriodLabel(ExportRequest filter) {
        if (filter.getMonth() != null && filter.getYear() != null) {
            return String.format("%02d/%d", filter.getMonth(), filter.getYear());
        } else if (filter.getYear() != null) {
            return String.valueOf(filter.getYear());
        }
        return "All time";
    }

    private String formatAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}

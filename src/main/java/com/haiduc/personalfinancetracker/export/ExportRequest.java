package com.haiduc.personalfinancetracker.export;

import com.haiduc.personalfinancetracker.common.enums.TransactionType;
import lombok.Data;

import java.util.UUID;

@Data
public class ExportRequest {
    private Short month;
    private Short year;
    private TransactionType type;
    private UUID categoryId;
}

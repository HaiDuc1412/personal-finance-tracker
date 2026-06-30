package com.haiduc.personalfinancetracker.budget;

import com.haiduc.personalfinancetracker.budget.event.BudgetAlertEvent;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;

@Component
public class EmailTemplateBuilder {

    public String buildSubject(BudgetAlertEvent event) {
        String emoji = event.getAlertType() == BudgetAlertEvent.AlertType.ALERT_100 ? "🚨" : "⚠️";
        String level = event.getAlertType() == BudgetAlertEvent.AlertType.ALERT_100 ? "100%" : "80%";
        return String.format("%s Budget Alert: %s has reached %s of limit",
                emoji, event.getCategoryName(), level);
    }

    public String buildHtmlBody(BudgetAlertEvent event) {
        boolean isOver = event.getAlertType() == BudgetAlertEvent.AlertType.ALERT_100;
        String color = isOver ? "#dc2626" : "#d97706";       // red : amber
        String bgColor = isOver ? "#fef2f2" : "#fffbeb";
        String level = isOver ? "100%" : "80%";
        String message = isOver
                ? "You have <strong>exceeded</strong> your budget limit for this category."
                : "You are approaching your budget limit for this category.";

        String spent = event.getSpentAmount().setScale(2, RoundingMode.HALF_UP).toPlainString();
        String limit = event.getLimitAmount().setScale(2, RoundingMode.HALF_UP).toPlainString();
        String pct = String.format("%.1f", event.getPercentage());

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"></head>
                <body style="margin:0;padding:0;background-color:#f3f4f6;font-family:Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f3f4f6;padding:32px 0;">
                    <tr><td align="center">
                      <table width="560" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 1px 3px rgba(0,0,0,.1);">
                
                        <!-- Header -->
                        <tr><td style="background-color:%s;padding:24px 32px;">
                          <h1 style="margin:0;color:#ffffff;font-size:20px;">Budget Alert — %s</h1>
                          <p style="margin:4px 0 0;color:rgba(255,255,255,.85);font-size:14px;">%s/%s</p>
                        </td></tr>
                
                        <!-- Body -->
                        <tr><td style="padding:32px;">
                          <p style="margin:0 0 16px;color:#374151;font-size:15px;">%s</p>
                
                          <!-- Stats box -->
                          <table width="100%%" cellpadding="16" style="background-color:%s;border-radius:6px;border:1px solid %s;margin-bottom:24px;">
                            <tr>
                              <td align="center">
                                <p style="margin:0;color:#6b7280;font-size:12px;text-transform:uppercase;letter-spacing:.5px;">Category</p>
                                <p style="margin:4px 0 0;color:#111827;font-size:18px;font-weight:bold;">%s</p>
                              </td>
                              <td align="center">
                                <p style="margin:0;color:#6b7280;font-size:12px;text-transform:uppercase;letter-spacing:.5px;">Spent</p>
                                <p style="margin:4px 0 0;color:%s;font-size:18px;font-weight:bold;">$%s</p>
                              </td>
                              <td align="center">
                                <p style="margin:0;color:#6b7280;font-size:12px;text-transform:uppercase;letter-spacing:.5px;">Limit</p>
                                <p style="margin:4px 0 0;color:#111827;font-size:18px;font-weight:bold;">$%s</p>
                              </td>
                              <td align="center">
                                <p style="margin:0;color:#6b7280;font-size:12px;text-transform:uppercase;letter-spacing:.5px;">Usage</p>
                                <p style="margin:4px 0 0;color:%s;font-size:18px;font-weight:bold;">%s%%</p>
                              </td>
                            </tr>
                          </table>
                
                          <p style="margin:0;color:#6b7280;font-size:13px;">
                            Period: <strong>%02d/%d</strong> &nbsp;|&nbsp; Review your spending in the Personal Finance Tracker dashboard.
                          </p>
                        </td></tr>
                
                        <!-- Footer -->
                        <tr><td style="background-color:#f9fafb;padding:16px 32px;border-top:1px solid #e5e7eb;">
                          <p style="margin:0;color:#9ca3af;font-size:12px;text-align:center;">
                            This is an automated alert from Personal Finance Tracker. Do not reply to this email.
                          </p>
                        </td></tr>
                
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(
                color, level, event.getCategoryName(),
                String.format("%02d/%d", event.getMonth(), event.getYear()),
                message,
                bgColor, color,
                event.getCategoryName(),
                color, spent,
                limit,
                color, pct,
                event.getMonth(), event.getYear()
        );
    }
}
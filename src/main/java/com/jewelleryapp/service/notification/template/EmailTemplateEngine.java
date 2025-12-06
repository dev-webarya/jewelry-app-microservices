package com.jewelleryapp.service.notification.template;

import org.springframework.stereotype.Component;

/**
 * Handles HTML construction. Separated from service logic for cleaner code.
 */
@Component
public class EmailTemplateEngine {

    public String buildProfessionalEmail(
            String icon,
            String themeColor,
            String title,
            String intro,
            String userName,
            String referenceId,
            String[][] details,
            String statusTitle,
            String statusDesc
    ) {
        StringBuilder rowsHtml = new StringBuilder();
        for (String[] row : details) {
            rowsHtml.append(String.format("""
                <tr>
                    <td style="padding: 12px 0; border-bottom: 1px solid #e5e7eb; color: #6b7280; font-size: 14px; width: 40%%; vertical-align: top;">%s</td>
                    <td style="padding: 12px 0; border-bottom: 1px solid #e5e7eb; color: #111827; font-size: 14px; font-weight: 500; text-align: right;">%s</td>
                </tr>
            """, row[0], row[1]));
        }

        return String.format("""
                <!DOCTYPE html>
                <html>
                <body style="font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f3f4f6; margin: 0; padding: 0;">
                    <div style="max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
                        <div style="background-color: %s; padding: 40px 20px; text-align: center; color: white;">
                            <div style="font-size: 48px; margin-bottom: 10px;">%s</div>
                            <h1 style="margin: 0; font-size: 24px; font-weight: bold;">%s</h1>
                        </div>
                        <div style="padding: 30px;">
                            <p style="color: #374151; font-size: 16px;">Hello <strong>%s</strong>,</p>
                            <p style="color: #6b7280; font-size: 16px;">%s</p>
                            
                            <div style="background-color: #f9fafb; border-radius: 8px; padding: 20px; border: 1px solid #e5e7eb; margin: 20px 0;">
                                <div style="font-size: 12px; text-transform: uppercase; color: #9ca3af; font-weight: bold; margin-bottom: 5px;">Reference ID</div>
                                <div style="font-size: 18px; font-weight: bold; color: #111827; margin-bottom: 15px;">%s</div>
                                <table style="width: 100%%; border-collapse: collapse;">%s</table>
                            </div>
                            
                            <div style="text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px dashed #e5e7eb;">
                                <h3 style="margin: 0; color: #111827; font-size: 16px;">%s</h3>
                                <p style="color: #6b7280; font-size: 14px; margin-top: 5px;">%s</p>
                            </div>
                        </div>
                        <div style="background-color: #f9fafb; padding: 20px; text-align: center; color: #9ca3af; font-size: 12px; border-top: 1px solid #e5e7eb;">
                            &copy; 2025 Jewellery App. All rights reserved.
                        </div>
                    </div>
                </body>
                </html>
                """, themeColor, icon, title, userName, intro, referenceId, rowsHtml.toString(), statusTitle, statusDesc);
    }
}
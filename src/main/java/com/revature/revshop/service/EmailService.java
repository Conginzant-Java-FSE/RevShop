package com.revature.revshop.service;

import com.revature.revshop.model.OrderItems;
import com.revature.revshop.model.Orders;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOrderConfirmation(Orders order, String toEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Order Confirmed - " + order.getOrderNumber());

            StringBuilder itemsHtml = new StringBuilder();
            if (order.getOrderItems() != null) {
                for (OrderItems item : order.getOrderItems()) {
                    BigDecimal price = item.getPriceAtPurchase();
                    BigDecimal subtotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));
                    itemsHtml.append("<tr>")
                            .append("<td style='padding:8px;border-bottom:1px solid #eee;'>")
                            .append(item.getProduct().getName()).append("</td>")
                            .append("<td style='padding:8px;border-bottom:1px solid #eee;text-align:center;'>")
                            .append(item.getQuantity()).append("</td>")
                            .append("<td style='padding:8px;border-bottom:1px solid #eee;text-align:right;'>₹")
                            .append(price).append("</td>")
                            .append("<td style='padding:8px;border-bottom:1px solid #eee;text-align:right;'>₹")
                            .append(subtotal).append("</td>")
                            .append("</tr>");
                }
            }

            String orderDate = order.getOrderDate() != null
                    ? order.getOrderDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
                    : "N/A";

            String html = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto;'>"
                    + "<div style='background:#0d6efd;color:white;padding:20px;text-align:center;border-radius:8px 8px 0 0;'>"
                    + "<h1 style='margin:0;'>Order Confirmed! ✅</h1></div>"
                    + "<div style='padding:20px;background:#f8f9fa;'>"
                    + "<p>Hi there,</p>"
                    + "<p>Your order has been placed successfully.</p>"
                    + "<table style='width:100%;margin:10px 0;'>"
                    + "<tr><td><strong>Order Number:</strong></td><td>" + order.getOrderNumber() + "</td></tr>"
                    + "<tr><td><strong>Order Date:</strong></td><td>" + orderDate + "</td></tr>"
                    + "</table>"
                    + "<h3 style='border-bottom:2px solid #0d6efd;padding-bottom:5px;'>Order Items</h3>"
                    + "<table style='width:100%;border-collapse:collapse;'>"
                    + "<thead><tr style='background:#e9ecef;'>"
                    + "<th style='padding:8px;text-align:left;'>Product</th>"
                    + "<th style='padding:8px;text-align:center;'>Qty</th>"
                    + "<th style='padding:8px;text-align:right;'>Price</th>"
                    + "<th style='padding:8px;text-align:right;'>Subtotal</th>"
                    + "</tr></thead><tbody>" + itemsHtml + "</tbody></table>"
                    + "<div style='text-align:right;margin-top:10px;font-size:18px;'>"
                    + "<strong>Total: ₹" + order.getTotalAmount() + "</strong></div>"
                    + "<hr style='margin:20px 0;'>"
                    + "<p style='text-align:center;color:#6c757d;'>Thank you for shopping with RevShop! 🛍️</p>"
                    + "</div></div>";

            helper.setText(html, true);
            mailSender.send(message);
            log.info("Order confirmation email sent to {}", toEmail);
        } catch (Exception e) {
            log.error(
                    "Failed to send order confirmation email to {}. Check your Brevo credentials in application.properties.",
                    toEmail, e);
        }
    }

    public void sendShippingNotification(Orders order, String toEmail) {
        sendStatusUpdate(order, toEmail, "SHIPPED");
    }

    public void sendOtp(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your Verification Code - RevShop");

            String html = "<div style='font-family:Arial,sans-serif;max-width:500px;margin:0 auto;border:1px solid #eee;border-radius:10px;overflow:hidden;'>"
                    + "<div style='background:#4F46E5;color:white;padding:30px;text-align:center;'>"
                    + "<h2 style='margin:0;'>Verification Code</h2></div>"
                    + "<div style='padding:30px;text-align:center;'>"
                    + "<p style='font-size:16px;color:#444;'>Use the code below to verify your account:</p>"
                    + "<div style='font-size:32px;font-weight:bold;letter-spacing:5px;color:#4F46E5;margin:20px 0;padding:15px;background:#f5f3ff;border-radius:8px;'>"
                    + otp + "</div>"
                    + "<p style='font-size:14px;color:#888;'>This code will expire in 5 minutes.</p>"
                    + "</div>"
                    + "<div style='background:#f9fafb;padding:20px;text-align:center;font-size:12px;color:#999;'>"
                    + "If you didn't request this, please ignore this email.</div>"
                    + "</div>";

            helper.setText(html, true);
            mailSender.send(message);
            log.info("OTP email sent to {}", toEmail);
        } catch (Exception e) {
            log.warn("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
        }
    }

    public void sendStatusUpdate(Orders order, String toEmail, String status) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Order Status Update - " + order.getOrderNumber());

            String statusColor = "#4F46E5"; // Default primary
            String icon = "📦";

            if ("SHIPPED".equalsIgnoreCase(status)) {
                statusColor = "#10B981";
                icon = "🚚";
            } else if ("DELIVERED".equalsIgnoreCase(status)) {
                statusColor = "#059669";
                icon = "✅";
            } else if ("CANCELLED".equalsIgnoreCase(status)) {
                statusColor = "#EF4444";
                icon = "❌";
            }

            String html = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto;'>"
                    + "<div style='background:" + statusColor
                    + ";color:white;padding:20px;text-align:center;border-radius:8px 8px 0 0;'>"
                    + "<h1 style='margin:0;'>Order Status Update " + icon + "</h1></div>"
                    + "<div style='padding:20px;background:#f8f9fa;'>"
                    + "<p>Hi there,</p>"
                    + "<p>The status of your order <strong>" + order.getOrderNumber()
                    + "</strong> has been updated to: <span style='color:" + statusColor + ";font-weight:bold;'>"
                    + status + "</span></p>"
                    + "<p>Tracking Details:</p>"
                    + "<div style='background:white;padding:15px;border-radius:8px;margin:15px 0;border-left:4px solid "
                    + statusColor + ";'>"
                    + "<p><strong>Current Status:</strong> " + status + "</p>"
                    + "<p><strong>Order Number:</strong> " + order.getOrderNumber() + "</p>"
                    + "</div>"
                    + "<p>Thank you for shopping with RevShop! 🛍️</p>"
                    + "</div></div>";

            helper.setText(html, true);
            mailSender.send(message);
            log.info("Status update ({}) email sent to {}", status, toEmail);
        } catch (Exception e) {
            log.warn("Failed to send status update email to {}: {}", toEmail, e.getMessage());
        }
    }
}

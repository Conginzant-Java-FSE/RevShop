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
    private static final String TD_END = "</td>";

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
                            .append(item.getProduct().getName()).append(TD_END)
                            .append("<td style='padding:8px;border-bottom:1px solid #eee;text-align:center;'>")
                            .append(item.getQuantity()).append(TD_END)
                            .append("<td style='padding:8px;border-bottom:1px solid #eee;text-align:right;'>₹")
                            .append(price).append(TD_END)
                            .append("<td style='padding:8px;border-bottom:1px solid #eee;text-align:right;'>₹")
                            .append(subtotal).append(TD_END)
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
            log.warn("Failed to send order confirmation email to {}: {}", toEmail, e.getMessage());
        }
    }

    public void sendShippingNotification(Orders order, String toEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your Order " + order.getOrderNumber() + " Has Been Shipped");

            String html = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto;'>"
                    + "<div style='background:#198754;color:white;padding:20px;text-align:center;border-radius:8px 8px 0 0;'>"
                    + "<h1 style='margin:0;'>Order Shipped! 🚚</h1></div>"
                    + "<div style='padding:20px;background:#f8f9fa;'>"
                    + "<p>Hi there,</p>"
                    + "<p>Great news! Your order <strong>" + order.getOrderNumber()
                    + "</strong> has been dispatched and is on its way to you.</p>"
                    + "<div style='background:white;padding:15px;border-radius:8px;margin:15px 0;'>"
                    + "<p><strong>Order Number:</strong> " + order.getOrderNumber() + "</p>"
                    + "<p><strong>Estimated Delivery:</strong> 3-5 business days</p>"
                    + "</div>"
                    + "<p>You will receive another notification once your order is delivered.</p>"
                    + "<hr style='margin:20px 0;'>"
                    + "<p style='text-align:center;color:#6c757d;'>Thank you for shopping with RevShop! 🛍️</p>"
                    + "</div></div>";

            helper.setText(html, true);
            mailSender.send(message);
            log.info("Shipping notification email sent to {}", toEmail);
        } catch (Exception e) {
            log.warn("Failed to send shipping notification email to {}: {}", toEmail, e.getMessage());
        }
    }
}

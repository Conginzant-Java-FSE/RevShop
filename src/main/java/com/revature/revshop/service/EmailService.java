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

    @Value("${spring.mail.username}")
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
                    + "<tr><td><strong>Payment Method:</strong></td><td>"
                    + (order.getPaymentMethod() != null ? order.getPaymentMethod() : "COD") + "</td></tr>"
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

    public void sendOrderStatusUpdateEmail(Orders order, String toEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            String status = order.getStatus().name();
            helper.setSubject("Order Update: Your order is now " + status);

            String color = status.equals("DELIVERED") ? "#198754" : "#0dcaf0";

            String html = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto;'>"
                    + "<div style='background:" + color
                    + ";color:white;padding:20px;text-align:center;border-radius:8px 8px 0 0;'>"
                    + "<h1 style='margin:0;'>Order Status Update</h1></div>"
                    + "<div style='padding:20px;background:#f8f9fa;'>"
                    + "<p>Hi there,</p>"
                    + "<p>Your order <strong>" + order.getOrderNumber() + "</strong> has a new status.</p>"
                    + "<div style='background:white;padding:15px;border-radius:8px;margin:15px 0;'>"
                    + "<p><strong>Current Status:</strong> <span style='color:" + color + ";font-weight:bold;'>"
                    + status + "</span></p>"
                    + "</div>"
                    + "<p>You can track your order in your dashboard for more details.</p>"
                    + "<hr style='margin:20px 0;'>"
                    + "<p style='text-align:center;color:#6c757d;'>Thank you for shopping with RevShop! 🛍️</p>"
                    + "</div></div>";

            helper.setText(html, true);
            mailSender.send(message);
            log.info("Order status update email sent to {}", toEmail);
        } catch (Exception e) {
            log.warn("Failed to send order status email to {}: {}", toEmail, e.getMessage());
        }
    }

    public void sendUserRegistrationEmail(String toEmail, String name) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to RevShop, " + name + "!");

            String html = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto;'>"
                    + "<div style='background:#6f42c1;color:white;padding:20px;text-align:center;border-radius:8px 8px 0 0;'>"
                    + "<h1 style='margin:0;'>Welcome to RevShop! 🎉</h1></div>"
                    + "<div style='padding:20px;background:#f8f9fa;'>"
                    + "<p>Hi <strong>" + name + "</strong>,</p>"
                    + "<p>Thank you for registering. Your account has been created successfully.</p>"
                    + "<p>Get ready to explore amazing products and unbeatable prices!</p>"
                    + "<a href='http://localhost:4200/login' style='display:inline-block;padding:10px 20px;margin:20px 0;background:#6f42c1;color:white;text-decoration:none;border-radius:5px;'>Login Now</a>"
                    + "<hr style='margin:20px 0;'>"
                    + "<p style='text-align:center;color:#6c757d;'>Happy Shopping! 🛍️</p>"
                    + "</div></div>";

            helper.setText(html, true);
            mailSender.send(message);
            log.info("Welcome email sent to {}", toEmail);
        } catch (Exception e) {
            log.warn("Failed to send welcome email to {}: {}", toEmail, e.getMessage());
        }
    }

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("RevShop Email Verification");

            String html = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto;'>"
                    + "<div style='background:#fd7e14;color:white;padding:20px;text-align:center;border-radius:8px 8px 0 0;'>"
                    + "<h1 style='margin:0;'>RevShop Email Verification</h1></div>"
                    + "<div style='padding:20px;background:#f8f9fa;'>"
                    + "<p>Hi there,</p>"
                    + "<p>Your verification code is:</p>"
                    + "<div style='font-size:32px;font-weight:bold;letter-spacing:5px;text-align:center;padding:20px;margin:20px;background:white;border:2px dashed #fd7e14;border-radius:8px;'>"
                    + otp
                    + "</div>"
                    + "<p style='color:red;'>This code will expire in 10 minutes.</p>"
                    + "<p style='color:#6c757d;'>Do not share this code with anyone.</p>"
                    + "<hr style='margin:20px 0;'>"
                    + "<p style='text-align:center;color:#6c757d;'>RevShop Security Team 🔒</p>"
                    + "</div></div>";

            helper.setText(html, true);
            mailSender.send(message);
            log.info("OTP email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email", e);
            throw new IllegalStateException("Failed to send OTP email to " + toEmail, e);
        }
    }

    public void sendPasswordResetLinkEmail(String toEmail, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Reset Your RevShop Password");

            String resetUrl = "http://localhost:4200/reset-password?token=" + token;

            String html = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto;'>"
                    + "<div style='background:#dc3545;color:white;padding:20px;text-align:center;border-radius:8px 8px 0 0;'>"
                    + "<h1 style='margin:0;'>Password Reset 🔑</h1></div>"
                    + "<div style='padding:20px;background:#f8f9fa;'>"
                    + "<p>Hi there,</p>"
                    + "<p>We received a request to reset your password. Click the button below to set a new password:</p>"
                    + "<div style='text-align:center;'>"
                    + "<a href='" + resetUrl
                    + "' style='display:inline-block;padding:12px 24px;margin:20px 0;background:#dc3545;color:white;text-decoration:none;border-radius:5px;font-weight:bold;'>Reset Password</a>"
                    + "</div>"
                    + "<p>Or copy and paste this link into your browser:</p>"
                    + "<p style='word-wrap:break-word;font-size:12px;color:#0d6efd;'>" + resetUrl + "</p>"
                    + "<p style='color:red;'>This link is valid for 15 minutes. If you did not request a password reset, you can safely ignore this email.</p>"
                    + "<hr style='margin:20px 0;'>"
                    + "<p style='text-align:center;color:#6c757d;'>RevShop Support</p>"
                    + "</div></div>";

            helper.setText(html, true);
            mailSender.send(message);
            log.info("Password reset email sent to {}", toEmail);
        } catch (Exception e) {
            log.warn("Failed to send Password reset email to {}: {}", toEmail, e.getMessage());
        }
    }
}

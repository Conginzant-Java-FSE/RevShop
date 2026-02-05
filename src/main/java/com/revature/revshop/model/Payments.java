package com.revature.revshop.model;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name ="payments")
@Data
public class Payments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Integer paymentId;
    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Orders order;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;
    @Column(name = "transaction_id", length = 100)
    private String transactionId;
    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;
    public enum PaymentMethod{
        CREDIT_CARD, DEBIT_CARD, NET_BANKING, UPI, WALLET
    }
    public enum PaymentStatus{
        PENDING, SUCCESS, FAILED, REFUNDED
    }
}
package com.revature.revshop.repository;

import com.revature.revshop.model.Payments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentsRepository extends JpaRepository<Payments, Integer> {

    Optional<Payments> findByOrder_OrderId(Long orderId);

    Optional<Payments> findByTransactionId(String transactionId);

    List<Payments> findByPaymentStatus(Payments.PaymentStatus paymentStatus);

    List<Payments> findByPaymentMethod(Payments.PaymentMethod paymentMethod);
}

package com.revature.revshop.service;

import com.revature.revshop.model.OrderItems;
import com.revature.revshop.model.Orders;
import com.revature.revshop.model.Product;
import com.revature.revshop.repository.OrderItemsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class OrderItemService {

    private final OrderItemsRepository orderItemsRepository;

    public OrderItemService(OrderItemsRepository orderItemsRepository) {
        this.orderItemsRepository = orderItemsRepository;
    }


    public OrderItems createOrderItem(Orders order, Product product, Integer quantity) {
        BigDecimal priceAtPurchase = product.getSellingPrice();

        OrderItems orderItem = new OrderItems();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(quantity);
        orderItem.setPriceAtPurchase(priceAtPurchase);

        return orderItemsRepository.save(orderItem);
    }

    //Calculates the subtotal for an order item.
    public BigDecimal calculateSubtotal(OrderItems orderItem) {
        if (orderItem.getPriceAtPurchase() == null || orderItem.getQuantity() == null) {
            return BigDecimal.ZERO;
        }
        return orderItem.getPriceAtPurchase().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
    }
}

package com.bookstore.order.service;

import com.bookstore.order.dto.PaymentCallbackRequest;
import com.bookstore.order.dto.PaymentResponse;
import com.bookstore.order.entity.Order;
import com.bookstore.order.entity.Payment;
import com.bookstore.order.enums.OrderStatus;
import com.bookstore.order.enums.PaymentMethod;
import com.bookstore.order.enums.PaymentStatus;
import com.bookstore.order.exception.BadRequestException;
import com.bookstore.order.exception.ResourceNotFoundException;
import com.bookstore.order.repository.OrderRepository;
import com.bookstore.order.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    /** Khách yêu cầu thanh toán cho đơn (cũng dùng cho thanh toán lại sau khi thất bại). */
    @Transactional
    public PaymentResponse createPayment(Long userId, Long orderId, PaymentMethod requested) {
        Order order = findOrder(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Không tìm thấy đơn hàng " + orderId);
        }
        if (order.getOrderStatus() == OrderStatus.CANCELLED || order.getOrderStatus() == OrderStatus.RETURNED) {
            throw new BadRequestException("Không thể thanh toán đơn hàng ở trạng thái " + order.getOrderStatus());
        }
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BadRequestException("Đơn hàng đã được thanh toán");
        }

        PaymentMethod method = requested != null ? requested : order.getPaymentMethod();

        Optional<Payment> pending = paymentRepository
                .findFirstByOrderIdAndStatusOrderByCreatedAtDesc(orderId, PaymentStatus.PENDING);
        if (pending.isPresent()) {
            if (pending.get().getMethod() == method) {
                return toResponse(pending.get());          // đã có giao dịch đang chờ, trả lại chính nó
            }
            pending.get().setStatus(PaymentStatus.FAILED); // đổi phương thức: hủy giao dịch cũ
        }

        Payment payment = newPayment(order, method);
        order.setPaymentMethod(method);
        order.setPaymentStatus(PaymentStatus.PENDING);
        return toResponse(paymentRepository.save(payment));
    }

    /** Cổng thanh toán (giả lập) báo kết quả về. */
    @Transactional
    public PaymentResponse handleCallback(PaymentCallbackRequest req) {
        Payment payment = paymentRepository.findByTransactionRef(req.transactionRef())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch " + req.transactionRef()));
        if (payment.getMethod() == PaymentMethod.COD) {
            throw new BadRequestException("Thanh toán COD không dùng callback");
        }
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Giao dịch đã được xử lý: " + payment.getStatus());
        }

        Order order = payment.getOrder();
        if (req.success()) {
            payment.setStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
            order.setPaymentStatus(PaymentStatus.PAID);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            order.setPaymentStatus(PaymentStatus.FAILED);
        }
        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPayments(Long orderId) {
        findOrder(orderId);
        return paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId)
                .stream().map(this::toResponse).toList();
    }

    // ---------- OrderService gọi vào các hàm này ----------

    /** Đơn giao thành công: COD thu tiền xong thì chuyển PAID. */
    @Transactional
    public void onOrderDelivered(Order order) {
        if (order.getPaymentMethod() != PaymentMethod.COD || order.getPaymentStatus() == PaymentStatus.PAID) {
            return;
        }
        Payment payment = paymentRepository
                .findFirstByOrderIdAndStatusOrderByCreatedAtDesc(order.getId(), PaymentStatus.PENDING)
                .orElseGet(() -> newPayment(order, PaymentMethod.COD));
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);
        order.setPaymentStatus(PaymentStatus.PAID);
    }

    /** Đơn bị hủy hoặc trả hàng: hoàn tiền giao dịch đã trả, đóng giao dịch đang chờ. */
    @Transactional
    public void onOrderClosed(Order order) {
        for (Payment p : paymentRepository.findByOrderIdOrderByCreatedAtDesc(order.getId())) {
            if (p.getStatus() == PaymentStatus.PAID) p.setStatus(PaymentStatus.REFUNDED);
            else if (p.getStatus() == PaymentStatus.PENDING) p.setStatus(PaymentStatus.FAILED);
        }
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        } else if (order.getPaymentStatus() == PaymentStatus.PENDING) {
            order.setPaymentStatus(PaymentStatus.UNPAID);
        }
    }

    // ---------- helpers ----------

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng " + orderId));
    }

    private Payment newPayment(Order order, PaymentMethod method) {
        Payment p = new Payment();
        p.setOrder(order);
        p.setMethod(method);
        p.setAmount(order.getTotalAmount());
        p.setStatus(PaymentStatus.PENDING);
        p.setTransactionRef("TXN-" + order.getId() + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return p;
    }

    private PaymentResponse toResponse(Payment p) {
        String instruction = null;
        if (p.getStatus() == PaymentStatus.PENDING) {
            instruction = p.getMethod() == PaymentMethod.COD
                    ? "Thanh toán tiền mặt khi nhận hàng"
                    : "Chuyển khoản với nội dung: " + p.getTransactionRef();
        }
        return new PaymentResponse(p.getId(), p.getOrder().getId(), p.getMethod(), p.getAmount(),
                p.getStatus(), p.getTransactionRef(), p.getCreatedAt(), p.getPaidAt(), instruction);
    }
}
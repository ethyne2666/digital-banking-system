package com.banking.paymentService.service;

import com.banking.paymentService.dto.CreatePaymentRequest;
import com.banking.paymentService.dto.PaymentOrderResponse;
import com.banking.paymentService.repository.PaymentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    private static final String PAYMENT_COMPLETED_TOPIC = "payment.completed";
    private static final String PAYMENT_FAILED_TOPIC = "payment.failed";


    /*
    *  1. Create order in razorpay
    *  2. save payment recore in DB
    *  3. return order details to frontend
    *  4. fronted show razorpay Checkout page
    *  5. User pays
    *  6. Razorpay call webhook
    * */

    public PaymentOrderResponse createPaymentOrder(CreatePaymentRequest request) {
        log.info("Creating payment order for account: {} amount: {}", request.getAccountNumber() , request.getAmount());

    }
}

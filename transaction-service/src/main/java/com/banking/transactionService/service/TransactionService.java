package com.banking.transactionService.service;

import com.banking.transactionService.dto.TransactionResponse;
import com.banking.transactionService.dto.TransferRequest;
import com.banking.transactionService.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    
    private static final String TRANSACTION_INITIATED_TOPIC= "transaction.initiated";
    private static final String TRANSACTION_COMPLETED_TOPIC= "transaction.completed";
    private static final String TRANSACTION_REFUNDED_TOPIC= "transaction.refunded";

    
    // 1. Initiate a transfer (status PENDING until OTP is verified)
    /*  SAGA STEP 1 : INITIATE TRANSFER
    * initiate transfer
    * Deducts from sender via feign
    * saves transaction as processing
    * publish event to kafka for fraud check
    * returns
    * */
    
    
    @Transactional
    public TransactionResponse transfer(TransferRequest request) {
        log.info("Transfer request: {} -> {}, amount: {}",
                request.getSenderAccountNumber(), request.getReceiverAccountNumber(), request.getAmount());


    }








    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(String transactionId) {

    }




    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionHistory(String accountNumber) {

    }




    @Transactional
    public TransactionResponse verifyOTP(String transactionId, String otp) {

    }

}
package com.banking.transactionService.service;

import com.banking.transactionService.client.AccountServiceClient;
import com.banking.transactionService.dto.TransactionResponse;
import com.banking.transactionService.dto.TransferRequest;
import com.banking.transactionService.entity.Transaction;
import com.banking.transactionService.entity.TransactionStatus;
import com.banking.transactionService.entity.TransactionType;
import com.banking.transactionService.event.TransactionInitiatedEvent;
import com.banking.transactionService.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    
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
    

    public TransactionResponse transfer(TransferRequest request) {
        log.info("SAGA START - Transfer request: {} -> {}, amount: {}",
                request.getSenderAccountNumber(), request.getReceiverAccountNumber(), request.getAmount());

        accountServiceClient.deductBalance(request.getSenderAccountNumber(), request.getAmount());

        Transaction transaction = new Transaction();
        transaction.setSenderAccountNumber(request.getSenderAccountNumber());
        transaction.setReceiverAccountNumber(request.getReceiverAccountNumber());
        transaction.setAmount(request.getAmount());
        transaction.setType(TransactionType.TRANSFER);
        transaction.setStatus(TransactionStatus.PROCESSING);
        transaction.setDescription(request.getDescription());
        transaction.setReferenceNumber(UUID.randomUUID().toString());

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction saved as PROCESSING: {}", savedTransaction.getId());

        //SAGA STEP - 2 PUBLISH FOR FRAUD CHECK
        TransactionInitiatedEvent event = new TransactionInitiatedEvent(
                savedTransaction.getId(),
                savedTransaction.getSenderAccountNumber(),
                savedTransaction.getReceiverAccountNumber(),
                savedTransaction.getAmount(),
                savedTransaction.getDescription()
        );

        kafkaTemplate.send(TRANSACTION_INITIATED_TOPIC , savedTransaction.getId(), event);
        log.info("SAGA STEP 2 - TRANSACTION INITIATED EVENT PUBLISHED: {}", savedTransaction.getId());

        return mapToResponse(savedTransaction);
    }




    public TransactionResponse getTransaction(String transactionId) {
        return mapToResponse(transactionRepository
                .findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found:"+transactionId))
        );
    }



    public List<TransactionResponse> getTransactionHistory(String accountNumber) {

        return transactionRepository
                .findBySenderAccountNumberOrderByCreatedAtDesc(accountNumber)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    private TransactionResponse mapToResponse(Transaction transaction){
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setSenderAccountNumber(transaction.getSenderAccountNumber());
        response.setReceiverAccountNumber(transaction.getReceiverAccountNumber());
        response.setAmount(transaction.getAmount());
        response.setType(transaction.getType());
        response.setStatus(transaction.getStatus());
        response.setDescription(transaction.getDescription());
        response.setReferenceNumber(transaction.getReferenceNumber());
        response.setFailureReason(transaction.getFailureReason());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setCompletedAt(transaction.getCompletedAt());

        return response;
    }



}
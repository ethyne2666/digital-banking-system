package com.banking.transactionService.entity;

//  PENDING -> PROCESSING  -> COMPLETED (clean transaction)
//  PENDING -> VERIFICATION  -> (suspicious detected)  ->  VERIFY WITH BANK  (COMPLETE WITH EMAIL,PHONE)
// if verified -> complete payment
// if flagged (SAGA REFUND)


public enum TransactionStatus {
    PENDING,
    PROCESSING,
    PENDING_VERIFICATION,
    COMPLETED,
    FAILED,
    FLAGGED
}

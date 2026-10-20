package com.banking.accountService.service;

import com.banking.accountService.dto.AccountResponse;
import com.banking.accountService.dto.CreateAccountRequest;
import com.banking.accountService.entity.Account;
import com.banking.accountService.entity.AccountStatus;
import com.banking.accountService.entity.AccountType;
import com.banking.accountService.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService {

    private static final SecureRandom secureRandom = new SecureRandom();

    private final AccountRepository accountRepository;

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request){
        log.info("Creating account for: {}", request.getEmail());

        if(accountRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Account already exists for email: "+request.getEmail());
        }

        Account account = new Account();
        account.setAccountHolderName(request.getAccountHolderName());
        account.setEmail(request.getEmail());
        account.setPhone(request.getPhone());
        account.setAccountType(request.getAccountType());
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(request.getInitialDeposite());
        account.setAccountNumber(generateAccountNumber());
        account.setDailyTransactionLimit(
                request.getAccountType() == AccountType.SAVINGS
                ? new BigDecimal("100000") : new BigDecimal("500000")
        );

        Account savedAccount = accountRepository.save(account);
        log.info("Account created: {}", savedAccount.getAccountNumber());

        return mapToResponse(savedAccount);
    }



    /*
    *  Block Account - Called by Fraud Detection service via kafka
    * */

    public void blockAccount(String accountNumber){
        log.info("Blocking account: {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found "));
        account.setStatus(AccountStatus.BLOCKED);
        accountRepository.save(account);
        log.info("Account blocked: {}", accountNumber);
    }




    // Credit balance
    //called by transaction service via kafka
    public void creditBalance(String accountNumber, BigDecimal amount) {
        log.info("Crediting {} to account: {}" , amount , accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found "));

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        log.info("Balance credited. New Balance: {}",account.getBalance());


    }




    // Deduct balance
    //called by transaction service via kafka
    public void deductBalance(String accountNumber , BigDecimal amount){
        log.info("Deducting balance {} from account: {}", amount, accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found "));

        if(account.getStatus() != AccountStatus.ACTIVE){
            throw new RuntimeException("Account is not active" + accountNumber);
        }

        if(account.getBalance().compareTo(amount) < 0){
            throw new RuntimeException("Insufficient funds for account" + accountNumber);
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        log.info("Balance updated . New Balance: {}",account.getBalance());
    }



    //Generate unique 12 digit account number
    private String generateAccountNumber() {
        String accountNumber;
        do {

            long number = secureRandom.nextLong(1_000_000_000_000L);
            accountNumber = String.format("%012d" , number);

        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }


    public AccountResponse getAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found "));
        return mapToResponse(account);
    }


    /*
    *
    * */


    public BigDecimal getBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found "));
        return account.getBalance();
    }







    private AccountResponse mapToResponse(Account account){
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setAccountHolderName(account.getAccountHolderName());
        response.setEmail(account.getEmail());
        response.setPhone(account.getPhone());
        response.setAccountType(account.getAccountType());
        response.setStatus(account.getStatus());
        response.setBalance(account.getBalance());
        response.setDailyTransactionLimit(account.getDailyTransactionLimit());
        response.setCreatedAt(account.getCreatedAt());
        return response;
    }

}

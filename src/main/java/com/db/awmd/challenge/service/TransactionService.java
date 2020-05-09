package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.TransactionException;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class TransactionService {

    @Getter
    private final AccountsRepository accountsRepository;

    private final EmailNotificationService emailNotificationService;

    @Autowired
    TransactionService(AccountsRepository accountsRepository, EmailNotificationService emailNotificationService){
        this.accountsRepository = accountsRepository;
        this.emailNotificationService = emailNotificationService;
    }

    /**
     * transferAmount method checks if both From account and To account numbers are valid, performs basic check if
     * the account numbers are found in the repository, checks if both account numbers are same.
     * @param accountFrom
     * @param accountTo
     * @param amount
     * @throws Exception
     */
    public void transferAmount(String accountFrom, String accountTo, BigDecimal amount) throws Exception {

        //Validate both the accounts. For now concrete implementation of isValidAccountNumber() not provided.
        if(!(AccountsService.isValidAccountNumber(accountFrom) && AccountsService.isValidAccountNumber(accountTo))){
            throw new TransactionException("AccountNumber not valid. Please enter valid From account and To account");
        }
        //Check if both the accounts are existing in the repository.
        Account fromAccount = this.accountsRepository.getAccount(accountFrom);
        if(fromAccount==null){
            throw new TransactionException("From Account not found: "+ accountFrom);
        }

        Account toAccount = this.accountsRepository.getAccount(accountTo);
        if(toAccount == null){
            throw new TransactionException("To Account not found: "+ accountTo);
        }

        //Check if both the account numbers are same.
        if(fromAccount.getAccountId().equals(toAccount.getAccountId())){
            throw new TransactionException("Both From and To account cannot be same: "+ accountTo);
        }
        log.info("Transfer initiated...");
        boolean success = this.accountsRepository.moneyTransfer(accountFrom,accountTo,amount);
        if(!success){
            throw new TransactionException("No sufficient balance to make a transfer of "+amount+ " in the account: "+accountFrom);
        }
        this.emailNotificationService.notifyAboutTransfer(fromAccount,"Amount "+amount.toString()+" has been debited from your account and sent to beneficiary account :" + toAccount.getAccountId());
        this.emailNotificationService.notifyAboutTransfer(toAccount,"Amount "+ amount.toString()+" has been credited to your account. Money transfer from account: " +fromAccount.getAccountId());

    }
}

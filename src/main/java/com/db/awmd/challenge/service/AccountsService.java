package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;

  private final EmailNotificationService emailNotificationService;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository, EmailNotificationService emailNotificationService) {
    this.accountsRepository = accountsRepository;
    this.emailNotificationService = emailNotificationService;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public boolean validateBalance( Account account, BigDecimal amount){
    return account != null && account.getBalance().compareTo(amount) >= 0;
  }

  public static final boolean isValidAccountNumber(String accountNumber){
    //perform check
    // check if the account is valid and exists in the system
    return true;
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }
}

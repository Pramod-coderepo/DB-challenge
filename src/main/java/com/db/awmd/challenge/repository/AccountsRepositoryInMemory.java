package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Repository;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

  private final Map<String, Account> accounts = new ConcurrentHashMap<>();

  /**
   * createAccount method adds a new account to the repository if the account id is not present.
   * @param account
   * @throws DuplicateAccountIdException
   */
  @Override
  public void createAccount(Account account) throws DuplicateAccountIdException {
    Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
    if (previousAccount != null) {
      throw new DuplicateAccountIdException(
        "Account id " + account.getAccountId() + " already exists!");
    }
  }

  /**
   * moneyTransfer method is used to transfer money from an account to other in thread safe manner.
   * Both the accounts are blocked in a sequential manner to avoid deadlock even if mutual transfer initiated.
   *
   * @param fromAccountId
   * @param toAccountId
   * @param amount
   * @return
   */
  @Override
  public Boolean moneyTransfer(String fromAccountId, String toAccountId, BigDecimal amount) {
    AtomicBoolean isTransactionSuccess= new AtomicBoolean();
    // get accounts based on ascending accountid
    // transfer operation always blocks account in the same order to prevent deadlock
    String firstAccountId = fromAccountId.compareTo(toAccountId)<0?fromAccountId:toAccountId;
    String secondAccountId = firstAccountId.equals(fromAccountId) ? toAccountId:fromAccountId;

    // computeIfPresent method invocation is performed atomically, from ConcurrentHashMap implementation
    accounts.computeIfPresent(firstAccountId, (k1, account1) -> {
          accounts.computeIfPresent(secondAccountId, (k2, account2) -> {
            if(firstAccountId.equals(fromAccountId))// should debit from first account and credit to second account
            {
              if (account1.getBalance().compareTo(amount) >= 0) {
                account1.setBalance(account1.getBalance().subtract(amount));
                account2.setBalance(account2.getBalance().add(amount));
                isTransactionSuccess.set(true);
              }
            }
            else{ // Debit from second account and credit to first.
              if (account2.getBalance().compareTo(amount) >= 0) {
                account2.setBalance(account2.getBalance().subtract(amount));
                account1.setBalance(account1.getBalance().add(amount));
                isTransactionSuccess.set(true);
              }
            }
            return account2;
          });
      return account1;
    });
    return isTransactionSuccess.get();
  }

  /**
   * getAccount checks and returns an account if present in the repository.
   * @param accountId
   * @return
   */
  @Override
  public Account getAccount(String accountId) {
    return accounts.get(accountId);
  }

 
  @Override
  public void clearAccounts() {
    accounts.clear();
  }

}

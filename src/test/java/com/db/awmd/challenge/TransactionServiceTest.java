package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private AccountsService accountsService;

    @Before
    public void prepareMockMvc() {
        // Reset the existing accounts before each test.
        accountsService.getAccountsRepository().clearAccounts();
    }

    /**
     * transferAmountTest method tests happy path for money transfer between two accounts
     */
    @Test
    public void transferAmountTest(){
        Account accountFrom = new Account("Id-123");
        accountFrom.setBalance(new BigDecimal(500));
        this.accountsService.createAccount(accountFrom);
        Account accountTo = new Account("Id-1234");
        accountTo.setBalance(new BigDecimal(500));
        this.accountsService.createAccount(accountTo);
        try {
            this.transactionService.transferAmount(accountFrom.getAccountId(),accountTo.getAccountId(), new BigDecimal(100));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertThat(this.accountsService.getAccount("Id-123").getBalance()).isEqualTo(new BigDecimal(400));
        assertThat(this.accountsService.getAccount("Id-1234").getBalance()).isEqualTo(new BigDecimal(600));
    }

    /**
     * transferAmountTest_ovrDraft method tests failure transaction when the transfering account does not have
     * enough balance to make a transfer.
     */
    @Test
    @ExceptionHandler(value = Exception.class)
    public void transferAmountTest_ovrDraft(){
        Account accountFrom = new Account("Id-12345");
        accountFrom.setBalance(new BigDecimal(500));
        this.accountsService.createAccount(accountFrom);
        Account accountTo = new Account("Id-12346");
        accountTo.setBalance(new BigDecimal(500));
        this.accountsService.createAccount(accountTo);
        try {
            this.transactionService.transferAmount(accountFrom.getAccountId(),accountTo.getAccountId(), new BigDecimal(600));
        } catch (Exception e) {
            e.printStackTrace();
            assertThat(e.getMessage().contains("No sufficient balance"));
        }
        assertThat(this.accountsService.getAccount("Id-12345").getBalance()).isEqualTo(new BigDecimal(500));
        assertThat(this.accountsService.getAccount("Id-12346").getBalance()).isEqualTo(new BigDecimal(500));
    }

    /**
     * transferAmountTest_accountNotFoundError method tests the scenario where an attempt is made to transfer amount from
     * non existing account in the repository.
     */
    @Test
    @ExceptionHandler(value = Exception.class)
    public void transferAmountTest_accountNotFoundError(){
        Account accountFrom = new Account("Id-123456");
        Account accountTo = new Account("Id-123457");
        accountTo.setBalance(new BigDecimal(500));
        this.accountsService.createAccount(accountTo);
        try {
            this.transactionService.transferAmount(accountFrom.getAccountId(),accountTo.getAccountId(), new BigDecimal(600));
        } catch (Exception e) {
            e.printStackTrace();
            assertThat(e.getMessage().contains("From Account not found"));
        }
    }

    /**
     * transferAmountTest_sameAccountError method tests the scenario when both from and to account numbers are same.
     */
    @Test
    @ExceptionHandler(value = Exception.class)
    public void transferAmountTest_sameAccountError(){
        Account accountTo = new Account("Id-123457");
        accountTo.setBalance(new BigDecimal(500));
        this.accountsService.createAccount(accountTo);
        try {
            this.transactionService.transferAmount(accountTo.getAccountId(),accountTo.getAccountId(), new BigDecimal(600));
        } catch (Exception e) {
            e.printStackTrace();
            assertThat(e.getMessage().contains("Both From and To account cannot be same"));
        }
    }
}

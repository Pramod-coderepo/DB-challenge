package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.WebApplicationContext;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class TransactionControllerTest {
    private MockMvc mockMvc;
    @Autowired
    private AccountsService accountsService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void prepareMockMvc() {
        this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

        // Reset the existing accounts before each test.
        accountsService.getAccountsRepository().clearAccounts();
    }

    /**
     * transferMoneyTest checks a happy path of money tansfer.
     * @throws Exception
     */
    @Test
    public void transferMoneyTest() throws Exception {

        String accountFromId = "Id-1234";
        Account account = new Account(accountFromId, new BigDecimal("500"));
        this.accountsService.createAccount(account);
        String accountToId = "Id-12345";
        Account accountTo = new Account(accountToId, new BigDecimal("400"));
        this.accountsService.createAccount(accountTo);

        Thread t1 = new Thread(()-> {
            try {
                this.mockMvc.perform(post(
                        "/v1/transaction/transfer").contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"accountFrom\":\"Id-1234\",\"accountTo\":\"Id-12345\",\"amount\":100}"))
                        .andExpect(status().is(200));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Thread t2 = new Thread(()-> {
            try {
                this.mockMvc.perform(post(
                        "/v1/transaction/transfer").contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"accountFrom\":\"Id-12345\",\"accountTo\":\"Id-1234\",\"amount\":300}"))
                        .andExpect(status().is(200));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t1.start();
        t2.start();

        t1.join();
        t2.join();

        this.mockMvc.perform(get("/v1/accounts/" + accountFromId))
                .andExpect(status().isOk())
                .andExpect(
                        content().string("{\"accountId\":\"" + accountFromId + "\",\"balance\":700}"));
    }

    /**
     * transferMoneyOverDraftTest method tests scenario where a transaction of amount more than available balance tried
     * to execute.
     * @throws Exception
     */
    @Test
    @ExceptionHandler(value = Exception.class)
    public void transferMoney_overDraftTest() throws Exception {

        String accountFromId = "Id-101234";
        Account account = new Account(accountFromId, new BigDecimal("500"));
        this.accountsService.createAccount(account);
        String accountToId = "Id-1012345";
        Account accountTo = new Account(accountToId, new BigDecimal("500"));
        this.accountsService.createAccount(accountTo);

            try {
                this.mockMvc.perform(post(
                        "/v1/transaction/transfer").contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"accountFrom\":\"Id-101234\",\"accountTo\":\"Id-1012345\",\"amount\":600}"))
                        .andExpect(status().is(400));
            } catch (Exception e) {
                assertThat(e.getMessage().contains("No sufficient balance to make a transfer of 600 in the account: Id-101234"));
            }

        this.mockMvc.perform(get("/v1/accounts/" + accountFromId))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"accountId\":\"" + accountFromId + "\",\"balance\":500}"));

        this.mockMvc.perform(get("/v1/accounts/" + accountToId))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"accountId\":\"" + accountToId + "\",\"balance\":500}"));
    }

    /**
     * transferMoneyValidation method tests if the input validations work fine.
     * @throws Exception
     */
    @Test
    @ExceptionHandler(value = Exception.class)
    public void transferMoney_validationTest() throws Exception {

        String accountFromId = "Id-101234";
        Account account = new Account(accountFromId, new BigDecimal("500"));
        this.accountsService.createAccount(account);
        String accountToId = "Id-1012345";
        Account accountTo = new Account(accountToId, new BigDecimal("500"));
        this.accountsService.createAccount(accountTo);

        Thread t1 = new Thread(()-> {
            try {
                this.mockMvc.perform(post(
                        "/v1/transaction/transfer").contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"accountFrom\":\"\",\"accountTo\":\"Id-1012345\",\"amount\":400}"))
                        .andExpect(status().is(400));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Thread t2 = new Thread(()-> {
            try {
                this.mockMvc.perform(post(
                        "/v1/transaction/transfer").contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"accountFrom\":\"Id-101234\",\"accountTo\":\"Id-1012345\",\"amount\":-1}"))
                        .andExpect(status().is(400));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t1.start();
        t2.start();

        t1.join();
        t2.join();
    }
}

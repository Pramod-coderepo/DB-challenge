package com.db.awmd.challenge.web;

import com.db.awmd.challenge.domain.MoneyTransfer;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.TransactionException;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.TransactionService;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;

@RestController
@RequestMapping("/v1/transaction")
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * transferMoney method initiates money transfer between 'From account' to 'To account'.
     * @param transferObj
     * @return
     * @throws Exception
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/transfer")
    @JsonCreator
    public ResponseEntity<Object> transferMoney(@RequestBody @Valid MoneyTransfer transferObj) throws Exception {

        log.info("Initiating transfer :{} ", transferObj);

        try {
            this.transactionService.transferAmount(transferObj.getAccountFrom(),transferObj.getAccountTo(),transferObj.getAmount());
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

}

package com.db.awmd.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class MoneyTransfer {

    @NotNull
    @NotEmpty (message ="From account cannot be empty")
    private String accountFrom;

    @NotNull
    @NotEmpty (message ="To account cannot be empty")
    private String accountTo;

    @NotNull
    @Min(value = 1, message = "Amount cannot be 0 or less...")
    private BigDecimal amount;

    @JsonCreator
    public MoneyTransfer(@JsonProperty("accountFrom") String accountFrom, @JsonProperty("accountTo") String accountTo, @JsonProperty("amount") BigDecimal amount) {
        this.accountFrom = accountFrom;
        this.accountTo = accountTo;
        this.amount = amount;
    }


}

package com.qihoo.smallbank.controller.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {
    private String seqNo;
    private String tranType;
    private String fromAccountCode;
    private String toAccountCode;
    private BigDecimal amount;
}

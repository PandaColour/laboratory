package com.qihoo.smallbank.service;

import java.math.BigDecimal;

public interface TransactionService {
    // 定义交易相关的业务方法
    void transfer(String tranType, String seqNo, String fromAccountCode, String toAccountCode, BigDecimal amount);
}

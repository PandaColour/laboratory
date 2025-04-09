package com.qihoo.smallbank.transaction;

import com.qihoo.smallbank.service.TransactionService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 交易实体类，用于封装交易信息
 * 使用原型模式，每次获取都是新的实例
 */
@Data
@Component
@Scope("prototype")
@RestController
@RequestMapping("/api/transaction")
public class TransactionEntry {
    @Autowired
    private TransactionService transactionService;

    /**
     * 交易类型
     */
    private String tranType;

    /**
     * 转出账户
     */
    private String fromAccountCode;

    /**
     * 转入账户
     */
    private String toAccountCode;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 默认构造函数
     */
    public TransactionEntry() {
    }

    /**
     * 带参数的构造函数
     */
    public TransactionEntry(String tranType, String fromAccountCode, 
                          String toAccountCode, BigDecimal amount) {
        this.tranType = tranType;
        this.fromAccountCode = fromAccountCode;
        this.toAccountCode = toAccountCode;
        this.amount = amount;
    }

    /**
     * 执行交易
     * @return 交易结果
     */
    @RequestMapping(value = "/transfer", method = RequestMethod.POST, 
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String doTransaction(@RequestBody TransactionEntry request) {
        // 参数校验
        if (StringUtils.isEmpty(request.getTranType())
                || StringUtils.isEmpty(request.getFromAccountCode())
                || StringUtils.isEmpty(request.getToAccountCode())
                || request.getAmount() == null) {
            return "参数错误";
        }

        // 这里可以调用 TransactionService 的 transfer 方法
        try {
            transactionService.transfer(request.getTranType(), UUID.randomUUID().toString(), 
                    request.getFromAccountCode(), request.getToAccountCode(), request.getAmount());
            return "success";
        } catch (Exception e) {
            return e.getMessage();
        }
    }
} 
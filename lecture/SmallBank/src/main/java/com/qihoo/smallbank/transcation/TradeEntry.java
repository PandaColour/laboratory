package com.qihoo.smallbank.transcation;

import com.qihoo.smallbank.controller.request.TransferRequest;
import com.qihoo.smallbank.transcation.model.NormalTrade;
import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 交易实体类，用于封装交易信息
 * 使用原型模式，每次获取都是新的实例
 */
@Component
public class TradeEntry implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

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

    private Boolean paramCheckValid(TransferRequest request) {
        if (Objects.isNull(request.getAmount()) ||
                request.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        return true;
    }


    public String doTransaction(TransferRequest request) {
        if (!this.paramCheckValid(request)){
            return "参数校验失败";
        }
        // 这里可以调用 TransactionService 的 transfer 方法
        NormalTrade trade = applicationContext.getBean(NormalTrade.class);
        if (trade.init(
                request.getSeqNo(),
                request.getTranType(),
                request.getFromAccountCode(),
                request.getToAccountCode(),
                request.getAmount())) {
            trade.doTransaction();
        } else {
            return "参数校验失败";
        }
        return "success";
    }


}
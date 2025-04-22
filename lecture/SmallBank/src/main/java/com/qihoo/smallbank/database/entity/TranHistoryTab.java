package com.qihoo.smallbank.database.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 资金变动流水表
 */
@Data
public class TranHistoryTab implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  自增主键
     */
    private Long id;

    /**
     *  最新流水号
     */
    private String seqNo;

    /**
     *  前一个流水号
     */
    private String preSeqNo;

    /**
     *  转出账户ID
     */
    private String fromAccount;

    /**
     *  转入账户ID
     */
    private String toAccount;

    /**
     *  交易金额
     */
    private BigDecimal amount;

    /**
     *  交易类型
     */
    private String tranType;

    /**
     *  资金方向, D-DEBIT贷, C-CREDIT借
     */
    private String tranDirection;

    /**
     *  状态：PROCESS-交易中, SUCCESS-成功
     */
    private String status;

    /**
     *  冲正标志, NULL-未冲正, X-已冲正
     */
    private String reverse;

    /**
     *  冲正流水号
     */
    private String reverseSeqNo;

    /**
     *  创建时间
     */
    private Date createdAt;

    /**
     *  修改时间
     */
    private Date updatedAt;


}
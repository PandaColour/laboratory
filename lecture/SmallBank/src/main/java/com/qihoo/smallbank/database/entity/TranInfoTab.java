package com.qihoo.smallbank.database.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 资金变动流水表
 */
@Data
public class TranInfoTab implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  自增主键
     */
    private Long id;

    /**
     *  流水号
     */
    private String seqNo;

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
     *  状态：BEGIN-开始,PROCESS-交易中, SUCCESS-成功, FAILED-失败
     */
    private String status;

    /**
     *  创建时间
     */
    private Date createdAt;

    /**
     *  修改时间
     */
    private Date updatedAt;


}
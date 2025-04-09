package com.qihoo.smallbank.database.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 账户信息表
 */
@Data
public class AccountTab implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  自增主键
     */
    private Integer id;

    /**
     *  账号号,客户可见
     */
    private String accountCode;

    /**
     *  内部唯一标识,内部代号
     */
    private String internalCode;

    /**
     *  上一次余额
     */
    private BigDecimal preBalance;

    /**
     *  当前余额
     */
    private BigDecimal currentBalance;

    /**
     *  最后一次修改的流水号
     */
    private String latestSeqNo;

    /**
     *  版本号（用于乐观锁）
     */
    private Integer version;

    /**
     *  创建时间
     */
    private Date createdAt;

    /**
     *  修改时间
     */
    private Date updatedAt;


}
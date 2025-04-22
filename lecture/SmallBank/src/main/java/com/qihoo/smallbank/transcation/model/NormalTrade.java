package com.qihoo.smallbank.transcation.model;

import com.qihoo.smallbank.database.entity.TranInfoTab;
import com.qihoo.smallbank.database.mapper.TranInfoTabMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Component
@Scope("prototype")
public class NormalTrade implements ApplicationContextAware {

    private String seqNo;

    /**
     * 交易类型
     */
    private String tranType;

    /**
     * 转出账户
     */
    private Account fromAccount;

    /**
     * 转入账户
     */
    private Account toAccount;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    private TranInfoTab tranInfoTab;

    @Autowired
    private TranInfoTabMapper tranInfoTabMapper;

    /**
     * 带参数的构造函数
     */
    public Boolean init(String seqNo,
                        String tranType,
                        String fromAccountCode,
                        String toAccountCode,
                        BigDecimal amount) {
        if (StringUtils.isEmpty(tranType)
                || StringUtils.isEmpty(fromAccountCode)
                || StringUtils.isEmpty(toAccountCode)
                || Objects.isNull(amount)
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Boolean.FALSE;
        }

        tranInfoTab = tranInfoTabMapper.findBySeqNo(seqNo);
        if (Objects.isNull(tranInfoTab)) {
            tranInfoTab = new TranInfoTab();
            tranInfoTab.setSeqNo(seqNo);
            tranInfoTab.setAmount(amount);
            tranInfoTab.setStatus("START");
            tranInfoTab.setTranType(tranType);
            tranInfoTab.setFromAccount(fromAccountCode);
            tranInfoTab.setToAccount(toAccountCode);
            tranInfoTabMapper.insert(tranInfoTab);
        } else {
            return Boolean.FALSE;
        }

        this.seqNo = seqNo;
        if (StringUtils.isEmpty(this.seqNo)) {
            this.seqNo = randomSeqNo();
        }
        this.tranType = tranType;
        this.fromAccount = applicationContext.getBean(Account.class);
        this.fromAccount.init(fromAccountCode);
        this.toAccount = applicationContext.getBean(Account.class);
        this.toAccount.init(toAccountCode);
        this.amount = amount;
        return Boolean.TRUE;
    }

    private String randomSeqNo() {
        return UUID.randomUUID().toString();
    }

    /**
     * 执行交易，外部调用这个接口保证事务安全
     * @return 交易结果
     */
    public String doTransaction() {
        this.updateStatus("PROCESSING");
        try {
            if ("success" == this.doAccountTransaction()) {
                this.updateStatus("SUCCESS");
                return "success";
            } else {
                this.updateStatus("FAILED");
                return "failed";
            }
        } catch (Exception e) {
            this.updateStatus("FAILED");
            throw e;
        }
    }

    /**
     * 内部方法，为事务传递设置为public,外部不要调用
     * @return
     */
    @Transactional
    public String doAccountTransaction() {
        if (this.fromAccount.getAccountCode().compareTo(this.toAccount.getAccountCode()) < 0) {
            if (!this.fromAccount.lockForUpdate() || !this.toAccount.lockForUpdate()) {
                return "failed";
            }
        } else {
            if (!this.toAccount.lockForUpdate() || !this.fromAccount.lockForUpdate()) {
                return "failed";
            }
        }
        fromAccount.credit(this.seqNo, this.tranType, this.toAccount, this.amount);
        toAccount.debit(this.seqNo, this.tranType, this.fromAccount, this.amount);
        return "success";
    }

    /**
     * 修改交易状态，外部谨慎调用
     * @param status
     */
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void updateStatus(String status) {
        tranInfoTab.setStatus(status);
        tranInfoTabMapper.updateBySeqNo(tranInfoTab);
    }

    private ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
package com.qihoo.smallbank.transcation.model;

import com.qihoo.smallbank.database.entity.AccountTab;
import com.qihoo.smallbank.database.entity.TranHistoryTab;
import com.qihoo.smallbank.database.mapper.AccountTabMapper;
import com.qihoo.smallbank.database.mapper.TranHistoryTabMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@Scope("prototype")
public class Account {

    private AccountTab accountTab;
    private String accountCode;
    private boolean locked = false;

    @Autowired
    private AccountTabMapper accountTabMapper;

    @Autowired
    private TranHistoryTabMapper tranHistoryTabMapper;

    public void init(String accountCode) {
        this.accountCode = accountCode;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public boolean lockForUpdate() {
        accountTab = accountTabMapper.findByAccountCodeForUpdate(accountCode);
        if (accountTab == null) {
            locked = false;
            return false;
        }
        locked = true;
        return true;
    }

    // 借记操作
    @Transactional
    public void debit(String seqNo, String tranType, Account otherAccount, BigDecimal amount) {
        if (!locked) {
            throw new RuntimeException("Account is not locked");
        }
        String latestSeqNo = accountTab.getLatestSeqNo();
        accountTab.setLatestSeqNo(seqNo);
        accountTab.setPreBalance(accountTab.getCurrentBalance());
        accountTab.setCurrentBalance(accountTab.getCurrentBalance().subtract(amount));

        TranHistoryTab tranHistoryDebit = new TranHistoryTab();
        tranHistoryDebit.setFromAccount(this.accountTab.getAccountCode());
        tranHistoryDebit.setToAccount(otherAccount.getAccountCode());
        tranHistoryDebit.setAmount(amount);
        tranHistoryDebit.setTranDirection("D");
        tranHistoryDebit.setPreSeqNo(latestSeqNo);
        tranHistoryDebit.setStatus("S");
        tranHistoryDebit.setTranType(tranType);
        tranHistoryDebit.setSeqNo(seqNo);

        accountTabMapper.updateCheckVersion(accountTab);
        tranHistoryTabMapper.insert(tranHistoryDebit);
    }

    // 贷记操作
    @Transactional
    public void credit(String seqNo, String tranType, Account otherAccount, BigDecimal amount) {
        if (!locked) {
            throw new RuntimeException("Account is not locked");
        }

        String latestSeqNo = this.accountTab.getLatestSeqNo();
        this.accountTab.setLatestSeqNo(seqNo);
        this.accountTab.setPreBalance(this.accountTab.getCurrentBalance());
        this.accountTab.setCurrentBalance(this.accountTab.getCurrentBalance().add(amount));

        TranHistoryTab tranHistoryCredit = new TranHistoryTab();
        tranHistoryCredit.setFromAccount(this.getAccountCode());
        tranHistoryCredit.setToAccount(otherAccount.getAccountCode());
        tranHistoryCredit.setAmount(amount);
        tranHistoryCredit.setTranDirection("C");
        tranHistoryCredit.setPreSeqNo(latestSeqNo);
        tranHistoryCredit.setStatus("S");
        tranHistoryCredit.setSeqNo(seqNo);
        tranHistoryCredit.setTranType(tranType);

        accountTabMapper.updateCheckVersion(accountTab);
        tranHistoryTabMapper.insert(tranHistoryCredit);
    }

    public boolean isLocked() {
        return locked;
    }
}

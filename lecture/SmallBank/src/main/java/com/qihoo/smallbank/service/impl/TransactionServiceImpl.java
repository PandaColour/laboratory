package com.qihoo.smallbank.service.impl;

import com.qihoo.smallbank.database.entity.AccountTab;
import com.qihoo.smallbank.database.entity.TranHistoryTab;
import com.qihoo.smallbank.database.mapper.AccountTabMapper;
import com.qihoo.smallbank.database.mapper.TranHistoryTabMapper;
import com.qihoo.smallbank.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    AccountTabMapper accountTabMapper;

    @Autowired
    TranHistoryTabMapper tranHistoryTabMapper;

    @Override
    @Transactional
    public void transfer(String tranType, String seqNo, String fromAccountCode, String toAccountCode, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        AccountTab fromAccount, toAccount;
        if (fromAccountCode.compareTo(toAccountCode) < 0) {
            // 获取并加锁 fromAccount 和 toAccount
            fromAccount = accountTabMapper.findByAccountCodeForUpdate(fromAccountCode);
            toAccount = accountTabMapper.findByAccountCodeForUpdate(toAccountCode);
        } else {
            toAccount = accountTabMapper.findByAccountCodeForUpdate(toAccountCode);
            fromAccount = accountTabMapper.findByAccountCodeForUpdate(fromAccountCode);

        }

        // 检查 fromAccount 余额是否足够
        if (fromAccount.getCurrentBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // 执行转账
        String fromLastestSeqNo = fromAccount.getLatestSeqNo();
        fromAccount.setLatestSeqNo(seqNo);
        fromAccount.setPreBalance(fromAccount.getCurrentBalance());
        fromAccount.setCurrentBalance(fromAccount.getCurrentBalance().subtract(amount));

        String toLastestSeqNo = toAccount.getLatestSeqNo();
        toAccount.setLatestSeqNo(seqNo);
        toAccount.setPreBalance(toAccount.getCurrentBalance());
        toAccount.setCurrentBalance(toAccount.getCurrentBalance().add(amount));

        TranHistoryTab tranHistoryDebit = new TranHistoryTab();
        tranHistoryDebit.setFromAccount(fromAccountCode);
        tranHistoryDebit.setToAccount(toAccountCode);
        tranHistoryDebit.setAmount(amount);
        tranHistoryDebit.setTranDirection("D");
        tranHistoryDebit.setPreSeqNo(fromLastestSeqNo);
        tranHistoryDebit.setStatus("S");
        tranHistoryDebit.setTranType(tranType);
        tranHistoryDebit.setSeqNo(seqNo);

        TranHistoryTab tranHistoryCredit = new TranHistoryTab();
        tranHistoryCredit.setFromAccount(toAccountCode);
        tranHistoryCredit.setToAccount(fromAccountCode);
        tranHistoryCredit.setAmount(amount);
        tranHistoryCredit.setTranDirection("C");
        tranHistoryCredit.setPreSeqNo(toLastestSeqNo);
        tranHistoryCredit.setStatus("S");
        tranHistoryCredit.setSeqNo(seqNo);
        tranHistoryCredit.setTranType(tranType);

        accountTabMapper.updateCheckVersion(fromAccount);
        tranHistoryTabMapper.insert(tranHistoryDebit);
        accountTabMapper.updateCheckVersion(toAccount);
        tranHistoryTabMapper.insert(tranHistoryCredit);
    }
}
package com.qihoo.smallbank.service.impl;

import com.qihoo.smallbank.database.entity.AccountTab;
import com.qihoo.smallbank.database.mapper.AccountTabMapper;
import com.qihoo.smallbank.database.mapper.TranHistoryTabMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NormalTradeServiceImplTest {

    @Mock
    private AccountTabMapper accountTabMapper;

    @Mock
    private TranHistoryTabMapper tranHistoryTabMapper;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private AccountTab fromAccount;
    private AccountTab toAccount;

    @Before
    public void setUp() {
        // 设置测试数据
        fromAccount = new AccountTab();
        fromAccount.setAccountCode("ACC001");
        fromAccount.setCurrentBalance(new BigDecimal("1000.00"));
        fromAccount.setLatestSeqNo("SEQ001");

        toAccount = new AccountTab();
        toAccount.setAccountCode("ACC002");
        toAccount.setCurrentBalance(new BigDecimal("500.00"));
        toAccount.setLatestSeqNo("SEQ002");
    }

    @Test
    public void testTransferSuccess() {
        // 准备测试数据
        String tranType = "TRANSFER";
        String seqNo = "SEQ003";
        String fromAccountCode = "ACC001";
        String toAccountCode = "ACC002";
        BigDecimal amount = new BigDecimal("100.00");

        // Mock方法调用
        when(accountTabMapper.findByAccountCodeForUpdate(fromAccountCode)).thenReturn(fromAccount);
        when(accountTabMapper.findByAccountCodeForUpdate(toAccountCode)).thenReturn(toAccount);

        // 执行测试
        transactionService.transfer(tranType, seqNo, fromAccountCode, toAccountCode, amount);

        // 验证结果
        verify(accountTabMapper, times(2)).updateCheckVersion(any(AccountTab.class));
        verify(tranHistoryTabMapper, times(2)).insert(any());

        ArgumentCaptor<AccountTab> accountCaptor = ArgumentCaptor.forClass(AccountTab.class);
        verify(accountTabMapper, times(2)).updateCheckVersion(accountCaptor.capture());
        
        List<AccountTab> capturedAccounts = accountCaptor.getAllValues();
        
        assertEquals(new BigDecimal("900.00"), capturedAccounts.get(0).getCurrentBalance());
        assertEquals(fromAccountCode, capturedAccounts.get(0).getAccountCode());
        
        assertEquals(new BigDecimal("600.00"), capturedAccounts.get(1).getCurrentBalance());
        assertEquals(toAccountCode, capturedAccounts.get(1).getAccountCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransferWithNegativeAmount() {
        transactionService.transfer("TRANSFER", "SEQ003", "ACC001", "ACC002", new BigDecimal("-100.00"));
    }

    @Test(expected = RuntimeException.class)
    public void testTransferWithInsufficientBalance() {
        // 准备测试数据
        String tranType = "TRANSFER";
        String seqNo = "SEQ003";
        String fromAccountCode = "ACC001";
        String toAccountCode = "ACC002";
        BigDecimal amount = new BigDecimal("2000.00"); // 大于账户余额

        when(accountTabMapper.findByAccountCodeForUpdate(fromAccountCode)).thenReturn(fromAccount);
        when(accountTabMapper.findByAccountCodeForUpdate(toAccountCode)).thenReturn(toAccount);

        transactionService.transfer(tranType, seqNo, fromAccountCode, toAccountCode, amount);
    }

    @Test
    public void testTransferWithDifferentAccountOrder() {
        // 准备测试数据
        String tranType = "TRANSFER";
        String seqNo = "SEQ003";
        String fromAccountCode = "ACC002"; // 账号顺序相反
        String toAccountCode = "ACC001";
        BigDecimal amount = new BigDecimal("100.00");

        // Mock方法调用
        when(accountTabMapper.findByAccountCodeForUpdate(anyString())).thenAnswer(invocation -> {
            String accountCode = invocation.getArgument(0);
            return accountCode.equals("ACC001") ? toAccount : fromAccount;
        });

        // 执行测试
        transactionService.transfer(tranType, seqNo, fromAccountCode, toAccountCode, amount);

        // 验证结果
        verify(accountTabMapper, times(2)).updateCheckVersion(any(AccountTab.class));
        verify(tranHistoryTabMapper, times(2)).insert(any());
    }
} 
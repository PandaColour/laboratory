package com.qihoo.smallbank.transcation.model;

import com.qihoo.smallbank.database.entity.AccountTab;
import com.qihoo.smallbank.database.entity.TranHistoryTab;
import com.qihoo.smallbank.database.mapper.AccountTabMapper;
import com.qihoo.smallbank.database.mapper.TranHistoryTabMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class AccountTest {

    @Mock
    private AccountTabMapper accountTabMapper;

    @Mock
    private TranHistoryTabMapper tranHistoryTabMapper;

    @Mock
    private Account otherAccount;

    @InjectMocks
    private Account account;

    private static final String ACCOUNT_CODE = "ACC001";
    private static final String OTHER_ACCOUNT_CODE = "ACC002";
    private static final String SEQ_NO = "TEST123";
    private static final String TRAN_TYPE = "TRANSFER";
    private static final BigDecimal AMOUNT = new BigDecimal("100.00");
    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("1000.00");

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        account.init(ACCOUNT_CODE);
        when(otherAccount.getAccountCode()).thenReturn(OTHER_ACCOUNT_CODE);
    }

    @Test
    public void testInit() {
        // Act
        account.init(ACCOUNT_CODE);

        // Assert
        assertEquals(ACCOUNT_CODE, account.getAccountCode());
    }

    @Test
    public void testLockForUpdate_Success() {
        // Arrange
        AccountTab accountTab = new AccountTab();
        accountTab.setAccountCode(ACCOUNT_CODE);
        accountTab.setCurrentBalance(INITIAL_BALANCE);
        when(accountTabMapper.findByAccountCodeForUpdate(ACCOUNT_CODE)).thenReturn(accountTab);

        // Act
        boolean result = account.lockForUpdate();

        // Assert
        assertTrue(result);
        assertTrue(account.isLocked());
        verify(accountTabMapper).findByAccountCodeForUpdate(ACCOUNT_CODE);
    }

    @Test
    public void testLockForUpdate_Failure() {
        // Arrange
        when(accountTabMapper.findByAccountCodeForUpdate(ACCOUNT_CODE)).thenReturn(null);

        // Act
        boolean result = account.lockForUpdate();

        // Assert
        assertFalse(result);
        assertFalse(account.isLocked());
        verify(accountTabMapper).findByAccountCodeForUpdate(ACCOUNT_CODE);
    }

    @Test
    public void testDebit_Success() {
        // Arrange
        AccountTab accountTab = new AccountTab();
        accountTab.setAccountCode(ACCOUNT_CODE);
        accountTab.setCurrentBalance(INITIAL_BALANCE);
        accountTab.setLatestSeqNo("PREV123");
        when(accountTabMapper.findByAccountCodeForUpdate(ACCOUNT_CODE)).thenReturn(accountTab);
        account.lockForUpdate();

        // Act
        account.debit(SEQ_NO, TRAN_TYPE, otherAccount, AMOUNT);

        // Assert
        verify(accountTabMapper).updateCheckVersion(argThat(tab -> 
            tab.getAccountCode().equals(ACCOUNT_CODE) &&
            tab.getCurrentBalance().equals(INITIAL_BALANCE.subtract(AMOUNT)) &&
            tab.getPreBalance().equals(INITIAL_BALANCE) &&
            tab.getLatestSeqNo().equals(SEQ_NO)
        ));
        verify(tranHistoryTabMapper).insert(argThat(history -> 
            history.getFromAccount().equals(ACCOUNT_CODE) &&
            history.getToAccount().equals(OTHER_ACCOUNT_CODE) &&
            history.getAmount().equals(AMOUNT) &&
            history.getTranDirection().equals("D") &&
            history.getPreSeqNo().equals("PREV123") &&
            history.getStatus().equals("S") &&
            history.getTranType().equals(TRAN_TYPE) &&
            history.getSeqNo().equals(SEQ_NO)
        ));
    }

    @Test(expected = RuntimeException.class)
    public void testDebit_Failure_NotLocked() {
        // Act
        account.debit(SEQ_NO, TRAN_TYPE, otherAccount, AMOUNT);
    }

    @Test
    public void testCredit_Success() {
        // Arrange
        AccountTab accountTab = new AccountTab();
        accountTab.setAccountCode(ACCOUNT_CODE);
        accountTab.setCurrentBalance(INITIAL_BALANCE);
        accountTab.setLatestSeqNo("PREV123");
        when(accountTabMapper.findByAccountCodeForUpdate(ACCOUNT_CODE)).thenReturn(accountTab);
        account.lockForUpdate();

        // Act
        account.credit(SEQ_NO, TRAN_TYPE, otherAccount, AMOUNT);

        // Assert
        verify(accountTabMapper).updateCheckVersion(argThat(tab -> 
            tab.getAccountCode().equals(ACCOUNT_CODE) &&
            tab.getCurrentBalance().equals(INITIAL_BALANCE.add(AMOUNT)) &&
            tab.getPreBalance().equals(INITIAL_BALANCE) &&
            tab.getLatestSeqNo().equals(SEQ_NO)
        ));
        verify(tranHistoryTabMapper).insert(argThat(history -> 
            history.getFromAccount().equals(ACCOUNT_CODE) &&
            history.getToAccount().equals(OTHER_ACCOUNT_CODE) &&
            history.getAmount().equals(AMOUNT) &&
            history.getTranDirection().equals("C") &&
            history.getPreSeqNo().equals("PREV123") &&
            history.getStatus().equals("S") &&
            history.getTranType().equals(TRAN_TYPE) &&
            history.getSeqNo().equals(SEQ_NO)
        ));
    }

    @Test(expected = RuntimeException.class)
    public void testCredit_Failure_NotLocked() {
        // Act
        account.credit(SEQ_NO, TRAN_TYPE, otherAccount, AMOUNT);
    }

    @Test
    public void testIsLocked() {
        // Arrange
        AccountTab accountTab = new AccountTab();
        when(accountTabMapper.findByAccountCodeForUpdate(ACCOUNT_CODE)).thenReturn(accountTab);

        // Act & Assert
        assertFalse(account.isLocked());
        account.lockForUpdate();
        assertTrue(account.isLocked());
    }
}

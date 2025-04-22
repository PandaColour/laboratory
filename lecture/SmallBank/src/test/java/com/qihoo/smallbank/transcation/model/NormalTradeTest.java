package com.qihoo.smallbank.transcation.model;

import com.qihoo.smallbank.database.entity.TranInfoTab;
import com.qihoo.smallbank.database.mapper.TranInfoTabMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class NormalTradeTest {

    @Mock
    private TranInfoTabMapper tranInfoTabMapper;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private Account fromAccount;

    @Mock
    private Account toAccount;

    @InjectMocks
    private NormalTrade normalTrade;

    private static final String SEQ_NO = "TEST123";
    private static final String TRAN_TYPE = "TRANSFER";
    private static final String FROM_ACCOUNT_CODE = "ACC001";
    private static final String TO_ACCOUNT_CODE = "ACC002";
    private static final BigDecimal AMOUNT = new BigDecimal("100.00");
    private static final BigDecimal ZERO_AMOUNT = BigDecimal.ZERO;
    private static final BigDecimal NEGATIVE_AMOUNT = new BigDecimal("-100.00");

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(applicationContext.getBean(Account.class)).thenReturn(fromAccount, toAccount);
        when(fromAccount.getAccountCode()).thenReturn(FROM_ACCOUNT_CODE);
        when(toAccount.getAccountCode()).thenReturn(TO_ACCOUNT_CODE);
    }

    // 测试初始化方法
    @Test
    public void testInit_Success() {
        // Arrange
        when(tranInfoTabMapper.findBySeqNo(anyString())).thenReturn(null);

        // Act
        Boolean result = normalTrade.init(SEQ_NO, TRAN_TYPE, FROM_ACCOUNT_CODE, TO_ACCOUNT_CODE, AMOUNT);

        // Assert
        assertTrue(result);
        verify(tranInfoTabMapper).insert(any(TranInfoTab.class));
        verify(applicationContext, times(2)).getBean(Account.class);
        verify(fromAccount).init(FROM_ACCOUNT_CODE);
        verify(toAccount).init(TO_ACCOUNT_CODE);
    }

    @Test
    public void testInit_Failure_ExistingTransaction() {
        // Arrange
        TranInfoTab existingTran = new TranInfoTab();
        when(tranInfoTabMapper.findBySeqNo(anyString())).thenReturn(existingTran);

        // Act
        Boolean result = normalTrade.init(SEQ_NO, TRAN_TYPE, FROM_ACCOUNT_CODE, TO_ACCOUNT_CODE, AMOUNT);

        // Assert
        assertFalse(result);
        verify(tranInfoTabMapper, never()).insert(any(TranInfoTab.class));
        verify(applicationContext, never()).getBean(Account.class);
    }

    @Test
    public void testInit_Failure_NullParameters() {
        // Act & Assert
        assertFalse(normalTrade.init(SEQ_NO, null, FROM_ACCOUNT_CODE, TO_ACCOUNT_CODE, AMOUNT));
        assertFalse(normalTrade.init(SEQ_NO, TRAN_TYPE, null, TO_ACCOUNT_CODE, AMOUNT));
        assertFalse(normalTrade.init(SEQ_NO, TRAN_TYPE, FROM_ACCOUNT_CODE, null, AMOUNT));
        assertFalse(normalTrade.init(SEQ_NO, TRAN_TYPE, FROM_ACCOUNT_CODE, TO_ACCOUNT_CODE, null));
        verify(applicationContext, never()).getBean(Account.class);

        assertTrue(normalTrade.init(null, TRAN_TYPE, FROM_ACCOUNT_CODE, TO_ACCOUNT_CODE, AMOUNT));
    }

    @Test
    public void testInit_Failure_InvalidAmount() {
        // Arrange
        when(tranInfoTabMapper.findBySeqNo(anyString())).thenReturn(null);

        // Act & Assert
        assertFalse(normalTrade.init(SEQ_NO, TRAN_TYPE, FROM_ACCOUNT_CODE, TO_ACCOUNT_CODE, ZERO_AMOUNT));
        assertFalse(normalTrade.init(SEQ_NO, TRAN_TYPE, FROM_ACCOUNT_CODE, TO_ACCOUNT_CODE, NEGATIVE_AMOUNT));
        verify(applicationContext, never()).getBean(Account.class);
        verify(tranInfoTabMapper, never()).insert(any(TranInfoTab.class));
    }

    // 测试交易执行方法
    @Test
    public void testDoTransaction_Success() {
        // Arrange
        when(tranInfoTabMapper.findBySeqNo(anyString())).thenReturn(null);
        when(fromAccount.lockForUpdate()).thenReturn(true);
        when(toAccount.lockForUpdate()).thenReturn(true);

        normalTrade.init(SEQ_NO, TRAN_TYPE, FROM_ACCOUNT_CODE, TO_ACCOUNT_CODE, AMOUNT);

        // Act
        String result = normalTrade.doTransaction();

        // Assert
        assertEquals("success", result);
        verify(tranInfoTabMapper, times(2)).updateBySeqNo(any(TranInfoTab.class));
        verify(fromAccount).credit(eq(SEQ_NO), eq(TRAN_TYPE), eq(toAccount), eq(AMOUNT));
        verify(toAccount).debit(eq(SEQ_NO), eq(TRAN_TYPE), eq(fromAccount), eq(AMOUNT));
    }

    @Test
    public void testDoTransaction_Failure_LockFailed() {
        // Arrange
        when(tranInfoTabMapper.findBySeqNo(anyString())).thenReturn(null);
        when(fromAccount.lockForUpdate()).thenReturn(false);

        normalTrade.init(SEQ_NO, TRAN_TYPE, FROM_ACCOUNT_CODE, TO_ACCOUNT_CODE, AMOUNT);

        // Act
        String result = normalTrade.doTransaction();

        // Assert
        assertEquals("failed", result);
        verify(tranInfoTabMapper, times(2)).updateBySeqNo(any(TranInfoTab.class));
        verify(fromAccount, never()).credit(anyString(), anyString(), any(Account.class), any(BigDecimal.class));
        verify(toAccount, never()).debit(anyString(), anyString(), any(Account.class), any(BigDecimal.class));
    }

    @Test(expected = Exception.class)
    public void testDoTransaction_Exception() {
        // Arrange
        when(tranInfoTabMapper.findBySeqNo(anyString())).thenReturn(null);
        when(fromAccount.lockForUpdate()).thenReturn(true);
        when(toAccount.lockForUpdate()).thenReturn(true);
        doThrow(new RuntimeException("Test exception")).when(fromAccount).credit(anyString(), anyString(), any(Account.class), any(BigDecimal.class));

        normalTrade.init(SEQ_NO, TRAN_TYPE, FROM_ACCOUNT_CODE, TO_ACCOUNT_CODE, AMOUNT);

        // Act
        normalTrade.doTransaction();
    }

    @Test
    public void testDoTransaction_AccountOrder() {
        // Arrange
        when(tranInfoTabMapper.findBySeqNo(anyString())).thenReturn(null);
        when(fromAccount.lockForUpdate()).thenReturn(true);
        when(toAccount.lockForUpdate()).thenReturn(true);

        // 设置账户顺序为 TO_ACCOUNT_CODE < FROM_ACCOUNT_CODE
        when(fromAccount.getAccountCode()).thenReturn("B");
        when(toAccount.getAccountCode()).thenReturn("A");

        normalTrade.init(SEQ_NO, TRAN_TYPE, "B", "A", AMOUNT);

        // Act
        String result = normalTrade.doTransaction();

        // Assert
        assertEquals("success", result);
        verify(toAccount).lockForUpdate();
        verify(fromAccount).lockForUpdate();
    }

    // 测试状态更新方法
    @Test
    public void testUpdateStatus() {
        // Arrange
        TranInfoTab tranInfoTab = new TranInfoTab();
        tranInfoTab.setSeqNo(SEQ_NO);
        when(tranInfoTabMapper.findBySeqNo(anyString())).thenReturn(tranInfoTab);

        normalTrade.init(SEQ_NO, TRAN_TYPE, FROM_ACCOUNT_CODE, TO_ACCOUNT_CODE, AMOUNT);

        // Act
        normalTrade.updateStatus("PROCESSING");

        // Assert
        verify(tranInfoTabMapper).updateBySeqNo(argThat(tab -> 
            tab.getSeqNo().equals(SEQ_NO) && 
            tab.getStatus().equals("PROCESSING")
        ));
    }
} 
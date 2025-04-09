package com.qihoo.smallbank.transcation;

import com.qihoo.smallbank.controller.request.TransferRequest;
import com.qihoo.smallbank.transcation.model.NormalTrade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.lang.reflect.InvocationTargetException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TradeEntryTest {

    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private TradeEntry tradeEntry;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void doTransaction_InvalidRequest_ReturnsValidationFailure() throws NoSuchMethodException, IllegalAccessException {
        TransferRequest request = new TransferRequest();
        request.setAmount(new BigDecimal("-1"));
        request.setFromAccountCode("123");
        request.setToAccountCode("456");
        request.setTranType("transfer");
        request.setSeqNo("12345");

        String result = tradeEntry.doTransaction(request);
        assertEquals("参数校验失败", result);
    }

    @Test
    public void doTransaction_ValidRequest_InitializationFails_ReturnsValidationFailure() {
        TransferRequest request = new TransferRequest();
        request.setAmount(BigDecimal.TEN);
        request.setFromAccountCode("123");
        request.setToAccountCode("456");
        request.setTranType("transfer");
        request.setSeqNo("12345");

        NormalTrade normalTrade = mock(NormalTrade.class);
        when(applicationContext.getBean(NormalTrade.class)).thenReturn(normalTrade);
        when(normalTrade.init(anyString(), anyString(), anyString(), anyString(), any(BigDecimal.class))).thenReturn(false);

        String result = tradeEntry.doTransaction(request);

        assertEquals("参数校验失败", result);
    }

    @Test
    public void doTransaction_ValidRequest_InitializationSucceeds_ReturnsSuccess() {
        TransferRequest request = new TransferRequest();
        request.setAmount(BigDecimal.TEN);
        request.setFromAccountCode("123");
        request.setToAccountCode("456");
        request.setTranType("transfer");
        request.setSeqNo("12345");

        NormalTrade normalTrade = mock(NormalTrade.class);
        when(applicationContext.getBean(NormalTrade.class)).thenReturn(normalTrade);
        when(normalTrade.init(anyString(), anyString(), anyString(), anyString(), any(BigDecimal.class))).thenReturn(true);

        String result = tradeEntry.doTransaction(request);

        assertEquals("success", result);
    }
}

package com.qihoo.smallbank.controller;

import com.qihoo.smallbank.controller.request.TransferRequest;
import com.qihoo.smallbank.transcation.TradeEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v2")
public class V2Controller {

    @Autowired
    private TradeEntry tradeEntry;

    @PostMapping("/transfer")
    public String transfer(@RequestBody TransferRequest request) {
        if (StringUtils.isEmpty(request.getSeqNo())) {
            request.setSeqNo(UUID.randomUUID().toString());
        }
        if (StringUtils.isEmpty(request.getTranType())
                || StringUtils.isEmpty(request.getFromAccountCode())
                || StringUtils.isEmpty(request.getToAccountCode())
                || Objects.isNull(request.getAmount())) {
            return "参数错误";
        }
        // 这里可以调用 TransactionService 的 transfer 方法
        return tradeEntry.doTransaction(request);
    }
}

package com.qihoo.smallbank.controller;

import com.qihoo.smallbank.controller.request.TransferRequest;
import com.qihoo.smallbank.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class V1Controller {

    @Autowired
    private TransactionService transactionService;

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
        try {
            transactionService.transfer(
                    request.getTranType(),
                    request.getSeqNo(),
                    request.getFromAccountCode(),
                    request.getToAccountCode(),
                    request.getAmount());
        } catch (Exception e) {
            return e.getMessage();
        }
        return "success";
    }
}
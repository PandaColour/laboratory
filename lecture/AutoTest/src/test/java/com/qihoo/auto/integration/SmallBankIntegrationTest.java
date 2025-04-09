package com.qihoo.auto.integration;

import com.qihoo.auto.TestApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    classes = TestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class SmallBankIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testV2TransferSuccess() throws Exception {
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("tranType", "MB001");
        requestBody.put("seqNo", "test-" + System.currentTimeMillis());
        requestBody.put("fromAccountCode", "660011003");
        requestBody.put("toAccountCode", "660011004");
        requestBody.put("amount", 1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        List<MediaType> accept = new ArrayList<>();
        accept.add(MediaType.APPLICATION_JSON);
        headers.setAccept(accept);

        // 将请求头和请求体封装到HttpEntity中
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

        // 发送POST请求
        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://127.0.0.1:8080/api/v2/transfer",
                entity,
                String.class
        );
        // 校验HTTP状态码
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // 校验响应体
        assertEquals("success", response.getBody());
    }


    @Test
    public void testV1TransferSuccess() throws Exception {
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("tranType", "MB001");
        requestBody.put("seqNo", "test-" + System.currentTimeMillis());
        requestBody.put("fromAccountCode", "660011001");
        requestBody.put("toAccountCode", "660011002");
        requestBody.put("amount", 1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        List<MediaType> accept = new ArrayList<>();
        accept.add(MediaType.APPLICATION_JSON);
        headers.setAccept(accept);

        // 将请求头和请求体封装到HttpEntity中
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

        // 发送POST请求
        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://127.0.0.1:8080/api/v1/transfer",
                entity,
                String.class
        );

        // 校验HTTP状态码
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 校验响应体
        assertEquals("success", response.getBody());
    }
}

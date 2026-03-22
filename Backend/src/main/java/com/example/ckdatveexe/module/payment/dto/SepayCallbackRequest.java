package com.example.ckdatveexe.module.payment.dto;

import lombok.Data;

@Data
public class SepayCallbackRequest {
    private String id;
    private String gateway;
    private String transactionDate;
    private String accountNumber;
    private String subAccount;
    private String amountIn;
    private String amountOut;
    private String accumulated;
    private String code;
    private String content;
    private String referenceCode;
    private String body;
}
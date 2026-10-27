package com.xzj.member.entity.dto;

import lombok.Data;

import java.io.Serializable;

/** Member-owned projection of SMS settings returned by message-service. */
@Data
public class SmsSetting implements Serializable {
    private String type;
    private Boolean isTestModel;
    private String loginTemplateCode;
    private String registerTemplateCode;
    private String findPasswordTemplateCode;
    private String walletPasswordTemplateCode;
    private String payPasswordTemplateCode;
    private String accessKeyId;
    private String accessSecret;
    private String signName;
    private String tencentSecretId;
    private String tencentSecretKey;
    private String tencentSdkAppId;
    private String tencentSignName;
    private String huaweiAppKey;
    private String huaweiAppSecret;
    private String huaweiSender;
    private String huaweiSignature;
}

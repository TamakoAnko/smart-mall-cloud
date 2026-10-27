package com.xzj.order.remote;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class GiftCardUsageRecordRemoteDTO implements Serializable {
    private String id;
    private String orderSn;
    private String tradeSn;
    private String memberId;
    private String credentialId;
    private String cardNo;
    private BigDecimal deductAmount;
    private BigDecimal changeAmount;
    private BigDecimal balanceAfter;
    private String flowType;
    private String bizType;
    private String bizSn;
    private Boolean deleteFlag;
    private Date createTime;
    private String memberName;
    private String memberMobile;
    private String giftCardName;
    private BigDecimal faceValue;
    private BigDecimal balance;
}

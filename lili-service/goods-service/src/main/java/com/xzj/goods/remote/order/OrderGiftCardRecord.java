package com.xzj.goods.remote.order;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OrderGiftCardRecord implements Serializable {
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
}

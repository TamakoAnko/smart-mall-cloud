package com.xzj.goods.remote.order;

import lombok.Data;
import java.io.Serializable;

@Data
public class CardKeyOrderDTO implements Serializable {
    private String orderSn;
    private String orderItemSn;
    private String memberId;
    private String skuId;
    private String orderType;
    private String orderStatus;
    private String payStatus;
}

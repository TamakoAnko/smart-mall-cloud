package com.xzj.goods.remote.order;

import com.xzj.goods.goods.entity.vo.CardKeyVO;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class GiftECouponOrderSummaryVO implements Serializable {
    private String orderSn;
    private String parentOrderSn;
    private String orderStatus;
    private String goodsName;
    private String skuId;
    private String cardKeyFulfillStatus;
    private String cardKeyFulfillMessage;
    private List<CardKeyVO> cardKeys;
    private Boolean cardKeyDelivered;
}

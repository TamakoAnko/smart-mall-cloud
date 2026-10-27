package com.xzj.goods.remote.order;

import lombok.Data;
import java.io.Serializable;

/** Order projection used by goods-service; the aggregate remains in order-service. */
@Data
public class Order implements Serializable {
    private String sn;
    private String memberId;
    private String storeId;
    private String orderType;
    private String orderPromotionType;
    private String parentOrderSn;
    private String orderStatus;
    private String payStatus;
    private String cancelReason;
    private Double flowPrice;
    private PriceDetailDTO priceDetailDTO;
    private String deductionMethod;
}

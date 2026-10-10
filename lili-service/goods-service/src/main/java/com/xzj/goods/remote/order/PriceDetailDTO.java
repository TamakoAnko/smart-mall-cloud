package com.xzj.goods.remote.order;

import lombok.Data;
import java.io.Serializable;

@Data
public class PriceDetailDTO implements Serializable {
    private Double goodsPrice;
    private Double flowPrice;
    private Double freightPrice;
    private Double giftCardPrice;
}

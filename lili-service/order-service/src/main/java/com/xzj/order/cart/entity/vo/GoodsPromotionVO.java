package com.xzj.order.cart.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/** Cart-facing promotion summary returned by goods-service. */
@Data
public class GoodsPromotionVO implements Serializable {
    private Date startTime;
    private Date endTime;
    private String promotionId;
    private String promotionType;
    private String title;
    private Integer limitNum;
}

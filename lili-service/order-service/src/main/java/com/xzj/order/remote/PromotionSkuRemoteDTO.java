package com.xzj.order.remote;

import lombok.Data;

import java.io.Serializable;

/** Promotion reference stored in an order snapshot. */
@Data
public class PromotionSkuRemoteDTO implements Serializable {
    private String promotionType;
    private String activityId;

    public PromotionSkuRemoteDTO() {
    }

    public PromotionSkuRemoteDTO(String promotionType, String activityId) {
        this.promotionType = promotionType;
        this.activityId = activityId;
    }
}

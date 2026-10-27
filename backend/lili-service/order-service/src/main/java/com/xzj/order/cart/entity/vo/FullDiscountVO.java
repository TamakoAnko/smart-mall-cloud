package com.xzj.order.cart.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** Cart-facing snapshot of a full-discount promotion. */
@Data
public class FullDiscountVO implements Serializable {
    private Double fullMoney;
    private Boolean fullMinusFlag;
    private Double fullMinus;
    private Boolean fullRateFlag;
    private Double fullRate;
    private Boolean pointFlag;
    private Integer point;
    private Boolean freeFreightFlag;
    private Boolean giftFlag;
    private String giftId;
    private Boolean couponFlag;
    private String couponId;
    private String title;
    private List<String> promotionGoodsIds;
    private String giftSkuId;
    private String giftSkuName;
    private String giftSkuThumbnail;

    public String notice() {
        StringBuilder result = new StringBuilder();
        if (Boolean.TRUE.equals(fullMinusFlag)) result.append(" 减").append(fullMinus).append("元 ");
        if (Boolean.TRUE.equals(fullRateFlag)) result.append(" 打").append(fullRate).append("折 ");
        if (Boolean.TRUE.equals(freeFreightFlag)) result.append(" 免运费 ");
        if (Boolean.TRUE.equals(pointFlag)) result.append(" 赠").append(point).append("积分 ");
        if (Boolean.TRUE.equals(couponFlag)) result.append(" 赠优惠券 ");
        if (Boolean.TRUE.equals(giftFlag) && giftSkuName != null) result.append(" 赠品[").append(giftSkuName).append("]");
        return result.toString();
    }
}

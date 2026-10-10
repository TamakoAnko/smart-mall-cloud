package com.xzj.promotion.promotion.entity.vos;

import cn.hutool.core.text.CharSequenceUtil;
import com.xzj.promotion.promotion.entity.dos.FullDiscount;
import com.xzj.promotion.promotion.entity.dos.PromotionGoods;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.List;

/**
 * 满优惠活动返回对象，使用 goods-service 内部的促销实体类型。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class FullDiscountVO extends FullDiscount {

    private static final long serialVersionUID = -2330552735874105354L;

    /** 促销关联的商品。 */
    private List<PromotionGoods> promotionGoodsList;

    /** 赠品 SKU 信息。 */
    private String giftSkuId;
    private String giftSkuName;
    private String giftSkuThumbnail;

    public FullDiscountVO(FullDiscount fullDiscount) {
        BeanUtils.copyProperties(fullDiscount, this);
    }

    public String notice() {
        StringBuilder message = new StringBuilder();
        if (Boolean.TRUE.equals(getFullMinusFlag())) {
            message.append(" 减").append(getFullMinus()).append("元 ");
        }
        if (Boolean.TRUE.equals(getFullRateFlag())) {
            message.append(" 打").append(getFullRate()).append("折 ");
        }
        if (Boolean.TRUE.equals(getFreeFreightFlag())) {
            message.append(" 免运费 ");
        }
        if (Boolean.TRUE.equals(getPointFlag())) {
            message.append(" 赠").append(getPoint()).append("积分 ");
        }
        if (Boolean.TRUE.equals(getCouponFlag())) {
            message.append(" 赠优惠券 ");
        }
        if (Boolean.TRUE.equals(getGiftFlag()) && CharSequenceUtil.isNotEmpty(giftSkuName)) {
            message.append(" 赠品[").append(giftSkuName).append("]");
        }
        return message.toString();
    }
}

package com.xzj.promotion.promotion.entity.vos;

import com.xzj.goods.remote.order.OrderGiftCardRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 礼品卡使用记录展示对象（含会员、活动、余额等扩展字段）
 *
 * @author Bulbasaur
 * @since 2026-05-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "礼品卡使用记录")
public class GiftCardUsageRecordVO extends OrderGiftCardRecord {

    @Schema(description = "客户名称")
    private String memberName;

    @Schema(description = "客户手机号")
    private String memberMobile;

    @Schema(description = "礼品卡名称")
    private String giftCardName;

    @Schema(description = "面额")
    private BigDecimal faceValue;

    @Schema(description = "当前余额")
    private BigDecimal balance;
}

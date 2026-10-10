package com.xzj.promotion.promotion.entity.dto;

import com.xzj.promotion.promotion.entity.dos.CouponActivity;
import com.xzj.promotion.promotion.entity.dos.CouponActivityItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 优惠券活动DTO
 *
 * @author Bulbasaur
 * @since 2021/5/21 7:16 下午
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CouponActivityDTO extends CouponActivity {

    @Schema(description = "优惠券列表")
    private List<CouponActivityItem> couponActivityItems;

    @Schema(description = "客户列表")
    private List<MemberDTO> memberDTOS;
}

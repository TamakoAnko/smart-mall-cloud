package com.xzj.promotion.promotion.entity.vos;

import com.xzj.promotion.promotion.entity.dos.PromotionGoods;
import lombok.Data;

import java.util.List;

/**
 * 拼图客户分享对象
 *
 * @author paulG
 * @since 2021/3/24
 **/
@Data
public class PintuanShareVO {

    private PromotionGoods promotionGoods;

    private List<PintuanMemberVO> pintuanMemberVOS;

}

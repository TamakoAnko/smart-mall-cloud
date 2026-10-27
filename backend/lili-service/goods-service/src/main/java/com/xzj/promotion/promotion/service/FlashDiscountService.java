package com.xzj.promotion.promotion.service;

import com.xzj.promotion.promotion.entity.dos.FlashDiscount;
import com.xzj.promotion.promotion.entity.vos.FlashDiscountVO;

public interface FlashDiscountService extends AbstractPromotionsService<FlashDiscount> {

    FlashDiscountVO getFlashDiscountVO(String id);
}

package com.xzj.promotion.promotion.service;

import com.xzj.promotion.promotion.entity.dos.NthItemDiscount;
import com.xzj.promotion.promotion.entity.vos.NthItemDiscountVO;

public interface NthItemDiscountService extends AbstractPromotionsService<NthItemDiscount> {

    NthItemDiscountVO getNthItemDiscountVO(String id);
}

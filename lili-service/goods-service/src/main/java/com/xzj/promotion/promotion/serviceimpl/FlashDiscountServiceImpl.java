package com.xzj.promotion.promotion.serviceimpl;

import cn.lili.common.enums.PromotionTypeEnum;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import com.xzj.promotion.promotion.entity.dos.FlashDiscount;
import com.xzj.promotion.promotion.entity.dto.search.PromotionGoodsSearchParams;
import com.xzj.promotion.promotion.entity.enums.PromotionsScopeTypeEnum;
import com.xzj.promotion.promotion.entity.enums.PromotionsStatusEnum;
import com.xzj.promotion.promotion.entity.vos.FlashDiscountVO;
import com.xzj.promotion.promotion.mapper.FlashDiscountMapper;
import com.xzj.promotion.promotion.service.FlashDiscountService;
import com.xzj.promotion.promotion.service.PromotionGoodsService;
import com.xzj.promotion.promotion.tools.PromotionTools;
import com.xzj.promotion.promotion.tools.SkuPromotionGoodsSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("goodsPromotionFlashDiscountServiceImpl")
public class FlashDiscountServiceImpl extends AbstractPromotionsServiceImpl<FlashDiscountMapper, FlashDiscount>
        implements FlashDiscountService {

    @Autowired
    private PromotionGoodsService promotionGoodsService;
    @Autowired
    private SkuPromotionGoodsSupport skuPromotionGoodsSupport;

    @Override
    public FlashDiscountVO getFlashDiscountVO(String id) {
        FlashDiscount flashDiscount = this.getById(id);
        if (flashDiscount == null) {
            throw new ServiceException(ResultCode.FLASH_DISCOUNT_NOT_EXIST_ERROR);
        }
        FlashDiscountVO vo = new FlashDiscountVO(flashDiscount);
        PromotionGoodsSearchParams searchParams = new PromotionGoodsSearchParams();
        searchParams.setPromotionId(flashDiscount.getId());
        vo.setPromotionGoodsList(promotionGoodsService.listFindAll(searchParams));
        return vo;
    }

    @Override
    public void checkPromotions(FlashDiscount promotions) {
        super.checkPromotions(promotions);
        if (promotions instanceof FlashDiscountVO) {
            PromotionTools.checkPromotionTime(promotions.getStartTime(), promotions.getEndTime());
        }
    }

    @Override
    public boolean updatePromotionsGoods(FlashDiscount promotions) {
        boolean result = super.updatePromotionsGoods(promotions);
        if (!PromotionsStatusEnum.CLOSE.name().equals(promotions.getPromotionStatus())
                && PromotionsScopeTypeEnum.PORTION_GOODS.name().equals(promotions.getScopeType())
                && promotions instanceof FlashDiscountVO vo) {
            skuPromotionGoodsSupport.replacePromotionGoods(vo, vo.getPromotionGoodsList(), getPromotionType());
        }
        return result;
    }

    @Override
    public PromotionTypeEnum getPromotionType() {
        return PromotionTypeEnum.FLASH_DISCOUNT;
    }
}

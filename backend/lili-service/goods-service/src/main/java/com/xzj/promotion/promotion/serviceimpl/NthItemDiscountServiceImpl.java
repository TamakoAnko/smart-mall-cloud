package com.xzj.promotion.promotion.serviceimpl;

import cn.lili.common.enums.PromotionTypeEnum;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import com.xzj.promotion.promotion.entity.dos.NthItemDiscount;
import com.xzj.promotion.promotion.entity.dto.search.PromotionGoodsSearchParams;
import com.xzj.promotion.promotion.entity.enums.PromotionsScopeTypeEnum;
import com.xzj.promotion.promotion.entity.enums.PromotionsStatusEnum;
import com.xzj.promotion.promotion.entity.vos.NthItemDiscountVO;
import com.xzj.promotion.promotion.mapper.NthItemDiscountMapper;
import com.xzj.promotion.promotion.service.NthItemDiscountService;
import com.xzj.promotion.promotion.service.PromotionGoodsService;
import com.xzj.promotion.promotion.tools.PromotionTools;
import com.xzj.promotion.promotion.tools.SkuPromotionGoodsSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("goodsPromotionNthItemDiscountServiceImpl")
public class NthItemDiscountServiceImpl extends AbstractPromotionsServiceImpl<NthItemDiscountMapper, NthItemDiscount>
        implements NthItemDiscountService {

    @Autowired
    private PromotionGoodsService promotionGoodsService;
    @Autowired
    private SkuPromotionGoodsSupport skuPromotionGoodsSupport;

    @Override
    public NthItemDiscountVO getNthItemDiscountVO(String id) {
        NthItemDiscount discount = this.getById(id);
        if (discount == null) {
            throw new ServiceException(ResultCode.NTH_ITEM_DISCOUNT_NOT_EXIST_ERROR);
        }
        NthItemDiscountVO vo = new NthItemDiscountVO(discount);
        PromotionGoodsSearchParams searchParams = new PromotionGoodsSearchParams();
        searchParams.setPromotionId(discount.getId());
        vo.setPromotionGoodsList(promotionGoodsService.listFindAll(searchParams));
        return vo;
    }

    @Override
    public void checkPromotions(NthItemDiscount promotions) {
        super.checkPromotions(promotions);
        if (promotions.getNthNum() == null || promotions.getNthNum() < 2) {
            throw new ServiceException(ResultCode.PARAMS_ERROR, "第N件须大于等于2");
        }
        if (promotions instanceof NthItemDiscountVO) {
            PromotionTools.checkPromotionTime(promotions.getStartTime(), promotions.getEndTime());
        }
    }

    @Override
    public boolean updatePromotionsGoods(NthItemDiscount promotions) {
        boolean result = super.updatePromotionsGoods(promotions);
        if (!PromotionsStatusEnum.CLOSE.name().equals(promotions.getPromotionStatus())
                && PromotionsScopeTypeEnum.PORTION_GOODS.name().equals(promotions.getScopeType())
                && promotions instanceof NthItemDiscountVO vo) {
            skuPromotionGoodsSupport.replacePromotionGoods(vo, vo.getPromotionGoodsList(), getPromotionType());
        }
        return result;
    }

    @Override
    public PromotionTypeEnum getPromotionType() {
        return PromotionTypeEnum.NTH_ITEM_DISCOUNT;
    }
}

package com.xzj.promotion.promotion.tools;

import cn.lili.common.enums.PromotionTypeEnum;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import com.xzj.goods.goods.service.GoodsSkuService;
import com.xzj.promotion.promotion.entity.dos.BasePromotions;
import com.xzj.promotion.promotion.entity.dos.PromotionGoods;
import com.xzj.promotion.promotion.entity.dto.search.PromotionGoodsSearchParams;
import com.xzj.promotion.promotion.service.PromotionConflictService;
import com.xzj.promotion.promotion.service.PromotionGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * SKU 级促销活动商品保存辅助
 */
@Component("goodsPromotionSkuPromotionGoodsSupport")
public class SkuPromotionGoodsSupport {

    @Autowired
    private PromotionGoodsService promotionGoodsService;
    @Autowired
    private GoodsSkuService goodsSkuService;
    @Autowired
    private PromotionConflictService promotionConflictService;

    /**
     * 保存指定 SKU 促销商品，含互斥校验
     */
    public void replacePromotionGoods(BasePromotions promotion, List<PromotionGoods> promotionGoodsList,
                                      PromotionTypeEnum promotionType) {
        if (promotionGoodsList == null || promotionGoodsList.isEmpty()) {
            return;
        }
        List<PromotionGoods> promotionGoods = PromotionTools.promotionGoodsInit(
                promotionGoodsList, promotion, promotionType);
        for (PromotionGoods promotionGood : promotionGoods) {
            if (goodsSkuService.getCanPromotionGoodsSkuByIdFromCache(promotionGood.getSkuId()) == null) {
                throw new ServiceException("商品[" + promotionGood.getGoodsName() + "]不存在或处于不可售卖状态");
            }
            if (promotionGood.getPrice() != null && promotionGood.getOriginalPrice() != null
                    && promotionGood.getPrice() > promotionGood.getOriginalPrice()) {
                throw new ServiceException(ResultCode.PROMOTION_PRICE_ERROR);
            }
            promotionConflictService.checkSkuExclusiveConflict(
                    promotionType, promotionGood.getSkuId(), promotion.getStartTime(), promotion.getEndTime(),
                    promotion.getId(), promotionGood.getGoodsName());
        }
        PromotionGoodsSearchParams searchParams = new PromotionGoodsSearchParams();
        searchParams.setPromotionId(promotion.getId());
        searchParams.setPromotionType(promotionType.name());
        promotionGoodsService.deletePromotionGoods(searchParams);
        promotionGoodsService.saveOrUpdateBatch(promotionGoods);
    }
}

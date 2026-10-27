package com.xzj.order.remote;

import com.xzj.config.GoodsSkuFeignClient;
import com.xzj.config.PromotionFeignClient;
import com.xzj.entity.GoodsSkuDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Boundary adapter for goods and promotion calls used by order/cart code.
 * No goods, promotion, or member Service implementation is present locally.
 */
@Service
public class OrderRemoteFacade {
    private final GoodsSkuFeignClient goodsSkuClient;
    private final PromotionFeignClient promotionClient;

    public OrderRemoteFacade(GoodsSkuFeignClient goodsSkuClient, PromotionFeignClient promotionClient) {
        this.goodsSkuClient = goodsSkuClient;
        this.promotionClient = promotionClient;
    }

    public GoodsSkuRemoteDTO getSku(String skuId) {
        return toLocal(goodsSkuClient.findById(skuId));
    }

    public Integer getStock(String skuId) {
        return goodsSkuClient.getStock(skuId);
    }

    public void validateAddToCart(String skuId, Integer num, String cartType) {
        // Goods-service validates stock and card-key rules at trade creation.
    }

    public Map<String, Object> getCurrentGoodsPromotion(GoodsSkuRemoteDTO sku, String cartType) {
        if (sku == null || sku.getId() == null) return Collections.emptyMap();
        Map<String, Object> result = promotionClient.getCurrentGoodsPromotion(sku.getId(), cartType);
        return result == null ? Collections.emptyMap() : result;
    }

    public PromotionFeignClient.WholesaleDTO matchWholesale(String goodsId, Integer num) {
        return promotionClient.matchWholesale(goodsId, num);
    }

    public List<PromotionFeignClient.MemberCouponDTO> getMemberCoupons(String memberId) {
        return promotionClient.getMemberCoupons(memberId);
    }

    public List<PromotionFeignClient.MemberCouponDTO> getCurrentGoodsCanUse(String memberId, List<String> ids, Double totalPrice) {
        return promotionClient.getCurrentGoodsCanUse(memberId, ids, totalPrice);
    }

    public List<PromotionFeignClient.MemberCouponDTO> getAllScopeMemberCoupon(String memberId, List<String> storeIds) {
        return promotionClient.getAllScopeMemberCoupon(memberId, storeIds);
    }

    public PromotionFeignClient.MemberCouponDTO getMemberCoupon(String memberId, String couponId) {
        return promotionClient.getMemberCoupon(memberId, couponId);
    }

    public boolean isECouponOnlyTrade(com.xzj.order.cart.entity.dto.TradeDTO trade) {
        return trade != null && trade.getSkuList() != null && !trade.getSkuList().isEmpty()
                && trade.getSkuList().stream().allMatch(item -> item.getGoodsSku() != null
                && "E_COUPON".equals(item.getGoodsSku().getGoodsType()));
    }

    public void normalizeECouponTrade(com.xzj.order.cart.entity.dto.TradeDTO trade) {
        if (trade == null) return;
        trade.setMemberAddress(null);
        trade.setStoreAddress(null);
        trade.getSkuList().forEach(item -> item.setDeliveryMethod("VIRTUAL"));
    }

    private GoodsSkuRemoteDTO toLocal(GoodsSkuDTO source) {
        if (source == null) return null;
        GoodsSkuRemoteDTO target = new GoodsSkuRemoteDTO();
        BeanUtils.copyProperties(source, target);
        return target;
    }
}

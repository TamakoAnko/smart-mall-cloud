package com.xzj.order.cart.render.impl;

import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.utils.CurrencyUtil;
import com.xzj.config.GoodsSkuFeignClient;
import com.xzj.config.PromotionFeignClient;
import com.xzj.entity.GoodsSkuDTO;
import com.xzj.order.cart.entity.dto.TradeDTO;
import com.xzj.order.cart.entity.enums.CartTypeEnum;
import com.xzj.order.cart.entity.enums.DeliveryMethodEnum;
import com.xzj.order.cart.entity.enums.RenderStepEnums;
import com.xzj.order.cart.entity.vo.CartSkuVO;
import com.xzj.order.cart.entity.vo.CartVO;
import com.xzj.order.cart.render.CartRenderStep;
import com.xzj.order.remote.GoodsSkuRemoteDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Cart validation is kept in order-service, while product and promotion data
 * are obtained through the goods-service internal API.
 */
@Slf4j
@Service
public class CheckDataRender implements CartRenderStep {
    @Autowired
    private GoodsSkuFeignClient goodsSkuFeignClient;

    @Autowired
    private PromotionFeignClient promotionFeignClient;

    @Override
    public RenderStepEnums step() {
        return RenderStepEnums.CHECK_DATA;
    }

    @Override
    public void render(TradeDTO tradeDTO) {
        validateAndRefreshSkus(tradeDTO);
        groupStores(tradeDTO);
    }

    private void validateAndRefreshSkus(TradeDTO tradeDTO) {
        if (tradeDTO.getSkuList() == null) return;
        for (CartSkuVO cartSku : tradeDTO.getSkuList()) {
            if (cartSku.getGoodsSku() == null || cartSku.getGoodsSku().getId() == null) {
                cartSku.setChecked(false);
                cartSku.setInvalid(true);
                cartSku.setErrorMessage("商品不存在");
                continue;
            }
            GoodsSkuDTO source = goodsSkuFeignClient.findById(cartSku.getGoodsSku().getId());
            GoodsSkuRemoteDTO current = toLocal(source);
            if (current == null || !"PASS".equals(current.getAuthFlag()) || !"UPPER".equals(current.getMarketEnable())) {
                cartSku.setChecked(false);
                cartSku.setInvalid(true);
                cartSku.setErrorMessage("商品已下架");
                continue;
            }
            cartSku.rebuildBySku(current);
            if (current.getQuantity() == null || current.getQuantity() < cartSku.getNum()) {
                cartSku.setChecked(false);
                cartSku.setErrorMessage("商品库存不足");
                continue;
            }
            Map<String, Object> promotions = promotionFeignClient.getCurrentGoodsPromotion(
                    current.getId(), tradeDTO.getCartTypeEnum().name());
            cartSku.setPromotionMap(promotions == null ? Collections.emptyMap() : promotions);
            Double price = Boolean.TRUE.equals(current.getPromotionFlag()) && current.getPromotionPrice() != null
                    ? current.getPromotionPrice() : current.getPrice();
            cartSku.setPurchasePrice(price);
            cartSku.setUtilPrice(price);
            cartSku.setSubTotal(CurrencyUtil.mul(price, cartSku.getNum()));
        }
        applyWholesalePrice(tradeDTO);
    }

    private void applyWholesalePrice(TradeDTO tradeDTO) {
        Map<String, List<CartSkuVO>> groups = tradeDTO.getCheckedSkuList().stream()
                .filter(item -> item.getGoodsSku() != null && "WHOLESALE".equals(item.getGoodsSku().getSalesModel()))
                .collect(Collectors.groupingBy(item -> item.getGoodsSku().getGoodsId()));
        groups.forEach((goodsId, items) -> {
            int quantity = items.stream().mapToInt(item -> Objects.requireNonNullElse(item.getNum(), 0)).sum();
            PromotionFeignClient.WholesaleDTO wholesale = promotionFeignClient.matchWholesale(goodsId, quantity);
            if (wholesale == null || wholesale.getPrice() == null) return;
            for (CartSkuVO item : items) {
                item.getGoodsSku().setPrice(wholesale.getPrice());
                item.setPurchasePrice(wholesale.getPrice());
                item.setUtilPrice(wholesale.getPrice());
                item.setSubTotal(CurrencyUtil.mul(wholesale.getPrice(), item.getNum()));
            }
        });
    }

    private void groupStores(TradeDTO tradeDTO) {
        List<CartVO> stores = new ArrayList<>();
        Map<String, List<CartSkuVO>> grouped = tradeDTO.getSkuList().stream()
                .filter(item -> item.getStoreId() != null)
                .collect(Collectors.groupingBy(CartSkuVO::getStoreId));
        grouped.forEach((storeId, items) -> {
            CartSkuVO first = items.get(0);
            CartVO cart = new CartVO(first);
            cart.setSkuList(items);
            cart.setChecked(items.stream().anyMatch(item -> Boolean.TRUE.equals(item.getChecked())));
            if (cart.getDeliveryMethod() == null || cart.getDeliveryMethod().isBlank()) {
                boolean virtual = items.stream().anyMatch(item -> item.getGoodsSku() != null
                        && "E_COUPON".equals(item.getGoodsSku().getGoodsType()));
                cart.setDeliveryMethod(virtual ? DeliveryMethodEnum.VIRTUAL.name() : DeliveryMethodEnum.LOGISTICS.name());
            }
            stores.add(cart);
        });
        tradeDTO.setCartList(stores);
    }

    private GoodsSkuRemoteDTO toLocal(GoodsSkuDTO source) {
        if (source == null) return null;
        GoodsSkuRemoteDTO target = new GoodsSkuRemoteDTO();
        BeanUtils.copyProperties(source, target);
        return target;
    }
}

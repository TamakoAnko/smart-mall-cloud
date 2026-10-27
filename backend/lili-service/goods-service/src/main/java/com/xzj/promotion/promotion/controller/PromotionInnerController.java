package com.xzj.promotion.promotion.controller;

import com.xzj.config.PromotionFeignClient;
import com.xzj.promotion.promotion.entity.dos.Coupon;
import com.xzj.promotion.promotion.service.CouponService;
import com.xzj.promotion.promotion.service.MemberCouponService;
import com.xzj.promotion.promotion.service.PromotionGoodsService;
import com.xzj.goods.goods.service.GoodsSkuService;
import com.xzj.goods.goods.service.WholesaleService;
import com.xzj.goods.goods.entity.dos.Wholesale;
import com.xzj.promotion.promotion.entity.dto.search.MemberCouponSearchParams;
import com.xzj.config.GoodsSkuFeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/promotion/inner")
public class PromotionInnerController {
    private final CouponService couponService;
    private final MemberCouponService memberCouponService;
    private final PromotionGoodsService promotionGoodsService;
    private final GoodsSkuService goodsSkuService;
    private final WholesaleService wholesaleService;

    public PromotionInnerController(CouponService couponService, MemberCouponService memberCouponService,
                                    PromotionGoodsService promotionGoodsService, GoodsSkuService goodsSkuService,
                                    WholesaleService wholesaleService) {
        this.couponService = couponService;
        this.memberCouponService = memberCouponService;
        this.promotionGoodsService = promotionGoodsService;
        this.goodsSkuService = goodsSkuService;
        this.wholesaleService = wholesaleService;
    }

    @GetMapping("/sku-promotion")
    public Map<String, Object> getCurrentGoodsPromotion(@RequestParam String skuId,
                                                        @RequestParam String cartType) {
        var sku = goodsSkuService.getGoodsSkuByIdFromCache(skuId);
        return sku == null ? Map.of() : promotionGoodsService.getCurrentGoodsPromotion(sku, cartType);
    }

    @GetMapping("/wholesale")
    public PromotionFeignClient.WholesaleDTO matchWholesale(@RequestParam String goodsId,
                                                             @RequestParam Integer num) {
        Wholesale source = wholesaleService.match(goodsId, num);
        if (source == null) return null;
        PromotionFeignClient.WholesaleDTO target = new PromotionFeignClient.WholesaleDTO();
        target.setPrice(source.getPrice());
        return target;
    }

    @GetMapping("/member-coupon/list")
    public List<PromotionFeignClient.MemberCouponDTO> getMemberCoupons(@RequestParam String memberId) {
        return memberCouponService.getMemberCoupons(memberId).stream().map(this::toMemberCoupon).toList();
    }

    @GetMapping("/member-coupon/live")
    public List<PromotionFeignClient.MemberCouponDTO> getLiveMemberCoupons(@RequestParam String memberId,
                                                                            @RequestParam String liveRoomId) {
        MemberCouponSearchParams params = new MemberCouponSearchParams();
        params.setMemberId(memberId);
        params.setLiveRoomId(liveRoomId);
        return memberCouponService.getMemberCoupons(params).stream().map(this::toMemberCoupon).toList();
    }

    @GetMapping("/member-coupon/current")
    public List<PromotionFeignClient.MemberCouponDTO> getCurrentGoodsCanUse(@RequestParam String memberId,
                                                                            @RequestParam List<String> couponIds,
                                                                            @RequestParam Double totalPrice) {
        return memberCouponService.getCurrentGoodsCanUse(memberId, couponIds, totalPrice)
                .stream().map(this::toMemberCoupon).toList();
    }

    @GetMapping("/member-coupon/scope")
    public List<PromotionFeignClient.MemberCouponDTO> getAllScopeMemberCoupon(@RequestParam String memberId,
                                                                               @RequestParam List<String> storeIds) {
        return memberCouponService.getAllScopeMemberCoupon(memberId, storeIds)
                .stream().map(this::toMemberCoupon).toList();
    }

    @GetMapping("/member-coupon/one")
    public PromotionFeignClient.MemberCouponDTO getMemberCoupon(@RequestParam String memberId,
                                                                @RequestParam String couponId) {
        MemberCouponSearchParams params = new MemberCouponSearchParams();
        params.setMemberId(memberId);
        params.setId(couponId);
        var source = memberCouponService.getMemberCoupon(params);
        return source == null ? null : toMemberCoupon(source);
    }

    private PromotionFeignClient.MemberCouponDTO toMemberCoupon(com.xzj.promotion.promotion.entity.dos.MemberCoupon source) {
        PromotionFeignClient.MemberCouponDTO target = new PromotionFeignClient.MemberCouponDTO();
        org.springframework.beans.BeanUtils.copyProperties(source, target);
        return target;
    }

    @GetMapping("/coupon/{couponId}")
    public PromotionFeignClient.CouponSummaryDTO getCoupon(@PathVariable String couponId) {
        Coupon source = couponService.getById(couponId);
        if (source == null) return null;
        PromotionFeignClient.CouponSummaryDTO target = new PromotionFeignClient.CouponSummaryDTO();
        target.setId(source.getId());
        target.setCouponName(source.getCouponName());
        target.setCouponType(source.getCouponType());
        target.setPrice(source.getPrice());
        target.setCouponDiscount(source.getCouponDiscount());
        target.setRangeDayType(source.getRangeDayType());
        target.setEffectiveDays(source.getEffectiveDays());
        target.setStartTime(source.getStartTime());
        target.setEndTime(source.getEndTime());
        return target;
    }

    @PostMapping("/member-coupon/receive")
    public Boolean receiveCoupon(@RequestParam String couponId, @RequestParam String memberId,
                                 @RequestParam String memberName) {
        memberCouponService.receiveCoupon(couponId, memberId, memberName);
        return true;
    }

    @PostMapping("/member-coupon/used")
    public Boolean usedCoupons(@RequestParam String memberId, @RequestParam List<String> ids) {
        memberCouponService.used(memberId, ids);
        return true;
    }
}

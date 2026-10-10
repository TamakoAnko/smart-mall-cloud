package com.xzj.order.serviceimpl;

import cn.hutool.json.JSONUtil;
import cn.lili.cache.Cache;
import cn.lili.cache.CachePrefix;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.event.TransactionCommitSendMQEvent;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.properties.RocketmqCustomProperties;
import com.xzj.member.MemberFeignClient;
import com.xzj.order.cart.entity.dto.MemberCouponDTO;
import com.xzj.order.cart.entity.dto.TradeDTO;
import com.xzj.order.cart.entity.enums.CartTypeEnum;
import com.xzj.order.cart.entity.enums.DeliveryMethodEnum;
import com.xzj.order.cart.entity.vo.CartVO;
import com.xzj.order.entity.dos.Order;
import com.xzj.order.entity.dos.Trade;
import com.xzj.order.entity.enums.PayStatusEnum;
import com.xzj.order.mapper.TradeMapper;
import com.xzj.order.service.OrderService;
import com.xzj.order.service.TradeService;
import com.xzj.config.PromotionFeignClient;
import cn.lili.rocketmq.tags.OrderTagsEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 交易业务层实现
 *
 * @author Chopper
 * @since 2020/11/17 7:39 下午
 */
@Service("orderTradeServiceImpl")
public class TradeServiceImpl extends ServiceImpl<TradeMapper, Trade> implements TradeService {

    /**
     * 缓存
     */
    @Autowired
    private Cache<Object> cache;

    /**
     * 订单
     */
    @Autowired
    @Lazy
    private OrderService orderService;

    /**
     * 客户
     */
    @Autowired
    private MemberFeignClient memberFeignClient;

    /**
     * 优惠券
     */
    @Autowired
    private PromotionFeignClient promotionFeignClient;

    /** 促销、卡密和砍价的内部调用入口。 */

    /**
     * RocketMQ 配置
     */
    @Autowired
    private RocketmqCustomProperties rocketmqCustomProperties;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Trade createTrade(TradeDTO tradeDTO) {
        //创建订单预校验
        createTradeCheck(tradeDTO);
        Trade trade = new Trade(tradeDTO);
        String key = CachePrefix.TRADE.getPrefix() + trade.getSn();
        //优惠券预处理
        couponPretreatment(tradeDTO);
        //积分预处理
        pointPretreatment(tradeDTO);
        //添加交易
        this.save(trade);
        //添加订单
        orderService.intoDB(tradeDTO);
        //砍价订单处理
        kanjiaPretreatment(tradeDTO);
        //写入缓存，给消费者调用
        cache.put(key, JSONUtil.toJsonStr(tradeDTO));
        applicationEventPublisher.publishEvent(new TransactionCommitSendMQEvent("订单创建消息", rocketmqCustomProperties.getOrderTopic(),
                OrderTagsEnum.ORDER_CREATE.name(), key));
        return trade;
    }

    /**
     * 创建交易入库前校验。
     * <ul>
     *   <li>E_COUPON：由 goods-service 的卡密内部接口完成混单、促销、卡池和金额校验</li>
     *   <li>收货地址：由 {@link com.xzj.order.cart.service.CartServiceImpl#createTrade} 在渲染前校验</li>
     *   <li>本方法：勾选状态、E_COUPON 跳过后续物流校验、实物配送区域与自提门店</li>
     * </ul>
     *
     * @param tradeDTO 购物车视图
     */
    private void createTradeCheck(TradeDTO tradeDTO) {
        promotionFeignClient.validateCardKeyTrade(JSONUtil.toJsonStr(tradeDTO));
        if (tradeDTO.getCartList().stream().noneMatch(CartVO::getChecked)) {
            throw new ServiceException(ResultCode.ORDER_NOT_EXIST_VALID);
        }
        // E_COUPON 专用校验由促销服务完成，订单服务只处理配送规则。
        if (!tradeDTO.getCartTypeEnum().equals(CartTypeEnum.VIRTUAL) && tradeDTO.getStoreAddress() == null) {
            if (tradeDTO.getNotSupportFreight() != null && !tradeDTO.getNotSupportFreight().isEmpty()) {
                StringBuilder stringBuilder = new StringBuilder("包含商品有-");
                tradeDTO.getNotSupportFreight().forEach(sku -> stringBuilder.append(sku.getGoodsSku().getGoodsName()));
                throw new ServiceException(ResultCode.ORDER_NOT_SUPPORT_DISTRIBUTION, stringBuilder.toString());
            }
            if (tradeDTO.getCartList().stream().allMatch(item -> item.getDeliveryMethod().equals(DeliveryMethodEnum.SELF_PICK_UP.name()))) {
                throw new ServiceException(ResultCode.STORE_ADDRESS_NOT_EXIST);
            }
        }
    }

    @Override
    public Trade getBySn(String sn) {
        LambdaQueryWrapper<Trade> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Trade::getSn, sn);
        return this.getOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payTrade(String tradeSn, String paymentName, String receivableNo) {
        LambdaQueryWrapper<Order> orderQueryWrapper = new LambdaQueryWrapper<>();
        orderQueryWrapper.eq(Order::getTradeSn, tradeSn);
        List<Order> orders = orderService.list(orderQueryWrapper);
        for (Order order : orders) {
            orderService.payOrder(order.getSn(), paymentName, receivableNo);
        }
        Trade trade = this.getBySn(tradeSn);
        trade.setPayStatus(PayStatusEnum.PAID.name());
        this.saveOrUpdate(trade);
    }

    @Override
    public void updateTradePrice(String tradeSn) {
        this.baseMapper.updateTradePrice(tradeSn);
    }

    /**
     * 优惠券预处理
     * 下单同时，扣除优惠券
     *
     * @param tradeDTO 购物车视图
     */
    private void couponPretreatment(TradeDTO tradeDTO) {
        List<MemberCouponDTO> memberCouponDTOList = new ArrayList<>();
        if (null != tradeDTO.getPlatformCoupon()) {
            memberCouponDTOList.add(tradeDTO.getPlatformCoupon());
        }
        Collection<MemberCouponDTO> storeCoupons = tradeDTO.getStoreCoupons().values();
        if (!storeCoupons.isEmpty()) {
            memberCouponDTOList.addAll(storeCoupons);
        }
        List<String> ids = memberCouponDTOList.stream().map(e -> e.getMemberCoupon().getId()).collect(Collectors.toList());
        promotionFeignClient.usedCoupons(tradeDTO.getMemberId(), ids);
    }

    /**
     * 创建交易，积分处理
     *
     * @param tradeDTO 购物车视图
     */
    private void pointPretreatment(TradeDTO tradeDTO) {
        //需要支付积分
        if (tradeDTO.getPriceDetailDTO() != null
                && tradeDTO.getPriceDetailDTO().getPayPoint() != null
                && tradeDTO.getPriceDetailDTO().getPayPoint() > 0) {
            StringBuilder orderSns = new StringBuilder();
            for (CartVO item : tradeDTO.getCartList()) {
                orderSns.append(item.getSn());
            }
            boolean result = memberFeignClient.updateMemberPoint(tradeDTO.getPriceDetailDTO().getPayPoint(), "REDUCE",
                    tradeDTO.getMemberId(),
                    "订单【" + orderSns + "】创建，积分扣减",
                    "ORDER_PAY");
            if (!result) {
                throw new ServiceException(ResultCode.PAY_POINT_ENOUGH);
            }
        }
    }

    /**
     * 创建交易、砍价处理
     *
     * @param tradeDTO 购物车视图
     */
    private void kanjiaPretreatment(TradeDTO tradeDTO) {
        if (tradeDTO.getCartTypeEnum().equals(CartTypeEnum.KANJIA)) {
            String kanjiaId = tradeDTO.getSkuList().get(0).getKanjiaId();
            promotionFeignClient.endKanjia(kanjiaId);
        }
    }
}

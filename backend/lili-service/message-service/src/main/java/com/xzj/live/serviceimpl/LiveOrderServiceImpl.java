package com.xzj.live.serviceimpl;

import cn.lili.cache.Cache;
import com.xzj.live.entity.vos.LiveOrderStatisticsVO;
import com.xzj.live.service.LiveOperateService;
import com.xzj.live.service.LiveOrderService;
import com.xzj.live.service.LiveRoomService;
import com.xzj.order.OrderFeignClient;
import com.xzj.order.OrderFeignClient.OrderSummaryDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @author chc
 * @since 2022/6/2114:46
 */
@Service("messageLiveLiveOrderServiceImpl")
@RequiredArgsConstructor
public class LiveOrderServiceImpl implements LiveOrderService {

    private final OrderFeignClient orderFeignClient;

    private final Cache cache;

    private final LiveRoomService liveRoomService;

    private final LiveOperateService liveOperateService;

    @Override
    public IPage<OrderSummaryDTO> queryOrderPage(String liveRoomId) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<OrderSummaryDTO> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 1000);
        page.setRecords(orderFeignClient.getLiveOrders(liveRoomId));
        page.setTotal(page.getRecords().size());
        return page;
    }

    @Override
    public LiveOrderStatisticsVO statistics(String liveRoomId) {
        LiveOrderStatisticsVO orderStatisticsVO = new LiveOrderStatisticsVO();

        List<OrderSummaryDTO> orderList = orderFeignClient.getLiveOrders(liveRoomId);

        if (orderList != null && !orderList.isEmpty()) {

            long salesVolume = orderList.stream()
                    .map(OrderSummaryDTO::getMemberId) // 提取 buyerId
                    .distinct()             // 去重
                    .count();               // 统计数量

            double totalAmount = orderList.stream()
                    .mapToDouble(order -> order.getFlowPrice() == null ? 0D : order.getFlowPrice())
                    .sum(); // 求和

            orderStatisticsVO.setTotalAmount(totalAmount);
            orderStatisticsVO.setSalesVolume(orderList.size());
            orderStatisticsVO.setDealUserCount(salesVolume);

            Long viewCount = liveOperateService.getCacheView(liveRoomId);
            if (viewCount > 0 && salesVolume > 0) {
                orderStatisticsVO.setConversionRate(((double) salesVolume / viewCount) * 100);
            } else {
                orderStatisticsVO.setConversionRate(0D);
            }

            if (totalAmount > 0) {
                // 2. 注意这里的强制类型转换 (double)，否则两个整数相除会丢失小数部分！
                double rawValue = ((double) totalAmount / orderList.size()) * 100;

                // 3. 经典四舍五入保留两位小数算法：乘 100 -> 四舍五入取整 -> 除 100.0
                double roundedValue = Math.round(rawValue * 100) / 100.0;
                orderStatisticsVO.setAverageOrderValue(roundedValue);
            } else {
                orderStatisticsVO.setAverageOrderValue(0D);
            }
        }


        return orderStatisticsVO;
    }


}

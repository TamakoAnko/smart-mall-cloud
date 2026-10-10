package com.xzj.live.service;

import com.xzj.live.entity.vos.LiveOrderStatisticsVO;
import com.xzj.order.OrderFeignClient.OrderSummaryDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * @author chc
 * @since 2022/6/2114:46
 */
public interface LiveOrderService {

    /**
     * 查询订单列表
     * @param orderSearchParams
     * @return
     */
    IPage<OrderSummaryDTO> queryOrderPage(String liveRoomId);

    /**
     * 数据统计
     * @param liveRoomId
     * @return
     */
    LiveOrderStatisticsVO statistics(String liveRoomId);

}

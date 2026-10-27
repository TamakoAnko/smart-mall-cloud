package com.xzj.promotion.distribution.service;

import com.xzj.promotion.distribution.entity.dos.DistributionOrder;
import com.xzj.promotion.distribution.entity.vos.DistributionOrderSearchParams;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;


/**
 * 分销订单业务层
 *
 * @author pikachu
 * @since 2020-03-15 10:46:33
 */
public interface DistributionOrderService extends IService<DistributionOrder> {

    /**
     * 获取分销订单分页
     * @param distributionOrderSearchParams 分销订单搜索参数
     * @return 分销订单分页
     */
    IPage<DistributionOrder> getDistributionOrderPage(DistributionOrderSearchParams distributionOrderSearchParams);

}

package com.xzj.promotion.distribution.serviceimpl;

import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xzj.promotion.distribution.entity.dos.DistributionOrder;
import com.xzj.promotion.distribution.entity.vos.DistributionOrderSearchParams;
import com.xzj.promotion.distribution.mapper.DistributionOrderMapper;
import com.xzj.promotion.distribution.service.DistributionOrderService;
import org.springframework.stereotype.Service;

/** Buyer-side distribution order queries. Settlement is owned by order-service. */
@Service("goodsDistributionDistributionOrderServiceImpl")
public class DistributionOrderServiceImpl extends ServiceImpl<DistributionOrderMapper, DistributionOrder>
        implements DistributionOrderService {
    @Override
    public IPage<DistributionOrder> getDistributionOrderPage(DistributionOrderSearchParams params) {
        return this.page(PageUtil.initPage(params), params.queryWrapper());
    }
}

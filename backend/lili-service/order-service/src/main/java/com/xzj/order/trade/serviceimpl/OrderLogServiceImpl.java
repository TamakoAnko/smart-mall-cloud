package com.xzj.order.trade.serviceimpl;

import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.SearchVO;
import com.xzj.order.trade.entity.dos.OrderLog;
import com.xzj.order.trade.mapper.OrderLogMapper;
import com.xzj.order.trade.service.OrderLogService;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单日志业务层实现
 *
 * @author Chopper
 * @since 2020-02-25 14:10:16
 */
@Service
public class OrderLogServiceImpl extends ServiceImpl<OrderLogMapper, OrderLog> implements OrderLogService {

    @Override
    public List<OrderLog> getOrderLog(String orderSn) {
        LambdaQueryWrapper<OrderLog> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(OrderLog::getOrderSn, orderSn);
        return this.list(lambdaQueryWrapper);
    }

    @Override
    public IPage<OrderLog> getByPage(OrderLog entity, SearchVO searchVO, PageVO pageVO) {
        return this.page(PageUtil.initPage(pageVO), PageUtil.initWrapper(entity, searchVO));
    }
}

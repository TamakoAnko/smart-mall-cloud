package com.xzj.payment.serviceimpl;

import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.SearchVO;
import com.xzj.payment.entity.RefundLog;
import com.xzj.payment.mapper.RefundLogMapper;
import com.xzj.payment.service.RefundLogService;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 退款日志 业务实现
 *
 * @author Chopper
 * @since 2020-12-19 09:25
 */
@Service("paymentRefundLogServiceImpl")
public class RefundLogServiceImpl extends ServiceImpl<RefundLogMapper, RefundLog> implements RefundLogService {

    @Override
    public RefundLog queryByAfterSaleSn(String sn) {
        return this.getOne(new LambdaUpdateWrapper<RefundLog>().eq(RefundLog::getAfterSaleNo, sn));
    }

    @Override
    public IPage<RefundLog> getByPage(RefundLog entity, SearchVO searchVO, PageVO pageVO) {
        return this.page(PageUtil.initPage(pageVO), PageUtil.initWrapper(entity, searchVO));
    }
}

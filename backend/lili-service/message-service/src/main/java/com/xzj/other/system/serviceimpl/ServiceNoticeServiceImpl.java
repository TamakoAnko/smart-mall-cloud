package com.xzj.other.system.serviceimpl;

import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.SearchVO;
import com.xzj.other.system.entity.dos.ServiceNotice;
import com.xzj.other.system.mapper.ServiceNoticeMapper;
import com.xzj.other.system.service.ServiceNoticeService;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 服务订阅消息业务层实现
 * @author Chopper
 * @since 2020/11/17 8:02 下午
 */
@Service
public class ServiceNoticeServiceImpl extends ServiceImpl<ServiceNoticeMapper, ServiceNotice> implements ServiceNoticeService {

    @Override
    public IPage<ServiceNotice> getByPage(ServiceNotice entity, SearchVO searchVO, PageVO pageVO) {
        return this.page(PageUtil.initPage(pageVO), PageUtil.initWrapper(entity, searchVO));
    }
}

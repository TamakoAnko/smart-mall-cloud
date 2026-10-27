package com.xzj.other.broadcast.serviceimpl;

import com.xzj.other.broadcast.entity.dos.StudioCommodity;
import com.xzj.other.broadcast.mapper.StudioCommodityMapper;
import com.xzj.other.broadcast.service.StudioCommodityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 直播间-商品关联业务层实现
 *
 * @author Bulbasaur
 * @since 2021/5/17 3:20 下午
 */
@Service("messageOtherStudioCommodityServiceImpl")
public class StudioCommodityServiceImpl extends ServiceImpl<StudioCommodityMapper, StudioCommodity> implements StudioCommodityService {
}

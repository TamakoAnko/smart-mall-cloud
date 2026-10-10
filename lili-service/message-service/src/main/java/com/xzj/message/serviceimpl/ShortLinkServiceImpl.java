package com.xzj.message.serviceimpl;

import com.xzj.message.entity.dos.ShortLink;
import com.xzj.message.mapper.ShortLinkMapper;
import com.xzj.message.service.ShortLinkService;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 短链接 业务实现
 *
 * @author Chopper
 */
@Service("messageShortLinkServiceImpl")
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLink> implements ShortLinkService {

    @Override
    public List<ShortLink> queryShortLinks(ShortLink shortLink) {
        return this.list(PageUtil.initWrapper(shortLink));
    }
}
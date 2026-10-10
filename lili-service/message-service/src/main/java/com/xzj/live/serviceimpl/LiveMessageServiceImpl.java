package com.xzj.live.serviceimpl;

import com.xzj.live.entity.dos.LiveMessage;
import com.xzj.live.entity.dto.LiveMessageSearchDTO;
import com.xzj.live.mapper.LiveMessageMapper;
import com.xzj.live.service.LiveMessageService;
import com.xzj.live.util.TencentLiveUtil;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author chc
 * @since 2022/6/2114:46
 */
@Service("messageLiveLiveMessageServiceImpl")
@RequiredArgsConstructor
public class LiveMessageServiceImpl extends ServiceImpl<LiveMessageMapper, LiveMessage> implements LiveMessageService {

    @Autowired
    private TencentLiveUtil tencentLiveUtil;


    @Override
    public IPage<LiveMessage> queryMessagePage(LiveMessageSearchDTO liveMessageSearchDTO) {
        return this.page(PageUtil.initPage(liveMessageSearchDTO),liveMessageSearchDTO.getQueryWrapper());
    }

    @Override
    public boolean removeByUserId(String liveId, String userId) {
        tencentLiveUtil.removeUserAllMessage(userId, liveId);
        return this.remove(new QueryWrapper<LiveMessage>().eq("live_room_id", liveId).eq("user_id", userId));

    }
}

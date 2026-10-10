package com.xzj.live.serviceimpl;

import cn.lili.cache.Cache;
import cn.lili.cache.CachePrefix;
import com.xzj.live.entity.dos.LiveUser;
import com.xzj.live.entity.dto.LiveUserSearchDTO;
import com.xzj.live.mapper.LiveUserMapper;
import com.xzj.live.service.LiveUserService;
import com.xzj.order.OrderFeignClient.OrderSummaryDTO;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author chc
 * @since 2022/6/2114:46
 */
@Service("messageLiveLiveUserServiceImpl")
@RequiredArgsConstructor
public class LiveUserServiceImpl extends ServiceImpl<LiveUserMapper, LiveUser> implements LiveUserService {

    private final Cache cache;

    @Override
    public void editLiveUserMuteFlag(String liveUserid, Boolean muteFlag) {
        LiveUser liveUser = this.getById(liveUserid);
        if(liveUser != null){
            liveUser.setMuteFlag(true);
        }
        this.updateUser(liveUser);
    }


    @Override
    public void updateUser(LiveUser liveUser) {
        this.updateById(liveUser);
        setLiveUserCache(liveUser);
    }


    @Override
    public LiveUser queryLiveUserDetail(String liveRoomId, String userId) {
        LiveUser liveUser = (LiveUser) cache.get(CachePrefix.LIVE_USER_DETAIL.getPrefix() + liveRoomId + "_" + userId);
        if (liveUser == null) {
            liveUser = findFromDb(liveRoomId, userId);
            setLiveUserCache(liveUser);
        }
        return liveUser;
    }

    @Override
    public LiveUser getOrCreate(LiveUser liveUser) {
        LiveUser existing = findFromDb(liveUser.getLiveRoomId(), liveUser.getUserId());
        if (existing != null) {
            setLiveUserCache(existing);
            return existing;
        }
        try {
            this.save(liveUser);
            setLiveUserCache(liveUser);
            return liveUser;
        } catch (DataIntegrityViolationException e) {
            existing = findFromDb(liveUser.getLiveRoomId(), liveUser.getUserId());
            if (existing != null) {
                setLiveUserCache(existing);
                return existing;
            }
            throw e;
        }
    }

    /**
     * 直接从数据库查询，避免缓存与并发创建导致的重复读
     */
    private LiveUser findFromDb(String liveRoomId, String userId) {
        return this.getOne(new LambdaQueryWrapper<LiveUser>()
                .eq(LiveUser::getLiveRoomId, liveRoomId)
                .eq(LiveUser::getUserId, userId)
                .orderByAsc(LiveUser::getCreateTime)
                .last("LIMIT 1"), false);
    }

    @Override
    public IPage<LiveUser> queryLiveUserList(LiveUserSearchDTO liveUserSearchDTO) {
        return this.page(PageUtil.initPage(liveUserSearchDTO), liveUserSearchDTO.getQueryWrapper());
    }

    @Override
    public List<LiveUser> getLiveUserList(LiveUserSearchDTO liveUserSearchDTO) {
        return this.list(liveUserSearchDTO.getQueryWrapper());
    }

    @Override
    public Long queryLiveUserCount(String liveRoomId) {
        return this.count(new LambdaQueryWrapper<LiveUser>().eq(LiveUser::getLiveRoomId, liveRoomId));
    }

    @Override
    public void addAmount(OrderSummaryDTO order) {
        LiveUser liveUser = queryLiveUserDetail(order.getLiveRoomId(), order.getMemberId());
        liveUser.setAmount(liveUser.getAmount() == null ? order.getFlowPrice() : liveUser.getAmount() + order.getFlowPrice());
        updateUser(liveUser);
    }

    @Override
    public void setUserWatchTime(String liveRoomId, String userId, String watchTime) {
        LiveUser liveUser = queryLiveUserDetail(liveRoomId, userId);
        liveUser.setWatchTime(watchTime);
        updateUser(liveUser);
    }

    private void setLiveUserCache(LiveUser liveUser) {
        if(liveUser == null){
            return;
        }
        cache.remove(CachePrefix.LIVE_USER_DETAIL.getPrefix() + liveUser.getLiveRoomId() + "_" + liveUser.getUserId());
        cache.put(CachePrefix.LIVE_USER_DETAIL.getPrefix() + liveUser.getLiveRoomId() + "_" + liveUser.getUserId(), liveUser,86400L);
    }
}

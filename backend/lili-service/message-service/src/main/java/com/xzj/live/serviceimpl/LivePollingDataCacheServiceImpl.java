package com.xzj.live.serviceimpl;

import com.xzj.live.entity.dos.LiveCoupon;
import com.xzj.live.entity.dos.LiveGoods;
import com.xzj.live.entity.dos.LiveRoom;
import com.xzj.live.entity.vos.LivePollingDataVO;
import com.xzj.live.service.LiveCouponService;
import com.xzj.live.service.LiveGoodsService;
import com.xzj.live.service.LivePollingDataCacheService;
import com.xzj.live.service.LiveRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author chc
 * @since 2022/6/2114:46
 */
@Service("messageLiveLivePollingDataCacheServiceImpl")
@RequiredArgsConstructor
public class LivePollingDataCacheServiceImpl implements LivePollingDataCacheService {

    private final LiveRoomService liveRoomService;

    private final LiveGoodsService liveGoodsService;

    private final LiveCouponService liveCouponService;

    @Override
    public LivePollingDataVO getGlobalPollingData(String liveId) {

        LiveRoom liveDetail = liveRoomService.getLiveRoomUserDetail(liveId);
        List<LiveGoods> goodsList = liveGoodsService.getByCache(liveId);
        List<LiveCoupon> liveCoupon = liveCouponService.getByCache(liveId);

        return  LivePollingDataVO.builder()
                .liveDetail(liveDetail)
                .goodsList(goodsList)
                .liveCoupon(liveCoupon).build();
    }
}

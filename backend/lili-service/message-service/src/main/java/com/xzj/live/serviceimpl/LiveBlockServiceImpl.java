package com.xzj.live.serviceimpl;

import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.vo.PageVO;
import com.xzj.live.entity.dos.LiveBlock;
import com.xzj.live.entity.dos.LiveRoom;
import com.xzj.live.mapper.LiveBlockMapper;
import com.xzj.live.service.LiveBlockService;
import com.xzj.live.service.LiveRoomService;
import com.xzj.member.MemberFeignClient;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author chc
 * @since 2022/6/2114:46
 */
@Service("messageLiveLiveBlockServiceImpl")
@RequiredArgsConstructor
public class LiveBlockServiceImpl extends ServiceImpl<LiveBlockMapper, LiveBlock> implements LiveBlockService {

    @Autowired
    private MemberFeignClient memberFeignClient;

    @Autowired
    private LiveRoomService liveRoomService;

    @Override
    public List<LiveBlock> liveBlockList() {
        AuthUser currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException(ResultCode.USER_NOT_LOGIN);
        }
        List<LiveBlock> liveBlocks = this.list(new LambdaQueryWrapper<LiveBlock>().eq(LiveBlock::getStoreId, currentUser.getStoreId()));
        return liveBlocks;
    }

    @Override
    public IPage<LiveBlock> liveBlockPage(PageVO page) {
        AuthUser currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException(ResultCode.USER_NOT_LOGIN);
        }
        IPage<LiveBlock> liveBlockPage = this.page(PageUtil.initPage(page), new LambdaQueryWrapper<LiveBlock>().eq(LiveBlock::getStoreId, currentUser.getStoreId()));
        return liveBlockPage;
    }


    @Override
    public void blockUser(String memberId, String liveRoomId, String reason) {
        MemberFeignClient.MemberSummaryDTO member = memberFeignClient.getMemberById(memberId);
        if (member == null) {
            throw new ServiceException(ResultCode.USER_NOT_EXIST);
        }
        LiveRoom liveRoom = liveRoomService.getById(liveRoomId);
        if (liveRoom == null) {
            throw new ServiceException(ResultCode.LIVE_ROOM_NOT_EXIST);
        }
        LiveBlock liveBlock = new LiveBlock();
        liveBlock.setUserId(memberId);
        liveBlock.setUserName(member.getNickName());
        liveBlock.setUserAvatar(null);
        liveBlock.setLiveRoomId(liveRoomId);
        liveBlock.setStoreId(liveRoom.getStoreId());
        liveBlock.setStoreName(liveRoom.getStoreName());
        liveBlock.setReason(reason);
        this.saveOrUpdate(liveBlock);
    }

    @Override
    public void unblockUser(String userId, String liveRoomId) {
        LiveRoom liveRoom = liveRoomService.getById(liveRoomId);
        if (liveRoom == null) {
            throw new ServiceException(ResultCode.LIVE_ROOM_NOT_EXIST);
        }
        this.remove(new LambdaQueryWrapper<LiveBlock>().eq(LiveBlock::getUserId, userId).eq(LiveBlock::getStoreId, liveRoom.getStoreId()));
    }
}

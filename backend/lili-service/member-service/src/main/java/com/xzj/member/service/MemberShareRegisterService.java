package com.xzj.member.service;

import cn.lili.common.vo.PageVO;
import com.xzj.member.entity.dos.Member;
import com.xzj.member.entity.vo.MemberShareBuyLogVO;
import com.xzj.order.OrderFeignClient;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.Date;

/**
 * 会员分享注册奖励服务
 */
public interface MemberShareRegisterService {

    /**
     * 获取或创建注册分享码
     *
     * @param memberId 会员ID
     * @return 分享码（用于邀请注册）
     */
    String getOrCreateRegisterShareCode(String memberId);

    /**
     * 绑定注册来源
     *
     * @param inviteeId 被邀请注册用户ID
     * @param shareCode 分享码
     */
    void bindRegisterSource(String inviteeId, String shareCode);

    /**
     * 处理分享注册经验值奖励
     *
     * @param invitee 被邀请注册用户
     */
    void handleShareRegisterReward(Member invitee);

    /**
     * 处理分享购买经验值奖励
     *
     * @param order 订单
     */
    void handleShareBuyReward(OrderFeignClient.OrderSummaryDTO order);

    /**
     * 分享购买奖励记录分页
     *
     * @param mobile 客户手机号（分享人或被分享人）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param page 分页参数
     * @return 分页结果
     */
    IPage<MemberShareBuyLogVO> getShareBuyLogPage(String mobile, Date startTime, Date endTime, PageVO page);
}

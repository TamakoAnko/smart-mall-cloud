package com.xzj.member.serviceimpl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.SwitchEnum;
import cn.lili.common.event.TransactionCommitSendMQEvent;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.properties.RocketmqCustomProperties;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.security.enums.UserEnums;
import cn.lili.common.sensitive.SensitiveWordsFilter;
import com.xzj.config.GoodsSkuFeignClient;
import com.xzj.entity.GoodsSkuDTO;
import com.xzj.member.entity.dos.Member;
import com.xzj.member.entity.dos.MemberEvaluation;
import com.xzj.member.entity.dto.EvaluationQueryParams;
import com.xzj.member.entity.dto.MemberEvaluationDTO;
import com.xzj.member.entity.enums.EvaluationGradeEnum;
import com.xzj.member.entity.vo.EvaluationNumberVO;
import com.xzj.member.entity.vo.MemberEvaluationListVO;
import com.xzj.member.entity.vo.MemberEvaluationVO;
import com.xzj.member.entity.vo.StoreRatingVO;
import com.xzj.member.mapper.MemberEvaluationMapper;
import com.xzj.member.service.MemberEvaluationService;
import com.xzj.member.service.MemberService;
import com.xzj.order.OrderFeignClient;
import cn.lili.mybatis.util.PageUtil;
import cn.lili.rocketmq.tags.GoodsTagsEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 客户商品评价业务层实现
 *
 * @author Bulbasaur
 * @since 2020-02-25 14:10:16
 */
@Service("memberMemberEvaluationService")
public class MemberEvaluationServiceImpl extends ServiceImpl<MemberEvaluationMapper, MemberEvaluation> implements MemberEvaluationService {

    /**
     * 订单
     */
    @Autowired
    @Lazy
    private OrderFeignClient orderFeignClient;
    /**
     * 客户
     */
    @Autowired
    private MemberService memberService;
    /**
     * 商品
     */
    @Autowired
    private GoodsSkuFeignClient goodsSkuFeignClient;
    /**
     * rocketMq配置
     */
    @Autowired
    private RocketmqCustomProperties rocketmqCustomProperties;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public IPage<MemberEvaluation> managerQuery(EvaluationQueryParams queryParams) {
        //获取评价分页
        return this.page(PageUtil.initPage(queryParams), queryParams.queryWrapper());
    }

    @Override
    public IPage<MemberEvaluationListVO> queryPage(EvaluationQueryParams evaluationQueryParams) {
        return this.baseMapper.getMemberEvaluationList(PageUtil.initPage(evaluationQueryParams), evaluationQueryParams.queryWrapper());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberEvaluationDTO addMemberEvaluation(MemberEvaluationDTO memberEvaluationDTO, Boolean isSelf) {
        //获取子订单信息
        OrderFeignClient.OrderItemSummaryDTO orderItem = orderFeignClient.getOrderItemBySn(memberEvaluationDTO.getOrderItemSn());

        if (orderItem == null) {
            throw new ServiceException(ResultCode.ORDER_ITEM_NOT_EXIST);
        }

        //获取订单信息
        OrderFeignClient.OrderSummaryDTO order = orderFeignClient.getOrderBySn(orderItem.getOrderSn());

        if (order == null) {
            throw new ServiceException(ResultCode.ORDER_NOT_EXIST);
        }

        //检测是否可以添加客户评价
        Member member;

        checkMemberEvaluation(orderItem, order);

        if (Boolean.TRUE.equals(isSelf)) {
            //自我评价商品时，获取当前登录用户信息
            member = memberService.getUserInfo();
        } else {
            //获取用户信息 非自己评价时，读取数据库
            member = memberService.getById(order.getMemberId());
            if (member == null) {
                throw new ServiceException(ResultCode.USER_NOT_EXIST);
            }
        }

        //获取商品信息
        GoodsSkuDTO goodsSku = goodsSkuFeignClient.findById(memberEvaluationDTO.getSkuId());

        if (goodsSku == null) {
            throw new ServiceException(ResultCode.GOODS_NOT_EXIST);
        }

        //新增用户评价
        MemberEvaluation memberEvaluation = new MemberEvaluation(memberEvaluationDTO, goodsSku, member, order);
        //过滤商品咨询敏感词
        memberEvaluation.setContent(SensitiveWordsFilter.filter(memberEvaluation.getContent()));
        //添加评价
        this.save(memberEvaluation);

        //修改订单货物评价状态为已评价
        orderFeignClient.updateCommentStatus(orderItem.getSn(), "FINISHED");
        //发送商品评价消息
        applicationEventPublisher.publishEvent(new TransactionCommitSendMQEvent("同步商品评价消息",
                rocketmqCustomProperties.getGoodsTopic(), GoodsTagsEnum.GOODS_COMMENT_COMPLETE.name(), JSONUtil.toJsonStr(memberEvaluation)));
        return memberEvaluationDTO;
    }

    @Override
    public MemberEvaluationVO queryById(String id) {
        return new MemberEvaluationVO(this.getById(id));
    }

    @Override
    public boolean updateStatus(String id, String status) {
        UpdateWrapper updateWrapper = Wrappers.update();
        updateWrapper.eq("id", id);
        updateWrapper.set("status", status.equals(SwitchEnum.OPEN.name()) ? SwitchEnum.OPEN.name() : SwitchEnum.CLOSE.name());
        return this.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTop(String id, Boolean top) {
        // 取消置顶时，只更新当前评论。
        if (!Boolean.TRUE.equals(top)) {
            LambdaUpdateWrapper<MemberEvaluation> updateWrapper = Wrappers.lambdaUpdate();
            updateWrapper.eq(MemberEvaluation::getId, id);
            updateWrapper.set(MemberEvaluation::getTop, false);
            return this.update(updateWrapper);
        }

        MemberEvaluation memberEvaluation = this.getById(id);
        if (memberEvaluation == null) {
            return false;
        }

        // 同一商品只能保留一条置顶评论，先清空其他评论的置顶状态。
        LambdaUpdateWrapper<MemberEvaluation> cancelTopWrapper = Wrappers.lambdaUpdate();
        cancelTopWrapper.eq(MemberEvaluation::getGoodsId, memberEvaluation.getGoodsId());
        cancelTopWrapper.eq(MemberEvaluation::getDeleteFlag, false);
        cancelTopWrapper.ne(MemberEvaluation::getId, id);
        cancelTopWrapper.set(MemberEvaluation::getTop, false);
        this.update(cancelTopWrapper);

        // 最后再将当前评论设为置顶。
        LambdaUpdateWrapper<MemberEvaluation> topWrapper = Wrappers.lambdaUpdate();
        topWrapper.eq(MemberEvaluation::getId, id);
        topWrapper.set(MemberEvaluation::getTop, true);
        return this.update(topWrapper);
    }

    @Override
    public boolean delete(String id) {
        LambdaUpdateWrapper<MemberEvaluation> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(MemberEvaluation::getDeleteFlag, true);
        updateWrapper.eq(MemberEvaluation::getId, id);
        return this.update(updateWrapper);
    }

    @Override
    public boolean reply(String id, String reply, String replyImage) {
        UpdateWrapper<MemberEvaluation> updateWrapper = Wrappers.update();
        updateWrapper.set("reply_status", true);
        updateWrapper.set("reply", reply);
        if (CharSequenceUtil.isNotEmpty(replyImage)) {
            updateWrapper.set("have_reply_image", true);
            updateWrapper.set("reply_image", replyImage);
        }
        updateWrapper.eq("id", id);
        return this.update(updateWrapper);
    }

    @Override
    public EvaluationNumberVO getEvaluationNumber(String goodsId) {
        EvaluationNumberVO evaluationNumberVO = new EvaluationNumberVO();
        List<Map<String, Object>> list = this.baseMapper.getEvaluationNumber(goodsId);


        Integer good = 0;
        Integer moderate = 0;
        Integer worse = 0;
        for (Map<String, Object> map : list) {
            if (map.get("grade").equals(EvaluationGradeEnum.GOOD.name())) {
                good = Integer.valueOf(map.get("num").toString());
            } else if (map.get("grade").equals(EvaluationGradeEnum.MODERATE.name())) {
                moderate = Integer.valueOf(map.get("num").toString());
            } else if (map.get("grade").equals(EvaluationGradeEnum.WORSE.name())) {
                worse = Integer.valueOf(map.get("num").toString());
            }
        }
        evaluationNumberVO.setAll(good + moderate + worse);
        evaluationNumberVO.setGood(good);
        evaluationNumberVO.setModerate(moderate);
        evaluationNumberVO.setWorse(worse);
        evaluationNumberVO.setHaveImage(this.count(new QueryWrapper<MemberEvaluation>()
                .eq("have_image", 1)
                .eq("status", SwitchEnum.OPEN.name())
                .eq("goods_id", goodsId)));

        return evaluationNumberVO;
    }

    @Override
    public long todayMemberEvaluation() {
        return this.count(new LambdaQueryWrapper<MemberEvaluation>().ge(MemberEvaluation::getCreateTime, DateUtil.beginOfDay(new DateTime())));
    }

    @Override
    public long getWaitReplyNum() {
        QueryWrapper<MemberEvaluation> queryWrapper = Wrappers.query();
        queryWrapper.eq(CharSequenceUtil.equals(UserContext.getCurrentUser().getRole().name(), UserEnums.STORE.name()),
                "store_id", UserContext.getCurrentUser().getStoreId());
        queryWrapper.eq("reply_status", false);
        return this.count(queryWrapper);
    }

    /**
     * 统计商品评价数量
     *
     * @param evaluationQueryParams 查询条件
     * @return 商品评价数量
     */
    @Override
    public long getEvaluationCount(EvaluationQueryParams evaluationQueryParams) {
        return this.count(evaluationQueryParams.queryWrapper());
    }

    @Override
    public List<Map<String, Object>> memberEvaluationNum(DateTime startDate, DateTime endDate) {
        return this.baseMapper.memberEvaluationNum(new QueryWrapper<MemberEvaluation>()
                .between("create_time", startDate, endDate));
    }

    @Override
    public StoreRatingVO getStoreRatingVO(String storeId, String status) {
        LambdaQueryWrapper<MemberEvaluation> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(MemberEvaluation::getStoreId, storeId);
        lambdaQueryWrapper.eq(MemberEvaluation::getStatus, SwitchEnum.OPEN.name());
        return this.baseMapper.getStoreRatingVO(lambdaQueryWrapper);
    }

    /**
     * 检测客户评价
     *
     * @param orderItem 子订单
     * @param order     订单
     */
    public void checkMemberEvaluation(OrderFeignClient.OrderItemSummaryDTO orderItem,
                                      OrderFeignClient.OrderSummaryDTO order) {

        //根据子订单编号判断是否评价过
        if ("FINISHED".equals(orderItem.getCommentStatus())) {
            throw new ServiceException(ResultCode.EVALUATION_DOUBLE_ERROR);
        }

        //判断是否是当前客户的订单
        if (UserContext.getCurrentUser() != null && !order.getMemberId().equals(UserContext.getCurrentUser().getId())) {
            throw new ServiceException(ResultCode.ORDER_NOT_USER);
        }
    }

}

package com.xzj.wallet.serviceimpl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.text.CharSequenceUtil;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.utils.SnowFlake;
import cn.lili.common.vo.PageVO;
import com.xzj.order.entity.enums.PayStatusEnum;
import com.xzj.order.trade.entity.vo.RechargeQueryVO;
import com.xzj.payment.entity.enums.PaymentMethodEnum;
import com.xzj.wallet.entity.dos.Recharge;
import com.xzj.wallet.entity.dto.MemberWalletUpdateDTO;
import com.xzj.wallet.entity.enums.DepositServiceTypeEnum;
import com.xzj.wallet.mapper.RechargeMapper;
import com.xzj.wallet.service.MemberWalletService;
import com.xzj.wallet.service.RechargeService;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

/**
 * 预存款业务层实现
 *
 * @author pikachu
 * @since 2020-02-25 14:10:16
 */
@Service("walletRechargeService")
public class RechargeServiceImpl extends ServiceImpl<RechargeMapper, Recharge> implements RechargeService {

    /**
     * 客户预存款
     */
    @Autowired
    @Lazy
    private MemberWalletService memberWalletService;

    @Override
    public Recharge recharge(Double price) {

        if (price == null || price <= 0 || price > 1000000) {
            throw new ServiceException(ResultCode.RECHARGE_PRICE_ERROR);
        }

        //获取当前登录的客户
        AuthUser authUser = UserContext.getCurrentUser();
        //构建sn
        String sn = "Y" + SnowFlake.getId();
        //整合充值订单数据
        Recharge recharge = new Recharge(sn, authUser.getId(), authUser.getUsername(), price);
        //添加预存款充值账单
        this.save(recharge);
        //返回预存款
        return recharge;
    }

    @Override
    public IPage<Recharge> rechargePage(PageVO page, RechargeQueryVO rechargeQueryVO) {
        //构建查询条件
        QueryWrapper<Recharge> queryWrapper = new QueryWrapper<>();
        //客户名称
        queryWrapper.like(!CharSequenceUtil.isEmpty(rechargeQueryVO.getMemberName()), "member_name", rechargeQueryVO.getMemberName());
        //充值订单号
        queryWrapper.eq(!CharSequenceUtil.isEmpty(rechargeQueryVO.getRechargeSn()), "recharge_sn", rechargeQueryVO.getRechargeSn());
        //客户id
        queryWrapper.eq(!CharSequenceUtil.isEmpty(rechargeQueryVO.getMemberId()), "member_id", rechargeQueryVO.getMemberId());
        //支付时间 开始时间和结束时间
        if (!CharSequenceUtil.isEmpty(rechargeQueryVO.getStartDate()) && !CharSequenceUtil.isEmpty(rechargeQueryVO.getEndDate())) {
            Date start = cn.hutool.core.date.DateUtil.parse(rechargeQueryVO.getStartDate());
            Date end = cn.hutool.core.date.DateUtil.parse(rechargeQueryVO.getEndDate());
            queryWrapper.between("pay_time", start, end);
        }
        queryWrapper.orderByDesc("create_time");
        //查询返回数据
        return this.page(PageUtil.initPage(page), queryWrapper);
    }

    @Override
    public void paySuccess(String sn, String receivableNo, String paymentMethod) {
        //根据sn获取支付账单
        Recharge recharge = this.getOne(new QueryWrapper<Recharge>().eq("recharge_sn", sn));
        //如果支付账单不为空则进行一下逻辑
        if (recharge != null && !recharge.getPayStatus().equals(PayStatusEnum.PAID.name())) {
            //将此账单支付状态更改为已支付
            recharge.setPayStatus(PayStatusEnum.PAID.name());
            recharge.setReceivableNo(receivableNo);
            recharge.setPayTime(new DateTime());
            recharge.setRechargeWay(paymentMethod);
            //执行保存操作
            this.updateById(recharge);
            //增加预存款余额
            memberWalletService.increase(new MemberWalletUpdateDTO(recharge.getRechargeMoney(), recharge.getMemberId(), "客户余额充值，充值单号为：" + recharge.getRechargeSn(), DepositServiceTypeEnum.WALLET_RECHARGE.name()));
        }
    }

    @Override
    public Recharge getRecharge(String sn) {
        Recharge recharge = this.getOne(new QueryWrapper<Recharge>().eq("recharge_sn", sn));
        if (recharge != null) {
            return recharge;
        }
        throw new ServiceException(ResultCode.ORDER_NOT_EXIST);
    }


    @Override
    public void rechargeOrderCancel(String sn) {
        Recharge recharge = this.getOne(new QueryWrapper<Recharge>().eq("recharge_sn", sn));
        if (recharge != null) {
            recharge.setPayStatus(PayStatusEnum.CANCEL.name());
            this.updateById(recharge);
        }
    }

    @Override
    public Double getRecharge(Date[] dates, PaymentMethodEnum paymentMethodEnum) {
        LambdaQueryWrapper<Recharge> queryWrapper = new LambdaQueryWrapper<Recharge>();
        queryWrapper.eq(Recharge::getPayStatus, PayStatusEnum.PAID.name());
        queryWrapper.between(Recharge::getPayTime, dates[0], dates[1]);
        if(Objects.nonNull(paymentMethodEnum)){
            queryWrapper.eq(Recharge::getRechargeWay,paymentMethodEnum.name());
        }
        return this.baseMapper.getRecharge(queryWrapper);
    }
}
package com.xzj.promotion.distribution.serviceimpl;

import cn.hutool.json.JSONUtil;
import cn.lili.cache.Cache;
import cn.lili.cache.CachePrefix;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.utils.BeanUtil;
import cn.lili.common.vo.PageVO;
import com.xzj.promotion.distribution.entity.dos.Distribution;
import com.xzj.promotion.distribution.entity.dto.DistributionApplyDTO;
import com.xzj.promotion.distribution.entity.dto.DistributionSearchParams;
import com.xzj.promotion.distribution.entity.enums.DistributionStatusEnum;
import com.xzj.promotion.distribution.mapper.DistributionMapper;
import com.xzj.promotion.distribution.service.DistributionService;
import com.xzj.member.MemberFeignClient;
import com.xzj.member.MemberFeignClient.MemberSummaryDTO;
import com.xzj.goods.config.dto.DistributionSetting;
import com.xzj.goods.config.enums.SettingEnum;
import com.xzj.message.SettingFeignClient;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 分销员接口实现
 *
 * @author pikachu
 * @since 2020-03-14 23:04:56
 */
@Service("goodsDistributionDistributionServiceImpl")
public class DistributionServiceImpl extends ServiceImpl<DistributionMapper, Distribution> implements DistributionService {

    /**
     * 客户
     */
    @Autowired
    private MemberFeignClient memberClient;
    /**
     * 缓存
     */
    @Autowired
    private Cache cache;
    /**
     * 设置
     */
    @Autowired
    private SettingFeignClient settingFeignClient;

    @Override
    public IPage<Distribution> distributionPage(DistributionSearchParams distributionSearchParams, PageVO page) {
        return this.page(PageUtil.initPage(page), distributionSearchParams.queryWrapper());
    }

    @Override
    public Distribution getDistribution() {

        return this.getOne(new LambdaQueryWrapper<Distribution>().eq(Distribution::getMemberId, UserContext.getCurrentUser().getId()));

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Distribution applyDistribution(DistributionApplyDTO distributionApplyDTO) {

        //检查分销开关
        checkDistributionSetting();

        //判断用户是否申请过分销
        Distribution distribution = getDistribution();

        //如果分销员非空并未审核则提示用户请等待，如果分销员为拒绝状态则重新提交申请
        if (Optional.ofNullable(distribution).isPresent()) {
            switch (DistributionStatusEnum.valueOf(distribution.getDistributionStatus())) {
                case REFUSE:
                case RETREAT:
                    distribution.setDistributionStatus(DistributionStatusEnum.APPLY.name());
                    BeanUtil.copyProperties(distributionApplyDTO, distribution);
                    this.updateById(distribution);
                    return distribution;
                default:
                    throw new ServiceException(ResultCode.DISTRIBUTION_IS_APPLY);
            }
        } else {
            //如果未申请分销员则新增进行申请
            //获取当前登录用户
            MemberSummaryDTO member = memberClient.getMemberById(UserContext.getCurrentUser().getId());
            //新建分销员
            distribution = new Distribution(member.getId(), member.getNickName(), distributionApplyDTO);
            //添加分销员
            this.save(distribution);
        }
        return distribution;
    }

    @Override
    public boolean audit(String id, String status) {

        //检查分销开关
        checkDistributionSetting();

        //根据id获取分销员
        Distribution distribution = this.getById(id);
        if (Optional.ofNullable(distribution).isPresent()) {
            if (status.equals(DistributionStatusEnum.PASS.name())) {
                distribution.setDistributionStatus(DistributionStatusEnum.PASS.name());
            } else {
                distribution.setDistributionStatus(DistributionStatusEnum.REFUSE.name());
            }
            return this.updateById(distribution);
        }
        return false;
    }

    @Override
    public boolean retreat(String id) {

        //检查分销开关
        checkDistributionSetting();

        //根据id获取分销员
        Distribution distribution = this.getById(id);
        if (Optional.ofNullable(distribution).isPresent()) {
            distribution.setDistributionStatus(DistributionStatusEnum.RETREAT.name());
            return this.updateById(distribution);
        }
        return false;
    }

    @Override
    public boolean resume(String id) {

        //检查分销开关
        checkDistributionSetting();

        //根据id获取分销员
        Distribution distribution = this.getById(id);
        if (Optional.ofNullable(distribution).isPresent()) {
            distribution.setDistributionStatus(DistributionStatusEnum.PASS.name());
            return this.updateById(distribution);
        }
        return false;
    }

    @Override
    public void bindingDistribution(String distributionId) {

        //判断用户是否登录，未登录不能进行绑定
        if (UserContext.getCurrentUser() == null) {
            throw new ServiceException(ResultCode.USER_NOT_LOGIN);
        }
        //储存分销关系时间
        Distribution distribution = this.getById(distributionId);
        if (distribution != null) {
            SettingFeignClient.SettingDTO setting = settingFeignClient.get(SettingEnum.DISTRIBUTION_SETTING.name());
            DistributionSetting distributionSetting = JSONUtil.toBean(setting.getSettingValue(), DistributionSetting.class);
            cache.put(CachePrefix.DISTRIBUTION.getPrefix() + "_" + UserContext.getCurrentUser().getId(), distribution.getId(), distributionSetting.getDistributionDay().longValue(), TimeUnit.DAYS);
        }

    }

    /**
     * 检查分销设置开关
     */
    @Override
    public void checkDistributionSetting() {
        //获取分销是否开启
        SettingFeignClient.SettingDTO setting = settingFeignClient.get(SettingEnum.DISTRIBUTION_SETTING.name());
        DistributionSetting distributionSetting = JSONUtil.toBean(setting.getSettingValue(), DistributionSetting.class);
        if (Boolean.FALSE.equals(distributionSetting.getIsOpen())) {
            throw new ServiceException(ResultCode.DISTRIBUTION_CLOSE);
        }
    }


    @Override
    public void subRebate(Double canRebate, String distributionId, Double distributionOrderPrice) {
        this.baseMapper.subRebate(canRebate, distributionId, distributionOrderPrice);
    }

    @Override
    public void addRebate(Double rebate, String distributionId, Double distributionOrderPrice) {
        this.baseMapper.addRebate(rebate, distributionId, distributionOrderPrice);
    }

    @Override
    public void addCanRebate(Double rebate, String distributionId) {
        this.baseMapper.addCanRebate(rebate, distributionId);
    }

    @Override
    public void addCashRebate(Double rebate, String distributionId) {
        this.baseMapper.addCashRebate(rebate, distributionId);
    }

    @Override
    public void subCashRebate(Double rebate, String distributionId) {
        this.baseMapper.subCashRebate(rebate, distributionId);
    }

}

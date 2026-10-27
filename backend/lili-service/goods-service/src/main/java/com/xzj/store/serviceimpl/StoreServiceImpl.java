package com.xzj.store.serviceimpl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.text.CharSequenceUtil;
import cn.lili.cache.Cache;
import cn.lili.cache.CachePrefix;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.properties.RocketmqCustomProperties;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.utils.BeanUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.message.LiliMessageTemplate;
import cn.lili.message.MessageSendCallbackBuilder;
import com.xzj.goods.goods.entity.dos.GoodsSku;
import com.xzj.goods.goods.service.GoodsService;
import com.xzj.goods.goods.service.GoodsSkuService;
import com.xzj.member.MemberFeignClient;
import com.xzj.member.MemberFeignClient.MemberSummaryDTO;
import com.xzj.store.entity.dos.Store;
import com.xzj.store.entity.dos.StoreDetail;
import com.xzj.store.entity.dto.*;
import com.xzj.store.entity.enums.StoreStatusEnum;
import com.xzj.store.entity.vos.StoreSearchParams;
import com.xzj.store.entity.vos.StoreVO;
import com.xzj.store.mapper.StoreMapper;
import com.xzj.store.service.StoreDetailService;
import com.xzj.store.service.StoreService;
import cn.lili.mybatis.util.PageUtil;
import cn.lili.rocketmq.tags.StoreTagsEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 店铺业务层实现
 *
 * @author pikachu
 * @since 2020-03-07 16:18:56
 */
@Service
public class StoreServiceImpl extends ServiceImpl<StoreMapper, Store> implements StoreService {

    /**
     * 客户
     */
    @Autowired
    @Lazy
    private MemberFeignClient memberClient;

    /**
     * 店员
     */
    /**
     * 商品
     */
    @Autowired
    @Lazy
    private GoodsService goodsService;

    @Autowired
    @Lazy
    private GoodsSkuService goodsSkuService;
    /**
     * 店铺详情
     */
    @Autowired
    @Lazy
    private StoreDetailService storeDetailService;

    @Autowired
    private RocketmqCustomProperties rocketmqCustomProperties;

    @Autowired
    private LiliMessageTemplate liliMessageTemplate;

    @Autowired
    @Lazy
    private Cache cache;

    @Override
    public IPage<StoreVO> findByConditionPage(StoreSearchParams storeSearchParams, PageVO page) {
        return this.baseMapper.getStoreList(PageUtil.initPage(page), storeSearchParams.queryWrapper());
    }

    @Override
    public StoreVO getStoreDetail() {
        AuthUser currentUser = Objects.requireNonNull(UserContext.getCurrentUser());
        StoreVO storeVO = this.baseMapper.getStoreDetail(currentUser.getStoreId());
        storeVO.setNickName(currentUser.getNickName());
        return storeVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Store add(AdminStoreApplyDTO adminStoreApplyDTO) {

        //判断店铺名称是否存在
        QueryWrapper<Store> queryWrapper = Wrappers.query();
        queryWrapper.eq("store_name", adminStoreApplyDTO.getStoreName());
        if (this.getOne(queryWrapper) != null) {
            throw new ServiceException(ResultCode.STORE_NAME_EXIST_ERROR);
        }

        MemberSummaryDTO member = memberClient.getMemberById(adminStoreApplyDTO.getMemberId());
        //判断用户是否存在
        if (member == null) {
            throw new ServiceException(ResultCode.USER_NOT_EXIST);
        }
        //判断是否拥有店铺
        if (Boolean.TRUE.equals(member.getHaveStore())) {
            throw new ServiceException(ResultCode.STORE_APPLY_DOUBLE_ERROR);
        }

        //添加店铺
        Store store = new Store(member, adminStoreApplyDTO);
        this.save(store);

        //判断是否存在店铺详情，如果没有则进行新建，如果存在则进行修改
        StoreDetail storeDetail = new StoreDetail(store, adminStoreApplyDTO);

        storeDetailService.save(storeDetail);

        //设置客户-店铺信息
        memberClient.updateStore(member.getId(), true, store.getId());
        return store;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Store edit(StoreEditDTO storeEditDTO) {
        if (storeEditDTO != null) {
            //判断店铺名是否唯一
            Store storeTmp = getOne(new QueryWrapper<Store>().eq("store_name", storeEditDTO.getStoreName()));
            if (storeTmp != null && !CharSequenceUtil.equals(storeTmp.getId(), storeEditDTO.getStoreId())) {
                throw new ServiceException(ResultCode.STORE_NAME_EXIST_ERROR);
            }
            //修改店铺详细信息
            updateStoreDetail(storeEditDTO);
            //修改店铺信息
            return updateStore(storeEditDTO);
        } else {
            throw new ServiceException(ResultCode.STORE_NOT_EXIST);
        }
    }

    /**
     * 修改店铺基本信息
     *
     * @param storeEditDTO 修改店铺信息
     */
    private Store updateStore(StoreEditDTO storeEditDTO) {
        Store store = this.getById(storeEditDTO.getStoreId());
        if (store != null) {
            BeanUtil.copyProperties(storeEditDTO, store);
            store.setId(storeEditDTO.getStoreId());
            boolean result = this.updateById(store);
            if (result) {
                storeDetailService.updateStoreGoodsInfo(store);
            }
            String destination = rocketmqCustomProperties.getStoreTopic() + ":" + StoreTagsEnum.EDIT_STORE_SETTING.name();
            //发送订单变更mq消息
            liliMessageTemplate.asyncSend(destination, store, MessageSendCallbackBuilder.commonCallback());
        }

        cache.remove(CachePrefix.STORE.getPrefix() + storeEditDTO.getStoreId());
        return store;
    }

    /**
     * 修改店铺详细信息
     *
     * @param storeEditDTO 修改店铺信息
     */
    private void updateStoreDetail(StoreEditDTO storeEditDTO) {
        StoreDetail storeDetail = new StoreDetail();
        BeanUtil.copyProperties(storeEditDTO, storeDetail);
        storeDetailService.update(storeDetail, new QueryWrapper<StoreDetail>().eq("store_id", storeEditDTO.getStoreId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean audit(String id, Integer passed) {
        Store store = this.getById(id);
        if (store == null) {
            throw new ServiceException(ResultCode.STORE_NOT_EXIST);
        }
        if (passed == 0) {
            store.setStoreDisable(StoreStatusEnum.OPEN.value());
            //修改客户 表示已有店铺
            memberClient.updateStore(store.getMemberId(), true, id);
            //设定商家的结算日
            storeDetailService.update(new LambdaUpdateWrapper<StoreDetail>()
                    .eq(StoreDetail::getStoreId, id)
                    .set(StoreDetail::getSettlementDay, new DateTime()));
        } else {
            store.setStoreDisable(StoreStatusEnum.REFUSED.value());
        }
        cache.remove(CachePrefix.STORE.getPrefix() + store.getId());
        return this.updateById(store);
    }

    @Override
    public boolean disable(String id) {
        Store store = this.getById(id);
        if (store != null) {

            LambdaUpdateWrapper<Store> storeLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            storeLambdaUpdateWrapper.eq(Store::getId, id);
            storeLambdaUpdateWrapper.set(Store::getStoreDisable, StoreStatusEnum.CLOSED.value());
            boolean update = this.update(storeLambdaUpdateWrapper);
            //下架所有此店铺商品
            if (update) {
                cache.remove(CachePrefix.STORE.getPrefix() + id);
                goodsService.underStoreGoods(id);
            }

            return update;
        }

        throw new ServiceException(ResultCode.STORE_NOT_EXIST);
    }

    @Override
    public boolean enable(String id) {
        Store store = this.getById(id);
        if (store != null) {
            store.setStoreDisable(StoreStatusEnum.OPEN.value());
            boolean update = this.updateById(store);
            if (update) {
                cache.remove(CachePrefix.STORE.getPrefix() + id);
            }
            return update;
        }
        throw new ServiceException(ResultCode.STORE_NOT_EXIST);
    }

    @Override
    public boolean applyFirstStep(StoreCompanyDTO storeCompanyDTO) {
        //获取当前操作的店铺
        Store store = getStoreByMember();

        //店铺为空，则新增店铺
        if (store == null) {
            AuthUser authUser = Objects.requireNonNull(UserContext.getCurrentUser());
            MemberSummaryDTO member = memberClient.getMemberById(authUser.getId());
            //根据客户创建店铺
            store = new Store(member);
            BeanUtil.copyProperties(storeCompanyDTO, store);
            this.save(store);
            StoreDetail storeDetail = new StoreDetail();
            storeDetail.setStoreId(store.getId());
            BeanUtil.copyProperties(storeCompanyDTO, storeDetail);
            return storeDetailService.save(storeDetail);
        } else {

            //校验迪纳普状态
            checkStoreStatus(store);
            //复制参数 修改已存在店铺
            BeanUtil.copyProperties(storeCompanyDTO, store);
            this.updateById(store);
            //判断是否存在店铺详情，如果没有则进行新建，如果存在则进行修改
            StoreDetail storeDetail = storeDetailService.getStoreDetail(store.getId());
            //如果店铺详情为空，则new ，否则复制对象，然后保存即可。
            if (storeDetail == null) {
                storeDetail = new StoreDetail();
                storeDetail.setStoreId(store.getId());
                BeanUtil.copyProperties(storeCompanyDTO, storeDetail);
                return storeDetailService.save(storeDetail);
            } else {
                BeanUtil.copyProperties(storeCompanyDTO, storeDetail);
                return storeDetailService.updateById(storeDetail);
            }
        }
    }

    @Override
    public boolean applySecondStep(StoreBankDTO storeBankDTO) {

        //获取当前操作的店铺
        Store store = getStoreByMember();
        //校验店铺状态
        checkStoreStatus(store);
        StoreDetail storeDetail = storeDetailService.getStoreDetail(store.getId());
        //设置店铺的银行信息
        BeanUtil.copyProperties(storeBankDTO, storeDetail);
        return storeDetailService.updateById(storeDetail);
    }

    @Override
    public boolean applyThirdStep(StoreOtherInfoDTO storeOtherInfoDTO) {
        //获取当前操作的店铺
        Store store = getStoreByMember();

        //校验店铺状态
        checkStoreStatus(store);
        BeanUtil.copyProperties(storeOtherInfoDTO, store);

        StoreDetail storeDetail = storeDetailService.getStoreDetail(store.getId());
        //设置店铺的其他信息
        BeanUtil.copyProperties(storeOtherInfoDTO, storeDetail);
        //设置店铺经营范围
        storeDetail.setGoodsManagementCategory(storeOtherInfoDTO.getGoodsManagementCategory());
        //最后一步申请，给予店铺设置库存预警默认值
        storeDetail.setStockWarning(10);
        //修改店铺详细信息
        storeDetailService.updateById(storeDetail);
        //设置店铺名称,修改店铺信息
        store.setStoreDisable(StoreStatusEnum.APPLYING.name());
        return this.updateById(store);
    }

    /**
     * 申请店铺时 对店铺状态进行校验判定
     *
     * @param store 店铺
     */
    private void checkStoreStatus(Store store) {

        //如果店铺状态为已开启、已关闭、申请中，则抛出异常
        if (store.getStoreDisable().equals(StoreStatusEnum.OPEN.name())
                || store.getStoreDisable().equals(StoreStatusEnum.CLOSED.name())
                || store.getStoreDisable().equals(StoreStatusEnum.APPLYING.name())
        ) {
            throw new ServiceException(ResultCode.STORE_STATUS_ERROR);
        }

    }

    @Override
    public void updateStoreGoodsNum(String storeId, Long num) {
        //修改店铺商品数量
        this.update(new LambdaUpdateWrapper<Store>()
                .set(Store::getGoodsNum, num)
                .eq(Store::getId, storeId));
    }

    @Override
    public void updateStoreCollectionNum(com.xzj.store.entity.dto.StoreCollectionCommand command) {
        baseMapper.updateCollection(command.getId(), command.getNum());
    }

    @Override
    public void storeToClerk() {
        //清空店铺信息方便重新导入不会有重复数据
        // 店员账号属于 member-service，由卖家端流程负责创建。
    }

    @Override
    public List<GoodsSku> getToMemberHistory(String memberId) {
        AuthUser currentUser = UserContext.getCurrentUser();
        List<String> skuIdList = new ArrayList<>();
        for (MemberFeignClient.FootprintDTO footprint : memberClient.getFootprints(memberId, currentUser.getStoreId())) {
            if (footprint.getSkuId() != null) {
                skuIdList.add(footprint.getSkuId());
            }
        }
        return goodsSkuService.getGoodsSkuByIdFromCache(skuIdList);
    }

    @Override
    public List<Store> listOpenStores() {
        return this.list(new LambdaQueryWrapper<Store>().eq(Store::getStoreDisable, StoreStatusEnum.OPEN.name()));
    }

    @Override
    public Store getStoreByMemberId(String memberId) {
        if (CharSequenceUtil.isBlank(memberId)) {
            return null;
        }
        return this.getOne(new LambdaQueryWrapper<Store>().eq(Store::getMemberId, memberId).last("limit 1"), false);
    }

    /**
     * 获取当前登录操作的店铺
     *
     * @return 店铺信息
     */
    private Store getStoreByMember() {
        LambdaQueryWrapper<Store> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (UserContext.getCurrentUser() != null) {
            lambdaQueryWrapper.eq(Store::getMemberId, UserContext.getCurrentUser().getId());
        }
        return this.getOne(lambdaQueryWrapper, false);
    }

}

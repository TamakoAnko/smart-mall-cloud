package com.xzj.store.serviceimpl;

import cn.lili.common.vo.PageVO;
import com.xzj.store.entity.dos.StoreAddress;
import com.xzj.store.mapper.GoodsStoreAddressMapper;
import com.xzj.store.service.StoreAddressService;
import cn.lili.mybatis.util.PageUtil;
import org.springframework.context.annotation.Primary;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 店铺地址（自提点）业务层实现
 *
 * @author Bulbasaur
 * @since 2020/11/22 16:00
 */
@Service
@Primary
public class GoodsStoreAddressServiceImpl extends ServiceImpl<GoodsStoreAddressMapper, StoreAddress> implements StoreAddressService {

    @Override
    public IPage<StoreAddress> getStoreAddress(String storeId, PageVO pageVo) {
        //获取当前登录商家账号
        LambdaQueryWrapper<StoreAddress> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(StoreAddress::getStoreId, storeId);
        return this.page(PageUtil.initPage(pageVo), lambdaQueryWrapper);
    }

    @Override
    public StoreAddress addStoreAddress(String storeId, StoreAddress storeAddress) {
        //获取当前登录商家账号
        storeAddress.setStoreId(storeId);
        //添加自提点
        this.save(storeAddress);
        return storeAddress;
    }

    @Override
    public StoreAddress editStoreAddress(String storeId, StoreAddress storeAddress) {
        //获取当前登录商家账号
        storeAddress.setStoreId(storeId);
        //添加自提点
        this.updateById(storeAddress);
        return storeAddress;
    }

    @Override
    public Boolean removeStoreAddress(String id) {
        return this.removeById(id);
    }
}
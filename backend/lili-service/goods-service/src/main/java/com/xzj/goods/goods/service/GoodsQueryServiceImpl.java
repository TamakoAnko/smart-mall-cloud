package com.xzj.goods.goods.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xzj.goods.goods.entity.Goods;
import com.xzj.goods.goods.entity.GoodsVO;
import com.xzj.goods.goods.mapper.GoodsEntityMapper;
import org.springframework.stereotype.Service;

/**
 * 商品查询服务实现。
 */
@Service
public class GoodsQueryServiceImpl implements GoodsQueryService {

    private final GoodsEntityMapper goodsMapper;

    /**
     * 注入商品数据访问对象。
     */
    public GoodsQueryServiceImpl(GoodsEntityMapper goodsMapper) {
        this.goodsMapper = goodsMapper;
    }

    /**
     * 查询未删除商品，并转换为接口返回对象。
     */
    @Override
    public GoodsVO getGoodsVO(String goodsId) {
        // 查询条件限定商品 ID 和未删除状态。
        Goods goods = goodsMapper.selectOne(new LambdaQueryWrapper<Goods>()
                .eq(Goods::getId, goodsId)
                .eq(Goods::getDeleteFlag, false));
        if (goods == null) {
            return null;
        }
        GoodsVO view = new GoodsVO();
        view.setId(goods.getId());
        view.setCreateBy(goods.getCreateBy());
        view.setCreateTime(goods.getCreateTime());
        view.setUpdateBy(goods.getUpdateBy());
        view.setUpdateTime(goods.getUpdateTime());
        view.setDeleteFlag(goods.getDeleteFlag());
        view.setGoodsName(goods.getGoodsName());
        view.setPrice(goods.getPrice());
        view.setCategoryPath(goods.getCategoryPath());
        view.setSellingPoint(goods.getSellingPoint());
        view.setMarketEnable(goods.getMarketEnable());
        view.setQuantity(goods.getQuantity());
        view.setGrade(goods.getGrade());
        view.setThumbnail(goods.getThumbnail());
        view.setSmall(goods.getSmall());
        view.setOriginal(goods.getOriginal());
        view.setCommentNum(goods.getCommentNum());
        view.setStoreId(goods.getStoreId());
        view.setStoreName(goods.getStoreName());
        view.setGoodsType(goods.getGoodsType());
        return view;
    }

    /**
     * 查询已上架商品分页，并支持按分类路径筛选。
     */
    @Override
    public IPage<Goods> page(Integer pageNumber, Integer pageSize, String categoryId) {
        // 分页条件只包含未删除且已上架的商品。
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<Goods>()
                .eq(Goods::getDeleteFlag, false)
                .eq(Goods::getMarketEnable, "UPPER")
                .orderByDesc(Goods::getCreateTime);
        if (categoryId != null && !categoryId.isBlank()) {
            wrapper.like(Goods::getCategoryPath, categoryId);
        }
        return goodsMapper.selectPage(new Page<>(pageNumber, pageSize), wrapper);
    }
}
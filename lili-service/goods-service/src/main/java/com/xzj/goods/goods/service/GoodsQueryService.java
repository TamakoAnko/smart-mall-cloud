package com.xzj.goods.goods.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xzj.goods.goods.entity.Goods;
import com.xzj.goods.goods.entity.GoodsVO;

/**
 * 商品查询服务。
 */
public interface GoodsQueryService {

    /**
     * 根据商品 ID 查询商品详情。
     */
    GoodsVO getGoodsVO(String goodsId);

    /**
     * 查询已上架商品分页，可按分类筛选。
     */
    IPage<Goods> page(Integer pageNumber, Integer pageSize, String categoryId);
}
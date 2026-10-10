package com.xzj.goods.goods.service;

import com.xzj.goods.goods.entity.GoodsSku;
import com.xzj.goods.goods.entity.GoodsSkuQueryParams;

import java.util.List;
import java.util.Map;

/**
 * 商品 SKU 查询服务。
 */
public interface GoodsSkuQueryService {

    /**
     * 查询指定商品下的 SKU 详情。
     */
    Map<String, Object> getGoodsSkuDetail(String goodsId, String skuId);

    /**
     * 根据查询条件查询 SKU 列表。
     */
    List<GoodsSku> list(GoodsSkuQueryParams queryParams);
}
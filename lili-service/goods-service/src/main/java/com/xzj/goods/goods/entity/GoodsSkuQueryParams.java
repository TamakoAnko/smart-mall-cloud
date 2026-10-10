package com.xzj.goods.goods.entity;

import lombok.Data;

import java.util.List;

@Data
public class GoodsSkuQueryParams {
    private String goodsId;
    private String skuId;
    private List<String> skuIds;
    private String goodsName;
    private String sn;
    private String simpleSpecs;
    private String storeId;
    private String categoryPath;
    private String marketEnable;
}
package com.xzj.goods.goods.entity;

import cn.lili.mybatis.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("li_goods_sku")
public class GoodsSku extends BaseEntity {
    private String goodsId;
    private String specs;
    private String simpleSpecs;
    private String goodsName;
    private String sn;
    private String categoryPath;
    private String sellingPoint;
    private String marketEnable;
    private Double price;
    private Double cost;
    private Integer quantity;
    private Double grade;
    private String thumbnail;
    private String big;
    private String small;
    private String original;
    private String storeId;
    private String storeName;
    private String authFlag;
    private String goodsType;
    private String brandId;
    private String storeCategoryPath;
    private Integer buyCount;
    private Integer virtualSales;
    private Integer commentNum;
    private Boolean selfOperated;
    private Boolean recommend;
    private String goodsVideo;
    private String mobileIntro;
}
package com.xzj.goods.goods.entity;

import cn.lili.mybatis.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("li_goods")
public class Goods extends BaseEntity {
    private String goodsName;
    private BigDecimal price;
    private String categoryPath;
    private String sellingPoint;
    private String marketEnable;
    private Integer quantity;
    private Double grade;
    private String thumbnail;
    private String small;
    private String original;
    private Integer commentNum;
    private String storeId;
    private String storeName;
    private String goodsType;
}

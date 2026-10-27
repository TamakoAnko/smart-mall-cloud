package com.xzj.goods.category.entity;

import cn.lili.mybatis.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("li_category")
public class Category extends BaseEntity {
    private String name;
    private String parentId;
    private Integer level;
    private BigDecimal sortOrder;
    private Double commissionRate;
    private String image;
    private Boolean supportChannel;
}

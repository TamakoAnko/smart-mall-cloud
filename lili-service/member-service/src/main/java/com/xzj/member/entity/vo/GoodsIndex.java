package com.xzj.member.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/** Member-service read model built from the goods-service SKU DTO. */
@Data
public class GoodsIndex implements Serializable {
    private String id;
    private String goodsId;
    private String goodsName;
    private String sn;
    private String storeId;
    private String storeName;
    private Integer buyCount;
    private String small;
    private String thumbnail;
    private String brandId;
    private String categoryPath;
    private String storeCategoryPath;
    private Double price;
    private Integer commentNum;
    private Double grade;
    private String intro;
    private String mobileIntro;
    private Boolean selfOperated;
    private Boolean recommend;
    private String salesModel;
    private String authFlag;
    private String sellingPoint;
    private String marketEnable;
    private String goodsVideo;
    private Date releaseTime;
    private String goodsType;
}

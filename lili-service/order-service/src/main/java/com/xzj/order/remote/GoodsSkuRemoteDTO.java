package com.xzj.order.remote;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/** SKU data returned by goods-service. It is a transport model, not a local goods entity. */
@Data
public class GoodsSkuRemoteDTO implements Serializable {
    private String id;
    private String goodsId;
    private String goodsName;
    private String simpleSpecs;
    private String thumbnail;
    private Double price;
    private Double cost;
    private Integer quantity;
    private String sn;
    private String storeId;
    private String storeName;
    private String categoryPath;
    private String storeCategoryPath;
    private String small;
    private String goodsVideo;
    private String mobileIntro;
    private String sellingPoint;
    private String marketEnable;
    private String authFlag;
    private String intro;
    private String salesModel;
    private String goodsType;
    private String brandId;
    private Integer buyCount;
    private Integer virtualSales;
    private Integer commentNum;
    private Double grade;
    private Boolean selfOperated;
    private Boolean recommend;
    private Boolean promotionFlag;
    private Double promotionPrice;
    private Date updateTime;
    private Date createTime;
    private String freightTemplateId;
    private String templateId;
}

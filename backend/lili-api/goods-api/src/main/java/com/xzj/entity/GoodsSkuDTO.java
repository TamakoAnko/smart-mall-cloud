package com.xzj.entity;

import java.io.Serializable;
import java.util.Date;

/** 足迹等会员业务展示商品时需要的 SKU 数据，不暴露商品服务实体。 */
public class GoodsSkuDTO implements Serializable {
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

    public String getId() { return id; }
    public void setId(String value) { id = value; }
    public String getGoodsId() { return goodsId; }
    public void setGoodsId(String value) { goodsId = value; }
    public String getGoodsName() { return goodsName; }
    public void setGoodsName(String value) { goodsName = value; }
    public String getSimpleSpecs() { return simpleSpecs; }
    public void setSimpleSpecs(String value) { simpleSpecs = value; }
    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String value) { thumbnail = value; }
    public Double getPrice() { return price; }
    public void setPrice(Double value) { price = value; }
    public Double getCost() { return cost; }
    public void setCost(Double value) { cost = value; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer value) { quantity = value; }
    public String getSn() { return sn; }
    public void setSn(String value) { sn = value; }
    public String getStoreId() { return storeId; }
    public void setStoreId(String value) { storeId = value; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String value) { storeName = value; }
    public String getCategoryPath() { return categoryPath; }
    public void setCategoryPath(String value) { categoryPath = value; }
    public String getStoreCategoryPath() { return storeCategoryPath; }
    public void setStoreCategoryPath(String value) { storeCategoryPath = value; }
    public String getSmall() { return small; }
    public void setSmall(String value) { small = value; }
    public String getGoodsVideo() { return goodsVideo; }
    public void setGoodsVideo(String value) { goodsVideo = value; }
    public String getMobileIntro() { return mobileIntro; }
    public void setMobileIntro(String value) { mobileIntro = value; }
    public String getSellingPoint() { return sellingPoint; }
    public void setSellingPoint(String value) { sellingPoint = value; }
    public String getMarketEnable() { return marketEnable; }
    public void setMarketEnable(String value) { marketEnable = value; }
    public String getAuthFlag() { return authFlag; }
    public void setAuthFlag(String value) { authFlag = value; }
    public String getIntro() { return intro; }
    public void setIntro(String value) { intro = value; }
    public String getSalesModel() { return salesModel; }
    public void setSalesModel(String value) { salesModel = value; }
    public String getGoodsType() { return goodsType; }
    public void setGoodsType(String value) { goodsType = value; }
    public String getBrandId() { return brandId; }
    public void setBrandId(String value) { brandId = value; }
    public Integer getBuyCount() { return buyCount; }
    public void setBuyCount(Integer value) { buyCount = value; }
    public Integer getVirtualSales() { return virtualSales; }
    public void setVirtualSales(Integer value) { virtualSales = value; }
    public Integer getCommentNum() { return commentNum; }
    public void setCommentNum(Integer value) { commentNum = value; }
    public Double getGrade() { return grade; }
    public void setGrade(Double value) { grade = value; }
    public Boolean getSelfOperated() { return selfOperated; }
    public void setSelfOperated(Boolean value) { selfOperated = value; }
    public Boolean getRecommend() { return recommend; }
    public void setRecommend(Boolean value) { recommend = value; }
    public Boolean getPromotionFlag() { return promotionFlag; }
    public void setPromotionFlag(Boolean value) { promotionFlag = value; }
    public Double getPromotionPrice() { return promotionPrice; }
    public void setPromotionPrice(Double value) { promotionPrice = value; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date value) { updateTime = value; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date value) { createTime = value; }
    public String getFreightTemplateId() { return freightTemplateId; }
    public void setFreightTemplateId(String value) { freightTemplateId = value; }
    public String getTemplateId() { return templateId; }
    public void setTemplateId(String value) { templateId = value; }
}

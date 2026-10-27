package com.xzj.goods.config.dto;

import lombok.Data;

import java.io.Serializable;

/** 商品服务使用的分销配置契约。 */
@Data
public class DistributionSetting implements Serializable {

    private Boolean isOpen;
    private Integer distributionDay;
}

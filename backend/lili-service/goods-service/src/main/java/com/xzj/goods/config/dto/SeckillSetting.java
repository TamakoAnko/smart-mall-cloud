package com.xzj.goods.config.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/** 商品服务使用的秒杀配置契约。 */
@Data
public class SeckillSetting implements Serializable {

    @NotNull
    private String hours;

    @NotNull
    private String seckillRule;
}

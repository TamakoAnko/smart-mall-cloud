package com.xzj.order.system.entity.dos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 设置
 * @author Chopper
 * @since 2020-02-25 14:10:16
 */
@Data
@Schema(description = "配置")
@NoArgsConstructor
public class Setting {

    @Schema(description = "配置值value")
    private String settingValue;

}

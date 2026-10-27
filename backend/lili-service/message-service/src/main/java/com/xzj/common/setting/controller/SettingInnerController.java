package com.xzj.common.setting.controller;

import com.xzj.message.SettingFeignClient;
import com.xzj.common.setting.entity.dos.Setting;
import com.xzj.common.setting.service.SettingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 配置内部接口，避免其他微服务依赖配置实体和 Mapper。 */
@RestController
@RequestMapping("/setting/inner")
public class SettingInnerController {
    private final SettingService settingService;

    public SettingInnerController(SettingService settingService) {
        this.settingService = settingService;
    }

    @GetMapping("/{key}")
    public SettingFeignClient.SettingDTO get(@PathVariable String key) {
        Setting source = settingService.get(key);
        if (source == null) return null;
        SettingFeignClient.SettingDTO target = new SettingFeignClient.SettingDTO();
        target.setKey(key);
        target.setSettingValue(source.getSettingValue());
        return target;
    }
}

package com.xzj.goods.config;

import com.xzj.message.SettingFeignClient;
import org.springframework.stereotype.Component;

/** Reads settings from message-service without sharing the settings entity or mapper. */
@Component
public class RemoteSettingService {

    private final SettingFeignClient settingFeignClient;

    public RemoteSettingService(SettingFeignClient settingFeignClient) {
        this.settingFeignClient = settingFeignClient;
    }

    public SettingFeignClient.SettingDTO get(String key) {
        return settingFeignClient.get(key);
    }

    public String getValue(String key) {
        SettingFeignClient.SettingDTO setting = get(key);
        return setting == null ? null : setting.getSettingValue();
    }
}

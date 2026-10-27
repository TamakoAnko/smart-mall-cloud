package com.xzj.message;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.Serializable;

/** 消息服务提供的配置读取接口，其他服务不得直接访问 li_setting。 */
@FeignClient(name = "message-service", contextId = "settingFeignClient", path = "/setting")
public interface SettingFeignClient {
    @GetMapping("/inner/{key}")
    SettingDTO get(@PathVariable("key") String key);

    class SettingDTO implements Serializable {
        private String key;
        private String settingValue;

        public String getKey() { return key; }
        public void setKey(String value) { key = value; }
        public String getSettingValue() { return settingValue; }
        public void setSettingValue(String value) { settingValue = value; }
    }
}

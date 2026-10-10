package com.xzj.order.system.serviceimpl;

import com.xzj.order.system.entity.dos.Setting;
import com.xzj.order.system.service.SettingService;
import com.xzj.message.SettingFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 配置业务层实现
 *
 * @author Chopper
 * @since 2020/11/17 3:52 下午
 */
@Service
public class SettingServiceImpl implements SettingService {

    @Autowired
    private SettingFeignClient settingFeignClient;

    @Override
    public Setting get(String key) {
        SettingFeignClient.SettingDTO remote = settingFeignClient.get(key);
        if (remote == null) {
            return null;
        }
        Setting setting = new Setting();
        setting.setSettingValue(remote.getSettingValue());
        return setting;
    }

    @Override
    public boolean saveUpdate(Setting setting) {
        throw new UnsupportedOperationException("订单服务不直接写入消息服务配置");
    }
}

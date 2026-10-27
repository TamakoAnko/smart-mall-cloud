package com.xzj.other.system.serviceimpl;

import com.xzj.other.system.entity.dos.Setting;
import com.xzj.other.system.mapper.SettingMapper;
import com.xzj.other.system.service.SettingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 配置业务层实现
 *
 * @author Chopper
 * @since 2020/11/17 3:52 下午
 */
@Service
public class SettingServiceImpl extends ServiceImpl<SettingMapper, Setting> implements SettingService {

    @Override
    public Setting get(String key) {
        return this.getById(key);
    }

    @Override
    public boolean saveUpdate(Setting setting) {
        return this.saveOrUpdate(setting);
    }
}
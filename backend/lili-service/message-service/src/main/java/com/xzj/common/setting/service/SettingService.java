package com.xzj.common.setting.service;

import com.xzj.common.setting.entity.dos.Setting;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;

/**
 * 配置业务层
 *
 * @author Chopper
 * @since 2020/11/17 3:46 下午
 */
@Service
@CacheConfig(cacheNames = "{setting}")
public interface SettingService extends IService<Setting> {

    /**
     * 通过key获取
     *
     * @param key
     * @return
     */
//    @Cacheable(key = "#key")
    Setting get(String key);

    /**
     * 修改
     *
     * @param setting
     * @return
     */
//    @CacheEvict(key = "#setting.id")
    boolean saveUpdate(Setting setting);
}
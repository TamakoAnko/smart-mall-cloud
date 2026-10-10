package com.xzj.order.system.service;

import com.xzj.order.system.entity.dos.Setting;
import org.springframework.cache.annotation.CacheConfig;

/**
 * 配置业务层
 *
 * @author Chopper
 * @since 2020/11/17 3:46 下午
 */
@CacheConfig(cacheNames = "{setting}")
public interface SettingService {

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

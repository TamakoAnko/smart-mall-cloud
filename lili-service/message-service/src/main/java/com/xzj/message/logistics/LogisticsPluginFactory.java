package com.xzj.message.logistics;

import cn.hutool.json.JSONUtil;
import cn.lili.common.exception.ServiceException;
import com.xzj.message.logistics.entity.enums.LogisticsEnum;
import com.xzj.message.logistics.plugin.kdniao.KdniaoPlugin;
import com.xzj.message.logistics.plugin.kuaidi100.Kuaidi100Plugin;
import com.xzj.other.system.entity.dos.Setting;
import com.xzj.other.system.entity.dto.LogisticsSetting;
import com.xzj.other.system.entity.enums.SettingEnum;
import com.xzj.other.system.service.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 文件服务抽象工厂 直接返回操作类
 *
 * @author Bulbasaur
 * @version v1.0
 * 2022-06-06 11:35
 */

@Component
public class LogisticsPluginFactory {


    @Autowired
    private SettingService settingService;


    /**
     * 获取logistics client
     *
     * @return
     */
    public LogisticsPlugin filePlugin() {

        LogisticsSetting logisticsSetting = null;
        try {
            Setting setting = settingService.get(SettingEnum.LOGISTICS_SETTING.name());
            logisticsSetting = JSONUtil.toBean(setting.getSettingValue(), LogisticsSetting.class);
            switch (LogisticsEnum.valueOf(logisticsSetting.getType())) {
                case KDNIAO:
                    return new KdniaoPlugin(logisticsSetting);
                case KUAIDI100:
                    return new Kuaidi100Plugin(logisticsSetting);
                default:
                    throw new ServiceException();
            }
        } catch (Exception e) {
            throw new ServiceException();
        }
    }


}

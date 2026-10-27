package com.xzj.common.sms.plugin;

import cn.hutool.json.JSONUtil;
import cn.lili.common.exception.ServiceException;
import com.xzj.common.sms.entity.enums.SmsEnum;
import com.xzj.common.sms.plugin.impl.AliSmsPlugin;
import com.xzj.common.sms.plugin.impl.HuaweiSmsPlugin;
import com.xzj.common.sms.plugin.impl.TencentSmsPlugin;
import com.xzj.common.setting.entity.dos.Setting;
import com.xzj.common.setting.entity.dto.SmsSetting;
import com.xzj.common.setting.entity.enums.SettingEnum;
import com.xzj.common.setting.service.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 短信服务抽象工厂 直接返回操作类
 *
 * @author Bulbasaur
 * @version v1.0
 * @since 2023-02-16
 */
@Component("commonSmsPluginFactory")
public class SmsPluginFactory {

    @Autowired
    private SettingService settingService;


    /**
     * 获取oss client
     *
     * @return
     */
    public SmsPlugin smsPlugin() {

        SmsSetting smsSetting = null;
        try {
            Setting setting = settingService.get(SettingEnum.SMS_SETTING.name());

            smsSetting = JSONUtil.toBean(setting.getSettingValue(), SmsSetting.class);


            switch (SmsEnum.valueOf(smsSetting.getType())) {

                case ALI:
                    return new AliSmsPlugin(smsSetting);
                case TENCENT:
                    return new TencentSmsPlugin(smsSetting);
                case HUAWEI:
                    return new HuaweiSmsPlugin(smsSetting);
                default:
                    throw new ServiceException();
            }
        } catch (Exception e) {
            throw new ServiceException();
        }
    }
}

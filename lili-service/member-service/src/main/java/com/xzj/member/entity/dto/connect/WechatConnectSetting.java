package com.xzj.member.entity.dto.connect;

import com.xzj.member.entity.dto.connect.dto.WechatConnectSettingItem;
import lombok.Data;

import java.util.List;

@Data
public class WechatConnectSetting {
    private List<WechatConnectSettingItem> wechatConnectSettingItems;
}

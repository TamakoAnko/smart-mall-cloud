package com.xzj.member.entity.dto.connect;

import com.xzj.member.entity.dto.connect.dto.QQConnectSettingItem;
import lombok.Data;

import java.util.List;

@Data
public class QQConnectSetting {
    private List<QQConnectSettingItem> qqConnectSettingItemList;
}

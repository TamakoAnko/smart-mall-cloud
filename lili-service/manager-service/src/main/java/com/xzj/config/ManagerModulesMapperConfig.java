package com.xzj.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/** Registers the management service's local business mappers during migration. */
@Configuration
@MapperScan({
        "cn.lili.modules.connect.mapper",
        "cn.lili.modules.distribution.mapper",
        "cn.lili.modules.file.mapper",
        "cn.lili.modules.finance.mapper",
        "cn.lili.modules.goods.mapper",
        "cn.lili.modules.im.mapper",
        "cn.lili.modules.live.mapper",
        "cn.lili.modules.member.mapper",
        "cn.lili.modules.message.mapper",
        "cn.lili.modules.order.aftersale.mapper",
        "cn.lili.modules.order.order.mapper",
        "cn.lili.modules.order.trade.mapper",
        "cn.lili.modules.page.mapper",
        "cn.lili.modules.payment.mapper",
        "cn.lili.modules.permission.mapper",
        "cn.lili.modules.procurement.mapper",
        "cn.lili.modules.promotion.mapper",
        "cn.lili.modules.search.mapper",
        "cn.lili.modules.sms.mapper",
        "cn.lili.modules.statistics.mapper",
        "cn.lili.modules.store.mapper",
        "cn.lili.modules.system.mapper",
        "cn.lili.modules.wallet.mapper",
        "cn.lili.modules.wechat.mapper",
        "cn.lili.modules.wxchannels.mapper"
})
public class ManagerModulesMapperConfig {
}

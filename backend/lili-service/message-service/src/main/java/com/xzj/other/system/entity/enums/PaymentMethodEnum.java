package com.xzj.other.system.entity.enums;

/** Payment methods owned by message-service settings. */
public enum PaymentMethodEnum {
    WECHAT("wechatPlugin", "微信"),
    ALIPAY("aliPayPlugin", "支付宝"),
    UNIONPAY("unionPayPlugin", "银联-云闪付"),
    WALLET("walletPlugin", "余额支付"),
    BANK_TRANSFER("bankTransferPlugin", "线下转账"),
    NOT_ACTUALLY_PAID("notActuallyPaidPlugin", "未实际支付");

    private final String plugin;
    private final String paymentName;

    PaymentMethodEnum(String plugin, String paymentName) {
        this.plugin = plugin;
        this.paymentName = paymentName;
    }

    public String getPlugin() {
        return plugin;
    }

    public String paymentName() {
        return paymentName;
    }
}

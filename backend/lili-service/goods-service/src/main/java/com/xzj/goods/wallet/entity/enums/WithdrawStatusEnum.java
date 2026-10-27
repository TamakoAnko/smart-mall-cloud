package com.xzj.goods.wallet.entity.enums;

/** 提现消息状态。名称必须与会员/订单服务保持一致。 */
public enum WithdrawStatusEnum {
    APPLY("申请中"),
    VIA_AUDITING("审核通过"),
    D_VIA_AUDITING("分销提现审核通过"),
    FAIL_AUDITING("审核未通过"),
    D_FAIL_AUDITING("分销提现审核未通过"),
    WAIT_USER_CONFIRM("等待用户确认"),
    SUCCESS("提现成功"),
    ERROR("提现失败");

    private final String description;

    WithdrawStatusEnum(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}

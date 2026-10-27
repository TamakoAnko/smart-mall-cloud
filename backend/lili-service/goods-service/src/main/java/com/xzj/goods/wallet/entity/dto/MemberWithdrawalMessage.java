package com.xzj.goods.wallet.entity.dto;

import lombok.Data;

/** 分销提现发送给会员服务的消息契约。 */
@Data
public class MemberWithdrawalMessage {

    private String memberWithdrawApplyId;
    private Double price;
    private String memberId;
    private String status;
    private Integer retryCount;
}

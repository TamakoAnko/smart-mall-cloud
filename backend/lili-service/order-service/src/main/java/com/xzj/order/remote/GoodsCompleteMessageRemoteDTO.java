package com.xzj.order.remote;

import lombok.Data;

import java.io.Serializable;

@Data
public class GoodsCompleteMessageRemoteDTO implements Serializable {
    private String goodsId;
    private String skuId;
    private String memberId;
    private Integer buyNum;
}

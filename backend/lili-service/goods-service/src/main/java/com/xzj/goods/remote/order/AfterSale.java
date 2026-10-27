package com.xzj.goods.remote.order;

import lombok.Data;
import java.io.Serializable;

@Data
public class AfterSale implements Serializable {
    private String sn;
    private String orderSn;
    private String orderItemSn;
    private Integer num;
    private Double actualRefundPrice;
}

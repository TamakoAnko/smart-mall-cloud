package com.xzj.order.remote;

import lombok.Data;

import java.io.Serializable;

@Data
public class StoreAfterSaleAddressRemoteDTO implements Serializable {
    private String salesConsigneeName;
    private String salesConsigneeMobile;
    private String salesConsigneeAddressId;
    private String salesConsigneeAddressPath;
    private String salesConsigneeDetail;
}

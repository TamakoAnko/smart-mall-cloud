package com.xzj.order.remote;

import lombok.Data;

import java.io.Serializable;

/** Store and pickup-address snapshots returned by goods-service. */
@Data
public class StoreRemoteDTO implements Serializable {
    private String id;
    private String storeName;
    private String storeLogo;
    private String storeDisable;
    private Boolean selfPickFlag;
}

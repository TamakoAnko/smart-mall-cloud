package com.xzj.order.remote;

import lombok.Data;

import java.io.Serializable;

@Data
public class StoreAddressRemoteDTO implements Serializable {
    private String id;
    private String storeId;
    private String addressName;
    private String center;
    private String address;
    private String mobile;
}

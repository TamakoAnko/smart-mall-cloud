package com.xzj.order.remote;

import lombok.Data;

import java.io.Serializable;

/** Address snapshot returned by member-service. */
@Data
public class MemberAddressRemoteDTO implements Serializable {
    private String id;
    private String memberId;
    private String name;
    private String mobile;
    private String consigneeAddressPath;
    private String consigneeAddressIdPath;
    private String detail;
    private Boolean isDefault;
    private String alias;
    private String lon;
    private String lat;
}

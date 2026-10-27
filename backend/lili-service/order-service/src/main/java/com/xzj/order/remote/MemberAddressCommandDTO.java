package com.xzj.order.remote;

import lombok.Data;

import java.io.Serializable;

@Data
public class MemberAddressCommandDTO implements Serializable {
    private String consigneeName;
    private String consigneeMobile;
    private String consigneeAddressPath;
    private String consigneeAddressIdPath;
    private String consigneeDetail;
}

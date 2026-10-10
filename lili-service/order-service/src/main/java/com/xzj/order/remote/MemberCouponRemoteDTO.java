package com.xzj.order.remote;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/** Coupon snapshot returned by goods-service; coupon ownership remains remote. */
@Data
public class MemberCouponRemoteDTO implements Serializable {
    private String id;
    private String memberId;
    private String couponId;
    private String couponName;
    private String couponType;
    private Double price;
    private Double discount;
    private Double consumeThreshold;
    private String storeId;
    private String storeName;
    private String rangeDayType;
    private Integer effectiveDays;
    private Date startTime;
    private Date endTime;
    private String memberCouponStatus;
    private Boolean platformFlag;
    private String scopeType;
    private String scopeId;
}

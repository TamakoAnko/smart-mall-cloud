package com.xzj.order.remote;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class BasePromotionRemoteDTO implements Serializable {
    private String id;
    private String title;
    private String storeId;
    private String promotionType;
    private Date startTime;
    private Date endTime;
}

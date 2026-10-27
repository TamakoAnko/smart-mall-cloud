package com.xzj.order.remote;

import lombok.Data;

import java.io.Serializable;

@Data
public class LiveRoomRemoteDTO implements Serializable {
    private String id;
    private Integer orderCount;
    private Integer orderUserCount;
}

package com.xzj.order.remote;

import lombok.Data;

import java.io.Serializable;

@Data
public class CardKeyRemoteDTO implements Serializable {
    private String id;
    private String cardNo;
    private String cardKey;
    private String status;
    private String fulfillStatus;
}

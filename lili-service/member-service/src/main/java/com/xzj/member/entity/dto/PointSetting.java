package com.xzj.member.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Member-owned projection of the point settings returned by message-service. */
@Data
public class PointSetting implements Serializable {
    private Integer register;
    private Integer consumer;
    private Integer money;
    private Integer signIn;
    private Integer comment;
    private List<PointSettingItem> pointSettingItems = new ArrayList<>();

    public Integer getRegister() { return nonNegative(register); }
    public Integer getConsumer() { return nonNegative(consumer); }
    public Integer getMoney() { return nonNegative(money); }
    public Integer getSignIn() { return nonNegative(signIn); }
    public Integer getComment() { return nonNegative(comment); }

    private Integer nonNegative(Integer value) {
        return value == null || value < 0 ? 0 : value;
    }
}

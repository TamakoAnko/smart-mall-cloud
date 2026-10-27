package com.xzj.member.entity.dto;

import lombok.Data;

import java.io.Serializable;

/** One day/point entry in the member point settings projection. */
@Data
public class PointSettingItem implements Comparable<PointSettingItem>, Serializable {
    private Integer day;
    private Integer point;

    public Integer getPoint() { return point == null || point < 0 ? 0 : point; }

    @Override
    public int compareTo(PointSettingItem other) {
        return this.day - other.getDay();
    }
}

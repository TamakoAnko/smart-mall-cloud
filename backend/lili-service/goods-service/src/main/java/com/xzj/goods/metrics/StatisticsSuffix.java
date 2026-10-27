package com.xzj.goods.metrics;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class StatisticsSuffix {
    private StatisticsSuffix() {
    }

    public static String suffix() {
        return suffix(null);
    }

    public static String suffix(String value) {
        return value == null ? new SimpleDateFormat("yyyyMMdd").format(new Date()) : value;
    }
}

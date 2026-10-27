package com.xzj.goods.metrics;

import cn.lili.cache.Cache;
import cn.lili.cache.CachePrefix;
import cn.lili.common.context.ThreadContextHolder;
import cn.lili.common.utils.IpUtils;
import cn.lili.common.utils.SpelUtil;
import cn.lili.common.vo.ResultMessage;
import com.xzj.goods.goods.entity.vos.GoodsSkuVO;
import com.xzj.goods.metrics.enums.PageViewEnum;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/** Counts buyer page views without depending on the seller statistics domain. */
@Aspect
@Component
@Slf4j
public class PageViewInterceptor {
    private final Cache cache;

    public PageViewInterceptor(Cache cache) {
        this.cache = cache;
    }

    @AfterReturning(returning = "result", pointcut = "@annotation(com.xzj.goods.metrics.PageViewPoint)")
    @SuppressWarnings("unchecked")
    public void interceptor(JoinPoint point, Object result) {
        PageViewPoint marker = ((MethodSignature) point.getSignature()).getMethod().getAnnotation(PageViewPoint.class);
        String storeId = "-1";
        if (marker.type() == PageViewEnum.SKU && result instanceof ResultMessage<?> message
                && message.getResult() instanceof Map<?, ?> data && data.get("data") instanceof GoodsSkuVO sku) {
            storeId = sku.getStoreId();
        } else if (marker.type() == PageViewEnum.STORE) {
            try {
                storeId = spelFormat(point, marker).get("id");
            } catch (Exception ignored) {
                return;
            }
        }
        try {
            String suffix = StatisticsSuffix.suffix();
            String ip = IpUtils.getIpAddress(ThreadContextHolder.getHttpRequest());
            cache.incr(CachePrefix.PV.getPrefix() + suffix, 60 * 60 * 48);
            cache.cumulative(CachePrefix.UV.getPrefix() + suffix, ip);
            cache.incr(CachePrefix.STORE_PV.getPrefix() + StatisticsSuffix.suffix(storeId), 60 * 60 * 48);
            cache.cumulative(CachePrefix.STORE_UV.getPrefix() + StatisticsSuffix.suffix(storeId), ip);
        } catch (Exception exception) {
            log.debug("Unable to record page view", exception);
        }
    }

    private Map<String, String> spelFormat(JoinPoint point, PageViewPoint marker) {
        Map<String, String> result = new HashMap<>(1);
        result.put("id", SpelUtil.compileParams(point, marker.id()));
        return result;
    }
}
